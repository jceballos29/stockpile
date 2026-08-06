package dev.jceballos.stockpile.infrastructure.ui;

import dev.jceballos.stockpile.inventory.application.command.DeleteProductCommandHandler;
import dev.jceballos.stockpile.inventory.application.command.RegisterProductCommandHandler;
import dev.jceballos.stockpile.inventory.application.command.UpdateProductCommandHandler;
import dev.jceballos.stockpile.inventory.application.query.ProductQueryHandler;
import dev.jceballos.stockpile.order.application.command.AddProductToOrderCommandHandler;
import dev.jceballos.stockpile.order.application.command.CancelOrderCommandHandler;
import dev.jceballos.stockpile.order.application.command.DispatchOrderCommandHandler;
import dev.jceballos.stockpile.order.application.command.PayOrderCommandHandler;
import dev.jceballos.stockpile.order.application.query.OrderQueryHandler;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import org.kordamp.ikonli.javafx.FontIcon;

import java.util.Objects;

/**
 * Ventana principal: sidebar de navegación (izquierda) + área de
 * contenido (centro) que se reemplaza según la pantalla activa -- sin
 * CardLayout: en JavaFX, alcanza con root.setCenter(nuevaVista).
 */
public class MainView {

    private final BorderPane root = new BorderPane();

    private final RegisterProductCommandHandler registerProductCommandHandler;
    private final UpdateProductCommandHandler updateProductCommandHandler;
    private final DeleteProductCommandHandler deleteProductCommandHandler;
    private final ProductQueryHandler productQueryHandler;
    private final AddProductToOrderCommandHandler addProductToOrderCommandHandler;
    private final CancelOrderCommandHandler cancelOrderCommandHandler;
    private final PayOrderCommandHandler payOrderCommandHandler;
    private final DispatchOrderCommandHandler dispatchOrderCommandHandler;
    private final OrderQueryHandler orderQueryHandler;


    public MainView(RegisterProductCommandHandler registerProductCommandHandler,
                    UpdateProductCommandHandler updateProductCommandHandler,
                    DeleteProductCommandHandler deleteProductCommandHandler,
                    ProductQueryHandler productQueryHandler,
                    AddProductToOrderCommandHandler addProductToOrderCommandHandler,
                    CancelOrderCommandHandler cancelOrderCommandHandler,
                    PayOrderCommandHandler payOrderCommandHandler,
                    DispatchOrderCommandHandler dispatchOrderCommandHandler,
                    OrderQueryHandler orderQueryHandler) {
        this.registerProductCommandHandler = Objects.requireNonNull(registerProductCommandHandler);
        this.updateProductCommandHandler = Objects.requireNonNull(updateProductCommandHandler);
        this.deleteProductCommandHandler = Objects.requireNonNull(deleteProductCommandHandler);
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

        final FontIcon warehouseIcon = new FontIcon("fas-warehouse");
        final FontIcon productsIcon = new FontIcon("fas-boxes");
        final FontIcon dashboardIcon = new FontIcon("fas-chart-line");
        final FontIcon ordersIcon = new FontIcon("fas-shopping-cart");

        warehouseIcon.setIconSize(48);
        warehouseIcon.setStyle("-fx-icon-color: -primary;");

        Label logo = new Label("Stockpile", warehouseIcon);
        logo.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: -primary;");
        logo.setPadding(new Insets(20, 20, 30, 20));

        Button dashboardButton = new Button("Dashboard", dashboardIcon);
        Button productsButton = new Button("Productos", productsIcon);
        Button ordersButton = new Button("Pedidos", ordersIcon);

        for (Button button  : new Button[]{dashboardButton, productsButton, ordersButton}) {
            button.setMaxWidth(Double.MAX_VALUE);
            button.setAlignment(Pos.CENTER_LEFT);
            button.setGraphicTextGap(12);
        }

        dashboardButton.setOnAction(e -> showDashboard());
        productsButton.setOnAction(e -> showProducts());
        ordersButton.setOnAction(e -> showOrders());

        for (Button button : new Button[]{dashboardButton, productsButton, ordersButton}) {
            button.setMaxWidth(Double.MAX_VALUE);
        }

        VBox navItems = new VBox(4, dashboardButton, productsButton, ordersButton);
        navItems.setPadding(new Insets(0, 12, 0, 12));

        VBox sidebar = new VBox(logo, navItems);
        sidebar.getStyleClass().add("sidebar");
        return sidebar;
    }

    private void showDashboard() {
        root.setCenter(new Label("Dashboard: pendiente"));
    }

    private void showProducts() {
        ProductsView productsView = new ProductsView(
                registerProductCommandHandler, updateProductCommandHandler,
                deleteProductCommandHandler, productQueryHandler);
        root.setCenter(productsView.getRoot());
    }

    private void showOrders() {
        root.setCenter(new Label("Pedidos: pendiente"));
    }

    public void show(Stage stage) {
        Scene scene = new Scene(root, 900, 650);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/styles.css")).toExternalForm());
        stage.setTitle("Stockpile");
        stage.setScene(scene);
        stage.show();
    }
}