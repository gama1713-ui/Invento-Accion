"""
Motor de recomendacion academica para Inventario IA.

Compara una compra tecnologica propuesta con contratos publicos
semejantes de SECOP II. Utiliza un metodo explicable de vecinos
comparables y no requiere librerias externas.

Los valores analizados son valores contractuales totales. No deben
interpretarse como precios unitarios ni como aprobaciones definitivas.
"""

from __future__ import annotations

import argparse
import json
import math
import statistics
import unicodedata
from pathlib import Path
from typing import Any


VERSION_RECOMENDADOR = "0.1.0"
VECINOS_PREDETERMINADOS = 25
MINIMO_COMPARABLES = 5

CATEGORIAS_VALIDAS = {
    "COMPRA_EQUIPOS",
    "COMPRA_MIXTA",
    "EQUIPOS_COMPLEMENTARIOS",
}


def normalizar_texto(valor):
    """
    Normaliza texto para comparar descripciones sin depender de tildes.
    """
    texto = str(valor or "").lower()
    texto = unicodedata.normalize("NFD", texto)

    texto = "".join(
        caracter
        for caracter in texto
        if unicodedata.category(caracter) != "Mn"
    )

    return " ".join(texto.split())


def tokenizar(texto):
    """
    Convierte un texto normalizado en un conjunto de palabras utiles.
    """
    texto = normalizar_texto(texto)

    tokens = {
        palabra.strip(".,;:()[]{}-_")
        for palabra in texto.split()
        if len(palabra.strip(".,;:()[]{}-_")) >= 3
    }

    return {
        token
        for token in tokens
        if token
    }


def convertir_numero(valor, nombre):
    """
    Convierte un valor a numero positivo.
    """
    try:
        numero = float(valor)
    except (TypeError, ValueError) as error:
        raise ValueError(
            f"{nombre} debe ser numerico."
        ) from error

    if not math.isfinite(numero):
        raise ValueError(
            f"{nombre} debe ser finito."
        )

    if numero <= 0:
        raise ValueError(
            f"{nombre} debe ser mayor que cero."
        )

    return numero


def cargar_contratos(ruta):
    """
    Carga y valida el conjunto limpio de contratos.
    """
    ruta = Path(ruta)

    if not ruta.is_file():
        raise FileNotFoundError(
            f"No existe el conjunto de contratos: {ruta}"
        )

    datos = json.loads(
        ruta.read_text(encoding="utf-8")
    )

    if not isinstance(datos, list):
        raise ValueError(
            "El conjunto de contratos debe ser una lista JSON."
        )

    if len(datos) < 1000:
        raise ValueError(
            "El conjunto debe contener al menos 1000 registros."
        )

    contratos = []

    for registro in datos:
        if not isinstance(registro, dict):
            continue

        valor = registro.get("valor_contrato_cop")

        try:
            valor = convertir_numero(
                valor,
                "valor_contrato_cop",
            )
        except ValueError:
            continue

        categoria = str(
            registro.get("categoria_ia", "")
        ).strip()

        if categoria not in CATEGORIAS_VALIDAS:
            continue

        texto = str(
            registro.get("texto_normalizado", "")
        ).strip()

        if not texto:
            texto = (
                str(registro.get("descripcion", ""))
                + " "
                + str(registro.get("objeto", ""))
            )

        contrato = dict(registro)
        contrato["valor_contrato_cop"] = valor
        contrato["_tokens"] = tokenizar(texto)

        contratos.append(contrato)

    if len(contratos) < 1000:
        raise ValueError(
            "No existen al menos 1000 contratos validos."
        )

    return contratos


def similitud_jaccard(tokens_consulta, tokens_contrato):
    """
    Calcula la similitud entre dos conjuntos de palabras.
    """
    if not tokens_consulta or not tokens_contrato:
        return 0.0

    union = tokens_consulta | tokens_contrato

    if not union:
        return 0.0

    interseccion = tokens_consulta & tokens_contrato

    return len(interseccion) / len(union)


def coincidencia_texto(valor_consulta, valor_contrato):
    """
    Devuelve uno cuando dos variables categoricas coinciden.
    """
    consulta = normalizar_texto(valor_consulta)
    contrato = normalizar_texto(valor_contrato)

    if not consulta or not contrato:
        return 0.0

    return 1.0 if consulta == contrato else 0.0


