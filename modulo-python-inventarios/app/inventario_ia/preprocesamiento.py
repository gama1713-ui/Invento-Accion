"""
Preprocesamiento reproducible para Inventario IA.

Este modulo transforma una muestra JSON de contratos publicos de
SECOP II en un conjunto limpio para analisis academico.

La salida conserva solamente campos publicos necesarios para comparar
contratos tecnologicos. No incluye NIT, representantes, supervisores,
cuentas bancarias ni otras variables personales.

Compatible con Python 3.10 o superior y biblioteca estandar.
"""

from __future__ import annotations

import argparse
import hashlib
import json
import math
import statistics
import unicodedata
from collections import Counter
from dataclasses import dataclass
from datetime import datetime
from datetime import timezone
from decimal import Decimal
from decimal import InvalidOperation
from pathlib import Path
from typing import Any
from typing import Iterable


VERSION_PREPROCESADOR = "0.1.0"
FUENTE_DATOS = "SECOP II - Contratos Electronicos"
CODIGO_UNSPSC_ESPERADO = "V1.4321"
VALOR_MINIMO_COP = Decimal("100000")
MINIMO_REGISTROS_SALIDA = 1000


PALABRAS_EQUIPOS = (
    "computador",
    "computadora",
    "equipo de computo",
    "equipos de computo",
    "equipo de sistema",
    "equipos de sistemas",
    "equipo tecnologico",
    "equipos tecnologicos",
    "dispositivo tecnologico",
    "dispositivos tecnologicos",
    "equipo informatico",
    "equipos informaticos",
    "portatil",
    "portatiles",
    "laptop",
    "macbook",
    "tablet",
    "tableta",
    "tabletas",
    "todo en uno",
    "estacion de trabajo",
    "estaciones de trabajo",
    "terminales",
    "hardware",
    "parque tecnologico",
    "dotacion tecnologica",
    "componente tecnologico",
    "componentes tecnologicos",
    "recurso tecnologico",
    "recursos tecnologicos",
)


PALABRAS_COMPLEMENTARIOS = (
    "impresora",
    "impresoras",
    "scanner",
    "escaner",
    "servidor",
    "servidores",
    "monitor",
    "monitores",
    "periferico",
    "perifericos",
    "almacenamiento",
    "accesorio",
    "accesorios",
)


PALABRAS_SOFTWARE = (
    "licencia",
    "licencias",
    "software",
    "hosting",
    "dominio",
    "suscripcion",
    "nube",
)


PALABRAS_SERVICIOS = (
    "mantenimiento",
    "reparacion",
    "soporte tecnico",
    "servicios profesionales",
    "consultoria",
)


PALABRAS_ARRENDAMIENTO = (
    "arrendamiento",
    "alquiler",
    "comodato",
    "prestamo de uso",
)


PALABRAS_NO_TECNOLOGIA = (
    "mobiliario",
    "muebles escolares",
    "negocio de comision",
    "bolsa mercantil",
    "obra civil",
    "adecuaciones locativas",
)


TIPOS_CONTRATO_ADMITIDOS = {
    "Compraventa",
    "Suministros",
}


ESTADOS_CONTRATO_EXCLUIDOS = {
    "Borrador",
    "Cancelado",
}


CATEGORIAS_ADMITIDAS = {
    "COMPRA_EQUIPOS",
    "COMPRA_MIXTA",
    "EQUIPOS_COMPLEMENTARIOS",
}


@dataclass
class ResultadoPreprocesamiento:
    """
    Contiene los registros limpios y sus metadatos.
    """

    registros: list[dict[str, Any]]
    metadatos: dict[str, Any]


def calcular_sha256(ruta: Path) -> str:
    """
    Calcula el SHA-256 sin cargar el archivo completo en memoria.
    """
    resumen = hashlib.sha256()

    with ruta.open("rb") as archivo:
        for bloque in iter(
            lambda: archivo.read(1024 * 1024),
            b"",
        ):
            resumen.update(bloque)

    return resumen.hexdigest()


def normalizar_texto(valor: Any) -> str:
    """
    Convierte un valor en texto comparable sin tildes.
    """
    texto = str(valor or "").lower()
    texto = unicodedata.normalize("NFD", texto)

    texto = "".join(
        caracter
        for caracter in texto
        if unicodedata.category(caracter) != "Mn"
    )

    return " ".join(texto.split())


def contiene_alguna(
    texto: str,
    palabras: Iterable[str],
) -> bool:
    """
    Indica si el texto contiene alguna expresion del grupo.
    """
    return any(
        palabra in texto
        for palabra in palabras
    )


