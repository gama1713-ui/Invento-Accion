"use strict";

/*
 * Consulta Inventario.
 *
 * Consume únicamente las rutas públicas entregadas por Spring Boot:
 *
 * GET /api/inventarios?pagina=1&tamano=25
 * GET /api/inventarios/estado
 * GET /api/inventarios/{codigo}
 *
 * Este módulo es exclusivamente de lectura.
 */

const TAMANO_PREDETERMINADO = 25;
const TAMANO_MAXIMO = 100;
const TIEMPO_MAXIMO_SOLICITUD_MS = 15000;

const formularioConsulta = document.getElementById(
  "formularioConsulta"
);

const codigoInventario = document.getElementById(
  "codigoInventario"
);

const botonMostrarTodos = document.getElementById(
  "botonMostrarTodos"
);

const mensajeConsulta = document.getElementById(
  "mensajeConsulta"
);

const cuerpoTablaInventarios = document.getElementById(
  "cuerpoTablaInventarios"
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
  cuerpoTablaInventarios.replaceChildren();

  const fila = document.createElement("tr");
  const celda = document.createElement("td");

  celda.colSpan = 9;
  celda.className = "text-center text-muted py-4";
  celda.textContent = texto;

  fila.appendChild(celda);
  cuerpoTablaInventarios.appendChild(fila);
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
  botonMostrarTodos.disabled = bloqueados;
  codigoInventario.disabled = bloqueados;
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
function agregarInventarioATabla(inventario) {
  const fila = document.createElement("tr");

  fila.appendChild(crearCelda(inventario.codigo));
  fila.appendChild(crearCelda(inventario.nombre));
  fila.appendChild(crearCelda(inventario.categoria));
  fila.appendChild(crearCelda(inventario.ubicacion));
  fila.appendChild(crearCelda(inventario.unidadMedida));
  fila.appendChild(crearCelda(inventario.cantidadActual));
  fila.appendChild(crearCelda(inventario.stockMinimo));

  const celdaEstado = document.createElement("td");

  celdaEstado.appendChild(
    crearEtiquetaEstado(inventario.estado)
  );

  fila.appendChild(celdaEstado);

  fila.appendChild(
    crearCelda(
      formatearFecha(inventario.ultimaActualizacion)
    )
  );

  cuerpoTablaInventarios.appendChild(fila);
}


/*
 * Muestra uno o varios registros.
 */
function mostrarInventarios(inventarios) {
  cuerpoTablaInventarios.replaceChildren();

  if (
    !Array.isArray(inventarios)
    || inventarios.length === 0
  ) {
    mostrarFilaInformativa(
      "No se encontraron inventarios."
    );
    return;
  }

  inventarios.forEach(agregarInventarioATabla);
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
    || "El servicio de inventarios no está disponible."
  );
}


/*
 * Consulta una página controlada de inventarios.
 */
async function consultarPaginaInventarios(
  paginaSolicitada
) {
  const tamano = obtenerTamanoPagina();

  mostrarMensaje(
    "Consultando inventarios...",
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
      `/api/inventarios?${parametros.toString()}`,
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

    mostrarInventarios(datos.elementos);
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
      "No fue posible cargar los inventarios."
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
 * Consulta un inventario por código.
 */
async function consultarInventarioPorCodigo(codigo) {
  const codigoLimpio = codigo.trim();

  if (!codigoLimpio) {
    mostrarMensaje(
      "Ingresa un código de inventario antes de consultar.",
      "warning"
    );

    codigoInventario.focus();
    return;
  }

  controlesPaginacion.classList.add("d-none");
  controlesPaginacion.classList.remove("d-flex");
  totalPaginas = 0;

  mostrarMensaje(
    `Consultando el código ${codigoLimpio}...`,
    "info"
  );

  mostrarFilaInformativa(
    "Buscando inventario..."
  );

  establecerControlesBloqueados(true);

  const solicitud = crearControladorDeSolicitud();

  try {
    const codigoSeguro =
      encodeURIComponent(codigoLimpio);

    const respuesta = await fetch(
      `/api/inventarios/${codigoSeguro}`,
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

    const inventario = await respuesta.json();

    mostrarInventarios([inventario]);

    mostrarMensaje(
      `Inventario ${inventario.codigo} `
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
 * Consulta el estado del servicio sin descargar inventarios.
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
      "/api/inventarios/estado",
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

    consultarInventarioPorCodigo(
      codigoInventario.value
    );
  }
);


botonMostrarTodos.addEventListener(
  "click",
  function () {
    codigoInventario.value = "";
    paginaActual = 1;

    consultarPaginaInventarios(
      paginaActual
    );
  }
);


botonPaginaAnterior.addEventListener(
  "click",
  function () {
    if (paginaActual > 1) {
      consultarPaginaInventarios(
        paginaActual - 1
      );
    }
  }
);


botonPaginaSiguiente.addEventListener(
  "click",
  function () {
    if (paginaActual < totalPaginas) {
      consultarPaginaInventarios(
        paginaActual + 1
      );
    }
  }
);


tamanoPagina.addEventListener(
  "change",
  function () {
    paginaActual = 1;

    consultarPaginaInventarios(
      paginaActual
    );
  }
);


consultarEstadoDelServicio();
