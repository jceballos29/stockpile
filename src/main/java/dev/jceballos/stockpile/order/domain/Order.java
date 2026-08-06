package dev.jceballos.stockpile.order.domain;

import dev.jceballos.stockpile.shared.Money;
import dev.jceballos.stockpile.shared.ProductId;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.Objects;

/**
 * Aggregate Root del contexto {@code order}: representa un pedido, con sus
 * líneas y su ciclo de vida.
 * <p>
 * A diferencia de los Value Objects del proyecto ({@code Money},
 * {@code OrderLine}, etc.), esta clase NO es un record: tiene identidad
 * propia (dos {@code Order} con el mismo {@code OrderId} son "el mismo
 * pedido" aunque su contenido difiera) y estado mutable a lo largo del
 * tiempo (él {@code status} cambia).
 */
public class Order {

    private final OrderId orderId;
    private final Currency currency;
    private final Instant createdAt;
    private final List<OrderLine> lines = new ArrayList<>();
    private OrderStatus status;

    private Order(OrderId orderId, Currency currency, Instant createdAt) {
        this.orderId = orderId;
        this.currency = currency;
        this.createdAt = createdAt;
        this.status = OrderStatus.OPEN;
    }

    /**
     * Crea un pedido nuevo, vacío, en estado {@code OPEN}.
     *
     * @param orderId  la identidad del nuevo pedido
     * @param currency la moneda que van a compartir todas las líneas de este pedido
     * @return un {@code Order} vacío y abierto
     * @throws NullPointerException si {@code orderId} o {@code currency} son nulos
     */
    public static Order open(OrderId orderId, Currency currency) {
        Objects.requireNonNull(orderId, "El identificador de la orden no puede ser nulo");
        Objects.requireNonNull(currency, "La moneda de la orden no puede ser nula");
        return new Order(orderId, currency, Instant.now());
    }

    /**
     * Reconstruye un {@code Order} a partir de datos ya persistidos --
     * uso exclusivo de adaptadores de infraestructura (ej. un repositorio
     * SQLite leyendo un pedido guardado).
     * <p>
     * A diferencia de {@link #open}, NO valida invariantes de negocio de
     * creación (no exige que empiece OPEN, no valida stock de las líneas
     * que recibe). Se asume que estos datos vienen de un {@code Order}
     * previamente valido, que ya paso por esas validaciones antes de
     * guardarse -- reconstruir no es una decision de negocio nueva, es
     * recordar una que ya se tomó.
     *
     * @param orderId  la identidad del pedido
     * @param currency la moneda del pedido
     * @param status   el estado en el que se encuentra actualmente
     * @param lines    las líneas ya persistidas del pedido
     * @param createdAt el momento real en que el pedido se creó originalmente
     *                  (no el momento de la reconstitución)
     * @return un {@code Order} en el estado exacto que se le indicó
     * @throws NullPointerException si algún parámetro es nulo
     */
    public static Order reconstitute(OrderId orderId, Currency currency, OrderStatus status, List<OrderLine> lines, Instant createdAt) {
        Objects.requireNonNull(orderId, "El identificador de la orden no puede ser nulo");
        Objects.requireNonNull(currency, "La moneda de la orden no puede ser nula");
        Objects.requireNonNull(status, "El estado de la orden no puede ser nulo");
        Objects.requireNonNull(lines, "Las lineas de la orden no pueden ser nulas");
        Objects.requireNonNull(createdAt, "La fecha de creacion no puede ser nula");

        Order order = new Order(orderId, currency, createdAt);
        order.status = status;
        order.lines.addAll(lines);
        return order;
    }

