from fastapi import FastAPI

from app.routes.inventario_ia import router as inventario_ia_router
from app.routes.inventarios import router as inventarios_router


# Crea la aplicacion principal de la API REST de inventarios.
app = FastAPI(
    title="API de Consulta de Inventarios",
    description=(
        "API REST educativa para consultar inventarios y evaluar "
        "compras tecnologicas en el proyecto Invento-Accion."
    ),
    version="1.1.0",
)


# Registra las rutas de consulta de inventarios.
app.include_router(inventarios_router)

# Registra las rutas academicas de Inventario IA.
app.include_router(inventario_ia_router)


@app.get("/")
def inicio():
    """
    Confirma que la API de inventarios se encuentra activa.

    Esta ruta no consulta ni modifica informacion de MongoDB.
    """
    return {
        "aplicacion": "API de Consulta de Inventarios",
        "estado": "activa",
        "inventario_ia": "disponible",
    }
