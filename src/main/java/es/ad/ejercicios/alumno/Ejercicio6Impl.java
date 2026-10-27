package es.ad.ejercicios.alumno;

import es.ad.ejercicios.Ejercicio6;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Ejercicio6Impl implements Ejercicio6 {
    @Override
    public List<Integer> primosHasta(int n) {
        return IntStream.rangeClosed(2, n)
                .filter(x -> IntStream.range(2, x)
                        .noneMatch(d -> x % d == 0))
                .boxed()
                .collect(Collectors.toList());

    }
}
