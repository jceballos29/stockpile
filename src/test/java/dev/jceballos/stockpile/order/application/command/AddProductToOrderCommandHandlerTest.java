package dev.jceballos.stockpile.order.application.command;

import dev.jceballos.stockpile.inventory.application.command.ReserveStockCommandHandler;
import dev.jceballos.stockpile.inventory.application.command.RestoreStockCommandHandler;
import dev.jceballos.stockpile.inventory.application.port.InMemoryProductWriteRepository;
import dev.jceballos.stockpile.inventory.domain.Product;
import dev.jceballos.stockpile.order.application.OrderNotFoundException;
import dev.jceballos.stockpile.order.application.port.InMemoryInventoryReadRepository;
import dev.jceballos.stockpile.order.application.port.InMemoryOrderWriteRepository;
import dev.jceballos.stockpile.order.application.port.ProductNotFoundException;
import dev.jceballos.stockpile.order.domain.InsufficientStockException;
import dev.jceballos.stockpile.order.domain.Order;
import dev.jceballos.stockpile.order.domain.OrderId;
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

class AddProductToOrderCommandHandlerTest {

    private static final Currency USD = Currency.getInstance("USD");

    private InMemoryOrderWriteRepository orderRepository;
    private InMemoryInventoryReadRepository inventoryReadRepository;
    private InMemoryProductWriteRepository productWriteRepository;
    private AddProductToOrderCommandHandler handler;

    @BeforeEach
    void setUp() {
        orderRepository = new InMemoryOrderWriteRepository();
        inventoryReadRepository = new InMemoryInventoryReadRepository();
        productWriteRepository = new InMemoryProductWriteRepository();

        InventoryStockReservationAdapter stockReservationPort = new InventoryStockReservationAdapter(
                new ReserveStockCommandHandler(productWriteRepository),
                new RestoreStockCommandHandler(productWriteRepository));

        handler = new AddProductToOrderCommandHandler(
                orderRepository, inventoryReadRepository, stockReservationPort, new NoOpUnitOfWork());
    }

    @Test
    void shouldAddLineReserveStockAndPersistOrder() {
        OrderId orderId = OrderId.newId();
        orderRepository.save(Order.open(orderId, USD));
        ProductId laptop = new ProductId("SKU-LAPTOP");
        inventoryReadRepository.withStock(laptop, 5);
        productWriteRepository.save(Product.register(laptop, "Laptop", Money.of(new BigDecimal("999.00"), "USD"), 5));

        handler.handle(new AddProductToOrderCommand(orderId, laptop, 2, Money.of(new BigDecimal("999.00"), "USD")));

        Order persistedOrder = orderRepository.findById(orderId).orElseThrow();
        assertThat(persistedOrder.lines()).hasSize(1);
        assertThat(persistedOrder.lines().get(0).quantity()).isEqualTo(2);

        Product persistedProduct = productWriteRepository.findById(laptop).orElseThrow();
        assertThat(persistedProduct.stock()).isEqualTo(3);
    }

    @Test
    void shouldThrowWhenOrderDoesNotExist() {
        ProductId laptop = new ProductId("SKU-LAPTOP");
        inventoryReadRepository.withStock(laptop, 5);

        assertThatThrownBy(() -> handler.handle(new AddProductToOrderCommand(
                OrderId.newId(), laptop, 1, Money.of(new BigDecimal("999.00"), "USD"))))
                .isInstanceOf(OrderNotFoundException.class);
    }

    @Test
    void shouldThrowWhenProductDoesNotExistInCatalog() {
        OrderId orderId = OrderId.newId();
        orderRepository.save(Order.open(orderId, USD));

        assertThatThrownBy(() -> handler.handle(new AddProductToOrderCommand(
                orderId, new ProductId("SKU-UNKNOWN"), 1, Money.of(new BigDecimal("10.00"), "USD"))))
                .isInstanceOf(ProductNotFoundException.class);
    }

    @Test
    void shouldThrowWhenStockIsInsufficientAndNotReserveNorPersist() {
        OrderId orderId = OrderId.newId();
        orderRepository.save(Order.open(orderId, USD));
        ProductId laptop = new ProductId("SKU-LAPTOP");
        inventoryReadRepository.withStock(laptop, 1);
        productWriteRepository.save(Product.register(laptop, "Laptop", Money.of(new BigDecimal("999.00"), "USD"), 1));

        assertThatThrownBy(() -> handler.handle(new AddProductToOrderCommand(
                orderId, laptop, 5, Money.of(new BigDecimal("999.00"), "USD"))))
                .isInstanceOf(InsufficientStockException.class);

        assertThat(orderRepository.findById(orderId).orElseThrow().lines()).isEmpty();
        assertThat(productWriteRepository.findById(laptop).orElseThrow().stock()).isEqualTo(1);
    }
}