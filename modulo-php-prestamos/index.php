<?php

/*
|--------------------------------------------------------------------------
| REGISTRO DE PRÉSTAMOS - INVENTO-ACCIÓN
|--------------------------------------------------------------------------
|
| Este archivo muestra el formulario principal del módulo PHP.
| Permite registrar préstamos de elementos tecnológicos.
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
    <title>Invento-Acción - Registro de Préstamos</title>

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

        .form-card {
            background-color: white;
            border-radius: 14px;
            box-shadow: 0 3px 12px rgba(0, 0, 0, 0.10);
            overflow: hidden;
        }

        .form-card-header {
            padding: 18px 22px;
            border-bottom: 1px solid #e9ecef;
            background-color: white;
        }

        .form-card-header h2 {
            margin: 0;
            color: #0d6efd;
            font-size: 22px;
        }

        .form-body {
            padding: 24px;
        }

        .form-grid {
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 18px;
        }

        .form-group {
            margin-bottom: 18px;
        }

        label {
            display: block;
            font-weight: bold;
            margin-bottom: 7px;
            color: #343a40;
        }

        input,
        textarea {
            width: 100%;
            padding: 11px 12px;
            border: 1px solid #ced4da;
            border-radius: 8px;
            font-size: 15px;
            font-family: Arial, Helvetica, sans-serif;
        }

        input:focus,
        textarea:focus {
            outline: none;
            border-color: #0d6efd;
            box-shadow: 0 0 0 3px rgba(13, 110, 253, 0.15);
        }

        textarea {
            resize: vertical;
        }

        .actions {
            display: flex;
            gap: 12px;
            margin-top: 10px;
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
            cursor: pointer;
            font-size: 15px;
        }

        .btn-primary:hover {
            background-color: #0b5ed7;
        }

        .btn-secondary {
            background-color: white;
            color: #6c757d;
            padding: 10px 16px;
            text-decoration: none;
            border-radius: 7px;
            font-weight: bold;
            display: inline-block;
            border: 1px solid #6c757d;
            font-size: 15px;
        }

        .btn-secondary:hover {
            background-color: #6c757d;
            color: white;
        }

        .alert-info {
            margin-top: 24px;
            background-color: #e7f1ff;
            border: 1px solid #b6d4fe;
            color: #084298;
            padding: 16px;
            border-radius: 12px;
        }

        @media (max-width: 900px) {
            .layout {
                flex-direction: column;
            }

            .sidebar {
                width: 100%;
            }

            .cards {
                grid-template-columns: 1fr;
            }

            .form-grid {
                grid-template-columns: 1fr;
            }

            .header-section {
                flex-direction: column;
                align-items: flex-start;
                gap: 15px;
            }

            .actions {
                flex-direction: column;
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

            <a href="index.php" class="sidebar-link active">
                Registrar préstamo
            </a>

            <a href="listarPrestamos.php" class="sidebar-link">
                Préstamos registrados
            </a>

        </aside>

        <main class="content">

            <section class="header-section">

                <div>
                    <h1>
                        Gestión de Préstamos de Equipos
                    </h1>

                    <p>
                        Registro de elementos tecnológicos prestados a usuarios o colaboradores.
                    </p>
                </div>

                <a href="listarPrestamos.php" class="btn-primary">
                    Ver préstamos registrados
                </a>

            </section>

            <section class="cards">

                <div class="card card-blue">
                    <h3>Total préstamos</h3>
                    <p class="card-number"><?php echo $totalPrestamos; ?></p>
                </div>

                <div class="card card-green">
                    <h3>Préstamos activos</h3>
                    <p class="card-number"><?php echo $prestamosActivos; ?></p>
                </div>

                <div class="card card-gray">
                    <h3>Préstamos devueltos</h3>
                    <p class="card-number"><?php echo $prestamosDevueltos; ?></p>
                </div>

            </section>

            <section class="form-card">

                <div class="form-card-header">
                    <h2>Registrar nuevo préstamo</h2>
                </div>

                <div class="form-body">

                    <form action="guardarPrestamo.php" method="POST">

                        <div class="form-grid">

                            <div class="form-group">
                                <label for="colaborador">Nombre del colaborador</label>
                                <input type="text" id="colaborador" name="colaborador" placeholder="Ejemplo: Pepito Pérez" required>
                            </div>

                            <div class="form-group">
                                <label for="elemento">Elemento prestado</label>
                                <input type="text" id="elemento" name="elemento" placeholder="Ejemplo: Cable HDMI" required>
                            </div>

                        </div>

                        <div class="form-grid">

                            <div class="form-group">
                                <label for="fechaPrestamo">Fecha del préstamo</label>
                                <input type="date" id="fechaPrestamo" name="fechaPrestamo" required>
                            </div>

                            <div class="form-group">
                                <label for="fechaDevolucion">Fecha de devolución</label>
                                <input type="date" id="fechaDevolucion" name="fechaDevolucion" required>
                            </div>

                        </div>

                        <div class="form-group">
                            <label for="observaciones">Observaciones</label>
                            <textarea id="observaciones" name="observaciones" rows="4" placeholder="Ejemplo: Placa metálica 0001, entregado en buen estado."></textarea>
                        </div>

                        <div class="actions">

                            <button type="submit" class="btn-primary">
                                Registrar préstamo
                            </button>

                            <a href="listarPrestamos.php" class="btn-secondary">
                                Consultar registros
                            </a>

                        </div>

                    </form>

                </div>

            </section>

            <div class="alert-info">
                <strong>Descripción del componente:</strong>
                Este formulario permite registrar préstamos de elementos tecnológicos,
                guardar la información mediante PHP en un archivo JSON y consultar
                posteriormente los registros desde una vista tipo dashboard.
            </div>

        </main>

    </div>

</body>

</html>