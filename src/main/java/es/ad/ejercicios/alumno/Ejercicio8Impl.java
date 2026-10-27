package es.ad.ejercicios.alumno;

import es.ad.ejercicios.Ejercicio8;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;

public class Ejercicio8Impl implements Ejercicio8 {

    @Override
    public boolean crearDirectorio(Path directorio) throws IOException {
        Files.createDirectories(directorio);
        return Files.isDirectory(directorio);
    }

    @Override
    public void crearFichero(Path fichero, List<String> lineas) throws IOException {
        Path padre = fichero.getParent();
        if (padre != null)
            Files.createDirectories(padre);

        // CREATE_NEW: falla con FileAlreadyExistsException si ya existe
        Files.write(fichero, lineas, StandardOpenOption.CREATE_NEW);
    }

    @Override
    public List<String> leerLineas(Path fichero) throws IOException {
        return Files.readAllLines(fichero); // cierra el fichero por sí solo
    }

    @Override
    public void copiar(Path origen, Path destino) throws IOException {
        Files.copy(origen, destino); // sin REPLACE_EXISTING lanza FileAlreadyExistsException
    }

    @Override
    public void mover(Path origen, Path destino) throws IOException {
        Files.move(origen, destino);
    }

    @Override
    public boolean existe(Path ruta) {
        return Files.exists(ruta);
    }

    @Override
    public boolean borrar(Path fichero) throws IOException {
        return Files.deleteIfExists(fichero);
    }
}
