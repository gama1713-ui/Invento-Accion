<?php

$archivo = 'datos/prestamos.json';

$prestamos = [];

if (file_exists($archivo)) {
    $contenido = file_get_contents($archivo);
    $prestamos = json_decode($contenido, true);

    if (!is_array($prestamos)) {
        $prestamos = [];
    }
}

$nuevoPrestamo = [
    "colaborador" => $_POST['colaborador'] ?? '',
    "elemento" => $_POST['elemento'] ?? '',
    "fechaPrestamo" => $_POST['fechaPrestamo'] ?? '',
    "fechaDevolucion" => $_POST['fechaDevolucion'] ?? '',
    "observaciones" => $_POST['observaciones'] ?? '',
    "estado" => "Activo"
];

$prestamos[] = $nuevoPrestamo;

file_put_contents(
    $archivo,
    json_encode($prestamos, JSON_PRETTY_PRINT | JSON_UNESCAPED_UNICODE)
);

header("Location: listarPrestamos.php");
exit;

?>