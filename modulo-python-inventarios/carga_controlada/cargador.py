import argparse
import csv
import os
import sys
from datetime import datetime
from pathlib import Path
from typing import Any

import certifi
from dotenv import load_dotenv
from pymongo import ASCENDING, MongoClient
from pymongo.collection import Collection
from pymongo.errors import BulkWriteError, PyMongoError


RAIZ_PROYECTO = Path(__file__).resolve().parents[2]

DIRECTORIO_PILOTO_PREDETERMINADO = (
    RAIZ_PROYECTO
    / "datos-prueba-locales"
    / "carga-controlada"
    / "piloto-100"
)

ARCHIVOS_PREDETERMINADOS = {
    "usuarios": DIRECTORIO_PILOTO_PREDETERMINADO
    / "usuarios_piloto.csv",
    "proveedores": DIRECTORIO_PILOTO_PREDETERMINADO
    / "proveedores_piloto.csv",
    "activos": DIRECTORIO_PILOTO_PREDETERMINADO
    / "activos_piloto.csv",
}

COLECCIONES = {
    "usuarios": "usuarios_prueba",
    "proveedores": "proveedores_prueba",
    "activos": "activos_prueba",
}

CAMPOS_UNICOS = {
    "usuarios": [
        "id_usuario",
        "id_busqueda",
        "qr_payload",
        "code128_value",
    ],
    "proveedores": [
        "id_proveedor",
        "id_busqueda",
        "qr_payload",
        "code128_value",
    ],
    "activos": [
        "id_activo",
        "codigo_inventario",
        "placa",
        "serial",
        "qr_payload",
        "code128_value",
    ],
}

CAMPO_PRINCIPAL = {
    "usuarios": "id_busqueda",
    "proveedores": "id_busqueda",
    "activos": "codigo_inventario",
}

LIMITES_SEGURIDAD = {
    "usuarios": 500,
    "proveedores": 200,
    "activos": 5000,
}

CONFIRMACION_REQUERIDA = "CARGA_DATOS_SINTETICOS"


def crear_argumentos() -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description=(
            "Valida y carga datos sintéticos de Invento-Acción. "
            "El modo predeterminado es simulación."
        )
    )

    parser.add_argument(
        "--directorio",
        type=Path,
        default=DIRECTORIO_PILOTO_PREDETERMINADO,
        help="Directorio que contiene los tres archivos CSV piloto.",
    )

    parser.add_argument(
        "--tamano-lote",
        type=int,
        default=100,
        help="Cantidad máxima de documentos por lote.",
    )

    parser.add_argument(
        "--ejecutar",
        action="store_true",
        help="Habilita la inserción real en MongoDB Atlas.",
    )

    parser.add_argument(
        "--confirmar",
        default="",
        help=(
            "Confirmación adicional requerida para insertar: "
            f"{CONFIRMACION_REQUERIDA}"
        ),
    )

    return parser.parse_args()


def leer_csv(ruta: Path) -> list[dict[str, str]]:
    if not ruta.is_file():
        raise FileNotFoundError(f"No existe el archivo: {ruta}")

    with ruta.open(
        "r",
        encoding="utf-8-sig",
        newline="",
    ) as archivo:
        lector = csv.DictReader(archivo)

        if not lector.fieldnames:
            raise ValueError(f"El archivo no tiene encabezados: {ruta}")

        return [
            {
                clave: (valor or "").strip()
                for clave, valor in fila.items()
            }
            for fila in lector
        ]


def validar_campos_unicos(
    entidad: str,
    registros: list[dict[str, str]],
) -> None:
    for campo in CAMPOS_UNICOS[entidad]:
        if not registros or campo not in registros[0]:
            raise ValueError(
                f"{entidad}: falta el campo obligatorio {campo}"
            )

        valores = [
            registro.get(campo, "").strip()
            for registro in registros
        ]

        vacios = sum(not valor for valor in valores)
        duplicados = len(valores) - len(set(valores))

        if vacios:
            raise ValueError(
                f"{entidad}.{campo}: {vacios} valores vacíos"
            )

        if duplicados:
            raise ValueError(
                f"{entidad}.{campo}: {duplicados} duplicados"
            )


def validar_limite(
    entidad: str,
    registros: list[dict[str, str]],
) -> None:
    limite = LIMITES_SEGURIDAD[entidad]

    if len(registros) > limite:
        raise ValueError(
            f"{entidad}: {len(registros)} registros exceden "
            f"el límite de seguridad de {limite}"
        )


