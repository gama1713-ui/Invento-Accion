"use strict";

/*
 * Consulta de Activos Sintéticos.
 *
 * Consume únicamente las rutas públicas entregadas por Spring Boot:
 *
 * GET /api/activos-prueba?pagina=1&tamano=25
 * GET /api/activos-prueba/estado
 * GET /api/activos-prueba/{codigo}
 *
 * Este módulo es exclusivamente de lectura.
 */

const TAMANO_PREDETERMINADO = 25;
const TAMANO_MAXIMO = 100;
const TIEMPO_MAXIMO_SOLICITUD_MS = 15000;

const formularioConsulta = document.getElementById(
  "formularioConsulta"
);

const identificadorActivo = document.getElementById(
  "identificadorActivo"
);

const botonMostrarActivos = document.getElementById(
  "botonMostrarActivos"
);

const mensajeConsulta = document.getElementById(
  "mensajeConsulta"
);

const cuerpoTablaActivos = document.getElementById(
  "cuerpoTablaActivos"
);

const estadoServicio = document.getElementById(
  "estadoServicio"
);

const controlesPaginacion = document.getElementById(
  "controlesPaginacion"
);

const tamanoPagina = document.getElementById(
  "tamanoPagina"
);

const botonPaginaAnterior = document.getElementById(
  "botonPaginaAnterior"
);

const botonPaginaSiguiente = document.getElementById(
  "botonPaginaSiguiente"
);

const informacionPagina = document.getElementById(
  "informacionPagina"
);

let paginaActual = 1;
let totalPaginas = 0;
let consultaActiva = null;


/*
 * Muestra un mensaje dentro de la página.
 */
function mostrarMensaje(texto, tipo) {
  mensajeConsulta.className = `alert alert-${tipo}`;
  mensajeConsulta.textContent = texto;
}


/*
 * Muestra una fila informativa dentro de la tabla.
 */
function mostrarFilaInformativa(texto) {
  cuerpoTablaActivos.replaceChildren();

  const fila = document.createElement("tr");
  const celda = document.createElement("td");

  celda.colSpan = 10;
  celda.className = "text-center text-muted py-4";
  celda.textContent = texto;

  fila.appendChild(celda);
  cuerpoTablaActivos.appendChild(fila);
}


/*
 * Cambia el indicador visual del estado del servicio.
 */
function actualizarEstadoServicio(texto, tipo) {
  estadoServicio.className = `badge text-bg-${tipo}`;
  estadoServicio.textContent = texto;
}


/*
 * Habilita o deshabilita los controles mientras existe
 * una solicitud en curso.
 */
function establecerControlesBloqueados(bloqueados) {
  botonMostrarActivos.disabled = bloqueados;
  identificadorActivo.disabled = bloqueados;
  tamanoPagina.disabled = bloqueados;

  const botonConsultar = formularioConsulta.querySelector(
    'button[type="submit"]'
  );

  if (botonConsultar) {
    botonConsultar.disabled = bloqueados;
  }

  if (bloqueados) {
    botonPaginaAnterior.disabled = true;
    botonPaginaSiguiente.disabled = true;
    return;
  }

  actualizarBotonesPaginacion();
}


/*
 * Cancela la solicitud anterior si todavía se encuentra activa.
 */
function cancelarConsultaAnterior() {
  if (consultaActiva) {
    consultaActiva.abort();
    consultaActiva = null;
  }
}


/*
 * Crea un controlador con timeout para una solicitud.
 */
function crearControladorDeSolicitud() {
  cancelarConsultaAnterior();

  const controlador = new AbortController();

  const temporizador = window.setTimeout(
    function () {
      controlador.abort();
    },
    TIEMPO_MAXIMO_SOLICITUD_MS
  );

  consultaActiva = controlador;

  return {
    controlador,
    finalizar: function () {
      window.clearTimeout(temporizador);

      if (consultaActiva === controlador) {
        consultaActiva = null;
      }
    }
  };
}


/*
 * Convierte la fecha recibida desde la API en texto legible.
 */
function formatearFecha(fechaRecibida) {
  if (!fechaRecibida) {
    return "Sin fecha";
  }

  const fecha = new Date(fechaRecibida);

  if (Number.isNaN(fecha.getTime())) {
    return fechaRecibida;
  }

  return new Intl.DateTimeFormat("es-CO", {
    dateStyle: "medium",
    timeStyle: "short"
  }).format(fecha);
}


/*
 * Crea una etiqueta visual para el estado del inventario.
 */
function crearEtiquetaEstado(estado) {
  const etiqueta = document.createElement("span");
  const estadoNormalizado = String(
    estado || ""
  ).toLowerCase();

  etiqueta.classList.add("etiqueta-estado");
  etiqueta.textContent = estado || "Sin estado";

  if (
    estadoNormalizado.includes("baja")
    || estadoNormalizado.includes("agotado")
  ) {
    etiqueta.classList.add("estado-bajo");
  } else {
    etiqueta.classList.add("estado-disponible");
  }

  return etiqueta;
}


