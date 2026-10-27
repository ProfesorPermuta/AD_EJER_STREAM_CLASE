package es.ad.ejercicios.alumno;

import java.util.Comparator;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

import es.ad.ejercicios.Ejercicio1;

public class Ejercicio1Impl implements Ejercicio1 {
    @Override
    public void ejecutar() {
        Predicate<Integer> isEven = (n) -> n % 2 == 0;

        System.out.println(isEven.test(4));
        System.out.println(isEven.test(7));

        //Function<String, Integer> longi = (c) -> c.length();
        Function<String, Integer> longi = String::length;
        System.out.println(longi.apply("lambda"));

        BiFunction<Integer, Integer, Integer> retBigger = (n1, n2) -> n1 > n2 ? n1 : n2;
        System.out.println(retBigger.apply(3, 9));


        Comparator<String> comparator = (c1, c2) -> c1.length() - c2.length();
        System.out.println(comparator.compare("Cádiz", "Sevilla"));
        


    }
}