def validar_relaciones(
    usuarios: list[dict[str, str]],
    proveedores: list[dict[str, str]],
    activos: list[dict[str, str]],
) -> None:
    ids_usuarios = {
        usuario["id_busqueda"]
        for usuario in usuarios
    }

    ids_proveedores = {
        proveedor["id_busqueda"]
        for proveedor in proveedores
    }

    usuarios_inexistentes = {
        activo["id_usuario"]
        for activo in activos
        if activo.get("id_usuario") not in ids_usuarios
    }

    proveedores_inexistentes = {
        activo["id_proveedor"]
        for activo in activos
        if activo.get("id_proveedor") not in ids_proveedores
    }

    if usuarios_inexistentes:
        raise ValueError(
            "Hay activos asociados con usuarios inexistentes: "
            f"{len(usuarios_inexistentes)}"
        )

    if proveedores_inexistentes:
        raise ValueError(
            "Hay activos asociados con proveedores inexistentes: "
            f"{len(proveedores_inexistentes)}"
        )


def convertir_entero(
    registro: dict[str, str],
    campo: str,
) -> None:
    valor = registro.get(campo, "")

    if valor:
        registro[campo] = int(valor)  # type: ignore[assignment]


def preparar_usuarios(
    registros: list[dict[str, str]],
) -> list[dict[str, Any]]:
    documentos: list[dict[str, Any]] = []

    for registro_original in registros:
        registro = dict(registro_original)
        convertir_entero(registro, "id_usuario")
        registro["dato_sintetico"] = True
        registro["tipo_dataset"] = "academico_prueba"
        documentos.append(registro)

    return documentos


def preparar_proveedores(
    registros: list[dict[str, str]],
) -> list[dict[str, Any]]:
    documentos: list[dict[str, Any]] = []

    for registro_original in registros:
        registro = dict(registro_original)
        convertir_entero(registro, "id_proveedor")
        registro["dato_sintetico"] = True
        registro["tipo_dataset"] = "academico_prueba"
        documentos.append(registro)

    return documentos


def preparar_activos(
    registros: list[dict[str, str]],
) -> list[dict[str, Any]]:
    documentos: list[dict[str, Any]] = []

    for registro_original in registros:
        registro = dict(registro_original)

        convertir_entero(registro, "id_activo")
        convertir_entero(registro, "garantia_meses")

        fecha_compra = registro.get("fecha_compra", "")

        if fecha_compra:
            registro["fecha_compra"] = datetime.strptime(
                fecha_compra,
                "%Y-%m-%d",
            )

        registro["dato_sintetico"] = True
        registro["tipo_dataset"] = "academico_prueba"
        documentos.append(registro)

    return documentos


def dividir_lotes(
    registros: list[dict[str, Any]],
    tamano: int,
):
    for inicio in range(0, len(registros), tamano):
        yield registros[inicio:inicio + tamano]


def crear_indices(
    coleccion: Collection,
    entidad: str,
) -> None:
    for campo in CAMPOS_UNICOS[entidad]:
        coleccion.create_index(
            [(campo, ASCENDING)],
            unique=True,
            name=f"uq_{campo}",
        )


def filtrar_existentes(
    coleccion: Collection,
    entidad: str,
    registros: list[dict[str, Any]],
) -> tuple[list[dict[str, Any]], int]:
    campo = CAMPO_PRINCIPAL[entidad]
    valores = [registro[campo] for registro in registros]

    existentes = {
        documento[campo]
        for documento in coleccion.find(
            {campo: {"$in": valores}},
            {campo: 1, "_id": 0},
        )
    }

    nuevos = [
        registro
        for registro in registros
        if registro[campo] not in existentes
    ]

    return nuevos, len(registros) - len(nuevos)


def insertar_entidad(
    coleccion: Collection,
    entidad: str,
    registros: list[dict[str, Any]],
    tamano_lote: int,
) -> dict[str, int]:
    nuevos, omitidos = filtrar_existentes(
        coleccion,
        entidad,
        registros,
    )

    insertados = 0
    rechazados = 0

    for lote in dividir_lotes(nuevos, tamano_lote):
        try:
            resultado = coleccion.insert_many(
                lote,
                ordered=False,
            )
            insertados += len(resultado.inserted_ids)

        except BulkWriteError as error:
            detalles = error.details or {}
            insertados += detalles.get("nInserted", 0)
            rechazados += len(detalles.get("writeErrors", []))

    return {
        "leidos": len(registros),
        "insertados": insertados,
        "omitidos": omitidos,
        "rechazados": rechazados,
    }