/*
 * Crea una celda segura mediante textContent.
 */
function crearCelda(texto) {
  const celda = document.createElement("td");
  celda.textContent = texto ?? "";
  return celda;
}


/*
 * Agrega un registro a la tabla.
 */
function agregarActivoATabla(activo) {
  const fila = document.createElement("tr");

  fila.appendChild(crearCelda(activo.codigo_inventario));
  fila.appendChild(crearCelda(activo.placa));
  fila.appendChild(crearCelda(activo.tipo_activo));
  fila.appendChild(crearCelda(activo.marca));
  fila.appendChild(crearCelda(activo.modelo));
  fila.appendChild(crearCelda(activo.serial));
  fila.appendChild(crearCelda(activo.usuario_asignado));
  fila.appendChild(crearCelda(activo.modalidad));
  fila.appendChild(crearCelda(activo.ciudad));

  const celdaEstado = document.createElement("td");

  celdaEstado.appendChild(
    crearEtiquetaEstado(activo.estado)
  );

  fila.appendChild(celdaEstado);
  cuerpoTablaActivos.appendChild(fila);
}


/*
 * Muestra uno o varios registros.
 */
function mostrarActivos(inventarios) {
  cuerpoTablaActivos.replaceChildren();

  if (
    !Array.isArray(inventarios)
    || inventarios.length === 0
  ) {
    mostrarFilaInformativa(
      "No se encontraron activos sintéticos."
    );
    return;
  }

  inventarios.forEach(agregarActivoATabla);
}


/*
 * Lee el mensaje de error enviado por la API.
 */
async function obtenerMensajeDeError(respuesta) {
  try {
    const datosError = await respuesta.json();

    if (datosError.detail) {
      return datosError.detail;
    }
  } catch (error) {
    console.error(
      "No fue posible leer el mensaje de la API.",
      error
    );
  }

  return "No fue posible completar la consulta.";
}


/*
 * Obtiene y valida el tamaño de página seleccionado.
 */
function obtenerTamanoPagina() {
  const tamano = Number.parseInt(
    tamanoPagina.value,
    10
  );

  if (
    !Number.isInteger(tamano)
    || tamano < 1
    || tamano > TAMANO_MAXIMO
  ) {
    return TAMANO_PREDETERMINADO;
  }

  return tamano;
}


/*
 * Actualiza los controles de navegación.
 */
function actualizarBotonesPaginacion() {
  const tienePaginas = totalPaginas > 0;

  controlesPaginacion.classList.toggle(
    "d-none",
    !tienePaginas
  );

  controlesPaginacion.classList.toggle(
    "d-flex",
    tienePaginas
  );

  botonPaginaAnterior.disabled = (
    !tienePaginas
    || paginaActual <= 1
  );

  botonPaginaSiguiente.disabled = (
    !tienePaginas
    || paginaActual >= totalPaginas
  );

  if (!tienePaginas) {
    informacionPagina.textContent =
      "Sin páginas disponibles";
    return;
  }

  informacionPagina.textContent =
    `Página ${paginaActual} de ${totalPaginas}`;
}


/*
 * Aplica la información de paginación recibida.
 */
function aplicarPaginacion(paginacion) {
  paginaActual = Number(
    paginacion.pagina || 1
  );

  totalPaginas = Number(
    paginacion.totalPaginas || 0
  );

  actualizarBotonesPaginacion();
}


/*
 * Describe los errores de cancelación y timeout.
 */
function obtenerMensajeSolicitud(error) {
  if (error && error.name === "AbortError") {
    return (
      "La consulta fue cancelada o superó "
      + "el tiempo máximo permitido."
    );
  }

  return (
    error.message
    || "El servicio de activos sintéticos no está disponible."
  );
}


/*
 * Consulta una página controlada de activos sintéticos.
 */
async function consultarPaginaActivos(
  paginaSolicitada
) {
  const tamano = obtenerTamanoPagina();

  mostrarMensaje(
    "Consultando activos sintéticos...",
    "info"
  );

  mostrarFilaInformativa(
    "Cargando información..."
  );

  establecerControlesBloqueados(true);

  const solicitud = crearControladorDeSolicitud();

  try {
    const parametros = new URLSearchParams({
      pagina: String(paginaSolicitada),
      tamano: String(tamano)
    });

    const respuesta = await fetch(
      `/api/activos-prueba?${parametros.toString()}`,
      {
        method: "GET",
        headers: {
          Accept: "application/json"
        },
        cache: "no-store",
        signal: solicitud.controlador.signal
      }
    );

    if (!respuesta.ok) {
      const mensajeError =
        await obtenerMensajeDeError(respuesta);

      throw new Error(mensajeError);
    }

    const datos = await respuesta.json();

    if (
      !datos
      || !Array.isArray(datos.elementos)
      || !datos.paginacion
    ) {
      throw new Error(
        "La respuesta del servicio no tiene "
        + "el formato esperado."
      );
    }

    mostrarActivos(datos.elementos);
    aplicarPaginacion(datos.paginacion);

    const totalRegistros = Number(
      datos.paginacion.totalRegistros || 0
    );

    mostrarMensaje(
      `Consulta completada. Total de registros: `
      + `${totalRegistros}.`,
      "success"
    );

    actualizarEstadoServicio(
      "Disponible",
      "success"
    );
  } catch (error) {
    if (
      error
      && error.name === "AbortError"
      && consultaActiva !== solicitud.controlador
    ) {
      return;
    }

    mostrarFilaInformativa(
      "No fue posible cargar los activos sintéticos."
    );

    mostrarMensaje(
      obtenerMensajeSolicitud(error),
      "danger"
    );

    actualizarEstadoServicio(
      "No disponible",
      "danger"
    );
  } finally {
    solicitud.finalizar();

    if (consultaActiva === null) {
      establecerControlesBloqueados(false);
    }
  }
}


