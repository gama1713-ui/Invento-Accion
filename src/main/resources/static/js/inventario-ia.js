"use strict";

/*
 * Interfaz web de Inventario IA.
 *
 * Consume exclusivamente las rutas publicas entregadas por Spring Boot:
 *
 * GET  /api/inventario-ia/health
 * GET  /api/inventario-ia/modelo
 * POST /api/inventario-ia/evaluar
 *
 * El navegador nunca se conecta directamente con FastAPI.
 */

const formulario = document.getElementById("formularioInventarioIa");
const descripcion = document.getElementById("descripcionCompra");
const valorPropuesto = document.getElementById("valorPropuesto");
const categoria = document.getElementById("categoriaCompra");
const departamento = document.getElementById("departamentoCompra");
const modalidad = document.getElementById("modalidadCompra");
const sector = document.getElementById("sectorCompra");
const cantidadVecinos = document.getElementById("cantidadVecinos");
const botonEvaluar = document.getElementById("botonEvaluar");
const mensaje = document.getElementById("mensajeInventarioIa");
const resultado = document.getElementById("resultadoInventarioIa");
const decision = document.getElementById("decisionInventarioIa");
const explicacion = document.getElementById("explicacionInventarioIa");
const valorReferencia = document.getElementById("valorReferencia");
const rangoReferencia = document.getElementById("rangoReferencia");
const diferenciaPorcentual = document.getElementById(
  "diferenciaPorcentual"
);
const comparablesEncontrados = document.getElementById(
  "comparablesEncontrados"
);
const cuerpoComparables = document.getElementById(
  "cuerpoComparables"
);
const listaLimitaciones = document.getElementById(
  "listaLimitaciones"
);
const estadoServicio = document.getElementById(
  "estadoServicioInventarioIa"
);


function mostrarMensaje(texto, tipo) {
  mensaje.className = `alert alert-${tipo}`;
  mensaje.textContent = texto;
}


function formatearMoneda(valor) {
  const numero = Number(valor);

  if (!Number.isFinite(numero)) {
    return "Sin dato";
  }

  return new Intl.NumberFormat("es-CO", {
    style: "currency",
    currency: "COP",
    maximumFractionDigits: 0
  }).format(numero);
}


function formatearPorcentaje(valor) {
  const numero = Number(valor);

  if (!Number.isFinite(numero)) {
    return "Sin dato";
  }

  return `${numero.toFixed(2)} %`;
}


function limpiarComparables() {
  cuerpoComparables.replaceChildren();
}


function agregarComparable(comparable) {
  const fila = document.createElement("tr");

  const valores = [
    comparable.id_contrato,
    comparable.categoria_ia,
    comparable.departamento,
    comparable.modalidad,
    formatearMoneda(comparable.valor_contrato_cop),
    Number(comparable.puntaje_similitud).toFixed(4)
  ];

  valores.forEach(function (valor) {
    const celda = document.createElement("td");
    celda.textContent = valor ?? "";
    fila.appendChild(celda);
  });

  cuerpoComparables.appendChild(fila);
}


function mostrarComparables(comparables) {
  limpiarComparables();

  if (!Array.isArray(comparables) || comparables.length === 0) {
    const fila = document.createElement("tr");
    const celda = document.createElement("td");

    celda.colSpan = 6;
    celda.className = "text-center text-muted";
    celda.textContent = "No se encontraron comparables para mostrar.";

    fila.appendChild(celda);
    cuerpoComparables.appendChild(fila);
    return;
  }

  comparables.forEach(agregarComparable);
}


function mostrarLimitaciones(limitaciones) {
  listaLimitaciones.replaceChildren();

  if (!Array.isArray(limitaciones)) {
    return;
  }

  limitaciones.forEach(function (texto) {
    const elemento = document.createElement("li");
    elemento.textContent = texto;
    listaLimitaciones.appendChild(elemento);
  });
}


function claseDecision(nombreDecision) {
  if (nombreDecision === "FAVORABLE_COMO_REFERENCIA") {
    return "alert alert-success";
  }

  if (
    nombreDecision === "REQUIERE_REVISION"
    || nombreDecision === "VALOR_ATIPICO_BAJO"
  ) {
    return "alert alert-warning";
  }

  return "alert alert-secondary";
}


