package dev.jceballos.stockpile.infrastructure.ui;

import dev.jceballos.stockpile.inventory.application.command.RegisterProductCommandHandler;
import dev.jceballos.stockpile.inventory.application.query.ProductQueryHandler;
import dev.jceballos.stockpile.order.application.command.AddProductToOrderCommandHandler;
import dev.jceballos.stockpile.order.application.command.CancelOrderCommandHandler;
import dev.jceballos.stockpile.order.application.command.DispatchOrderCommandHandler;
import dev.jceballos.stockpile.order.application.command.PayOrderCommandHandler;
import dev.jceballos.stockpile.order.application.query.OrderQueryHandler;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.Objects;

/**
 * Ventana principal: sidebar de navegación (izquierda) + área de
 * contenido (centro) que se reemplaza según la pantalla activa -- sin
 * CardLayout: en JavaFX, alcanza con root.setCenter(nuevaVista).
 */
public class MainView {

    private final BorderPane root = new BorderPane();

    private final RegisterProductCommandHandler registerProductCommandHandler;
    private final ProductQueryHandler productQueryHandler;
    private final AddProductToOrderCommandHandler addProductToOrderCommandHandler;
    private final CancelOrderCommandHandler cancelOrderCommandHandler;
    private final PayOrderCommandHandler payOrderCommandHandler;
    private final DispatchOrderCommandHandler dispatchOrderCommandHandler;
    private final OrderQueryHandler orderQueryHandler;

    public MainView(RegisterProductCommandHandler registerProductCommandHandler,
                    ProductQueryHandler productQueryHandler,
                    AddProductToOrderCommandHandler addProductToOrderCommandHandler,
                    CancelOrderCommandHandler cancelOrderCommandHandler,
                    PayOrderCommandHandler payOrderCommandHandler,
                    DispatchOrderCommandHandler dispatchOrderCommandHandler,
                    OrderQueryHandler orderQueryHandler) {
        this.registerProductCommandHandler = Objects.requireNonNull(registerProductCommandHandler);
        this.productQueryHandler = Objects.requireNonNull(productQueryHandler);
        this.addProductToOrderCommandHandler = Objects.requireNonNull(addProductToOrderCommandHandler);
        this.cancelOrderCommandHandler = Objects.requireNonNull(cancelOrderCommandHandler);
        this.payOrderCommandHandler = Objects.requireNonNull(payOrderCommandHandler);
        this.dispatchOrderCommandHandler = Objects.requireNonNull(dispatchOrderCommandHandler);
        this.orderQueryHandler = Objects.requireNonNull(orderQueryHandler);

        root.setLeft(buildSidebar());
        showDashboard();
    }

    private VBox buildSidebar() {
        Button dashboardButton = new Button("Dashboard");
        Button productsButton = new Button("Productos");
        Button ordersButton = new Button("Pedidos");

        dashboardButton.setOnAction(e -> showDashboard());
        productsButton.setOnAction(e -> showProducts());
        ordersButton.setOnAction(e -> showOrders());

        for (Button button : new Button[]{dashboardButton, productsButton, ordersButton}) {
            button.setMaxWidth(Double.MAX_VALUE);
        }

        VBox sidebar = new VBox(8, dashboardButton, productsButton, ordersButton);
        sidebar.setPadding(new Insets(15));
        sidebar.setPrefWidth(180);
        sidebar.getStyleClass().add("sidebar");
        return sidebar;
    }

    private void showDashboard() {
        root.setCenter(new Label("Dashboard: pendiente (próximo paso)"));
    }

    private void showProducts() {
        root.setCenter(new Label("Productos: pendiente (próximo paso)"));
    }

    private void showOrders() {
        root.setCenter(new Label("Pedidos: pendiente (próximo paso)"));
    }

    public void show(Stage stage) {
        Scene scene = new Scene(root, 900, 650);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/styles.css")).toExternalForm());
        stage.setTitle("Stockpile");
        stage.setScene(scene);
        stage.show();
    }
}