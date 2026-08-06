package dev.jceballos.stockpile.infrastructure.ui;

import dev.jceballos.stockpile.inventory.application.command.DeleteProductCommandHandler;
import dev.jceballos.stockpile.inventory.application.command.RegisterProductCommandHandler;
import dev.jceballos.stockpile.inventory.application.command.UpdateProductCommandHandler;
import dev.jceballos.stockpile.inventory.application.port.ProductQuery;
import dev.jceballos.stockpile.inventory.application.port.ProductView;
import dev.jceballos.stockpile.inventory.application.query.ProductQueryHandler;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

import java.util.Objects;
import java.util.Optional;

public class ProductsView {

    private final RegisterProductCommandHandler registerProductCommandHandler;
    private final UpdateProductCommandHandler updateProductCommandHandler;
    private final DeleteProductCommandHandler deleteProductCommandHandler;
    private final ProductQueryHandler productQueryHandler;

    private final ObservableList<ProductView> products = FXCollections.observableArrayList();
    private final BorderPane root = new BorderPane();

    public ProductsView(RegisterProductCommandHandler registerProductCommandHandler,
                        UpdateProductCommandHandler updateProductCommandHandler,
                        DeleteProductCommandHandler deleteProductCommandHandler,
                        ProductQueryHandler productQueryHandler) {
        this.registerProductCommandHandler = Objects.requireNonNull(registerProductCommandHandler);
        this.updateProductCommandHandler = Objects.requireNonNull(updateProductCommandHandler);
        this.deleteProductCommandHandler = Objects.requireNonNull(deleteProductCommandHandler);
        this.productQueryHandler = Objects.requireNonNull(productQueryHandler);

        root.setPadding(new Insets(24));
        root.setTop(buildHeader());
        root.setCenter(buildTable());
        reload();
    }

    private HBox buildHeader() {
        Label title = new Label("Productos");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");

        Button addButton = new Button("+ Agregar Producto");
        addButton.getStyleClass().add("primary-button");
        addButton.setOnAction(e -> openForm(Optional.empty()));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox header = new HBox(title, spacer, addButton);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(0, 0, 16, 0));
        return header;
    }

    private TableView<ProductView> buildTable() {
        TableView<ProductView> table = new TableView<>(products);
        table.getStyleClass().add("card");
        table.setPlaceholder(new Label("No hay productos registrados todavía."));

        TableColumn<ProductView, String> skuColumn = new TableColumn<>("SKU");
        skuColumn.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().productId().value()));

        TableColumn<ProductView, String> nameColumn = new TableColumn<>("Nombre");
        nameColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().name()));

        TableColumn<ProductView, String> priceColumn = new TableColumn<>("Precio");
        priceColumn.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().price().amount() + " " + data.getValue().price().currency().getCurrencyCode()));

        TableColumn<ProductView, Number> stockColumn = new TableColumn<>("Stock");
        stockColumn.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().stock()));

        table.getColumns().addAll(skuColumn, nameColumn, priceColumn, stockColumn);

        table.setRowFactory(tv -> {
            TableRow<ProductView> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty()) {
                    openForm(Optional.of(row.getItem()));
                }
            });
            return row;
        });

        return table;
    }

    private void openForm(Optional<ProductView> existingProduct) {
        ProductFormDialog dialog = new ProductFormDialog(
                registerProductCommandHandler, updateProductCommandHandler,
                deleteProductCommandHandler, existingProduct);
        boolean changed = dialog.showAndWait(root.getScene().getWindow());
        if (changed) {
            reload();
        }
    }

    private void reload() {
        products.setAll(productQueryHandler.list(ProductQuery.firstPage(50)).items());
    }

    public BorderPane getRoot() {
        return root;
    }
}
