import os

import certifi

from dotenv import load_dotenv
from pymongo import MongoClient
from pymongo.collection import Collection
from pymongo.database import Database


# Carga las variables locales desde un archivo .env cuando existe.
# En Render, las variables se obtendrán directamente del servicio.
load_dotenv()


# Lee la dirección de MongoDB desde el entorno.
# Si no existe, utiliza MongoDB local para las pruebas de desarrollo.
MONGODB_URI = os.getenv(
    "MONGODB_URI",
    "mongodb://localhost:27017/invento_accion",
)

# Define la base y la colección usadas únicamente por este módulo.
MONGODB_DATABASE = os.getenv("MONGODB_DATABASE", "invento_accion")
MONGODB_COLLECTION = os.getenv("MONGODB_COLLECTION", "inventarios")


# Crea el cliente que permite comunicarse con MongoDB.
# El tiempo máximo evita que una conexión caída quede esperando demasiado.
mongo_client = MongoClient(
    MONGODB_URI,
    serverSelectionTimeoutMS=5000,
    tlsCAFile=certifi.where(),
)

# Selecciona la base de datos del proyecto Invento-Accion.
database: Database = mongo_client[MONGODB_DATABASE]

# Selecciona la colección exclusiva del módulo de inventarios.
inventarios_collection: Collection = database[MONGODB_COLLECTION]


def comprobar_conexion() -> bool:
    """
    Comprueba si MongoDB responde.

    Esta función no crea, modifica ni elimina información.
    Solo envía una solicitud sencilla para validar la conexión.
    """
    mongo_client.admin.command("ping")
    return True
