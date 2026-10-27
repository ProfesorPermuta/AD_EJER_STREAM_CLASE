package es.ad.ejercicios.alumno;

import es.ad.ejercicios.Ejercicio4;
import java.util.List;

public class Ejercicio4Impl implements Ejercicio4 {
    @Override
    public List<Integer> tresMayoresDistintos(List<Integer> numeros) {
        return numeros.stream()
                .sorted((n1, n2) -> n2 - n1)
                .distinct()
                .limit(3)
                .toList();
    }

    @Override
    public int suma(List<Integer> numeros) {
        return numeros.stream().reduce(0, (acc, n) -> acc + n);
    }

    @Override
    public double media(List<Integer> numeros) {
        
        return numeros.stream().mapToInt(i -> i).average().orElseGet(null);
    }

    @Override
    public int maximo(List<Integer> numeros) {
        return numeros.stream().mapToInt(i->i).max().orElse(0);
    }
}
