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


const botonEjemploInventarioIa = document.getElementById(
  "botonEjemploInventarioIa"
);

const botonOtroEjemploInventarioIa = document.getElementById(
  "botonOtroEjemploInventarioIa"
);

const botonUsarEjemploInventarioIa = document.getElementById(
  "botonUsarEjemploInventarioIa"
);

const panelEjemploInventarioIa = document.getElementById(
  "panelEjemploInventarioIa"
);

const nivelEjemploInventarioIa = document.getElementById(
  "nivelEjemploInventarioIa"
);

const tituloEjemploInventarioIa = document.getElementById(
  "tituloEjemploInventarioIa"
);

const textoEjemploInventarioIa = document.getElementById(
  "textoEjemploInventarioIa"
);

const valorEjemploInventarioIa = document.getElementById(
  "valorEjemploInventarioIa"
);

const categoriaEjemploInventarioIa = document.getElementById(
  "categoriaEjemploInventarioIa"
);

const departamentoEjemploInventarioIa = document.getElementById(
  "departamentoEjemploInventarioIa"
);

const modalidadEjemploInventarioIa = document.getElementById(
  "modalidadEjemploInventarioIa"
);


const ejemplosInventarioIa = [
  {
    nivel: "Básico",
    titulo: "Compra de computadores portátiles",
    descripcion:
      "Adquisición de 10 computadores portátiles para uso administrativo.",
    valor: 50000000,
    categoria: "COMPRA_EQUIPOS",
    departamento: "",
    modalidad: "",
    sector: "",
    vecinos: 25
  },
  {
    nivel: "Básico",
    titulo: "Compra de monitores",
    descripcion:
      "Adquisición de 20 monitores para puestos de trabajo de oficina.",
    valor: 24000000,
    categoria: "EQUIPOS_COMPLEMENTARIOS",
    departamento: "",
    modalidad: "",
    sector: "",
    vecinos: 25
  },
  {
    nivel: "Intermedio",
    titulo: "Equipos para una sala de formación",
    descripcion:
      "Adquisición de 25 computadores portátiles, 25 mouse y 25 maletines para una sala de formación.",
    valor: 145000000,
    categoria: "COMPRA_MIXTA",
    departamento: "Distrito Capital de Bogotá",
    modalidad: "",
    sector: "Educación Nacional",
    vecinos: 30
  },
  {
    nivel: "Intermedio",
    titulo: "Renovación de puestos de trabajo",
    descripcion:
      "Compra de 30 computadores de escritorio, 30 monitores y periféricos para renovar puestos de trabajo administrativos.",
    valor: 210000000,
    categoria: "COMPRA_MIXTA",
    departamento: "Cundinamarca",
    modalidad: "Mínima cuantía",
    sector: "",
    vecinos: 30
  },
  {
    nivel: "Avanzado",
    titulo: "Infraestructura de servidores",
    descripcion:
      "Adquisición de servidores, sistema de almacenamiento, unidades de respaldo, UPS e instalación para fortalecer la infraestructura tecnológica institucional.",
    valor: 950000000,
    categoria: "EQUIPOS_COMPLEMENTARIOS",
    departamento: "Antioquia",
    modalidad: "Selección abreviada subasta inversa",
    sector: "Servicio Público",
    vecinos: 40
  },
  {
    nivel: "Avanzado",
    titulo: "Dotación tecnológica integral",
    descripcion:
      "Adquisición de computadores portátiles, monitores, estaciones de acoplamiento, licencias ofimáticas, configuración e instalación para personal administrativo.",
    valor: 720000000,
    categoria: "COMPRA_MIXTA",
    departamento: "Distrito Capital de Bogotá",
    modalidad: "Selección abreviada subasta inversa",
    sector: "Educación Nacional",
    vecinos: 40
  },
  {
    nivel: "Súper avanzada",
    titulo: "Modernización tecnológica institucional",
    descripcion:
      "Modernización tecnológica mediante la adquisición de 120 computadores portátiles, 60 computadores de escritorio, 160 monitores, periféricos, licencias, instalación, configuración, capacitación, garantía extendida y soporte técnico.",
    valor: 3200000000,
    categoria: "COMPRA_MIXTA",
    departamento: "Distrito Capital de Bogotá",
    modalidad: "Licitación pública",
    sector: "Servicio Público",
    vecinos: 60
  },
  {
    nivel: "Súper avanzada",
    titulo: "Centro de procesamiento y continuidad",
    descripcion:
      "Implementación de infraestructura tecnológica para centro de procesamiento de datos con servidores, almacenamiento, virtualización, respaldo, redes, UPS, instalación, migración, capacitación, garantía y mantenimiento especializado.",
    valor: 5800000000,
    categoria: "EQUIPOS_COMPLEMENTARIOS",
    departamento: "Antioquia",
    modalidad: "Licitación pública",
    sector: "Tecnologías de la Información",
    vecinos: 75
  }
];