def calcular_puntaje(consulta, contrato):
    """
    Calcula un puntaje explicable de semejanza entre cero y uno.
    """
    puntaje_texto = similitud_jaccard(
        consulta["tokens"],
        contrato["_tokens"],
    )

    puntaje_categoria = coincidencia_texto(
        consulta.get("categoria"),
        contrato.get("categoria_ia"),
    )

    puntaje_departamento = coincidencia_texto(
        consulta.get("departamento"),
        contrato.get("departamento"),
    )

    puntaje_modalidad = coincidencia_texto(
        consulta.get("modalidad"),
        contrato.get("modalidad"),
    )

    puntaje_sector = coincidencia_texto(
        consulta.get("sector"),
        contrato.get("sector"),
    )

    return (
        puntaje_texto * 0.60
        + puntaje_categoria * 0.20
        + puntaje_departamento * 0.08
        + puntaje_modalidad * 0.07
        + puntaje_sector * 0.05
    )


def percentil(valores, proporcion):
    """
    Obtiene un percentil mediante interpolacion lineal.
    """
    if not valores:
        raise ValueError(
            "No existen valores para calcular el percentil."
        )

    ordenados = sorted(valores)

    if len(ordenados) == 1:
        return ordenados[0]

    posicion = (len(ordenados) - 1) * proporcion
    inferior = math.floor(posicion)
    superior = math.ceil(posicion)

    if inferior == superior:
        return ordenados[inferior]

    peso = posicion - inferior

    return (
        ordenados[inferior] * (1 - peso)
        + ordenados[superior] * peso
    )


def seleccionar_comparables(
    contratos,
    consulta,
    cantidad_vecinos,
):
    """
    Selecciona los contratos con mayor puntaje de semejanza.
    """
    evaluados = []

    for contrato in contratos:
        puntaje = calcular_puntaje(
            consulta,
            contrato,
        )

        if puntaje <= 0:
            continue

        evaluados.append(
            (puntaje, contrato)
        )

    evaluados.sort(
        key=lambda elemento: (
            elemento[0],
            elemento[1]["valor_contrato_cop"],
        ),
        reverse=True,
    )

    return evaluados[:cantidad_vecinos]


def clasificar_valor(
    valor_propuesto,
    limite_inferior,
    limite_superior,
    cantidad_comparables,
):
    """
    Clasifica de forma referencial el valor propuesto.
    """
    if cantidad_comparables < MINIMO_COMPARABLES:
        return (
            "INFORMACION_INSUFICIENTE",
            "No existen suficientes contratos comparables.",
        )

    margen_inferior = limite_inferior * 0.80
    margen_superior = limite_superior * 1.20

    if valor_propuesto < margen_inferior:
        return (
            "VALOR_ATIPICO_BAJO",
            (
                "El valor propuesto esta considerablemente "
                "por debajo del rango de comparables."
            ),
        )

    if valor_propuesto > margen_superior:
        return (
            "REQUIERE_REVISION",
            (
                "El valor propuesto esta considerablemente "
                "por encima del rango de comparables."
            ),
        )

    return (
        "FAVORABLE_COMO_REFERENCIA",
        (
            "El valor propuesto se encuentra dentro del "
            "rango ampliado de contratos comparables."
        ),
    )


