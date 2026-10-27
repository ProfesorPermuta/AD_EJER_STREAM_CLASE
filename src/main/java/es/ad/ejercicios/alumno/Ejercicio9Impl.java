package es.ad.ejercicios.alumno;

import es.ad.ejercicios.Ejercicio9;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Ejercicio9Impl implements Ejercicio9 {

    @Override
    public List<List<String>> gruposDeAnagramas(Path fichero) throws IOException {
        try (Stream<String> lineas = Files.lines(fichero)) {
            return lineas
                    .map(String::trim)
                    .filter(p -> !p.isEmpty())
                    .collect(Collectors.groupingBy(Ejercicio9Impl::clave))
                    .values().stream()
                    .filter(grupo -> grupo.size() > 1)
                    .toList();
        }
    }

    /** Letras de la palabra ordenadas: dos palabras son anagramas si tienen la misma clave. */
    private static String clave(String palabra) {
        char[] letras = palabra.toLowerCase().toCharArray();
        Arrays.sort(letras);
        return new String(letras);
    }
}
