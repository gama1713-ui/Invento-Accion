<?php

/*
|--------------------------------------------------------------------------
| LISTADO DE PRÉSTAMOS - INVENTO-ACCIÓN
|--------------------------------------------------------------------------
|
| Este archivo consulta los préstamos registrados en datos/prestamos.json
| y los muestra en una interfaz visual tipo dashboard.
|
| En palabras sencillas:
| - Lee el archivo JSON.
| - Cuenta los préstamos registrados.
| - Cuenta los préstamos activos.
| - Muestra los datos en una tabla organizada.
|
*/

$archivo = 'datos/prestamos.json';

$prestamos = [];

if (file_exists($archivo)) {
    $contenido = file_get_contents($archivo);
    $datosJson = json_decode($contenido, true);

    if (is_array($datosJson)) {
        $prestamos = $datosJson;
    }
}

$totalPrestamos = count($prestamos);

$prestamosActivos = 0;
$prestamosDevueltos = 0;

foreach ($prestamos as $prestamo) {
    $estado = $prestamo['estado'] ?? '';

    if ($estado === 'Activo') {
        $prestamosActivos++;
    }

    if ($estado === 'Devuelto') {
        $prestamosDevueltos++;
    }
}

?>

<!DOCTYPE html>
<html lang="es">

<head>
    <meta charset="UTF-8">

    <meta name="viewport" content="width=device-width, initial-scale=1.0">

    <title>Invento-Acción - Préstamos Registrados</title>

    <style>
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
            height: 60px;
            background-color: #0d6efd;
            color: white;
            display: flex;
            align-items: center;
            padding: 0 24px;
            box-shadow: 0 2px 8px rgba(0, 0, 0, 0.18);
        }

        .topbar-title {
            color: white;
            text-decoration: none;
            font-size: 21px;
            font-weight: bold;
            margin-right: 24px;
        }

        .topbar-subtitle {
            font-size: 14px;
            color: #e7f1ff;
        }

        .layout {
            display: flex;
            min-height: calc(100vh - 60px);
        }

        .sidebar {
            width: 250px;
            background-color: white;
            border-right: 1px solid #dee2e6;
            padding: 22px 16px;
        }

        .sidebar-title {
            font-size: 12px;
            color: #6c757d;
            text-transform: uppercase;
            font-weight: bold;
            margin-bottom: 16px;
        }

        .sidebar-link {
            display: block;
            padding: 11px 14px;
            margin-bottom: 8px;
            text-decoration: none;
            color: #333333;
            border-radius: 8px;
            font-size: 15px;
        }

        .sidebar-link:hover {
            background-color: #e7f1ff;
            color: #0d6efd;
        }

        .sidebar-link.active {
            background-color: #0d6efd;
            color: white;
            font-weight: bold;
        }

        .content {
            flex: 1;
            padding: 30px;
        }

        .header-section {
            display: flex;
            justify-content: space-between;
            align-items: center;
            border-bottom: 1px solid #dee2e6;
            padding-bottom: 18px;
            margin-bottom: 24px;
        }

        .header-section h1 {
            margin: 0;
            color: #0d6efd;
            font-size: 28px;
        }

        .header-section p {
            margin: 8px 0 0;
            color: #6c757d;
        }

        .btn-primary {
            background-color: #0d6efd;
            color: white;
            padding: 10px 16px;
            text-decoration: none;
            border-radius: 7px;
            font-weight: bold;
            display: inline-block;
            border: none;
        }

        .btn-primary:hover {
            background-color: #0b5ed7;
        }

        .cards {
            display: grid;
            grid-template-columns: repeat(3, 1fr);
            gap: 18px;
            margin-bottom: 26px;
        }

        .card {
            border-radius: 14px;
            padding: 22px;
            color: white;
            box-shadow: 0 3px 12px rgba(0, 0, 0, 0.12);
        }

        .card-blue {
            background-color: #0d6efd;
        }

        .card-green {
            background-color: #198754;
        }

        .card-gray {
            background-color: #6c757d;
        }

        .card h3 {
            margin: 0 0 12px;
            font-size: 18px;
        }

        .card-number {
            font-size: 36px;
            font-weight: bold;
            margin: 0;
        }

        .table-card {
            background-color: white;
            border-radius: 14px;
            box-shadow: 0 3px 12px rgba(0, 0, 0, 0.10);
            overflow: hidden;
        }

        .table-card-header {
            padding: 18px 22px;
            border-bottom: 1px solid #e9ecef;
            background-color: white;
        }

        .table-card-header h2 {
            margin: 0;
            color: #0d6efd;
            font-size: 22px;
        }

        .table-wrapper {
            padding: 22px;
            overflow-x: auto;
        }

        table {
            width: 100%;
            border-collapse: collapse;
            background-color: white;
        }

        thead {
            background-color: #0d6efd;
            color: white;
        }

        th,
        td {
            border: 1px solid #dee2e6;
            padding: 12px;
            text-align: left;
            font-size: 14px;
        }

        tbody tr:nth-child(even) {
            background-color: #f8f9fa;
        }

        tbody tr:hover {
            background-color: #e7f1ff;
        }

        .badge-activo {
            background-color: #198754;
            color: white;
            padding: 7px 12px;
            border-radius: 20px;
            font-size: 13px;
            font-weight: bold;
            display: inline-block;
        }

        .badge-devuelto {
            background-color: #6c757d;
            color: white;
            padding: 7px 12px;
            border-radius: 20px;
            font-size: 13px;
            font-weight: bold;
            display: inline-block;
        }

        .badge-sin-estado {
            background-color: #ffc107;
            color: #212529;
            padding: 7px 12px;
            border-radius: 20px;
            font-size: 13px;
            font-weight: bold;
            display: inline-block;
        }

        .alert-info {
            margin-top: 24px;
            background-color: #e7f1ff;
            border: 1px solid #b6d4fe;
            color: #084298;
            padding: 16px;
            border-radius: 12px;
        }

        .empty-message {
            text-align: center;
            color: #6c757d;
            padding: 18px;
        }

        @media (max-width: 900px) {
            .layout {
                flex-direction: column;
            }

            .sidebar {
                width: 100%;
                min-height: auto;
            }

            .cards {
                grid-template-columns: 1fr;
            }

            .header-section {
                flex-direction: column;
                align-items: flex-start;
                gap: 15px;
            }
        }
    </style>
