package dev.jceballos.stockpile.inventory.application.port;

import java.util.Objects;
import java.util.Optional;

/**
 * Criterios de búsqueda para listar productos: filtro opcional por nombre
 * (búsqueda parcial, no exacta), y paginación base cero.
 * <p>
 * Mismo patron de Query Object que {@code order.application.port.OrderQuery}.
 *
 * @param nameContains filtro opcional: solo productos cuyo nombre contenga
 *                      este texto (búsqueda parcial); {@code Optional.empty()}
 *                      para no filtrar
 * @param page         la página solicitada, base cero
 * @param pageSize     la cantidad de elementos por página
 */
public record ProductQuery(Optional<String> nameContains, int page, int pageSize) {

    /**
     * @throws NullPointerException     si {@code nameContains} es nulo (usar {@code Optional.empty()})
     * @throws IllegalArgumentException si {@code page} es negativo, o si
     *                                  {@code pageSize} no es mayor a cero
     */
    public ProductQuery {
        Objects.requireNonNull(nameContains, "El filtro de nombre no puede ser nulo (usar Optional.empty())");
        if (page < 0) {
            throw new IllegalArgumentException("La página no puede ser negativa");
        }
        if (pageSize <= 0) {
            throw new IllegalArgumentException("El tamaño de página debe ser mayor a cero");
        }
    }

    /**
     * Primera página, sin filtrar por nombre.
     *
     * @param pageSize la cantidad de elementos por página
     * @return un {@code ProductQuery} para la primera página sin filtro
     */
    public static ProductQuery firstPage(int pageSize) {
        return new ProductQuery(Optional.empty(), 0, pageSize);
    }

    /**
     * Productos cuyo nombre contenga el texto dado.
     *
     * @param nameFragment el texto a buscar dentro del nombre
     * @param page         la página solicitada
     * @param pageSize     la cantidad de elementos por página
     * @return un {@code ProductQuery} con ese filtro
     */
    public static ProductQuery byNameContains(String nameFragment, int page, int pageSize) {
        return new ProductQuery(Optional.of(nameFragment), page, pageSize);
    }
}