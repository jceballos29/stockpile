package dev.jceballos.stockpile.infrastructure.ui;

import dev.jceballos.stockpile.inventory.application.command.DeleteProductCommand;
import dev.jceballos.stockpile.inventory.application.command.DeleteProductCommandHandler;
import dev.jceballos.stockpile.inventory.application.command.RegisterProductCommandHandler;
import dev.jceballos.stockpile.inventory.application.command.UpdateProductCommandHandler;
import dev.jceballos.stockpile.inventory.application.port.ProductQuery;
import dev.jceballos.stockpile.inventory.application.port.ProductView;
import dev.jceballos.stockpile.inventory.application.query.ProductQueryHandler;
import dev.jceballos.stockpile.shared.PagedResult;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.Objects;
import java.util.Optional;

import javafx.scene.paint.Color;
import org.kordamp.ikonli.javafx.FontIcon;

public class ProductsView {

    private static final int PAGE_SIZE = 10;

    private final RegisterProductCommandHandler registerProductCommandHandler;
    private final UpdateProductCommandHandler updateProductCommandHandler;
    private final DeleteProductCommandHandler deleteProductCommandHandler;
    private final ProductQueryHandler productQueryHandler;

    private final ObservableList<ProductView> products = FXCollections.observableArrayList();
    private final BorderPane root = new BorderPane();
    private final TextField searchField = new TextField();
    private final Label summaryLabel = new Label();
    private final Label pageLabel = new Label();
    private final Button previousButton = new Button("< Anterior");
    private final Button nextButton = new Button("Siguiente >");

    private int currentPage = 0;
    private PagedResult<ProductView> currentResult;

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
        root.setBottom(buildFooter());
        reload();
    }

    private VBox buildHeader() {
        Label title = new Label("Productos");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");

        searchField.setPromptText("Buscar producto...");
        searchField.getStyleClass().add("search-field");
        searchField.setPrefWidth(280);
        searchField.textProperty().addListener((obs, oldValue, newValue) -> {
            currentPage = 0;
            reload();
        });

        Button addButton = new Button("+ Agregar Producto");
        addButton.getStyleClass().add("primary-button");
        addButton.setOnAction(e -> openForm(Optional.empty()));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox toolbar = new HBox(12, searchField, spacer, addButton);
        toolbar.setAlignment(Pos.CENTER_LEFT);

        summaryLabel.setStyle("-fx-text-fill: -text-secondary;");

        VBox header = new VBox(8, title, toolbar, summaryLabel);
        header.setPadding(new Insets(0, 0, 16, 0));
        return header;
    }

    private TableView<ProductView> buildTable() {
        TableView<ProductView> table = new TableView<>(products);
        table.getStyleClass().add("table");
        table.setPlaceholder(new Label("No hay productos que coincidan con la búsqueda."));

        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        TableColumn<ProductView, String> skuColumn = new TableColumn<>("SKU");
        skuColumn.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().productId().value()));
        skuColumn.setStyle("-fx-alignment: CENTER;");

        TableColumn<ProductView, String> nameColumn = new TableColumn<>("Nombre");
        nameColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().name()));
        nameColumn.setPrefWidth(220);
        nameColumn.setStyle("-fx-alignment: CENTER;");

        TableColumn<ProductView, String> priceColumn = new TableColumn<>("Precio");
        priceColumn.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().price().amount() + " " + data.getValue().price().currency().getCurrencyCode()));
        priceColumn.setStyle("-fx-alignment: CENTER;");

        TableColumn<ProductView, Number> stockColumn = new TableColumn<>("Stock");
        stockColumn.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().stock()));
        stockColumn.setStyle("-fx-alignment: CENTER;");

        // Extraemos la creación de la columna
        TableColumn<ProductView, Void> actionsColumn = buildActionsColumn();

        // En lugar de addAll, usamos add individualmente para evitar el warning del vararg
        table.getColumns().add(skuColumn);
        table.getColumns().add(nameColumn);
        table.getColumns().add(priceColumn);
        table.getColumns().add(stockColumn);
        table.getColumns().add(actionsColumn);

        return table;
    }

    private HBox buildFooter() {
        previousButton.setOnAction(e -> {
            if (currentPage > 0) {
                currentPage--;
                reload();
            }
        });
        nextButton.setOnAction(e -> {
            if (currentResult != null && currentResult.hasNext()) {
                currentPage++;
                reload();
            }
        });

        HBox footer = new HBox(12, previousButton, pageLabel, nextButton);
        footer.setAlignment(Pos.CENTER);
        footer.setPadding(new Insets(16, 0, 0, 0));
        return footer;
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

    private void confirmAndDelete(ProductView product) {
        boolean confirmed = Dialogs.confirm(root.getScene().getWindow(), "Confirmar eliminación",
                "¿Eliminar el producto \"" + product.name() + "\"? Esta acción no se puede deshacer.");
        if (confirmed) {
            deleteProductCommandHandler.handle(new DeleteProductCommand(product.productId()));
            reload();
        }
    }

    private void reload() {
        String search = searchField.getText();
        ProductQuery query = (search == null || search.isBlank())
                ? new ProductQuery(Optional.empty(), currentPage, PAGE_SIZE)
                : ProductQuery.byNameContains(search.trim(), currentPage, PAGE_SIZE);

        currentResult = productQueryHandler.list(query);
        products.setAll(currentResult.items());

        summaryLabel.setText(currentResult.totalElements() + " producto(s) encontrado(s)");
        int totalPages = Math.max(currentResult.totalPages(), 1);
        pageLabel.setText("Página " + (currentPage + 1) + " de " + totalPages);
        previousButton.setDisable(currentPage == 0);
        nextButton.setDisable(!currentResult.hasNext());
    }

    private TableColumn<ProductView, Void> buildActionsColumn() {
        TableColumn<ProductView, Void> actionsColumn = new TableColumn<>("");

        actionsColumn.setStyle("-fx-alignment: CENTER-RIGHT;");

        actionsColumn.setCellFactory(col -> new TableCell<>() {

            private final FontIcon editIcon = new FontIcon("fas-pen");
            private final FontIcon deleteIcon = new FontIcon("fas-trash");

            private final Button editButton = new Button("", editIcon);
            private final Button deleteButton = new Button("", deleteIcon);
            private final HBox box = new HBox(6, editButton, deleteButton);

            {
                box.setAlignment(Pos.CENTER_RIGHT);

                editIcon.setIconSize(12);
                editIcon.setIconColor(Color.valueOf("#333333"));
                editButton.getStyleClass().add("icon-button");
                editButton.setTooltip(new Tooltip("Editar producto"));
                editButton.setOnAction(e -> openForm(Optional.ofNullable(currentRowItem())));

                deleteIcon.setIconSize(12);
                deleteIcon.setIconColor(Color.valueOf("#333333"));
                deleteButton.getStyleClass().add("icon-button");
                deleteButton.setTooltip(new Tooltip("Eliminar producto"));
                deleteButton.setOnMouseEntered(e -> deleteIcon.setIconColor(Color.valueOf("#d32f2f"))); // Rojo
                deleteButton.setOnMouseExited(e -> deleteIcon.setIconColor(Color.valueOf("#333333")));
                deleteButton.setOnAction(e -> confirmAndDelete(currentRowItem()));
            }

            private ProductView currentRowItem() {
                return getTableView().getItems().get(getIndex());
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    setGraphic(box);
                }
            }
        });
        return actionsColumn;
    }

    public BorderPane getRoot() {
        return root;
    }
}
