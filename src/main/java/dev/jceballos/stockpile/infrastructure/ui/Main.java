package dev.jceballos.stockpile.infrastructure.ui;

import dev.jceballos.stockpile.infrastructure.persistence.SchemaInitializer;
import dev.jceballos.stockpile.infrastructure.persistence.SqliteConnectionFactory;
import dev.jceballos.stockpile.infrastructure.persistence.SqliteUnitOfWork;
import dev.jceballos.stockpile.inventory.application.command.RegisterProductCommandHandler;
import dev.jceballos.stockpile.inventory.application.command.ReserveStockCommandHandler;
import dev.jceballos.stockpile.inventory.application.command.RestoreStockCommandHandler;
import dev.jceballos.stockpile.inventory.application.port.ProductReadRepository;
import dev.jceballos.stockpile.inventory.application.port.ProductWriteRepository;
import dev.jceballos.stockpile.inventory.application.query.ProductQueryHandler;
import dev.jceballos.stockpile.inventory.infrastructure.persistence.SqliteProductReadRepository;
import dev.jceballos.stockpile.inventory.infrastructure.persistence.SqliteProductWriteRepository;
import dev.jceballos.stockpile.order.application.command.AddProductToOrderCommandHandler;
import dev.jceballos.stockpile.order.application.command.CancelOrderCommandHandler;
import dev.jceballos.stockpile.order.application.command.DispatchOrderCommandHandler;
import dev.jceballos.stockpile.order.application.command.PayOrderCommandHandler;
import dev.jceballos.stockpile.order.application.port.InventoryReadRepository;
import dev.jceballos.stockpile.order.application.port.OrderReadRepository;
import dev.jceballos.stockpile.order.application.port.OrderWriteRepository;
import dev.jceballos.stockpile.order.application.port.StockReservationPort;
import dev.jceballos.stockpile.order.application.query.OrderQueryHandler;
import dev.jceballos.stockpile.order.infrastructure.integration.InventoryStockQueryAdapter;
import dev.jceballos.stockpile.order.infrastructure.integration.InventoryStockReservationAdapter;
import dev.jceballos.stockpile.order.infrastructure.persistence.SqliteOrderReadRepository;
import dev.jceballos.stockpile.order.infrastructure.persistence.SqliteOrderWriteRepository;
import dev.jceballos.stockpile.shared.application.port.UnitOfWork;

import javafx.application.Application;
import javafx.stage.Stage;

import java.sql.Connection;

/**
 * Composition Root: único lugar del proyecto donde se instancian
 * implementaciones concretas (Sqlite*, adaptadores de integración) y se
 * conectan entre sí via Dependency Injection manual, sin ningún
 * framework. Ninguna otra clase del proyecto hace {@code new} de un
 * repositorio o de un adaptador.
 * <p>
 * Extiende {@code Application} porque JavaFX exige instanciar la clase
 * de arranque por reflexión (constructor público sin argumentos) -- por
 * eso los handlers armados en {@code main()} se guardan en campos
 * {@code static}, único puente posible hacia {@code start(Stage)}.
 */
public final class Main extends Application {

    private static RegisterProductCommandHandler registerProductCommandHandler;
    private static ProductQueryHandler productQueryHandler;
    private static AddProductToOrderCommandHandler addProductToOrderCommandHandler;
    private static CancelOrderCommandHandler cancelOrderCommandHandler;
    private static PayOrderCommandHandler payOrderCommandHandler;
    private static DispatchOrderCommandHandler dispatchOrderCommandHandler;
    private static OrderQueryHandler orderQueryHandler;

    public static void main(String[] args) {
        Connection connection = new SqliteConnectionFactory("jdbc:sqlite:stockpile.db").createConnection();
        new SchemaInitializer().initialize(connection);

        UnitOfWork unitOfWork = new SqliteUnitOfWork(connection);

        // --- Inventory ---
        ProductWriteRepository productWriteRepository = new SqliteProductWriteRepository(connection);
        ProductReadRepository productReadRepository = new SqliteProductReadRepository(connection);

        RegisterProductCommandHandler registerProductCommandHandler =
                new RegisterProductCommandHandler(productWriteRepository);
        ReserveStockCommandHandler reserveStockCommandHandler =
                new ReserveStockCommandHandler(productWriteRepository);
        RestoreStockCommandHandler restoreStockCommandHandler =
                new RestoreStockCommandHandler(productWriteRepository);
        ProductQueryHandler productQueryHandler = new ProductQueryHandler(productReadRepository);

        // --- Integración order <-> inventory ---
        InventoryReadRepository inventoryReadRepository =
                new InventoryStockQueryAdapter(productQueryHandler);
        StockReservationPort stockReservationPort =
                new InventoryStockReservationAdapter(reserveStockCommandHandler, restoreStockCommandHandler);

        // --- Order ---
        OrderWriteRepository orderWriteRepository = new SqliteOrderWriteRepository(connection);
        OrderReadRepository orderReadRepository = new SqliteOrderReadRepository(connection);

        AddProductToOrderCommandHandler addProductToOrderCommandHandler = new AddProductToOrderCommandHandler(
                orderWriteRepository, inventoryReadRepository, stockReservationPort, unitOfWork);
        CancelOrderCommandHandler cancelOrderCommandHandler =
                new CancelOrderCommandHandler(orderWriteRepository, stockReservationPort, unitOfWork);
        PayOrderCommandHandler payOrderCommandHandler =
                new PayOrderCommandHandler(orderWriteRepository, unitOfWork);
        DispatchOrderCommandHandler dispatchOrderCommandHandler =
                new DispatchOrderCommandHandler(orderWriteRepository, unitOfWork);
        OrderQueryHandler orderQueryHandler = new OrderQueryHandler(orderReadRepository);

        Main.registerProductCommandHandler = registerProductCommandHandler;
        Main.productQueryHandler = productQueryHandler;
        Main.addProductToOrderCommandHandler = addProductToOrderCommandHandler;
        Main.cancelOrderCommandHandler = cancelOrderCommandHandler;
        Main.payOrderCommandHandler = payOrderCommandHandler;
        Main.dispatchOrderCommandHandler = dispatchOrderCommandHandler;
        Main.orderQueryHandler = orderQueryHandler;

        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        MainView mainView = new MainView(
                registerProductCommandHandler,
                productQueryHandler,
                addProductToOrderCommandHandler,
                cancelOrderCommandHandler,
                payOrderCommandHandler,
                dispatchOrderCommandHandler,
                orderQueryHandler);
        mainView.show(primaryStage);
    }
}