def recomendar(
    contratos,
    descripcion,
    valor_propuesto,
    categoria="",
    departamento="",
    modalidad="",
    sector="",
    cantidad_vecinos=VECINOS_PREDETERMINADOS,
):
    """
    Genera una recomendacion academica explicable.
    """
    descripcion = str(descripcion or "").strip()

    if len(descripcion) < 10:
        raise ValueError(
            "La descripcion debe contener al menos 10 caracteres."
        )

    valor_propuesto = convertir_numero(
        valor_propuesto,
        "valor_propuesto",
    )

    if categoria and categoria not in CATEGORIAS_VALIDAS:
        raise ValueError(
            "La categoria suministrada no es valida."
        )

    if cantidad_vecinos < MINIMO_COMPARABLES:
        raise ValueError(
            "La cantidad de vecinos debe ser al menos cinco."
        )

    consulta = {
        "descripcion": descripcion,
        "tokens": tokenizar(descripcion),
        "categoria": categoria,
        "departamento": departamento,
        "modalidad": modalidad,
        "sector": sector,
    }

    comparables = seleccionar_comparables(
        contratos=contratos,
        consulta=consulta,
        cantidad_vecinos=cantidad_vecinos,
    )

    valores = [
        contrato["valor_contrato_cop"]
        for _, contrato in comparables
    ]

    if not valores:
        return {
            "version_recomendador": VERSION_RECOMENDADOR,
            "decision": "INFORMACION_INSUFICIENTE",
            "explicacion": (
                "No se encontraron contratos comparables."
            ),
            "valor_propuesto_cop": valor_propuesto,
            "comparables_encontrados": 0,
            "decision_final_humana": True,
            "limitaciones": [
                (
                    "La recomendacion usa valores "
                    "contractuales totales."
                ),
                (
                    "No representa un precio unitario "
                    "ni una aprobacion definitiva."
                ),
            ],
        }

    valores_log = [
        math.log1p(valor)
        for valor in valores
    ]

    valor_estimado = math.expm1(
        statistics.median(valores_log)
    )

    limite_inferior = percentil(
        valores,
        0.25,
    )

    limite_superior = percentil(
        valores,
        0.75,
    )

    decision, explicacion = clasificar_valor(
        valor_propuesto=valor_propuesto,
        limite_inferior=limite_inferior,
        limite_superior=limite_superior,
        cantidad_comparables=len(comparables),
    )

    diferencia_porcentual = (
        (valor_propuesto - valor_estimado)
        / valor_estimado
        * 100
    )

    comparables_resumidos = []

    for puntaje, contrato in comparables[:5]:
        comparables_resumidos.append(
            {
                "id_contrato": contrato.get("id_contrato"),
                "categoria_ia": contrato.get("categoria_ia"),
                "departamento": contrato.get("departamento"),
                "modalidad": contrato.get("modalidad"),
                "valor_contrato_cop": contrato.get(
                    "valor_contrato_cop"
                ),
                "puntaje_similitud": round(puntaje, 4),
            }
        )

    return {
        "version_recomendador": VERSION_RECOMENDADOR,
        "decision": decision,
        "explicacion": explicacion,
        "valor_propuesto_cop": round(valor_propuesto, 2),
        "valor_referencia_cop": round(valor_estimado, 2),
        "rango_referencial_cop": {
            "inferior": round(limite_inferior, 2),
            "superior": round(limite_superior, 2),
        },
        "diferencia_porcentual": round(
            diferencia_porcentual,
            2,
        ),
        "comparables_encontrados": len(comparables),
        "comparables_destacados": comparables_resumidos,
        "variables_consideradas": [
            "descripcion",
            "categoria",
            "departamento",
            "modalidad",
            "sector",
        ],
        "decision_final_humana": True,
        "limitaciones": [
            (
                "La recomendacion usa valores "
                "contractuales totales de SECOP II."
            ),
            (
                "Los valores no representan necesariamente "
                "precios unitarios."
            ),
            (
                "El resultado es academico y no reemplaza "
                "una evaluacion tecnica, financiera o juridica."
            ),
        ],
    }


def construir_argumentos():
    """
    Define los argumentos disponibles por consola.
    """
    parser = argparse.ArgumentParser(
        description=(
            "Genera una recomendacion academica de compra "
            "usando contratos comparables de SECOP II."
        )
    )

    parser.add_argument(
        "--datos",
        required=True,
        type=Path,
        help="Archivo JSON de contratos limpios.",
    )

    parser.add_argument(
        "--descripcion",
        required=True,
        help="Descripcion de la compra propuesta.",
    )

    parser.add_argument(
        "--valor",
        required=True,
        type=float,
        help="Valor contractual propuesto en COP.",
    )

    parser.add_argument(
        "--categoria",
        default="",
        choices=sorted(CATEGORIAS_VALIDAS),
        help="Categoria academica opcional.",
    )

    parser.add_argument(
        "--departamento",
        default="",
        help="Departamento opcional.",
    )

    parser.add_argument(
        "--modalidad",
        default="",
        help="Modalidad contractual opcional.",
    )

    parser.add_argument(
        "--sector",
        default="",
        help="Sector opcional.",
    )

    parser.add_argument(
        "--vecinos",
        type=int,
        default=VECINOS_PREDETERMINADOS,
        help="Cantidad maxima de contratos comparables.",
    )

    return parser.parse_args()


def main():
    """
    Ejecuta una recomendacion desde la linea de comandos.
    """
    argumentos = construir_argumentos()

    contratos = cargar_contratos(
        argumentos.datos
    )

    resultado = recomendar(
        contratos=contratos,
        descripcion=argumentos.descripcion,
        valor_propuesto=argumentos.valor,
        categoria=argumentos.categoria,
        departamento=argumentos.departamento,
        modalidad=argumentos.modalidad,
        sector=argumentos.sector,
        cantidad_vecinos=argumentos.vecinos,
    )

    print(
        json.dumps(
            resultado,
            ensure_ascii=False,
            indent=2,
            sort_keys=True,
        )
    )

    return 0


if __name__ == "__main__":
    raise SystemExit(main())
