package dev.jceballos.stockpile.order.application.command;

import dev.jceballos.stockpile.inventory.application.command.ReserveStockCommandHandler;
import dev.jceballos.stockpile.inventory.application.command.RestoreStockCommandHandler;
import dev.jceballos.stockpile.inventory.application.port.InMemoryProductWriteRepository;
import dev.jceballos.stockpile.inventory.domain.Product;
import dev.jceballos.stockpile.order.application.OrderNotFoundException;
import dev.jceballos.stockpile.order.application.port.InMemoryOrderWriteRepository;
import dev.jceballos.stockpile.order.domain.InvalidOrderStateException;
import dev.jceballos.stockpile.order.domain.Order;
import dev.jceballos.stockpile.order.domain.OrderId;
import dev.jceballos.stockpile.order.domain.OrderStatus;
import dev.jceballos.stockpile.order.infrastructure.integration.InventoryStockReservationAdapter;
import dev.jceballos.stockpile.shared.Money;
import dev.jceballos.stockpile.shared.ProductId;

import dev.jceballos.stockpile.shared.application.port.NoOpUnitOfWork;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Currency;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CancelOrderCommandHandlerTest {

    private static final Currency USD = Currency.getInstance("USD");

    private InMemoryOrderWriteRepository orderRepository;
    private InMemoryProductWriteRepository productRepository;
    private CancelOrderCommandHandler handler;

    @BeforeEach
    void setUp() {
        orderRepository = new InMemoryOrderWriteRepository();
        productRepository = new InMemoryProductWriteRepository();

        InventoryStockReservationAdapter stockReservationPort = new InventoryStockReservationAdapter(
                new ReserveStockCommandHandler(productRepository),
                new RestoreStockCommandHandler(productRepository));

        handler = new CancelOrderCommandHandler(orderRepository, stockReservationPort, new NoOpUnitOfWork());
    }

    @Test
    void shouldCancelOrderAndReleaseStockForEachLine() {
        ProductId laptop = new ProductId("SKU-LAPTOP");
        ProductId mouse = new ProductId("SKU-MOUSE");
        productRepository.save(Product.register(laptop, "Laptop", Money.of(new BigDecimal("999.00"), "USD"), 3));
        productRepository.save(Product.register(mouse, "Mouse", Money.of(new BigDecimal("25.00"), "USD"), 7));

        OrderId orderId = OrderId.newId();
        Order order = Order.open(orderId, USD);
        order.addLine(laptop, 2, Money.of(new BigDecimal("999.00"), "USD"), 5);
        order.addLine(mouse, 3, Money.of(new BigDecimal("25.00"), "USD"), 10);
        orderRepository.save(order);

        handler.handle(new CancelOrderCommand(orderId));

        assertThat(orderRepository.findById(orderId).orElseThrow().status()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(productRepository.findById(laptop).orElseThrow().stock()).isEqualTo(5);
        assertThat(productRepository.findById(mouse).orElseThrow().stock()).isEqualTo(10);
    }

    @Test
    void shouldAllowCancellingAnEmptyOrder() {
        OrderId orderId = OrderId.newId();
        orderRepository.save(Order.open(orderId, USD));

        handler.handle(new CancelOrderCommand(orderId));

        assertThat(orderRepository.findById(orderId).orElseThrow().status()).isEqualTo(OrderStatus.CANCELLED);
    }

    @Test
    void shouldThrowWhenOrderDoesNotExist() {
        assertThatThrownBy(() -> handler.handle(new CancelOrderCommand(OrderId.newId())))
                .isInstanceOf(OrderNotFoundException.class);
    }

    @Test
    void shouldThrowWhenOrderIsNotOpenAndNotReleaseStock() {
        ProductId laptop = new ProductId("SKU-LAPTOP");
        productRepository.save(Product.register(laptop, "Laptop", Money.of(new BigDecimal("999.00"), "USD"), 3));

        OrderId orderId = OrderId.newId();
        Order order = Order.open(orderId, USD);
        order.addLine(laptop, 2, Money.of(new BigDecimal("999.00"), "USD"), 5);
        order.pay();
        orderRepository.save(order);

        assertThatThrownBy(() -> handler.handle(new CancelOrderCommand(orderId)))
                .isInstanceOf(InvalidOrderStateException.class);

        assertThat(productRepository.findById(laptop).orElseThrow().stock()).isEqualTo(3);
    }
}