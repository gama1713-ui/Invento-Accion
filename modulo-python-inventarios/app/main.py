from fastapi import FastAPI

from app.routes.inventarios import router as inventarios_router


# Crea la aplicación principal de la API REST de inventarios.
app = FastAPI(
    title="API de Consulta de Inventarios",
    description=(
        "API REST educativa para consultar información actualizada "
        "de inventarios en el proyecto Invento-Accion."
    ),
    version="1.0.0",
)


# Registra las rutas encargadas de consultar los inventarios.
app.include_router(inventarios_router)


@app.get("/")
def inicio():
    """
    Confirma que la API de inventarios se encuentra activa.

    Esta ruta no consulta ni modifica información de MongoDB.
    """
    return {
        "aplicacion": "API de Consulta de Inventarios",
        "estado": "activa",
    }