def clasificar_contrato(texto: str) -> str:
    """
    Asigna una categoria exclusiva mediante reglas explicables.
    """
    if contiene_alguna(
        texto,
        PALABRAS_NO_TECNOLOGIA,
    ):
        return "EXCLUIR_NO_TECNOLOGIA"

    if contiene_alguna(
        texto,
        PALABRAS_ARRENDAMIENTO,
    ):
        return "ARRENDAMIENTO_COMODATO"

    tiene_equipo = contiene_alguna(
        texto,
        PALABRAS_EQUIPOS,
    )

    tiene_complemento = contiene_alguna(
        texto,
        PALABRAS_COMPLEMENTARIOS,
    )

    tiene_software = contiene_alguna(
        texto,
        PALABRAS_SOFTWARE,
    )

    tiene_servicio = contiene_alguna(
        texto,
        PALABRAS_SERVICIOS,
    )

    if tiene_equipo and (
        tiene_complemento
        or tiene_software
        or tiene_servicio
    ):
        return "COMPRA_MIXTA"

    if tiene_equipo:
        return "COMPRA_EQUIPOS"

    if tiene_software:
        return "SOFTWARE_LICENCIAS"

    if tiene_servicio:
        return "MANTENIMIENTO_SERVICIO"

    if tiene_complemento:
        return "EQUIPOS_COMPLEMENTARIOS"

    return "SIN_CLASIFICAR"


def convertir_decimal(valor: Any) -> Decimal | None:
    """
    Convierte un valor a Decimal y rechaza valores no finitos.
    """
    try:
        numero = Decimal(str(valor).strip())
    except (
        InvalidOperation,
        TypeError,
        ValueError,
    ):
        return None

    if not numero.is_finite():
        return None

    return numero


def extraer_fecha(
    valor: Any,
) -> tuple[str | None, int | None, int | None]:
    """
    Normaliza una fecha y devuelve fecha, anio y mes.
    """
    texto = str(valor or "").strip()

    if not texto:
        return None, None, None

    fecha_base = texto[:10]

    try:
        fecha = datetime.strptime(
            fecha_base,
            "%Y-%m-%d",
        )
    except ValueError:
        return texto, None, None

    return fecha_base, fecha.year, fecha.month


def crear_texto_analisis(
    registro: dict[str, Any],
) -> str:
    """
    Une descripcion y objeto para realizar la clasificacion.
    """
    descripcion = str(
        registro.get(
            "descripcion_del_proceso",
            "",
        )
    )

    objeto = str(
        registro.get(
            "objeto_del_contrato",
            "",
        )
    )

    return normalizar_texto(
        descripcion + " " + objeto
    )