function mostrarResultado(datos) {
  decision.className = claseDecision(datos.decision);
  decision.textContent = datos.decision || "SIN_DECISION";

  explicacion.textContent = datos.explicacion || "Sin explicacion.";

  valorReferencia.textContent = formatearMoneda(
    datos.valor_referencia_cop
  );

  const rango = datos.rango_referencial_cop || {};

  rangoReferencia.textContent =
    `${formatearMoneda(rango.inferior)} a `
    + `${formatearMoneda(rango.superior)}`;

  diferenciaPorcentual.textContent = formatearPorcentaje(
    datos.diferencia_porcentual
  );

  comparablesEncontrados.textContent = String(
    datos.comparables_encontrados ?? 0
  );

  mostrarComparables(datos.comparables_destacados);
  mostrarLimitaciones(datos.limitaciones);

  resultado.hidden = false;
  resultado.scrollIntoView({
    behavior: "smooth",
    block: "start"
  });
}


async function obtenerError(respuesta) {
  try {
    const datos = await respuesta.json();

    if (typeof datos.detail === "string") {
      return datos.detail;
    }

    if (Array.isArray(datos.detail)) {
      return datos.detail
        .map(function (elemento) {
          return elemento.msg || "Dato invalido.";
        })
        .join(" ");
    }
  } catch (error) {
    console.error("No fue posible interpretar el error.", error);
  }

  return "No fue posible completar la evaluacion.";
}


async function comprobarServicio() {
  try {
    const respuesta = await fetch(
      "/api/inventario-ia/health",
      {
        method: "GET",
        headers: {
          Accept: "application/json"
        },
        cache: "no-store"
      }
    );

    if (!respuesta.ok) {
      throw new Error("El servicio no esta disponible.");
    }

    const datos = await respuesta.json();

    estadoServicio.className = "badge bg-success";
    estadoServicio.textContent =
      `Activo, ${datos.registros_disponibles} registros`;
  } catch (error) {
    estadoServicio.className = "badge bg-danger";
    estadoServicio.textContent = "No disponible";
  }
}


async function evaluarCompra(evento) {
  evento.preventDefault();

  const valor = Number(valorPropuesto.value);
  const vecinos = Number(cantidadVecinos.value);

  if (descripcion.value.trim().length < 10) {
    mostrarMensaje(
      "La descripcion debe contener al menos 10 caracteres.",
      "warning"
    );
    descripcion.focus();
    return;
  }

  if (!Number.isFinite(valor) || valor <= 0) {
    mostrarMensaje(
      "El valor propuesto debe ser mayor que cero.",
      "warning"
    );
    valorPropuesto.focus();
    return;
  }

  if (!Number.isInteger(vecinos) || vecinos < 5 || vecinos > 100) {
    mostrarMensaje(
      "La cantidad de comparables debe estar entre 5 y 100.",
      "warning"
    );
    cantidadVecinos.focus();
    return;
  }

  botonEvaluar.disabled = true;
  resultado.hidden = true;

  mostrarMensaje(
    "Analizando contratos publicos comparables...",
    "info"
  );

  const solicitud = {
    descripcion: descripcion.value.trim(),
    valor_propuesto: valor,
    categoria: categoria.value,
    departamento: departamento.value.trim(),
    modalidad: modalidad.value.trim(),
    sector: sector.value.trim(),
    cantidad_vecinos: vecinos
  };

  try {
    const respuesta = await fetch(
      "/api/inventario-ia/evaluar",
      {
        method: "POST",
        headers: {
          Accept: "application/json",
          "Content-Type": "application/json"
        },
        cache: "no-store",
        body: JSON.stringify(solicitud)
      }
    );

    if (!respuesta.ok) {
      const textoError = await obtenerError(respuesta);
      throw new Error(textoError);
    }

    const datos = await respuesta.json();

    mostrarResultado(datos);

    mostrarMensaje(
      "Evaluacion academica completada correctamente.",
      "success"
    );
  } catch (error) {
    mostrarMensaje(
      error.message || "El servicio de Inventario IA no esta disponible.",
      "danger"
    );
  } finally {
    botonEvaluar.disabled = false;
  }
}


formulario.addEventListener("submit", evaluarCompra);

comprobarServicio();
