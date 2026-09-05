<?php

/*
|--------------------------------------------------------------------------
| TRANSPORTE SITP - INVENTO-ACCIÓN
|--------------------------------------------------------------------------
|
| Esta página permite consultar paraderos del SITP cercanos a una localidad
| de Bogotá.
|
| La información se obtiene desde un servicio público de ArcGIS.
| El servicio devuelve los datos en formato GeoJSON.
|
|
*/

/*
|--------------------------------------------------------------------------
| 1. CONFIGURACIÓN GENERAL
|--------------------------------------------------------------------------
|
| Se define la dirección del servicio público de paraderos del SITP.
| También se establece el radio de búsqueda y la cantidad máxima que se
| mostrará en la tabla.
|
*/

$urlServicio = 'https://services2.arcgis.com/NEwhEo9GGSHXcRXV/arcgis/rest/services/Paraderos_SITP_Bogot%C3%A1_D_C/FeatureServer/0/query';

$radioBusqueda = 750;
$maximoResultados = 50;

/*
|--------------------------------------------------------------------------
| 2. LOCALIDADES Y PUNTOS DE REFERENCIA
|--------------------------------------------------------------------------
|
| La API de paraderos no incluye un campo llamado localidad.
|
| Por esta razón, cada localidad tiene un punto geográfico de referencia.
| La página consulta los paraderos ubicados dentro de un radio de 750 metros
| alrededor de dicho punto.
|
| Estos puntos sirven únicamente como referencia para realizar la consulta.
| No representan los límites geográficos oficiales de las localidades.
|
*/

$localidades = [
    'Antonio Nariño' => [
        'latitud' => 4.5897,
        'longitud' => -74.1009
    ],
    'Barrios Unidos' => [
        'latitud' => 4.6664,
        'longitud' => -74.0840
    ],
    'Bosa' => [
        'latitud' => 4.6171,
        'longitud' => -74.1907
    ],
    'Chapinero' => [
        'latitud' => 4.6486,
        'longitud' => -74.0636
    ],
    'Ciudad Bolívar' => [
        'latitud' => 4.5795,
        'longitud' => -74.1574
    ],
    'Engativá' => [
        'latitud' => 4.7071,
        'longitud' => -74.1072
    ],
    'Fontibón' => [
        'latitud' => 4.6782,
        'longitud' => -74.1436
    ],
    'Kennedy' => [
        'latitud' => 4.6268,
        'longitud' => -74.1573
    ],
    'La Candelaria' => [
        'latitud' => 4.5964,
        'longitud' => -74.0730
    ],
    'Los Mártires' => [
        'latitud' => 4.6030,
        'longitud' => -74.0886
    ],
    'Puente Aranda' => [
        'latitud' => 4.6149,
        'longitud' => -74.1117
    ],
    'Rafael Uribe Uribe' => [
        'latitud' => 4.5653,
        'longitud' => -74.1164
    ],
    'San Cristóbal' => [
        'latitud' => 4.5647,
        'longitud' => -74.0836
    ],
    'Santa Fe' => [
        'latitud' => 4.6069,
        'longitud' => -74.0685
    ],
    'Suba' => [
        'latitud' => 4.7410,
        'longitud' => -74.0844
    ],
    'Sumapaz' => [
        'latitud' => 4.2570,
        'longitud' => -74.2260
    ],
    'Teusaquillo' => [
        'latitud' => 4.6448,
        'longitud' => -74.0938
    ],
    'Tunjuelito' => [
        'latitud' => 4.5875,
        'longitud' => -74.1410
    ],
    'Usaquén' => [
        'latitud' => 4.6951,
        'longitud' => -74.0320
    ],
    'Usme' => [
        'latitud' => 4.4766,
        'longitud' => -74.1050
    ]
];

/*
|--------------------------------------------------------------------------
| 3. VARIABLES INICIALES
|--------------------------------------------------------------------------
|
| Estas variables almacenan el resultado de la consulta, los mensajes
| para el usuario y el código de respuesta de la página.
|
*/

$localidadSeleccionada = '';
$paraderos = [];
$mensaje = '';
$tipoMensaje = '';
$codigoRespuesta = null;
$totalEncontrados = 0;

/*
|--------------------------------------------------------------------------
| 4. FUNCIÓN PARA PROTEGER LOS DATOS MOSTRADOS
|--------------------------------------------------------------------------
|
| Esta función evita que un texto recibido desde la API sea interpretado
| como código HTML dentro de la página.
|
*/