def limpiar_registros(
    datos: list[dict[str, Any]],
    sha256_entrada: str,
) -> ResultadoPreprocesamiento:
    """
    Aplica reglas de calidad y seleccion.
    """
    categorias_totales: Counter[str] = Counter()
    categorias_salida: Counter[str] = Counter()
    motivos_exclusion: Counter[str] = Counter()

    ids_observados: set[str] = set()
    registros_limpios: list[dict[str, Any]] = []

    for registro in datos:
        if not isinstance(registro, dict):
            motivos_exclusion[
                "REGISTRO_NO_ES_OBJETO"
            ] += 1
            continue

        id_contrato = str(
            registro.get(
                "id_contrato",
                "",
            )
        ).strip()

        if not id_contrato:
            motivos_exclusion[
                "ID_CONTRATO_AUSENTE"
            ] += 1
            continue

        if id_contrato in ids_observados:
            motivos_exclusion[
                "ID_CONTRATO_DUPLICADO"
            ] += 1
            continue

        ids_observados.add(id_contrato)

        codigo_unspsc = str(
            registro.get(
                "codigo_de_categoria_principal",
                "",
            )
        ).strip()

        if not codigo_unspsc.startswith(CODIGO_UNSPSC_ESPERADO):
            motivos_exclusion[
                "UNSPSC_NO_ADMITIDO"
            ] += 1
            continue

        texto_analisis = crear_texto_analisis(
            registro
        )

        categoria = clasificar_contrato(
            texto_analisis
        )

        categorias_totales[categoria] += 1

        tipo_contrato = str(
            registro.get(
                "tipo_de_contrato",
                "",
            )
        ).strip()

        if (
            tipo_contrato
            not in TIPOS_CONTRATO_ADMITIDOS
        ):
            motivos_exclusion[
                "TIPO_CONTRATO_NO_ADMITIDO"
            ] += 1
            continue

        estado_contrato = str(
            registro.get(
                "estado_contrato",
                "",
            )
        ).strip()

        if (
            estado_contrato
            in ESTADOS_CONTRATO_EXCLUIDOS
        ):
            motivos_exclusion[
                "ESTADO_CONTRATO_EXCLUIDO"
            ] += 1
            continue

        if categoria not in CATEGORIAS_ADMITIDAS:
            motivos_exclusion[
                "CATEGORIA_NO_ADMITIDA"
            ] += 1
            continue

        valor = convertir_decimal(
            registro.get(
                "valor_del_contrato"
            )
        )

        if valor is None:
            motivos_exclusion[
                "VALOR_INVALIDO"
            ] += 1
            continue

        if valor < VALOR_MINIMO_COP:
            motivos_exclusion[
                "VALOR_SIMBOLICO"
            ] += 1
            continue

        fecha_firma, anio_firma, mes_firma = (
            extraer_fecha(
                registro.get("fecha_de_firma")
            )
        )

        descripcion = " ".join(
            str(
                registro.get(
                    "descripcion_del_proceso",
                    "",
                )
            ).split()
        )

        objeto = " ".join(
            str(
                registro.get(
                    "objeto_del_contrato",
                    "",
                )
            ).split()
        )

        valor_float = float(valor)

        registro_limpio = {
            "id_contrato": id_contrato,
            "proceso_de_compra": str(
                registro.get(
                    "proceso_de_compra",
                    "",
                )
            ).strip(),
            "codigo_unspsc": codigo_unspsc,
            "categoria_ia": categoria,
            "tipo_contrato": tipo_contrato,
            "modalidad": str(
                registro.get(
                    "modalidad_de_contratacion",
                    "",
                )
            ).strip(),
            "estado_contrato": estado_contrato,
            "departamento": str(
                registro.get(
                    "departamento",
                    "",
                )
            ).strip(),
            "ciudad": str(
                registro.get(
                    "ciudad",
                    "",
                )
            ).strip(),
            "sector": str(
                registro.get(
                    "sector",
                    "",
                )
            ).strip(),
            "descripcion": descripcion,
            "objeto": objeto,
            "texto_normalizado": texto_analisis,
            "valor_contrato_cop": valor_float,
            "log_valor_contrato": math.log1p(
                valor_float
            ),
            "fecha_firma": fecha_firma,
            "anio_firma": anio_firma,
            "mes_firma": mes_firma,
            "fuente": FUENTE_DATOS,
        }

        registros_limpios.append(
            registro_limpio
        )

        categorias_salida[categoria] += 1

    registros_limpios.sort(
        key=lambda elemento: elemento[
            "id_contrato"
        ]
    )

    valores = sorted(
        registro["valor_contrato_cop"]
        for registro in registros_limpios
    )

    if valores:
        ultimo = len(valores) - 1

        indice_p95 = min(
            ultimo,
            int(ultimo * 0.95),
        )

        indice_p99 = min(
            ultimo,
            int(ultimo * 0.99),
        )

        estadisticas_valores = {
            "minimo": valores[0],
            "mediana": statistics.median(
                valores
            ),
            "percentil_95": valores[
                indice_p95
            ],
            "percentil_99": valores[
                indice_p99
            ],
            "maximo": valores[-1],
        }
    else:
        estadisticas_valores = {
            "minimo": None,
            "mediana": None,
            "percentil_95": None,
            "percentil_99": None,
            "maximo": None,
       }

    metadatos = {
        "version_preprocesador": (
            VERSION_PREPROCESADOR
        ),
        "fuente": FUENTE_DATOS,
        "codigo_unspsc": (
            CODIGO_UNSPSC_ESPERADO
        ),
        "sha256_entrada": sha256_entrada,
        "registros_entrada": len(datos),
        "registros_salida": len(
            registros_limpios
        ),
        "categorias_entrada": dict(
            sorted(
                categorias_totales.items()
            )
        ),
        "categorias_salida": dict(
            sorted(
                categorias_salida.items()
            )
        ),
        "motivos_exclusion": dict(
            sorted(
                motivos_exclusion.items()
            )
        ),
        "tipos_contrato_admitidos": sorted(
            TIPOS_CONTRATO_ADMITIDOS
        ),
        "estados_excluidos": sorted(
            ESTADOS_CONTRATO_EXCLUIDOS
        ),
        "categorias_admitidas": sorted(
            CATEGORIAS_ADMITIDAS
        ),
        "valor_minimo_cop": float(
            VALOR_MINIMO_COP
        ),
        "estadisticas_valores": (
            estadisticas_valores
        ),
    }

    return ResultadoPreprocesamiento(
        registros=registros_limpios,
        metadatos=metadatos,
    )