/*
 * Consulta un activo por código, placa, serial o ID.
 */
async function consultarActivoPorIdentificador(codigo) {
  const codigoLimpio = codigo.trim();

  if (!codigoLimpio) {
    mostrarMensaje(
      "Ingresa un código, placa, serial o ID antes de consultar.",
      "warning"
    );

    identificadorActivo.focus();
    return;
  }

  controlesPaginacion.classList.add("d-none");
  controlesPaginacion.classList.remove("d-flex");
  totalPaginas = 0;

  mostrarMensaje(
    `Consultando el identificador ${codigoLimpio}...`,
    "info"
  );

  mostrarFilaInformativa(
    "Buscando activo sintético..."
  );

  establecerControlesBloqueados(true);

  const solicitud = crearControladorDeSolicitud();

  try {
    const codigoSeguro =
      encodeURIComponent(codigoLimpio);

    const respuesta = await fetch(
      `/api/activos-prueba/${codigoSeguro}`,
      {
        method: "GET",
        headers: {
          Accept: "application/json"
        },
        cache: "no-store",
        signal: solicitud.controlador.signal
      }
    );

    if (!respuesta.ok) {
      const mensajeError =
        await obtenerMensajeDeError(respuesta);

      throw new Error(mensajeError);
    }

    const activo = await respuesta.json();

    mostrarActivos([activo]);

    mostrarMensaje(
      `Activo ${activo.codigo_inventario} `
      + "encontrado correctamente.",
      "success"
    );

    actualizarEstadoServicio(
      "Disponible",
      "success"
    );
  } catch (error) {
    if (
      error
      && error.name === "AbortError"
      && consultaActiva !== solicitud.controlador
    ) {
      return;
    }

    mostrarFilaInformativa(
      "No se encontró información para mostrar."
    );

    mostrarMensaje(
      obtenerMensajeSolicitud(error),
      "danger"
    );
  } finally {
    solicitud.finalizar();

    if (consultaActiva === null) {
      establecerControlesBloqueados(false);
    }
  }
}


/*
 * Consulta el estado sin descargar activos sintéticos.
 */
async function consultarEstadoDelServicio() {
  const controlador = new AbortController();

  const temporizador = window.setTimeout(
    function () {
      controlador.abort();
    },
    TIEMPO_MAXIMO_SOLICITUD_MS
  );

  actualizarEstadoServicio(
    "Verificando",
    "warning"
  );

  try {
    const respuesta = await fetch(
      "/api/activos-prueba/estado",
      {
        method: "GET",
        headers: {
          Accept: "application/json"
        },
        cache: "no-store",
        signal: controlador.signal
      }
    );

    if (!respuesta.ok) {
      throw new Error(
        "El servicio no respondió correctamente."
      );
    }

    const datos = await respuesta.json();

    if (datos.estado !== "disponible") {
      throw new Error(
        "El servicio informó un estado no disponible."
      );
    }

    actualizarEstadoServicio(
      "Disponible",
      "success"
    );
  } catch (error) {
    actualizarEstadoServicio(
      "No disponible",
      "danger"
    );
  } finally {
    window.clearTimeout(temporizador);
  }
}


/*
 * Eventos de la página.
 */
formularioConsulta.addEventListener(
  "submit",
  function (evento) {
    evento.preventDefault();

    consultarActivoPorIdentificador(
      identificadorActivo.value
    );
  }
);


botonMostrarActivos.addEventListener(
  "click",
  function () {
    identificadorActivo.value = "";
    paginaActual = 1;

    consultarPaginaActivos(
      paginaActual
    );
  }
);


botonPaginaAnterior.addEventListener(
  "click",
  function () {
    if (paginaActual > 1) {
      consultarPaginaActivos(
        paginaActual - 1
      );
    }
  }
);


botonPaginaSiguiente.addEventListener(
  "click",
  function () {
    if (paginaActual < totalPaginas) {
      consultarPaginaActivos(
        paginaActual + 1
      );
    }
  }
);


tamanoPagina.addEventListener(
  "change",
  function () {
    paginaActual = 1;

    consultarPaginaActivos(
      paginaActual
    );
  }
);


consultarEstadoDelServicio();