    /**
     * Agrega (o acumula, si el producto ya estaba en el pedido) una cantidad
     * de un producto, validando que el stock disponible informado alcance.
     *
     * @param productId      el producto a agregar
     * @param quantity       la cantidad a agregar (se suma a la ya existente, si la había)
     * @param unitPrice      el precio unitario; debe estar en la misma moneda del pedido
     * @param availableStock el stock disponible del producto en este momento
     * @throws NullPointerException        si {@code productId} o {@code unitPrice} son nulos
     * @throws IllegalArgumentException    si {@code quantity} no es mayor a cero, o si
     *                                      la moneda de {@code unitPrice} no coincide
     *                                      con la del pedido
     * @throws InsufficientStockException  si la cantidad total acumulada para este
     *                                      producto excedería {@code availableStock}
     */
    public void addLine(ProductId productId, int quantity, Money unitPrice, int availableStock) {
        requireStatus(OrderStatus.OPEN);
        Objects.requireNonNull(productId, "El producto no puede ser nulo");
        Objects.requireNonNull(unitPrice, "El precio unitario no puede ser nulo");
        if (quantity <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a cero");
        }
        if (!unitPrice.currency().equals(this.currency)) {
            throw new IllegalArgumentException(
                    "La moneda del precio no coincide con la moneda del pedido: "
                            + unitPrice.currency() + " vs " + this.currency);
        }

        int alreadyRequested = quantityOf(productId);
        int totalRequested = alreadyRequested + quantity;

        if (totalRequested > availableStock) {
            throw new InsufficientStockException(productId, totalRequested, availableStock);
        }

        lines.removeIf(line -> line.productId().equals(productId));
        lines.add(new OrderLine(productId, totalRequested, unitPrice));
    }

    /**
     * Marca el pedido como pagado. Requiere que este OPEN y que tenga al
     * menos una línea -- no tiene sentido pagar un pedido vacío.
     *
     * @throws InvalidOrderStateException si el pedido no está OPEN, o si no tiene líneas
     */
    public void pay() {
        requireStatus(OrderStatus.OPEN);
        if (lines.isEmpty()) {
            throw new InvalidOrderStateException("No se puede pagar un pedido sin lineas");
        }
        this.status = OrderStatus.PAID;
    }

    /**
     * Marca el pedido como despachado. Requiere que este PAID.
     *
     * @throws InvalidOrderStateException si el pedido no está PAID
     */
    public void dispatch() {
        requireStatus(OrderStatus.PAID);
        this.status = OrderStatus.DISPATCHED;
    }

    /**
     * Cancela el pedido. Solo válido mientras esta OPEN (antes de pagar) --
     * cancelar un pedido ya pagado implicaría lógica de reembolso, fuera de
     * alcance de este método (ver brief.md, sección 5).
     * <p>
     * A diferencia de {@link #pay()}, SI se puede cancelar un pedido vacío:
     * abandonar un carrito sin productos es una operación válida.
     * <p>
     * Este método no restituye stock -- eso es responsabilidad de la capa
     * de aplicación (ver plan.md, Fase 5), que va a coordinar la
     * restitución en Inventory antes o después de llamar a este método.
     * El dominio {@code order} no conoce, ni debería conocer, al contexto
     * {@code inventory}.
     *
     * @throws InvalidOrderStateException si el pedido no está OPEN
     */
    public void cancel() {
        requireStatus(OrderStatus.OPEN);
        this.status = OrderStatus.CANCELLED;
    }

    /**
     * Calcula el total del pedido sumando el subtotal de todas sus líneas.
     *
     * @return un {@code Money} con el total; {@code 0.00} si no tiene líneas
     */
    public Money calculateTotal() {
        return lines.stream()
                .map(OrderLine::lineTotal)
                .reduce(Money.zero(currency), Money::add);
    }

    /**
     * @return una copia inmutable de las líneas actuales del pedido
     */
    public List<OrderLine> lines() {
        return List.copyOf(lines);
    }

    public OrderId orderId() {
        return orderId;
    }

    public OrderStatus status() {
        return status;
    }

    public Currency currency() {
        return currency;
    }

    public Instant createdAt() { return createdAt; }

    private void requireStatus(OrderStatus expected) {
        if (this.status != expected) {
            throw InvalidOrderStateException.invalidTransition(expected, this.status);
        }
    }

    private int quantityOf(ProductId productId) {
        return lines.stream()
                .filter(line -> line.productId().equals(productId))
                .mapToInt(OrderLine::quantity)
                .findFirst()
                .orElse(0);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Order other)) return false;
        return this.orderId.equals(other.orderId);
    }

    @Override
    public int hashCode() {
        return orderId.hashCode();
    }
}