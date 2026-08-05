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

import javax.swing.*;
import java.sql.Connection;


/**
 * Composition Root: único lugar del proyecto donde se instancian
 * implementaciones concretas (Sqlite*, adaptadores de integración) y se
 * conectan entre sí via Dependency Injection manual, sin ningún
 * framework. Ninguna otra clase del proyecto hace {@code new} de un
 * repositorio o de un adaptador.
 */
public final class Main {

    private Main() {
    }

    static void main() {
        Connection connection = new SqliteConnectionFactory("jdbc:sqlite:stockpile.db").createConnection();
        new SchemaInitializer().initialize(connection);

        UnitOfWork unitOfWork = new SqliteUnitOfWork(connection);

        // INVENTORY
        ProductWriteRepository productWriteRepository = new SqliteProductWriteRepository(connection);
        ProductReadRepository productReadRepository = new SqliteProductReadRepository(connection);

        RegisterProductCommandHandler registerProductCommandHandler = new RegisterProductCommandHandler(productWriteRepository);
        ReserveStockCommandHandler reserveStockCommandHandler = new ReserveStockCommandHandler(productWriteRepository);
        RestoreStockCommandHandler restoreStockCommandHandler = new RestoreStockCommandHandler(productWriteRepository);
        ProductQueryHandler productQueryHandler = new ProductQueryHandler(productReadRepository);

        // INTEGRATION ORDER <--> INVENTORY
        InventoryReadRepository inventoryReadRepository = new InventoryStockQueryAdapter(productQueryHandler);
        StockReservationPort stockReservationPort = new InventoryStockReservationAdapter(reserveStockCommandHandler, restoreStockCommandHandler);

        // ORDER
        OrderWriteRepository orderWriteRepository = new SqliteOrderWriteRepository(connection);
        OrderReadRepository orderReadRepository = new SqliteOrderReadRepository(connection);

        AddProductToOrderCommandHandler addProductToOrderCommandHandler = new AddProductToOrderCommandHandler(orderWriteRepository, inventoryReadRepository, stockReservationPort, unitOfWork);
        CancelOrderCommandHandler cancelOrderCommandHandler = new CancelOrderCommandHandler(orderWriteRepository, stockReservationPort, unitOfWork);
        PayOrderCommandHandler payOrderCommandHandler = new PayOrderCommandHandler(orderWriteRepository, unitOfWork);
        DispatchOrderCommandHandler dispatchOrderCommandHandler = new DispatchOrderCommandHandler(orderWriteRepository, unitOfWork);
        OrderQueryHandler orderQueryHandler = new OrderQueryHandler(orderReadRepository);

        SwingUtilities.invokeLater(() -> {
            MainFrame mainFrame = new MainFrame(
                    registerProductCommandHandler,
                    productQueryHandler,
                    addProductToOrderCommandHandler,
                    cancelOrderCommandHandler,
                    payOrderCommandHandler,
                    dispatchOrderCommandHandler,
                    orderQueryHandler);
            mainFrame.setVisible(true);
        });

    }
}
