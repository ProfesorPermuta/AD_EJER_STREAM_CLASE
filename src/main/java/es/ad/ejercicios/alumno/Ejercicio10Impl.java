package es.ad.ejercicios.alumno;

import es.ad.ejercicios.Ejercicio10;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Ejercicio10Impl implements Ejercicio10 {

    /** Una línea del CSV ya interpretada. */
    private record Venta(int id, String mes, String tienda, String producto, String categoria,
            int unidades, BigDecimal precio) {
        BigDecimal importe() {
            return precio
            .multiply(BigDecimal.valueOf(unidades));
        }
    }

    /** Lee el CSV completo y lo convierte en una lista de ventas. */
    private static List<Venta> leer(Path csv) throws IOException {
        // Files.lines mantiene el fichero abierto, por eso el try-with-resources:
        // el Stream se cierra solo al terminar y libera el fichero.
        try (Stream<String> lineas = Files.lines(csv)) {
            return lineas
                    .skip(1) // cabecera
                    .filter(l -> !l.isBlank())
                    //.map(Ejercicio10Impl::parsear)
                     .map(l -> parsear(l))
                    // toList() debe llamarse dentro del try: el Stream es perezoso y
                    // si se devolviera sin consumir, el fichero ya estaría cerrado.
                    .toList();
        }
    }

    /** Convierte una línea del CSV en una Venta. Columnas: id,fecha,tienda,producto,categoria,unidades,precio. */
    private static Venta parsear(String linea) {
        // Separamos por comas; cada trozo se limpia con trim() por si hay espacios sobrantes
        String[] c = linea.split(",");
        return new Venta(
                Integer.parseInt(c[0].trim()),
                c[1].trim().substring(5, 7), // yyyy-MM-dd -> MM
                c[2].trim(),
                c[3].trim(),
                c[4].trim(),
                Integer.parseInt(c[5].trim()),
                new BigDecimal(c[6].trim()));
    }

    /** Suma de importes agrupada por la clave indicada, ordenada por clave. */
    private static Map<String, BigDecimal> ingresosPor(List<Venta> ventas, Function<Venta, String> clave) {
        return ventas.stream()
        .collect(Collectors.groupingBy(clave, TreeMap::new,
                Collectors.reducing(BigDecimal.ZERO, Venta::importe, BigDecimal::add)));
    }

    /** Total de unidades vendidas de cada producto (producto -> unidades), ordenado por producto. */
    private static Map<String, Integer> unidadesPorProducto(List<Venta> ventas) {
        // groupingBy agrupa por producto; el 2º parámetro (TreeMap) fija el tipo de mapa
        // resultante y el 3º (summingInt) suma las unidades de cada grupo.
        return ventas.stream()
            .collect(
                Collectors.groupingBy(
                    venta -> venta.producto(), 
                    () -> new TreeMap<>(),
                    Collectors.summingInt(venta -> venta.unidades())));
    }

    /** Entrada (clave, valor) del mapa con el valor más alto. */
    private static Map.Entry<String, Integer> maximo(Map<String, Integer> mapa) {
        // comparingByValue compara las entradas por su valor; max devuelve un Optional
        // (el mapa podría estar vacío) y orElseThrow lanza excepción en ese caso.
        return mapa.entrySet()
        .stream()
        .max(Map.Entry.comparingByValue()).orElseThrow();
    }

    @Override
    public int numeroVentas(Path csv) throws IOException {
        // Cada línea válida del CSV es una venta, así que basta con contarlas
        // (count devuelve long, de ahí el cast a int).
        return (int)leer(csv).stream().count();
    }

    @Override
    public int unidadesTotales(Path csv) throws IOException {
        // mapToInt convierte el Stream<Venta> en un IntStream de unidades, que ya tiene sum()
        return leer(csv).stream().mapToInt(Venta::unidades).sum();
    }

    @Override
    public BigDecimal ingresosTotales(Path csv) throws IOException {
        // Paso 1: transformar cada venta en su importe (precio * unidades)
        // Paso 2: reducir todos los importes a uno solo sumándolos, empezando en 0.
        //         Se usa BigDecimal (no double) para no perder precisión con el dinero.
        return leer(csv).stream()
            .map(venta -> venta.importe())
            //<17.3, 20.2, 15.4>
            .reduce(BigDecimal.ZERO, (acc, importe) -> acc.add(importe));
    }

    @Override
    public String productoMasVendido(Path csv) throws IOException {
        // Se agrupan las unidades por producto, se busca la entrada con más unidades
        // y nos quedamos con la clave (el nombre del producto).
        return maximo(unidadesPorProducto(leer(csv))).getKey();
    }

    @Override
    public int unidadesProductoMasVendido(Path csv) throws IOException {
        // Igual que productoMasVendido, pero devolviendo el valor (las unidades) en vez de la clave
        return maximo(unidadesPorProducto(leer(csv))).getValue();
    }

    @Override
    public int idVentaMayorImporte(Path csv) throws IOException {
        // Comparator.comparing(Venta::importe) ordena las ventas por importe (BigDecimal es Comparable);
        // max se queda con la mayor y de ella devolvemos solo el id.
        return leer(csv).stream().max(Comparator.comparing(Venta::importe)).orElseThrow().id();
    }

    // @Override
    // public Map<String, BigDecimal> ingresosPorTienda(Path csv) throws IOException
    // {
    // return ingresosPor(leer(csv), Venta::tienda);
    // }

    // @Override
    // public Map<String, BigDecimal> ingresosPorMes(Path csv) throws IOException {
    // return ingresosPor(leer(csv), Venta::mes);
    // }

    // @Override
    // public String categoriaMasIngresos(Path csv) throws IOException {
    // return maximo(ingresosPor(leer(csv), Venta::categoria)).getKey();
    // }

    @Override
    public Map<String, BigDecimal> ingresosPorTienda(Path csv) throws IOException {

        // Objetivo: obtener un Map<Integer, BigDecimal> (id de tienda -> ingresos totales).
        //
        // Paso 1: groupingBy funciona como un GROUP BY de SQL. Agrupa las ventas por tienda
        //         y, de entrada, daría un Map<Integer, List<Venta>>.
        //
        // Paso 2: groupingBy recibe dos parámetros:
        //         - La clave del grupo: v -> v.tienda()
        //         - Un "collector" que dice qué hacer con la lista de ventas de cada grupo.
        //
        // Paso 3: Queremos convertir cada List<Venta> en un único número (la suma de importes),
        //         así que usamos Collectors.reducing, que recibe tres parámetros:
        //         - Valor inicial: BigDecimal.ZERO (la suma empieza en 0)
        //         - Qué extraer de cada venta: v -> v.importe()
        //         - Cómo combinar dos valores: (acc, i) -> acc.add(i)  (acumulado + importe actual)
        //
        // Resultado: cada lista de ventas queda reducida a la suma de sus importes.
        return leer(csv).stream().collect(
            Collectors.groupingBy(v -> v.tienda(),
                Collectors.reducing(
                    BigDecimal.ZERO, 
                    v -> v.importe(), 
                    (acc, i) -> acc.add(i))));

    }

    @Override
    public Map<String, BigDecimal> ingresosPorMes(Path csv) throws IOException {

        // Misma idea que ingresosPorTienda, cambiando solo la clave del grupo:
        // agrupamos por mes (v.mes(), ya extraído como "MM" en parsear) y
        // reducimos cada grupo a la suma de los importes de sus ventas.
        return leer(csv).stream().collect(
            Collectors.groupingBy(v -> v.mes(),
                Collectors.reducing(
                    BigDecimal.ZERO, 
                    v -> v.importe(), 
                    (acc, i) -> acc.add(i))));
    }

    @Override
    public String categoriaMasIngresos(Path csv) throws IOException {

        // Paso 1: sumar el importe de las ventas agrupadas por categoria
        var ingresosCategoria = leer(csv).stream().collect(
            Collectors.groupingBy(v -> v.categoria(),
                Collectors.reducing(
                    BigDecimal.ZERO, 
                    v -> v.importe(), 
                    (acc, i) -> acc.add(i))));

        // Paso 2: recorrer las entradas (categoria -> ingresos) y quedarnos con la mayor
        // comparando los BigDecimal con compareTo (devuelve negativo, 0 o positivo)
        var max = ingresosCategoria.entrySet().stream()
            .max((e1, e2) -> e1.getValue().compareTo(e2.getValue()));

        // Paso 3: max es un Optional (el mapa podria estar vacio); devolvemos la clave
        return max.get().getKey();
    }

    @Override
    public List<Integer> idsVentasMayoresDe(Path csv, BigDecimal limite) throws IOException {
        return leer(csv).stream()
                // Paso 1: quedarnos solo con las ventas cuyo importe supera el límite.
                //         compareTo(...) > 0 significa "estrictamente mayor" (no se usa > con BigDecimal)
                .filter(v -> v.importe().compareTo(limite) > 0)
                // Paso 2: de cada venta que pasa el filtro, quedarnos solo con su id
                .map(Venta::id)
                .toList();
    }

    @Override
    public void escribirProcesamiento(Path csv, Path salida, boolean sobrescribir) throws IOException {
        // Paso 1: construir cada línea del informe (clave,valor) reutilizando los métodos anteriores.
        //         toPlainString evita la notación científica de BigDecimal (p. ej. 1E+3).
        List<String> out = new ArrayList<>();
        out.add("numero_ventas," + numeroVentas(csv));
        out.add("unidades_totales," + unidadesTotales(csv));
        out.add("ingresos_totales," + ingresosTotales(csv).toPlainString());
        ingresosPorTienda(csv).forEach((t, i) -> out.add("ingresos_tienda," + t + "," + i.toPlainString()));
        out.add("producto_mas_vendido," + productoMasVendido(csv) + "," + unidadesProductoMasVendido(csv));
        out.add("venta_mayor_importe," + idVentaMayorImporte(csv));
        ingresosPorMes(csv).forEach((m, i) -> out.add("ingresos_mes," + m + "," + i.toPlainString()));
        out.add("categoria_mas_ingresos," + categoriaMasIngresos(csv));
        out.add("ventas_mayores_de_500," + idsVentasMayoresDe(csv, new BigDecimal("500")));

        // Paso 2: escribir las líneas en el fichero de salida.
        // CREATE_NEW lanza FileAlreadyExistsException sin tocar el fichero existente
        // Con sobrescribir=true se crea si no existe y, si existe, se vacía (TRUNCATE_EXISTING) antes de escribir
        if (sobrescribir)
            Files.write(salida, out, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.WRITE);
        else
            Files.write(salida, out, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
    }
}
