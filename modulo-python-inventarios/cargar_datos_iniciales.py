import json
from datetime import datetime
from pathlib import Path

from app.database import inventarios_collection
from app.models import InventarioRespuesta


# Define la ubicación del archivo que contiene los registros educativos.
ARCHIVO_DATOS = Path(__file__).parent / "datos-iniciales.json"


def cargar_datos() -> None:
    """
    Carga los registros iniciales sin duplicar códigos existentes.

    El script no elimina ni reemplaza documentos.
    Solamente inserta registros cuyo código todavía no existe.
    """

    # Lee el archivo JSON local utilizando codificación UTF-8.
    with ARCHIVO_DATOS.open("r", encoding="utf-8") as archivo:
        datos = json.load(archivo)

    registros_nuevos = []
    registros_omitidos = 0

    for registro in datos:
        # Valida que el registro cumpla la estructura definida por la API.
        inventario_validado = InventarioRespuesta.model_validate(registro)

        # Convierte el modelo validado en un diccionario para MongoDB.
        documento = inventario_validado.model_dump()

        # Convierte la fecha del JSON en una fecha real para MongoDB.
        if isinstance(documento["ultimaActualizacion"], str):
            documento["ultimaActualizacion"] = datetime.fromisoformat(
                documento["ultimaActualizacion"]
            )

        # Comprueba si el código ya está guardado para evitar duplicados.
        existe = inventarios_collection.find_one(
            {"codigo": documento["codigo"]},
            {"_id": 1},
        )

        if existe is None:
            registros_nuevos.append(documento)
        else:
            registros_omitidos += 1

    # Inserta únicamente los registros que todavía no existen.
    if registros_nuevos:
        inventarios_collection.insert_many(registros_nuevos)

    print("Registros insertados:", len(registros_nuevos))
    print("Registros omitidos:", registros_omitidos)


if __name__ == "__main__":
    cargar_datos()