let indiceEjemploInventarioIa = -1;


function nombreCategoriaEjemplo(codigo) {
  const nombres = {
    COMPRA_EQUIPOS: "Compra de equipos",
    COMPRA_MIXTA: "Compra mixta",
    EQUIPOS_COMPLEMENTARIOS: "Equipos complementarios"
  };

  return nombres[codigo] || "Detectar por descripción";
}


function seleccionarEjemploAleatorio() {
  let nuevoIndice = Math.floor(
    Math.random() * ejemplosInventarioIa.length
  );

  if (
    ejemplosInventarioIa.length > 1
    && nuevoIndice === indiceEjemploInventarioIa
  ) {
    nuevoIndice = (
      nuevoIndice + 1
    ) % ejemplosInventarioIa.length;
  }

  indiceEjemploInventarioIa = nuevoIndice;

  const ejemplo = ejemplosInventarioIa[nuevoIndice];

  nivelEjemploInventarioIa.textContent = ejemplo.nivel;
  tituloEjemploInventarioIa.textContent = ejemplo.titulo;
  textoEjemploInventarioIa.textContent = ejemplo.descripcion;

  valorEjemploInventarioIa.textContent = formatearMoneda(
    ejemplo.valor
  );

  categoriaEjemploInventarioIa.textContent =
    nombreCategoriaEjemplo(ejemplo.categoria);

  departamentoEjemploInventarioIa.textContent =
    ejemplo.departamento || "No especificado";

  modalidadEjemploInventarioIa.textContent =
    ejemplo.modalidad || "No especificada";

  panelEjemploInventarioIa.hidden = false;
}


function usarEjemploInventarioIa() {
  if (indiceEjemploInventarioIa < 0) {
    seleccionarEjemploAleatorio();
  }

  const ejemplo = ejemplosInventarioIa[
    indiceEjemploInventarioIa
  ];

  descripcion.value = ejemplo.descripcion;
  valorPropuesto.value = String(ejemplo.valor);
  categoria.value = ejemplo.categoria;
  departamento.value = ejemplo.departamento;
  modalidad.value = ejemplo.modalidad;
  sector.value = ejemplo.sector;
  cantidadVecinos.value = String(ejemplo.vecinos);

  mostrarMensaje(
    "Ejemplo copiado. Puedes adaptarlo antes de evaluar la compra.",
    "success"
  );

  formulario.scrollIntoView({
    behavior: "smooth",
    block: "start"
  });

  descripcion.focus();
}


botonEjemploInventarioIa.addEventListener(
  "click",
  seleccionarEjemploAleatorio
);

botonOtroEjemploInventarioIa.addEventListener(
  "click",
  seleccionarEjemploAleatorio
);

botonUsarEjemploInventarioIa.addEventListener(
  "click",
  usarEjemploInventarioIa
);



formulario.addEventListener("submit", evaluarCompra);

comprobarServicio();