function escapar(?string $texto): string
{
    return htmlspecialchars(
        $texto ?? '',
        ENT_QUOTES | ENT_SUBSTITUTE,
        'UTF-8'
    );
}

/*
|--------------------------------------------------------------------------
| 5. FUNCIÓN PARA CALCULAR LA DISTANCIA
|--------------------------------------------------------------------------
|
| La API devuelve la latitud y longitud de cada paradero.
|
| Esta función calcula una distancia aproximada en metros entre el punto
| de referencia de la localidad y cada paradero.
|
*/

function calcularDistancia(
    float $latitudInicial,
    float $longitudInicial,
    float $latitudFinal,
    float $longitudFinal
): float {
    $radioTierra = 6371000;

    $latitud1 = deg2rad($latitudInicial);
    $latitud2 = deg2rad($latitudFinal);

    $diferenciaLatitud = deg2rad($latitudFinal - $latitudInicial);
    $diferenciaLongitud = deg2rad($longitudFinal - $longitudInicial);

    $a = sin($diferenciaLatitud / 2) ** 2
        + cos($latitud1)
        * cos($latitud2)
        * sin($diferenciaLongitud / 2) ** 2;

    $c = 2 * atan2(sqrt($a), sqrt(1 - $a));

    return $radioTierra * $c;
}

/*
|--------------------------------------------------------------------------
| 6. FUNCIÓN PARA LEER EL CÓDIGO HTTP
|--------------------------------------------------------------------------
|
| Cuando file_get_contents() consulta una dirección web, PHP guarda las
| cabeceras de respuesta en la variable $http_response_header.
|
| Esta función busca el código HTTP recibido, por ejemplo 200, 404 o 500.
|
*/

function obtenerCodigoHttp(array $cabeceras): int
{
    foreach ($cabeceras as $cabecera) {
        if (preg_match('/HTTP\/\S+\s+(\d{3})/', $cabecera, $coincidencias)) {
            return (int) $coincidencias[1];
        }
    }

    return 0;
}

/*
|--------------------------------------------------------------------------
| 7. VALIDACIÓN Y CONSULTA DE LA API
|--------------------------------------------------------------------------
|
| Este bloque se ejecuta solamente cuando el usuario envía el formulario.
|
*/

