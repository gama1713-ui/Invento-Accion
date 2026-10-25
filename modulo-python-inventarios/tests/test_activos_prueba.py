from datetime import datetime, timezone

import pytest
from fastapi.testclient import TestClient

from app.main import app
from app.routes import activos_prueba


client = TestClient(app)


ACTIVO_SINTETICO = {
    "id_activo": 1,
    "codigo_inventario": "INV-TEST-0001",
    "placa": "PLACA-TEST-0001",
    "tipo_activo": "Computador portátil",
    "descripcion": "Activo sintético para pruebas automatizadas",
    "marca": "Marca Académica",
    "modelo": "Modelo Prueba",
    "serial": "SERIAL-TEST-0001",
    "hostname": "EQUIPO-TEST-001",
    "extension_ip": "",
    "sistema_operativo": "Sistema de prueba",
    "memoria": "16 GB",
    "almacenamiento": "512 GB",
    "especificacion": "Configuración académica sintética",
    "estado": "Activo",
    "id_usuario": "USR-TEST-001",
    "usuario_asignado": "Usuario Sintético",
    "vinculacion": "Académica",
    "modalidad": "A",
    "area_tecnologia": "Pruebas de software",
    "ciudad": "Bogotá",
    "id_proveedor": "PROV-TEST-001",
    "fecha_compra": datetime(
        2026,
        1,
        15,
        12,
        0,
        tzinfo=timezone.utc,
    ),
    "garantia_meses": 12,
    "qr_payload": "INV-TEST-0001",
    "code128_value": "INV-TEST-0001",
    "observaciones": "Dato sintético exclusivo para pytest",
    "dato_sintetico": True,
    "tipo_dataset": "academico_prueba",
}


class CursorSimulado:
    def __init__(self, documentos):
        self.documentos = [
            documento.copy()
            for documento in documentos
        ]

    def sort(self, campo, orden):
        self.documentos.sort(
            key=lambda documento: documento.get(campo, "")
        )
        return self

    def skip(self, cantidad):
        self.documentos = self.documentos[cantidad:]
        return self

    def limit(self, cantidad):
        self.documentos = self.documentos[:cantidad]
        return self

    def __iter__(self):
        return iter(self.documentos)


class ColeccionSimulada:
    def __init__(self, documentos=None):
        self.documentos = [
            documento.copy()
            for documento in (documentos or [])
        ]

    def count_documents(self, filtro):
        return len(self.documentos)

    def find(self, filtro, campos):
        return CursorSimulado(self.documentos)

    def find_one(self, filtro, campos):
        condiciones = filtro.get("$or", [])

        for documento in self.documentos:
            for condicion in condiciones:
                campo, valor = next(iter(condicion.items()))

                if documento.get(campo) == valor:
                    return documento.copy()

        return None


@pytest.fixture(autouse=True)
def reemplazar_coleccion_mongodb(monkeypatch):
    coleccion = ColeccionSimulada([ACTIVO_SINTETICO])

    monkeypatch.setattr(
        activos_prueba,
        "activos_prueba_collection",
        coleccion,
    )

    return coleccion


def test_cp01_disponibilidad_del_servicio():
    response = client.get("/")

    assert response.status_code == 200
    assert response.json()["estado"] == "activa"


def test_cp02_consulta_codigo_existente():
    response = client.get(
        "/api/activos-prueba/INV-TEST-0001"
    )

    assert response.status_code == 200
    assert (
        response.json()["codigo_inventario"]
        == "INV-TEST-0001"
    )
    assert response.json()["dato_sintetico"] is True


def test_cp03_codigo_inexistente():
    response = client.get(
        "/api/activos-prueba/INV-CODIGO-INEXISTENTE"
    )

    assert response.status_code == 404
    assert (
        response.json()["detail"]
        == "Activo sintético no encontrado."
    )


def test_cp04_primera_pagina():
    response = client.get(
        "/api/activos-prueba",
        params={"pagina": 1, "tamano": 10},
    )

    assert response.status_code == 200
    assert response.headers["content-type"].startswith(
        "application/json"
    )

    contenido = response.json()

    assert len(contenido["elementos"]) == 1
    assert contenido["paginacion"]["pagina"] == 1
    assert contenido["paginacion"]["tamano"] == 10
    assert contenido["paginacion"]["totalRegistros"] == 1


@pytest.mark.parametrize("tamano", [0, 101])
def test_cp05_limite_invalido(tamano):
    response = client.get(
        "/api/activos-prueba",
        params={"pagina": 1, "tamano": tamano},
    )

    assert response.status_code == 422


def test_cp06_resultado_vacio(monkeypatch):
    monkeypatch.setattr(
        activos_prueba,
        "activos_prueba_collection",
        ColeccionSimulada([]),
    )

    response = client.get(
        "/api/activos-prueba",
        params={"pagina": 1, "tamano": 10},
    )

    assert response.status_code == 200

    contenido = response.json()

    assert contenido["elementos"] == []
    assert contenido["paginacion"]["totalRegistros"] == 0
    assert contenido["paginacion"]["totalPaginas"] == 0
    assert contenido["paginacion"]["tieneSiguiente"] is False
