"use strict";

/*
 * Código JavaScript para la página Consulta Inventario.
 *
 * Este archivo consume las rutas públicas entregadas por Spring Boot:
 *
 * GET /api/inventarios
 * GET /api/inventarios/{codigo}
 *
 * Spring Boot se comunica internamente con FastAPI.
 * El navegador nunca utiliza directamente el puerto interno 8000.
 *
 * Este módulo solamente consulta información.
 * No crea, modifica ni elimina registros.
 */


/*
 * Obtiene los elementos principales de la página.
 */
const formularioConsulta = document.getElementById("formularioConsulta");
const codigoInventario = document.getElementById("codigoInventario");
const botonMostrarTodos = document.getElementById("botonMostrarTodos");
const mensajeConsulta = document.getElementById("mensajeConsulta");
const cuerpoTablaInventarios = document.getElementById(
  "cuerpoTablaInventarios"
);


/*
 * Muestra un mensaje dentro de la página.
 *
 * Los tipos permitidos son:
 * info, success, warning y danger.
 */
function mostrarMensaje(texto, tipo) {
  mensajeConsulta.className = `alert alert-${tipo}`;
  mensajeConsulta.textContent = texto;
}


/*
 * Limpia la tabla y muestra un mensaje en una sola fila.
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
 * Convierte la fecha recibida desde la API en un texto fácil de leer.
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
 * Crea una etiqueta visual para mostrar el estado del inventario.
 */
function crearEtiquetaEstado(estado) {
  const etiqueta = document.createElement("span");
  const estadoNormalizado = String(estado || "").toLowerCase();

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
 * Crea una celda segura utilizando texto.
 *
 * Se usa textContent para evitar insertar HTML recibido desde la API.
 */
function crearCelda(texto) {
  const celda = document.createElement("td");
  celda.textContent = texto ?? "";
  return celda;
}


/*
 * Agrega un registro de inventario a la tabla.
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
  celdaEstado.appendChild(crearEtiquetaEstado(inventario.estado));
  fila.appendChild(celdaEstado);

  fila.appendChild(
    crearCelda(formatearFecha(inventario.ultimaActualizacion))
  );

  cuerpoTablaInventarios.appendChild(fila);
}


/*
 * Muestra uno o varios inventarios.
 */
function mostrarInventarios(inventarios) {
  cuerpoTablaInventarios.replaceChildren();

  if (!Array.isArray(inventarios) || inventarios.length === 0) {
    mostrarFilaInformativa("No se encontraron inventarios.");
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
    console.error("No fue posible leer el mensaje de la API.", error);
  }

  return "No fue posible completar la consulta.";
}


/*
 * Consulta todos los inventarios.
 */
async function consultarTodosLosInventarios() {
  mostrarMensaje("Consultando inventarios...", "info");
  mostrarFilaInformativa("Cargando información...");

  try {
    const respuesta = await fetch("/api/inventarios", {
      method: "GET",
      headers: {
        Accept: "application/json"
      },
      cache: "no-store"
    });

    if (!respuesta.ok) {
      const mensajeError = await obtenerMensajeDeError(respuesta);
      throw new Error(mensajeError);
    }

    const inventarios = await respuesta.json();

    mostrarInventarios(inventarios);

    mostrarMensaje(
      `Consulta completada. Registros encontrados: ${inventarios.length}.`,
      "success"
    );
  } catch (error) {
    mostrarFilaInformativa("No fue posible cargar los inventarios.");

    mostrarMensaje(
      error.message || "El servicio de inventarios no está disponible.",
      "danger"
    );
  }
}


/*
 * Consulta un inventario utilizando su código.
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

  mostrarMensaje(
    `Consultando el código ${codigoLimpio}...`,
    "info"
  );

  mostrarFilaInformativa("Buscando inventario...");

  try {
    const codigoSeguro = encodeURIComponent(codigoLimpio);

    const respuesta = await fetch(
      `/api/inventarios/${codigoSeguro}`,
      {
        method: "GET",
        headers: {
          Accept: "application/json"
        },
        cache: "no-store"
      }
    );

    if (!respuesta.ok) {
      const mensajeError = await obtenerMensajeDeError(respuesta);
      throw new Error(mensajeError);
    }

    const inventario = await respuesta.json();

    mostrarInventarios([inventario]);

    mostrarMensaje(
      `Inventario ${inventario.codigo} encontrado correctamente.`,
      "success"
    );
  } catch (error) {
    mostrarFilaInformativa("No se encontró información para mostrar.");

    mostrarMensaje(
      error.message || "No fue posible consultar el inventario.",
      "danger"
    );
  }
}


/*
 * Atiende el envío del formulario de búsqueda.
 */
formularioConsulta.addEventListener("submit", function (evento) {
  evento.preventDefault();

  consultarInventarioPorCodigo(codigoInventario.value);
});


/*
 * Atiende el botón que consulta todos los inventarios.
 */
botonMostrarTodos.addEventListener("click", function () {
  codigoInventario.value = "";
  consultarTodosLosInventarios();
});