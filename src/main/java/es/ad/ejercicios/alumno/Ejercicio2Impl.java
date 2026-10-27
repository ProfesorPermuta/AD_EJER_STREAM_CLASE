package es.ad.ejercicios.alumno;

import es.ad.ejercicios.Ejercicio2;
import java.util.List;

public class Ejercicio2Impl implements Ejercicio2 {
    @Override
    public int contarCiudades(List<String> ciudades) {
        return (int) ciudades.stream().count();
    }

    @Override
    public int contarMasLargasQue(List<String> ciudades, int minimo) {
        return (int) ciudades.stream().filter(c -> c.length() > minimo).count();
    }

    @Override
    public int sumaLongitudes(List<String> ciudades) {
        //[Vigo, Madrid, Coruña]
        return ciudades.stream()
        //[4, 6, 6]
        .map(c -> c.length())
        //16
        .reduce(0, (acc, n) -> acc + n);
    }
}