if ($_SERVER['REQUEST_METHOD'] === 'GET' && isset($_GET['localidad'])) {

    /*
    |--------------------------------------------------------------------------
    | 7.1. RECIBIR Y LIMPIAR LA LOCALIDAD
    |--------------------------------------------------------------------------
    */

    $localidadSeleccionada = trim((string) $_GET['localidad']);

    /*
    |--------------------------------------------------------------------------
    | 7.2. VALIDAR QUE SE HAYA ELEGIDO UNA LOCALIDAD
    |--------------------------------------------------------------------------
    */

    if ($localidadSeleccionada === '') {
        $codigoRespuesta = 400;
        $tipoMensaje = 'error';
        $mensaje = 'Debes seleccionar una localidad para realizar la consulta.';

        http_response_code(400);

    /*
    |--------------------------------------------------------------------------
    | 7.3. VALIDAR QUE LA LOCALIDAD EXISTA EN LA LISTA
    |--------------------------------------------------------------------------
    */

    } elseif (!array_key_exists($localidadSeleccionada, $localidades)) {
        $codigoRespuesta = 400;
        $tipoMensaje = 'error';
        $mensaje = 'La localidad seleccionada no existe o no es válida.';

        http_response_code(400);

    } else {

        /*
        |--------------------------------------------------------------------------
        | 7.4. OBTENER LAS COORDENADAS DE LA LOCALIDAD
        |--------------------------------------------------------------------------
        */

        $latitudReferencia =
            $localidades[$localidadSeleccionada]['latitud'];

        $longitudReferencia =
            $localidades[$localidadSeleccionada]['longitud'];

        /*
        |--------------------------------------------------------------------------
        | 7.5. PREPARAR LOS PARÁMETROS DE LA CONSULTA
        |--------------------------------------------------------------------------
        |
        | outFields indica los campos que queremos recibir.
        | returnGeometry permite obtener la latitud y longitud.
        | f=geojson solicita la respuesta en formato GeoJSON.
        |
        */

        $parametros = [
            'where' => '1=1',
            'geometry' => $longitudReferencia . ',' . $latitudReferencia,
            'geometryType' => 'esriGeometryPoint',
            'inSR' => '4326',
            'distance' => $radioBusqueda,
            'units' => 'esriSRUnit_Meter',
            'outFields' => implode(',', [
                'OBJECTID',
                'NTRCODIGO',
                'NTRNOMBRE',
                'NTRTIPO',
                'NTRMODO',
                'NTRMTRANSP',
                'NTRDIRECCION'
            ]),
            'returnGeometry' => 'true',
            'outSR' => '4326',
            'resultRecordCount' => 200,
            'f' => 'geojson'
        ];

        /*
        |--------------------------------------------------------------------------
        | 7.6. CONSTRUIR LA DIRECCIÓN COMPLETA DE LA API
        |--------------------------------------------------------------------------
        */

        $urlConsulta = $urlServicio . '?'
            . http_build_query(
                $parametros,
                '',
                '&',
                PHP_QUERY_RFC3986
            );

        /*
        |--------------------------------------------------------------------------
        | 7.7. CONFIGURAR LA CONEXIÓN
        |--------------------------------------------------------------------------
        |
        | timeout evita que la página quede esperando de forma indefinida.
        | ignore_errors permite leer la respuesta aunque la API devuelva
        | un código diferente de 200.
        |
        */

        $opcionesConexion = [
            'http' => [
                'method' => 'GET',
                'timeout' => 30,
                'ignore_errors' => true,
                'header' => implode("\r\n", [
                    'Accept: application/geo+json, application/json',
                    'User-Agent: Invento-Accion-SITP/1.0'
                ])
            ]
        ];

        $contexto = stream_context_create($opcionesConexion);

        /*
        |--------------------------------------------------------------------------
        | 7.8. REALIZAR EL LLAMADO A LA API
        |--------------------------------------------------------------------------
        |
        | El símbolo @ evita que PHP muestre un error técnico en pantalla.
        | En su lugar, la página mostrará un mensaje fácil de entender.
        |
        */

        $respuestaApi = @file_get_contents(
            $urlConsulta,
            false,
            $contexto
        );

        $cabecerasRespuesta = $http_response_header ?? [];
        $estadoApi = obtenerCodigoHttp($cabecerasRespuesta);

        /*
        |--------------------------------------------------------------------------
        | 7.9. MANEJAR UNA FALLA DE CONEXIÓN
        |--------------------------------------------------------------------------
        */

        if ($respuestaApi === false) {
            $codigoRespuesta = 500;
            $tipoMensaje = 'error';
            $mensaje = 'No fue posible conectarse con el servicio de paraderos. '
                . 'Por favor, inténtalo nuevamente.';

            http_response_code(500);

        /*
        |--------------------------------------------------------------------------
        | 7.10. MANEJAR UN ERROR DEL SERVICIO EXTERNO
        |--------------------------------------------------------------------------
        */

        } elseif ($estadoApi !== 0 && $estadoApi !== 200) {
            $codigoRespuesta = 500;
            $tipoMensaje = 'error';
            $mensaje = 'El servicio público respondió con un error. '
                . 'Por favor, inténtalo más tarde.';

            http_response_code(500);

        } else {

            /*
            |--------------------------------------------------------------------------
            | 7.11. DECODIFICAR LA RESPUESTA JSON
            |--------------------------------------------------------------------------
            |
            | json_decode() convierte el texto GeoJSON en un arreglo de PHP.
            |
            */

            $datosApi = json_decode($respuestaApi, true);

            /*
            |--------------------------------------------------------------------------
            | 7.12. VALIDAR QUE EL JSON SEA CORRECTO
            |--------------------------------------------------------------------------
            */

            if (
                json_last_error() !== JSON_ERROR_NONE
                || !is_array($datosApi)
            ) {
                $codigoRespuesta = 500;
                $tipoMensaje = 'error';
                $mensaje = 'La respuesta del servicio no tiene un formato válido.';

                http_response_code(500);

            /*
            |--------------------------------------------------------------------------
            | 7.13. VALIDAR SI ARCGIS DEVOLVIÓ UN ERROR
            |--------------------------------------------------------------------------
            */

            } elseif (isset($datosApi['error'])) {
                $codigoRespuesta = 500;
                $tipoMensaje = 'error';
                $mensaje = 'El servicio geográfico no pudo procesar la consulta.';

                http_response_code(500);

            /*
            |--------------------------------------------------------------------------
            | 7.14. VALIDAR QUE EXISTA EL ARREGLO DE RESULTADOS
            |--------------------------------------------------------------------------
            */

            } elseif (
                !isset($datosApi['features'])
                || !is_array($datosApi['features'])
            ) {
                $codigoRespuesta = 500;
                $tipoMensaje = 'error';
                $mensaje = 'La respuesta recibida no contiene una lista de paraderos.';

                http_response_code(500);

            } else {

                /*
                |--------------------------------------------------------------------------
                | 7.15. RECORRER LOS RESULTADOS CON FOREACH
                |--------------------------------------------------------------------------
                |
                | Cada elemento del arreglo features representa un paradero.
                |
                */

                foreach ($datosApi['features'] as $resultado) {

                    $propiedades = $resultado['properties'] ?? [];
                    $geometria = $resultado['geometry'] ?? [];
                    $coordenadas = $geometria['coordinates'] ?? [];

                    /*
                    |--------------------------------------------------------------------------
                    | Verificar que el paradero tenga longitud y latitud
                    |--------------------------------------------------------------------------
                    */

                    if (
                        !isset($coordenadas[0], $coordenadas[1])
                        || !is_numeric($coordenadas[0])
                        || !is_numeric($coordenadas[1])
                    ) {
                        continue;
                    }

                    $longitudParadero = (float) $coordenadas[0];
                    $latitudParadero = (float) $coordenadas[1];

                    /*
                    |--------------------------------------------------------------------------
                    | Calcular la distancia aproximada desde la localidad
                    |--------------------------------------------------------------------------
                    */

                    $distancia = calcularDistancia(
                        $latitudReferencia,
                        $longitudReferencia,
                        $latitudParadero,
                        $longitudParadero
                    );

                    /*
                    |--------------------------------------------------------------------------
                    | Guardar los datos necesarios para la tabla
                    |--------------------------------------------------------------------------
                    */

                    $paraderos[] = [
                        'codigo' => (string) (
                            $propiedades['NTRCODIGO'] ?? 'Sin código'
                        ),
                        'nombre' => (string) (
                            $propiedades['NTRNOMBRE'] ?? 'Sin nombre'
                        ),
                        'direccion' => (string) (
                            $propiedades['NTRDIRECCION'] ?? 'Sin dirección'
                        ),
                        'tipo' => (string) (
                            $propiedades['NTRTIPO'] ?? 'No informado'
                        ),
                        'modo' => (string) (
                            $propiedades['NTRMODO'] ?? 'No informado'
                        ),
                        'modalidad' => (string) (
                            $propiedades['NTRMTRANSP'] ?? 'No informada'
                        ),
                        'latitud' => $latitudParadero,
                        'longitud' => $longitudParadero,
                        'distancia' => $distancia
                    ];
                }

                /*
                |--------------------------------------------------------------------------
                | 7.16. ORDENAR LOS PARADEROS POR DISTANCIA
                |--------------------------------------------------------------------------
                */

                usort(
                    $paraderos,
                    function (array $paraderoA, array $paraderoB): int {
                        return $paraderoA['distancia']
                            <=> $paraderoB['distancia'];
                    }
                );

                /*
                |--------------------------------------------------------------------------
                | 7.17. LIMITAR LA CANTIDAD DE RESULTADOS
                |--------------------------------------------------------------------------
                */

                $paraderos = array_slice(
                    $paraderos,
                    0,
                    $maximoResultados
                );

                $totalEncontrados = count($paraderos);

                /*
                |--------------------------------------------------------------------------
                | 7.18. MANEJAR RESULTADOS VACÍOS
                |--------------------------------------------------------------------------
                */

                if ($totalEncontrados === 0) {
                    $codigoRespuesta = 404;
                    $tipoMensaje = 'advertencia';
                    $mensaje = 'No se encontraron paraderos cercanos al punto '
                        . 'de referencia de la localidad seleccionada.';

                    http_response_code(404);

                /*
                |--------------------------------------------------------------------------
                | 7.19. MOSTRAR EL RESULTADO CORRECTO
                |--------------------------------------------------------------------------
                */

                } else {
                    $codigoRespuesta = 200;
                    $tipoMensaje = 'exito';
                    $mensaje = 'Consulta realizada correctamente. Se encontraron '
                        . $totalEncontrados
                        . ' paraderos cercanos.';

                    http_response_code(200);
                }
            }
        }
    }
}

