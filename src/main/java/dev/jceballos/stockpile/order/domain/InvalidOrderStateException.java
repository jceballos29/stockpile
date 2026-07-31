package dev.jceballos.stockpile.order.domain;

/**
 * Se lanza cuando se intenta una operacion sobre un {@code Order} que
 * requiere que esté en un estado distinto al que tiene actualmente --
 * por ejemplo, agregar una lénea a un pedido que ya no esté OPEN, pagar
 * un pedido sin líneas, o despachar un pedido que no esté pagado.
 * <p>
 * Unchecked por el mismo motivo que {@link InsufficientStockException}:
 * es una regla de negocio violada, no algo que quien llama pueda resolver
 * en el momento.
 */
public class InvalidOrderStateException extends RuntimeException {

    public InvalidOrderStateException(String message) {
        super(message);
    }

    /**
     * Construye la excepción para el caso más común: una operación que
     * requería un estado específico, pero el pedido está en otro.
     *
     * @param expected el estado que la operación requería
     * @param actual   el estado real del pedido en el momento del intento
     * @return una excepción con mensaje descriptivo de la transición inválida
     */
    public static InvalidOrderStateException invalidTransition(OrderStatus expected, OrderStatus actual) {
        return new InvalidOrderStateException(
                "La operación requiere que el pedido este en estado " + expected
                        + ", pero esta en " + actual);
    }
}
