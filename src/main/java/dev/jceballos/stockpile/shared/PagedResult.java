package dev.jceballos.stockpile.shared;

import java.util.List;

/**
 * Resultado paginado genérico, reutilizable por cualquier puerto de
 * consulta del proyecto (hoy {@code OrderReadRepository}; en el futuro,
 * el equivalente en {@code inventory} para listar productos).
 * <p>
 * Pertenece al Shared Kernel: la paginación es un concepto transversal,
 * no propio de ningún Bounded Context.
 *
 * @param items          los elementos de esta página
 * @param page           el número de página actual, base cero
 * @param pageSize       el tamaño de página solicitado
 * @param totalElements  la cantidad total de elementos que existen, sin paginar
 * @param <T> el tipo de elemento paginado
 */
public record PagedResult<T>(List<T> items, int page, int pageSize, long totalElements) {

    /**
     * @return la cantidad total de páginas, redondeada hacia arriba
     */
    public int totalPages() {
        return (int) Math.ceil((double) totalElements / pageSize);
    }

    /**
     * @return {@code true} si existe al menos una página después de la actual
     */
    public boolean hasNext() {
        return (long) (page + 1) * pageSize < totalElements;
    }
}