/*
  Archivo: dashboard-inventario.js

  Este archivo crea un componente visual usando React.
  No usa JSX para evitar errores con simbolos HTML escapados.
  Todo se construye con React.createElement.
*/

(function () {
  "use strict";

  /*
    Datos temporales del tablero.

    Estos datos se muestran en la pantalla como ejemplo.
    Mas adelante se pueden conectar con datos reales de Spring Boot,
    MongoDB Atlas o servicios internos del proyecto Invento Accion.
  */
  var resumenInventario = [
    {
      titulo: "Activos registrados",
      valor: "120",
      descripcion: "Cantidad total de activos tecnologicos registrados."
    },
    {
      titulo: "Activos disponibles",
      valor: "85",
      descripcion: "Equipos disponibles para ser usados o asignados."
    },
    {
      titulo: "Activos asignados",
      valor: "28",
      descripcion: "Equipos que actualmente se encuentran asignados."
    },
    {
      titulo: "Alertas pendientes",
      valor: "7",
      descripcion: "Novedades que requieren revision administrativa."
    }
  ];

  /*
    Funcion auxiliar para crear elementos de React.

    Esta funcion evita repetir React.createElement muchas veces
    y permite construir la interfaz de forma mas ordenada.
  */
  function crearElemento(tipo, propiedades) {
    var hijos = Array.prototype.slice.call(arguments, 2);
    return window.React.createElement.apply(
      window.React,
      [tipo, propiedades].concat(hijos)
    );
  }

  /*
    Componente TarjetaResumen.

    Este componente representa una tarjeta del tablero.
    Recibe un titulo, un valor y una descripcion.
  */
  function TarjetaResumen(propiedades) {
    return crearElemento(
      "div",
      { className: "col-md-3 mb-3" },
      crearElemento(
        "div",
        { className: "card h-100 shadow-sm" },
        crearElemento(
          "div",
          { className: "card-body text-center" },
          crearElemento("h5", { className: "card-title" }, propiedades.titulo),
          crearElemento("h2", { className: "text-primary" }, propiedades.valor),
          crearElemento("p", { className: "card-text" }, propiedades.descripcion)
        )
      )
    );
  }

  /*
    Componente DashboardInventario.

    Este es el componente principal del tablero.
    Muestra el titulo, una descripcion general y las tarjetas del resumen.
  */
  function DashboardInventario() {
    var tarjetas = resumenInventario.map(function (item, indice) {
      return crearElemento(TarjetaResumen, {
        key: indice,
        titulo: item.titulo,
        valor: item.valor,
        descripcion: item.descripcion
      });
    });

    return crearElemento(
      "section",
      { className: "container mt-4 mb-4" },

      crearElemento(
        "div",
        { className: "mb-4" },
        crearElemento("h2", null, "Dashboard React de Invento Accion"),
        crearElemento(
          "p",
          null,
          "Este componente muestra un resumen visual del inventario usando React dentro de la interfaz del proyecto."
        )
      ),

      crearElemento(
        "div",
        { className: "row" },
        tarjetas
      ),

      crearElemento(
        "div",
        { className: "alert alert-info mt-4" },
        "Este bloque fue desarrollado como primera integracion local de React en Invento Accion."
      )
    );
  }

  /*
    Punto de montaje de React.

    Aqui se busca el contenedor llamado react-dashboard-root.
    Si existe, React dibuja el componente DashboardInventario dentro de ese espacio.
  */
  var contenedorReact = document.getElementById("react-dashboard-root");

  if (contenedorReact && window.React && window.ReactDOM) {
    var raizReact = window.ReactDOM.createRoot(contenedorReact);
    raizReact.render(crearElemento(DashboardInventario, null));
  } else {
    console.error("No fue posible cargar React o encontrar el contenedor react-dashboard-root.");
  }
})();