def mostrar_resumen_validacion(
    usuarios: list[dict[str, str]],
    proveedores: list[dict[str, str]],
    activos: list[dict[str, str]],
) -> None:
    total = len(usuarios) + len(proveedores) + len(activos)

    print("\n===== RESUMEN DE VALIDACION =====")
    print("Usuarios:", len(usuarios))
    print("Proveedores:", len(proveedores))
    print("Activos:", len(activos))
    print("Total documentos:", total)
    print("Colecciones destino:")

    for entidad, coleccion in COLECCIONES.items():
        print(f"  {entidad}: {coleccion}")


def cargar_archivos(
    directorio: Path,
) -> tuple[
    list[dict[str, str]],
    list[dict[str, str]],
    list[dict[str, str]],
]:
    rutas = {
        "usuarios": directorio / "usuarios_piloto.csv",
        "proveedores": directorio / "proveedores_piloto.csv",
        "activos": directorio / "activos_piloto.csv",
    }

    usuarios = leer_csv(rutas["usuarios"])
    proveedores = leer_csv(rutas["proveedores"])
    activos = leer_csv(rutas["activos"])

    conjuntos = {
        "usuarios": usuarios,
        "proveedores": proveedores,
        "activos": activos,
    }

    for entidad, registros in conjuntos.items():
        validar_limite(entidad, registros)
        validar_campos_unicos(entidad, registros)

    validar_relaciones(
        usuarios,
        proveedores,
        activos,
    )

    return usuarios, proveedores, activos


def ejecutar_carga(
    usuarios: list[dict[str, str]],
    proveedores: list[dict[str, str]],
    activos: list[dict[str, str]],
    tamano_lote: int,
) -> None:
    ruta_env = (
        RAIZ_PROYECTO
        / "modulo-python-inventarios"
        / ".env"
    )

    load_dotenv(ruta_env)

    mongodb_uri = os.getenv("MONGODB_URI")

    if not mongodb_uri:
        raise RuntimeError(
            "MONGODB_URI no está configurada. "
            "La carga real fue cancelada."
        )

    nombre_base = os.getenv(
        "MONGODB_DATABASE",
        "invento_accion",
    )

    cliente = MongoClient(
        mongodb_uri,
        serverSelectionTimeoutMS=5000,
        tlsCAFile=certifi.where(),
    )

    try:
        cliente.admin.command("ping")
        base_datos = cliente[nombre_base]

        documentos = {
            "usuarios": preparar_usuarios(usuarios),
            "proveedores": preparar_proveedores(proveedores),
            "activos": preparar_activos(activos),
        }

        print("\n===== RESULTADO DE CARGA =====")

        for entidad in (
            "usuarios",
            "proveedores",
            "activos",
        ):
            coleccion = base_datos[COLECCIONES[entidad]]
            crear_indices(coleccion, entidad)

            resumen = insertar_entidad(
                coleccion,
                entidad,
                documentos[entidad],
                tamano_lote,
            )

            print(f"\n[{entidad.upper()}]")

            for clave, valor in resumen.items():
                print(f"{clave}: {valor}")

            print(
                "total_coleccion:",
                coleccion.count_documents({}),
            )

    finally:
        cliente.close()


def main() -> int:
    argumentos = crear_argumentos()

    if argumentos.tamano_lote < 1:
        print(
            "ERROR: el tamaño del lote debe ser mayor que cero.",
            file=sys.stderr,
        )
        return 1

    try:
        usuarios, proveedores, activos = cargar_archivos(
            argumentos.directorio
        )

        mostrar_resumen_validacion(
            usuarios,
            proveedores,
            activos,
        )

        if not argumentos.ejecutar:
            print("\nMODO: SIMULACION")
            print(
                "No se realizó ninguna conexión ni inserción "
                "en MongoDB."
            )
            return 0

        if argumentos.confirmar != CONFIRMACION_REQUERIDA:
            print(
                "\nERROR: se solicitó una carga real, pero falta "
                "la confirmación exacta.",
                file=sys.stderr,
            )
            print(
                f"Confirmación requerida: {CONFIRMACION_REQUERIDA}",
                file=sys.stderr,
            )
            return 2

        print("\nMODO: CARGA REAL CONTROLADA")

        ejecutar_carga(
            usuarios,
            proveedores,
            activos,
            argumentos.tamano_lote,
        )

        return 0

    except (
        FileNotFoundError,
        ValueError,
        RuntimeError,
        PyMongoError,
    ) as error:
        print(f"\nERROR: {error}", file=sys.stderr)
        return 1


if __name__ == "__main__":
    raise SystemExit(main())
