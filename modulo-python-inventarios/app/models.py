from datetime import datetime

from pydantic import BaseModel, Field


class InventarioRespuesta(BaseModel):
    """
    Define la información de inventario que la API entrega al usuario.

    Este modelo ayuda a validar que cada respuesta tenga una estructura clara.
    No guarda, modifica ni elimina información de MongoDB.
    """

    # Código único utilizado para consultar el elemento del inventario.
    codigo: str = Field(..., description="Código único del inventario")

    # Nombre con el que se identifica el producto o activo.
    nombre: str = Field(..., description="Nombre del producto")

    # Grupo al que pertenece el producto.
    categoria: str = Field(..., description="Categoría del producto")

    # Lugar donde se encuentra almacenado el producto.
    ubicacion: str = Field(..., description="Ubicación del producto")

    # Forma en que se cuenta el producto, por ejemplo Unidad o Caja.
    unidadMedida: str = Field(..., description="Unidad de medida")

    # Cantidad que se encuentra disponible actualmente.
    cantidadActual: int = Field(
        ...,
        ge=0,
        description="Cantidad disponible",
    )

    # Cantidad mínima recomendada antes de generar una alerta.
    stockMinimo: int = Field(
        ...,
        ge=0,
        description="Cantidad mínima esperada",
    )

    # Identificador opcional relacionado con una etiqueta QR.
    etiquetaQr: str | None = Field(
        default=None,
        description="Etiqueta QR del inventario",
    )

    # Estado actual del producto, por ejemplo Disponible o Agotado.
    estado: str = Field(..., description="Estado del producto")

    # Fecha de la última actualización del inventario.
    ultimaActualizacion: datetime = Field(
        ...,
        description="Fecha de la última actualización",
    )