?>
<!DOCTYPE html>
<html lang="es">

<head>
    <meta charset="UTF-8">

    <meta
        name="viewport"
        content="width=device-width, initial-scale=1.0"
    >

    <meta
        name="description"
        content="Consulta de paraderos del SITP cercanos a localidades de Bogotá"
    >

    <title>Transporte SITP | Invento-Acción</title>

    <style>
        /*
        |--------------------------------------------------------------------------
        | ESTILOS GENERALES
        |--------------------------------------------------------------------------
        */

        * {
            box-sizing: border-box;
        }

        body {
            margin: 0;
            font-family: Arial, Helvetica, sans-serif;
            background-color: #f4f6f9;
            color: #212529;
        }

        .topbar {
            min-height: 64px;
            padding: 12px 28px;
            background: linear-gradient(135deg, #0d6efd, #084298);
            color: white;
            display: flex;
            align-items: center;
            gap: 20px;
            box-shadow: 0 3px 12px rgba(0, 0, 0, 0.18);
        }

        .topbar-title {
            margin: 0;
            font-size: 22px;
            font-weight: bold;
        }

        .topbar-description {
            margin: 0;
            color: #e7f1ff;
            font-size: 14px;
        }

        .container {
            width: min(1180px, calc(100% - 32px));
            margin: 30px auto;
        }

        .page-header {
            margin-bottom: 24px;
        }

        .page-header h1 {
            margin: 0 0 8px;
            color: #0d6efd;
            font-size: 30px;
        }

        .page-header p {
            margin: 0;
            color: #6c757d;
            line-height: 1.6;
        }

        /*
        |--------------------------------------------------------------------------
        | TARJETA DEL FORMULARIO
        |--------------------------------------------------------------------------
        */

        .search-card {
            background-color: white;
            border-radius: 16px;
            padding: 24px;
            margin-bottom: 24px;
            box-shadow: 0 4px 16px rgba(0, 0, 0, 0.09);
        }

        .search-card h2 {
            margin: 0 0 18px;
            color: #14324a;
            font-size: 21px;
        }

        .form-row {
            display: grid;
            grid-template-columns: minmax(220px, 1fr) auto;
            gap: 14px;
            align-items: end;
        }

        .form-group label {
            display: block;
            margin-bottom: 8px;
            font-weight: bold;
            color: #343a40;
        }

        select {
            width: 100%;
            min-height: 46px;
            padding: 10px 12px;
            border: 1px solid #ced4da;
            border-radius: 9px;
            background-color: white;
            color: #212529;
            font-size: 15px;
        }

        select:focus {
            outline: none;
            border-color: #0d6efd;
            box-shadow: 0 0 0 3px rgba(13, 110, 253, 0.15);
        }

        .btn-primary {
            min-height: 46px;
            padding: 11px 22px;
            border: none;
            border-radius: 9px;
            background-color: #0d6efd;
            color: white;
            font-size: 15px;
            font-weight: bold;
            cursor: pointer;
        }

        .btn-primary:hover {
            background-color: #0b5ed7;
        }

        /*
        |--------------------------------------------------------------------------
        | MENSAJES
        |--------------------------------------------------------------------------
        */

        .message {
            padding: 16px 18px;
            margin-bottom: 24px;
            border-radius: 12px;
            line-height: 1.5;
        }

        .message strong {
            display: block;
            margin-bottom: 4px;
        }

        .message-exito {
            background-color: #d1e7dd;
            border: 1px solid #a3cfbb;
            color: #0a3622;
        }

        .message-error {
            background-color: #f8d7da;
            border: 1px solid #f1aeb5;
            color: #58151c;
        }

        .message-advertencia {
            background-color: #fff3cd;
            border: 1px solid #ffe69c;
            color: #664d03;
        }

        /*
        |--------------------------------------------------------------------------
        | RESUMEN DE LA CONSULTA
        |--------------------------------------------------------------------------
        */

        .summary-grid {
            display: grid;
            grid-template-columns: repeat(3, 1fr);
            gap: 16px;
            margin-bottom: 24px;
        }

        .summary-card {
            padding: 20px;
            border-radius: 14px;
            background-color: white;
            box-shadow: 0 3px 12px rgba(0, 0, 0, 0.08);
        }

        .summary-card span {
            display: block;
            margin-bottom: 7px;
            color: #6c757d;
            font-size: 13px;
            font-weight: bold;
            text-transform: uppercase;
        }

        .summary-card strong {
            color: #14324a;
            font-size: 21px;
        }

        /*
        |--------------------------------------------------------------------------
        | TABLA DE RESULTADOS
        |--------------------------------------------------------------------------
        */

        .results-card {
            background-color: white;
            border-radius: 16px;
            overflow: hidden;
            box-shadow: 0 4px 16px rgba(0, 0, 0, 0.09);
        }

        .results-header {
            padding: 20px 24px;
            border-bottom: 1px solid #e9ecef;
        }

        .results-header h2 {
            margin: 0 0 6px;
            color: #0d6efd;
            font-size: 22px;
        }

        .results-header p {
            margin: 0;
            color: #6c757d;
            font-size: 14px;
        }

        .table-container {
            width: 100%;
            overflow-x: auto;
        }

        table {
            width: 100%;
            border-collapse: collapse;
            min-width: 1020px;
        }

        th {
            padding: 13px 12px;
            background-color: #14324a;
            color: white;
            text-align: left;
            font-size: 13px;
            white-space: nowrap;
        }

        td {
            padding: 13px 12px;
            border-bottom: 1px solid #e9ecef;
            font-size: 14px;
            vertical-align: middle;
        }

        tbody tr:nth-child(even) {
            background-color: #f8f9fa;
        }

        tbody tr:hover {
            background-color: #e7f1ff;
        }

        .distance-badge {
            display: inline-block;
            padding: 5px 9px;
            border-radius: 20px;
            background-color: #d1e7dd;
            color: #0a3622;
            font-size: 12px;
            font-weight: bold;
            white-space: nowrap;
        }

        .code-badge {
            display: inline-block;
            padding: 5px 9px;
            border-radius: 7px;
            background-color: #e7f1ff;
            color: #084298;
            font-weight: bold;
        }

        /*
        |--------------------------------------------------------------------------
        | INFORMACIÓN DE LA FUENTE
        |--------------------------------------------------------------------------
        */

        .information-box {
            margin-top: 24px;
            padding: 18px;
            border-radius: 13px;
            background-color: #e7f1ff;
            border: 1px solid #b6d4fe;
            color: #084298;
            line-height: 1.6;
        }

        .information-box strong {
            display: block;
            margin-bottom: 5px;
        }

        footer {
            margin-top: 32px;
            padding: 24px;
            text-align: center;
            color: #6c757d;
            font-size: 13px;
        }

        /*
        |--------------------------------------------------------------------------
        | DISEÑO PARA PANTALLAS PEQUEÑAS
        |--------------------------------------------------------------------------
        */

        @media (max-width: 820px) {
            .topbar {
                align-items: flex-start;
                flex-direction: column;
                gap: 5px;
            }

            .form-row {
                grid-template-columns: 1fr;
            }

            .btn-primary {
                width: 100%;
            }

            .summary-grid {
                grid-template-columns: 1fr;
            }

            .container {
                width: min(100% - 20px, 1180px);
                margin-top: 20px;
            }

            .page-header h1 {
                font-size: 25px;
            }
        }
    </style>
</head>

<body>

    <!-- Barra superior del módulo -->
    <header class="topbar">
        <p class="topbar-title">Invento-Acción</p>

        <p class="topbar-description">
            Sistema de Inventario Tecnológico | Transporte SITP
        </p>
    </header>

    <main class="container">

        <!-- Título principal de la página -->
        <section class="page-header">
            <h1>Consulta de Paraderos del SITP</h1>

            <p>
                Selecciona una localidad de Bogotá para consultar los
                paraderos cercanos a su punto geográfico de referencia.
            </p>
        </section>

        <!-- Formulario para elegir la localidad -->
        <section class="search-card">
            <h2>Buscar paraderos por localidad</h2>

            <form method="GET" action="">
                <div class="form-row">

                    <div class="form-group">
                        <label for="localidad">
                            Localidad de Bogotá
                        </label>

                        <select
                            id="localidad"
                            name="localidad"
                            required
                        >
                            <option value="">
                                Selecciona una localidad
                            </option>

                            <?php foreach ($localidades as $nombreLocalidad => $coordenadas): ?>
                                <option
                                    value="<?php echo escapar($nombreLocalidad); ?>"
                                    <?php
                                    echo $localidadSeleccionada === $nombreLocalidad
                                        ? 'selected'
                                        : '';
                                    ?>
                                >
                                    <?php echo escapar($nombreLocalidad); ?>
                                </option>
                            <?php endforeach; ?>
                        </select>
                    </div>

                    <button type="submit" class="btn-primary">
                        Consultar paraderos
                    </button>

                </div>
            </form>
        </section>

        <!-- Mensaje de éxito, advertencia o error -->
        <?php if ($mensaje !== ''): ?>
            <div class="message message-<?php echo escapar($tipoMensaje); ?>">
                <strong>
                    Estado <?php echo escapar((string) $codigoRespuesta); ?>
                </strong>

                <?php echo escapar($mensaje); ?>
            </div>
        <?php endif; ?>

        <!-- Resumen mostrado cuando existen resultados -->
        <?php if ($codigoRespuesta === 200 && $totalEncontrados > 0): ?>

            <section class="summary-grid">

                <article class="summary-card">
                    <span>Localidad consultada</span>
                    <strong>
                        <?php echo escapar($localidadSeleccionada); ?>
                    </strong>
                </article>

                <article class="summary-card">
                    <span>Radio de búsqueda</span>
                    <strong>
                        <?php echo escapar((string) $radioBusqueda); ?> metros
                    </strong>
                </article>

                <article class="summary-card">
                    <span>Paraderos mostrados</span>
                    <strong>
                        <?php echo escapar((string) $totalEncontrados); ?>
                    </strong>
                </article>

            </section>

            <!-- Tabla de resultados -->
            <section class="results-card">

                <div class="results-header">
                    <h2>Paraderos encontrados</h2>

                    <p>
                        Los registros están organizados desde el más cercano
                        hasta el más lejano.
                    </p>
                </div>

                <div class="table-container">
                    <table>
                        <thead>
                            <tr>
                                <th>No.</th>
                                <th>Código</th>
                                <th>Nombre</th>
                                <th>Dirección</th>
                                <th>Tipo</th>
                                <th>Modo</th>
                                <th>Modalidad</th>
                                <th>Distancia</th>
                                <th>Latitud</th>
                                <th>Longitud</th>
                            </tr>
                        </thead>

                        <tbody>

                            <?php foreach ($paraderos as $indice => $paradero): ?>
                                <tr>
                                    <td>
                                        <?php echo escapar((string) ($indice + 1)); ?>
                                    </td>

                                    <td>
                                        <span class="code-badge">
                                            <?php echo escapar($paradero['codigo']); ?>
                                        </span>
                                    </td>

                                    <td>
                                        <?php echo escapar($paradero['nombre']); ?>
                                    </td>

                                    <td>
                                        <?php echo escapar($paradero['direccion']); ?>
                                    </td>

                                    <td>
                                        Código
                                        <?php echo escapar($paradero['tipo']); ?>
                                    </td>

                                    <td>
                                        Código
                                        <?php echo escapar($paradero['modo']); ?>
                                    </td>

                                    <td>
                                        Código
                                        <?php echo escapar($paradero['modalidad']); ?>
                                    </td>

                                    <td>
                                        <span class="distance-badge">
                                            <?php
                                            echo escapar(
                                                (string) round(
                                                    $paradero['distancia']
                                                )
                                            );
                                            ?>
                                            m
                                        </span>
                                    </td>

                                    <td>
                                        <?php
                                        echo escapar(
                                            number_format(
                                                $paradero['latitud'],
                                                6,
                                                '.',
                                                ''
                                            )
                                        );
                                        ?>
                                    </td>

                                    <td>
                                        <?php
                                        echo escapar(
                                            number_format(
                                                $paradero['longitud'],
                                                6,
                                                '.',
                                                ''
                                            )
                                        );
                                        ?>
                                    </td>
                                </tr>
                            <?php endforeach; ?>

                        </tbody>
                    </table>
                </div>

            </section>

        <?php endif; ?>

        <!-- Explicación sencilla sobre el funcionamiento -->
        <section class="information-box">
            <strong>Información de la consulta</strong>

            Los datos se obtienen desde el servicio geográfico público de
            paraderos del SITP de Bogotá. La fuente no incluye un campo de
            localidad, por lo que la búsqueda utiliza un punto central de
            referencia y un radio de <?php echo escapar((string) $radioBusqueda); ?>
            metros. La página muestra un máximo de
            <?php echo escapar((string) $maximoResultados); ?> paraderos.
        </section>

    </main>

    <footer>
        Invento-Acción | Módulo de consulta geográfica Transporte SITP
    </footer>

</body>

</html>