from math import ceil

from fastapi import APIRouter, HTTPException, Query
from pymongo.errors import PyMongoError

from app.database import activos_prueba_collection
from app.models import ActivoPruebaRespuesta


router = APIRouter(
    prefix="/api/activos-prueba",
    tags=["Activos sintéticos de prueba"],
)

TAMANO_PREDETERMINADO = 25
TAMANO_MAXIMO = 100

FILTRO_DATASET = {
    "dato_sintetico": True,
    "tipo_dataset": "academico_prueba",
}

CAMPOS_RESPUESTA = {
    "_id": 0,
    "id_activo": 1,
    "codigo_inventario": 1,
    "placa": 1,
    "tipo_activo": 1,
    "descripcion": 1,
    "marca": 1,
    "modelo": 1,
    "serial": 1,
    "hostname": 1,
    "extension_ip": 1,
    "sistema_operativo": 1,
    "memoria": 1,
    "almacenamiento": 1,
    "especificacion": 1,
    "estado": 1,
    "id_usuario": 1,
    "usuario_asignado": 1,
    "vinculacion": 1,
    "modalidad": 1,
    "area_tecnologia": 1,
    "ciudad": 1,
    "id_proveedor": 1,
    "fecha_compra": 1,
    "garantia_meses": 1,
    "qr_payload": 1,
    "code128_value": 1,
    "observaciones": 1,
    "dato_sintetico": 1,
    "tipo_dataset": 1,
}


def preparar_activo(documento: dict) -> dict:
    activo = documento.copy()
    activo.pop("_id", None)

    return ActivoPruebaRespuesta.model_validate(
        activo
    ).model_dump()


@router.get("/estado")
def consultar_estado_activos_prueba():
    """
    Comprueba el servicio sin descargar activos.
    """
    try:
        activos_prueba_collection.database.command("ping")

        total = activos_prueba_collection.count_documents(
            FILTRO_DATASET
        )

        return {
            "servicio": "activos-prueba",
            "estado": "disponible",
            "totalRegistros": total,
            "tamanoPredeterminado": TAMANO_PREDETERMINADO,
            "tamanoMaximo": TAMANO_MAXIMO,
            "datoSintetico": True,
        }

    except PyMongoError as error:
        raise HTTPException(
            status_code=503,
            detail=(
                "El servicio de activos sintéticos "
                "no está disponible."
            ),
        ) from error


@router.get("")
def consultar_activos_prueba(
    pagina: int = Query(default=1, ge=1),
    tamano: int = Query(
        default=TAMANO_PREDETERMINADO,
        ge=1,
        le=TAMANO_MAXIMO,
    ),
):
    """
    Devuelve una página de activos sintéticos.

    Nunca entrega más de 100 documentos por solicitud.
    """
    try:
        total_registros = (
            activos_prueba_collection.count_documents(
                FILTRO_DATASET
            )
        )

        total_paginas = (
            ceil(total_registros / tamano)
            if total_registros > 0
            else 0
        )

        documentos = list(
            activos_prueba_collection.find(
                FILTRO_DATASET,
                CAMPOS_RESPUESTA,
            )
            .sort("codigo_inventario", 1)
            .skip((pagina - 1) * tamano)
            .limit(tamano)
        )

        elementos = [
            preparar_activo(documento)
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
            detail=(
                "No fue posible consultar "
                "los activos sintéticos."
            ),
        ) from error

    except ValueError as error:
        raise HTTPException(
            status_code=500,
            detail=(
                "Un activo sintético no cumple "
                "el modelo esperado."
            ),
        ) from error


@router.get(
    "/{identificador}",
    response_model=ActivoPruebaRespuesta,
)
def consultar_activo_prueba(identificador: str):
    """
    Busca por código, placa, serial o id numérico.
    """
    valor = identificador.strip()

    if not valor:
        raise HTTPException(
            status_code=400,
            detail="El identificador del activo es obligatorio.",
        )

    condiciones = [
        {"codigo_inventario": valor},
        {"placa": valor},
        {"serial": valor},
    ]

    if valor.isdigit():
        condiciones.append({"id_activo": int(valor)})

    filtro = {
        **FILTRO_DATASET,
        "$or": condiciones,
    }

    try:
        documento = activos_prueba_collection.find_one(
            filtro,
            CAMPOS_RESPUESTA,
        )

        if documento is None:
            raise HTTPException(
                status_code=404,
                detail="Activo sintético no encontrado.",
            )

        return preparar_activo(documento)

    except HTTPException:
        raise

    except PyMongoError as error:
        raise HTTPException(
            status_code=503,
            detail=(
                "No fue posible consultar "
                "el activo sintético."
            ),
        ) from error

    except ValueError as error:
        raise HTTPException(
            status_code=500,
            detail=(
                "El activo sintético no cumple "
                "el modelo esperado."
            ),
        ) from error
