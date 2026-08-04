package dev.jceballos.stockpile.order.application.port;
import dev.jceballos.stockpile.order.domain.OrderStatus;

import java.util.Objects;
import java.util.Optional;

/**
 * Criterios de búsqueda para listar pedidos: filtro opcional por estado, y
 * paginación base cero (page=0 es la primera página, la misma convención
 * que usan la mayoría de las APIs profesionales).
 * <p>
 * Query Object del lado de lectura de CQRS -- agrupar los criterios en un
 * solo objeto evita que agregar un filtro nuevo en el futuro obligue a
 * cambiar la firma de {@code OrderReadRepository.findAll(...)} y todo el
 * código que ya la llama.
 *
 * @param status   filtro opcional por estado; {@code Optional.empty()} para no filtrar
 * @param page     la página solicitada, base cero
 * @param pageSize la cantidad de elementos por página
 */
public record OrderQuery(Optional<OrderStatus> status, int page, int pageSize) {

    /**
     * @throws NullPointerException     si {@code status} es nulo (usar {@code Optional.empty()})
     * @throws IllegalArgumentException si {@code page} es negativo, o si
     *                                  {@code pageSize} no es mayor a cero
     */
    public OrderQuery {
        Objects.requireNonNull(status, "El filtro de estado no puede ser nulo (usar Optional.empty())");
        if (page < 0) {
            throw new IllegalArgumentException("La pagina no puede ser negativa");
        }
        if (pageSize <= 0) {
            throw new IllegalArgumentException("El tamaño de pagina debe ser mayor a cero");
        }
    }

    /**
     * Primera página, sin filtrar por estado.
     *
     * @param pageSize la cantidad de elementos por página
     * @return un {@code OrderQuery} para la primera pagina sin filtro
     */
    public static OrderQuery firstPage(int pageSize) {
        return new OrderQuery(Optional.empty(), 0, pageSize);
    }

    /**
     * Pedidos filtrados por un estado específico.
     *
     * @param status   el estado a filtrar
     * @param page     la página solicitada
     * @param pageSize la cantidad de elementos por página
     * @return un {@code OrderQuery} con ese filtro
     */
    public static OrderQuery byStatus(OrderStatus status, int page, int pageSize) {
        return new OrderQuery(Optional.of(status), page, pageSize);
    }
}