"""
Rutas FastAPI para el componente Inventario IA.

Expone un servicio academico de recomendacion basado en contratos
publicos comparables de SECOP II.
"""

from functools import lru_cache
from pathlib import Path

from fastapi import APIRouter
from fastapi import HTTPException
from pydantic import BaseModel
from pydantic import Field

from app.inventario_ia.recomendador import CATEGORIAS_VALIDAS
from app.inventario_ia.recomendador import VERSION_RECOMENDADOR
from app.inventario_ia.recomendador import cargar_contratos
from app.inventario_ia.recomendador import recomendar


router = APIRouter(
    prefix="/api/inventario-ia",
    tags=["Inventario IA"],
)


RUTA_MODULO = Path(__file__).resolve().parents[2]

RUTA_DATOS = (
    RUTA_MODULO
    / "datos_inventario_ia"
    / "contratos_limpios.json"
)


class EvaluacionCompraEntrada(BaseModel):
    """
    Datos recibidos para evaluar una compra tecnologica.
    """

    descripcion: str = Field(
        ...,
        min_length=10,
        max_length=1000,
    )

    valor_propuesto: float = Field(
        ...,
        gt=0,
    )

    categoria: str = Field(
        default="",
        max_length=100,
    )

    departamento: str = Field(
        default="",
        max_length=150,
    )

    modalidad: str = Field(
        default="",
        max_length=200,
    )

    sector: str = Field(
        default="",
        max_length=200,
    )

    cantidad_vecinos: int = Field(
        default=25,
        ge=5,
        le=100,
    )


@lru_cache(maxsize=1)
def obtener_contratos():
    """
    Carga los contratos una sola vez durante la ejecucion.
    """
    return cargar_contratos(RUTA_DATOS)


@router.get("/health")
def comprobar_inventario_ia():
    """
    Confirma que el componente y sus datos estan disponibles.
    """
    try:
        contratos = obtener_contratos()
    except (FileNotFoundError, ValueError) as error:
        raise HTTPException(
            status_code=503,
            detail=(
                "El conjunto de datos de Inventario IA "
                "no esta disponible."
            ),
        ) from error

    return {
        "componente": "Inventario IA",
        "estado": "activo",
        "version_recomendador": VERSION_RECOMENDADOR,
        "registros_disponibles": len(contratos),
        "fuente": "SECOP II - Contratos Electronicos",
    }


@router.get("/modelo")
def consultar_modelo():
    """
    Entrega informacion general y limitaciones del recomendador.
    """
    return {
        "nombre": "Recomendador por contratos comparables",
        "version": VERSION_RECOMENDADOR,
        "metodo": "Vecinos comparables explicables",
        "categorias": sorted(CATEGORIAS_VALIDAS),
        "decision_final_humana": True,
        "limitaciones": [
            (
                "Utiliza valores contractuales totales "
                "y no precios unitarios."
            ),
            (
                "El resultado tiene finalidad academica "
                "y no reemplaza una aprobacion de compra."
            ),
        ],
    }


@router.post("/evaluar")
def evaluar_compra(entrada: EvaluacionCompraEntrada):
    """
    Evalua una compra propuesta con contratos comparables.
    """
    if (
        entrada.categoria
        and entrada.categoria not in CATEGORIAS_VALIDAS
    ):
        raise HTTPException(
            status_code=422,
            detail="La categoria suministrada no es valida.",
        )

    try:
        contratos = obtener_contratos()

        return recomendar(
            contratos=contratos,
            descripcion=entrada.descripcion,
            valor_propuesto=entrada.valor_propuesto,
            categoria=entrada.categoria,
            departamento=entrada.departamento,
            modalidad=entrada.modalidad,
            sector=entrada.sector,
            cantidad_vecinos=entrada.cantidad_vecinos,
        )

    except FileNotFoundError as error:
        raise HTTPException(
            status_code=503,
            detail=(
                "El conjunto de datos de Inventario IA "
                "no esta disponible."
            ),
        ) from error

    except ValueError as error:
        raise HTTPException(
            status_code=422,
            detail=str(error),
        ) from error