def leer_entrada(
    ruta: Path,
) -> list[dict[str, Any]]:
    """
    Lee y valida el archivo JSON de entrada.
    """
    if not ruta.is_file():
        raise FileNotFoundError(
            "No existe el archivo de entrada: "
            f"{ruta}"
        )

    datos = json.loads(
        ruta.read_text(
            encoding="utf-8"
        )
    )

    if not isinstance(datos, list):
        raise ValueError(
            "El archivo debe contener "
            "una lista JSON."
        )

    return datos


def escribir_json(
    ruta: Path,
    contenido: Any,
) -> None:
    """
    Escribe JSON UTF-8 de manera controlada.
    """
    ruta.parent.mkdir(
        parents=True,
        exist_ok=True,
    )

    temporal = ruta.with_suffix(
        ruta.suffix + ".tmp"
    )

    temporal.write_text(
        json.dumps(
            contenido,
            ensure_ascii=False,
            indent=2,
            sort_keys=True,
        )
        + "\n",
        encoding="utf-8",
    )

    temporal.replace(ruta)


def procesar(
    entrada: Path,
    salida: Path,
    metadatos: Path,
    minimo_registros: int,
) -> ResultadoPreprocesamiento:
    """
    Ejecuta el flujo completo.
    """
    sha256_entrada = calcular_sha256(
        entrada
    )

    datos = leer_entrada(
        entrada
    )

    resultado = limpiar_registros(
        datos=datos,
        sha256_entrada=sha256_entrada,
    )

    if (
        len(resultado.registros)
        < minimo_registros
    ):
        raise ValueError(
            "El conjunto limpio no cumple "
            "el minimo requerido. "
            "Registros obtenidos: "
            f"{len(resultado.registros)}. "
            "Minimo requerido: "
            f"{minimo_registros}."
        )

    escribir_json(
        salida,
        resultado.registros,
    )

    sha256_salida = calcular_sha256(
        salida
    )

    resultado.metadatos[
        "sha256_salida"
    ] = sha256_salida

    resultado.metadatos[
        "archivo_salida"
    ] = salida.name

    resultado.metadatos[
        "fecha_procesamiento_utc"
    ] = (
        datetime.now(timezone.utc)
        .replace(microsecond=0)
        .isoformat()
    )

    escribir_json(
        metadatos,
        resultado.metadatos,
    )

    return resultado


def construir_argumentos() -> argparse.Namespace:
    """
    Define los argumentos de consola.
    """
    parser = argparse.ArgumentParser(
        description=(
            "Preprocesa contratos tecnologicos "
            "de SECOP II para Inventario IA."
        )
    )

    parser.add_argument(
        "--entrada",
        required=True,
        type=Path,
        help="Archivo JSON original.",
    )

    parser.add_argument(
        "--salida",
        required=True,
        type=Path,
        help="Archivo JSON limpio.",
    )

    parser.add_argument(
        "--metadatos",
        required=True,
        type=Path,
        help="Archivo JSON de metadatos.",
    )

    parser.add_argument(
        "--minimo-registros",
        type=int,
        default=MINIMO_REGISTROS_SALIDA,
        help=(
            "Cantidad minima requerida. "
            "Valor predeterminado: "
            f"{MINIMO_REGISTROS_SALIDA}."
        ),
    )

    return parser.parse_args()


def main() -> int:
    """
    Punto de entrada de consola.
    """
    argumentos = construir_argumentos()

    resultado = procesar(
        entrada=argumentos.entrada,
        salida=argumentos.salida,
        metadatos=argumentos.metadatos,
        minimo_registros=(
            argumentos.minimo_registros
        ),
    )

    print(
        "PREPROCESAMIENTO_COMPLETADO=SI"
    )

    print(
        "REGISTROS_SALIDA="
        f"{len(resultado.registros)}"
    )

    print(
        "SHA256_ENTRADA="
        f"{resultado.metadatos['sha256_entrada']}"
    )

    print(
        "SHA256_SALIDA="
        f"{resultado.metadatos['sha256_salida']}"
    )

    return 0


if __name__ == "__main__":
    raise SystemExit(main())
