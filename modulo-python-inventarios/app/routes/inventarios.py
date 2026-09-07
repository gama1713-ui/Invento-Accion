from fastapi import APIRouter, HTTPException
from pymongo.errors import PyMongoError

from app.database import inventarios_collection
from app.models import InventarioRespuesta


# Agrupa las rutas relacionadas con la consulta de inventarios.
router = APIRouter(
    prefix="/api/inventarios",
    tags=["Inventarios"],
)


def preparar_inventario(documento: dict) -> dict:
    """
    Prepara un documento de MongoDB antes de enviarlo como respuesta.

    MongoDB agrega un campo interno llamado _id.
    La página web no necesita ese campo, por eso se retira de la respuesta.
    """
    inventario = documento.copy()
    inventario.pop("_id", None)
    return inventario


@router.get("", response_model=list[InventarioRespuesta])
def consultar_inventarios():
    """
    Consulta todos los elementos almacenados en la colección inventarios.

    Esta operación es únicamente de lectura.
    No crea, modifica ni elimina información.
    """
    try:
        documentos = inventarios_collection.find().sort("nombre", 1)
        return [preparar_inventario(documento) for documento in documentos]
    except PyMongoError as error:
        raise HTTPException(
            status_code=503,
            detail="No fue posible consultar los inventarios.",
        ) from error


@router.get("/{codigo}", response_model=InventarioRespuesta)
def consultar_inventario_por_codigo(codigo: str):
    """
    Busca un elemento del inventario utilizando su código.

    Si el código no existe, la API responde con el estado HTTP 404.
    """
    try:
        documento = inventarios_collection.find_one({"codigo": codigo})

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
