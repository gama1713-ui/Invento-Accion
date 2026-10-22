from math import ceil

from fastapi import APIRouter, HTTPException, Query
from pymongo.errors import PyMongoError

from app.database import inventarios_collection
from app.models import InventarioRespuesta


router = APIRouter(
    prefix="/api/inventarios",
    tags=["Inventarios"],
)


TAMANO_PREDETERMINADO = 25
TAMANO_MAXIMO = 100

CAMPOS_RESPUESTA = {
    "_id": 0,
    "codigo": 1,
    "nombre": 1,
    "categoria": 1,
    "ubicacion": 1,
    "unidadMedida": 1,
    "cantidadActual": 1,
    "stockMinimo": 1,
    "etiquetaQr": 1,
    "estado": 1,
    "ultimaActualizacion": 1,
}


def preparar_inventario(documento: dict) -> dict:
    """
    Prepara un documento de MongoDB para entregarlo a la página.

    La proyección de la consulta excluye normalmente el campo _id.
    La eliminación adicional protege la respuesta si otra consulta
    entrega accidentalmente ese campo.
    """
    inventario = documento.copy()
    inventario.pop("_id", None)
    return inventario


@router.get("/estado")
def consultar_estado_inventarios():
    """
    Verifica si la colección de inventarios está disponible.

    No descarga documentos del inventario.
    """
    try:
        inventarios_collection.database.command("ping")

        return {
            "servicio": "inventarios",
            "estado": "disponible",
            "tamanoPredeterminado": TAMANO_PREDETERMINADO,
            "tamanoMaximo": TAMANO_MAXIMO,
        }

    except PyMongoError as error:
        raise HTTPException(
            status_code=503,
            detail="El servicio de inventarios no está disponible.",
        ) from error


@router.get("")
def consultar_inventarios(
    pagina: int = Query(
        default=1,
        ge=1,
        description="Número de página solicitado",
    ),
    tamano: int = Query(
        default=TAMANO_PREDETERMINADO,
        ge=1,
        le=TAMANO_MAXIMO,
        description="Cantidad máxima de registros por página",
    ),
):
    """
    Consulta inventarios mediante paginación obligatoria.

    La ruta nunca devuelve más de 100 documentos por solicitud.
    Esta operación es exclusivamente de lectura.
    """
    try:
        total_registros = inventarios_collection.count_documents({})
        total_paginas = (
            ceil(total_registros / tamano)
            if total_registros > 0
            else 0
        )

        desplazamiento = (pagina - 1) * tamano

        documentos = list(
            inventarios_collection.find(
                {},
                CAMPOS_RESPUESTA,
            )
            .sort("nombre", 1)
            .skip(desplazamiento)
            .limit(tamano)
        )

        elementos = [
            preparar_inventario(documento)
            for documento in documentos
        ]

        return {
            "elementos": elementos,
            "paginacion": {
                "pagina": pagina,
                "tamano": tamano,
                "totalRegistros": total_registros,
                "totalPaginas": total_paginas,
                "tieneAnterior": pagina > 1,
                "tieneSiguiente": pagina < total_paginas,
            },
        }

    except PyMongoError as error:
        raise HTTPException(
            status_code=503,
            detail="No fue posible consultar los inventarios.",
        ) from error


@router.get(
    "/{codigo}",
    response_model=InventarioRespuesta,
)
def consultar_inventario_por_codigo(codigo: str):
    """
    Busca un único inventario mediante su código.

    Si el código no existe, la API responde con HTTP 404.
    """
    codigo_limpio = codigo.strip()

    if not codigo_limpio:
        raise HTTPException(
            status_code=400,
            detail="El código de inventario es obligatorio.",
        )

    try:
        documento = inventarios_collection.find_one(
            {"codigo": codigo_limpio},
            CAMPOS_RESPUESTA,
        )

        if documento is None:
            raise HTTPException(
                status_code=404,
                detail="Inventario no encontrado.",
            )

        return preparar_inventario(documento)

    except HTTPException:
        raise

    except PyMongoError as error:
        raise HTTPException(
            status_code=503,
            detail="No fue posible consultar el inventario.",
        ) from error