</head>

<body>

    <header class="topbar">
        <a href="index.php" class="topbar-title">
            Invento-Acción
        </a>

        <span class="topbar-subtitle">
            Sistema de Inventario Tecnológico | Módulo PHP de Préstamos
        </span>
    </header>

    <div class="layout">

        <aside class="sidebar">

            <div class="sidebar-title">
                Opciones del módulo
            </div>

            <a href="index.php" class="sidebar-link">
                Registrar préstamo
            </a>

            <a href="listarPrestamos.php" class="sidebar-link active">
                Préstamos registrados
            </a>

            ../
                Volver a Invento-Acción
            </a>

        </aside>

        <main class="content">

            <section class="header-section">

                <div>
                    <h1>
                        Préstamos Registrados
                    </h1>

                    <p>
                        Consulta de elementos tecnológicos registrados desde el módulo PHP de Invento-Acción.
                    </p>
                </div>

                <a href="index.php" class="btn-primary">
                    Registrar nuevo préstamo
                </a>

            </section>

            <section class="cards">

                <div class="card card-blue">
                    <h3>
                        Total préstamos
                    </h3>

                    <p class="card-number">
                        <?php echo $totalPrestamos; ?>
                    </p>
                </div>

                <div class="card card-green">
                    <h3>
                        Préstamos activos
                    </h3>

                    <p class="card-number">
                        <?php echo $prestamosActivos; ?>
                    </p>
                </div>

                <div class="card card-gray">
                    <h3>
                        Préstamos devueltos
                    </h3>

                    <p class="card-number">
                        <?php echo $prestamosDevueltos; ?>
                    </p>
                </div>

            </section>

            <section class="table-card">

                <div class="table-card-header">
                    <h2>
                        Listado de préstamos
                    </h2>
                </div>

                <div class="table-wrapper">

                    <table>

                        <thead>
                            <tr>
                                <th>#</th>
                                <th>Colaborador</th>
                                <th>Elemento</th>
                                <th>Fecha préstamo</th>
                                <th>Fecha devolución</th>
                                <th>Observaciones</th>
                                <th>Estado</th>
                            </tr>
                        </thead>

                        <tbody>

                            <?php if (empty($prestamos)) : ?>

                                <tr>
                                    <td colspan="7" class="empty-message">
                                        No existen préstamos registrados.
                                    </td>
                                </tr>

                            <?php else : ?>

                                <?php foreach ($prestamos as $indice => $prestamo) : ?>

                                    <tr>
                                        <td>
                                            <?php echo $indice + 1; ?>
                                        </td>

                                        <td>
                                            <?php echo htmlspecialchars($prestamo['colaborador'] ?? '', ENT_QUOTES, 'UTF-8'); ?>
                                        </td>

                                        <td>
                                            <?php echo htmlspecialchars($prestamo['elemento'] ?? '', ENT_QUOTES, 'UTF-8'); ?>
                                        </td>

                                        <td>
                                            <?php echo htmlspecialchars($prestamo['fechaPrestamo'] ?? '', ENT_QUOTES, 'UTF-8'); ?>
                                        </td>

                                        <td>
                                            <?php echo htmlspecialchars($prestamo['fechaDevolucion'] ?? '', ENT_QUOTES, 'UTF-8'); ?>
                                        </td>

                                        <td>
                                            <?php echo htmlspecialchars($prestamo['observaciones'] ?? '', ENT_QUOTES, 'UTF-8'); ?>
                                        </td>

                                        <td>
                                            <?php if (($prestamo['estado'] ?? '') === 'Activo') : ?>

                                                <span class="badge-activo">
                                                    Activo
                                                </span>

                                            <?php elseif (($prestamo['estado'] ?? '') === 'Devuelto') : ?>

                                                <span class="badge-devuelto">
                                                    Devuelto
                                                </span>

                                            <?php else : ?>

                                                <span class="badge-sin-estado">
                                                    Sin estado
                                                </span>

                                            <?php endif; ?>
                                        </td>
                                    </tr>

                                <?php endforeach; ?>

                            <?php endif; ?>

                        </tbody>

                    </table>

                </div>

            </section>

            <div class="alert-info">
                <strong>Descripción del componente:</strong>
                Esta vista permite consultar los préstamos registrados en el archivo JSON del módulo PHP.
                La información se presenta en una tabla visual tipo dashboard, facilitando la lectura,
                el seguimiento y la presentación de la evidencia del proyecto.
            </div>

        </main>

    </div>

</body>

</html>