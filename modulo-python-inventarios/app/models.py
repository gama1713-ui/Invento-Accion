from datetime import datetime

from pydantic import BaseModel, Field


class InventarioRespuesta(BaseModel):
    """
    Define la información de inventario agregado que la API
    entrega al usuario.

    Este modelo solamente valida respuestas.
    No guarda, modifica ni elimina información de MongoDB.
    """

    codigo: str = Field(
        ...,
        description="Código único del inventario",
    )

    nombre: str = Field(
        ...,
        description="Nombre del producto",
    )

    categoria: str = Field(
        ...,
        description="Categoría del producto",
    )

    ubicacion: str = Field(
        ...,
        description="Ubicación del producto",
    )

    unidadMedida: str = Field(
        ...,
        description="Unidad de medida",
    )

    cantidadActual: int = Field(
        ...,
        ge=0,
        description="Cantidad disponible",
    )

    stockMinimo: int = Field(
        ...,
        ge=0,
        description="Cantidad mínima esperada",
    )

    etiquetaQr: str | None = Field(
        default=None,
        description="Etiqueta QR del inventario",
    )

    estado: str = Field(
        ...,
        description="Estado del producto",
    )

    ultimaActualizacion: datetime = Field(
        ...,
        description="Fecha de la última actualización",
    )


class ActivoPruebaRespuesta(BaseModel):
    """
    Define la información pública de un activo sintético académico.

    El modelo valida documentos de la colección activos_prueba.
    No crea, modifica, reemplaza ni elimina documentos.
    """

    id_activo: int = Field(
        ...,
        ge=1,
        description="Identificador numérico del activo sintético",
    )

    codigo_inventario: str = Field(
        ...,
        min_length=1,
        description="Código único de inventario",
    )

    placa: str = Field(
        ...,
        min_length=1,
        description="Placa patrimonial sintética",
    )

    tipo_activo: str = Field(
        ...,
        min_length=1,
        description="Tipo del activo tecnológico",
    )

    descripcion: str = Field(
        ...,
        description="Descripción del activo",
    )

    marca: str = Field(
        ...,
        min_length=1,
        description="Marca del activo",
    )

    modelo: str = Field(
        ...,
        min_length=1,
        description="Modelo del activo",
    )

    serial: str = Field(
        ...,
        min_length=1,
        description="Serial sintético único",
    )

    hostname: str = Field(
        ...,
        description="Hostname cuando corresponde",
    )

    extension_ip: str = Field(
        ...,
        description="Extensión de telefonía IP cuando corresponde",
    )

    sistema_operativo: str = Field(
        ...,
        description="Sistema operativo o firmware",
    )

    memoria: str = Field(
        ...,
        description="Capacidad de memoria",
    )

    almacenamiento: str = Field(
        ...,
        description="Capacidad de almacenamiento",
    )

    especificacion: str = Field(
        ...,
        description="Especificación principal",
    )

    estado: str = Field(
        ...,
        min_length=1,
        description="Estado actual del activo",
    )

    id_usuario: str = Field(
        ...,
        min_length=1,
        description="Identificador del usuario sintético asignado",
    )

    usuario_asignado: str = Field(
        ...,
        min_length=1,
        description="Nombre sintético del usuario asignado",
    )

    vinculacion: str = Field(
        ...,
        min_length=1,
        description="Tipo de vinculación simulada",
    )

    modalidad: str = Field(
        ...,
        min_length=1,
        description="Modalidad A, B o C",
    )

    area_tecnologia: str = Field(
        ...,
        min_length=1,
        description="Área tecnológica simulada",
    )

    ciudad: str = Field(
        ...,
        min_length=1,
        description="Ciudad simulada de asignación",
    )

    id_proveedor: str = Field(
        ...,
        min_length=1,
        description="Identificador del proveedor sintético",
    )

    fecha_compra: datetime = Field(
        ...,
        description="Fecha simulada de compra",
    )

    garantia_meses: int = Field(
        ...,
        ge=0,
        description="Garantía expresada en meses",
    )

    qr_payload: str = Field(
        ...,
        min_length=1,
        description="Contenido textual para generar el código QR",
    )

    code128_value: str = Field(
        ...,
        min_length=1,
        description="Valor textual para Code 128",
    )

    observaciones: str = Field(
        ...,
        description="Observaciones académicas del activo",
    )

    dato_sintetico: bool = Field(
        ...,
        description="Confirma que el documento es sintético",
    )

    tipo_dataset: str = Field(
        ...,
        min_length=1,
        description="Clasificación académica del conjunto de datos",
    )
