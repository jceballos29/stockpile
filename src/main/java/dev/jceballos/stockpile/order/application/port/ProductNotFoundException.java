package dev.jceballos.stockpile.order.application.port;

import dev.jceballos.stockpile.shared.ProductId;

/**
 * Se lanza cuando se consulta o se intenta reservar stock de un producto
 * que no existe en el catálogo. Distingue explicitamente este caso de
 * "el producto existe, pero tiene 0 unidades disponibles".
 * <p>
 * Vive en {@code order.application.port} -- igual que {@code
 * InventoryReadRepository} y {@code StockReservationPort}, es parte del
 * contrato que {@code order} define para lo que necesita de
 * {@code inventory}, no una excepción del dominio {@code inventory} en sí.
 */
public class ProductNotFoundException extends RuntimeException {

  public ProductNotFoundException(ProductId productId) {
    super("No se encontró el producto en el catalogo: " + productId);
  }
}