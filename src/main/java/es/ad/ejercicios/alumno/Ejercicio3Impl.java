package es.ad.ejercicios.alumno;

import es.ad.ejercicios.Ejercicio3;
import java.util.List;

public class Ejercicio3Impl implements Ejercicio3 {
    @Override
    public List<String> largasEnMayusculas(List<String> ciudades) {

        return ciudades.stream()
                .filter(c -> c.length() > 6)
                .map(c -> c.toUpperCase())
                .toList();

    }
}
