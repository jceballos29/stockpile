package dev.jceballos.stockpile.infrastructure.ui;

import dev.jceballos.stockpile.inventory.application.command.*;
import dev.jceballos.stockpile.inventory.application.port.ProductView;

import dev.jceballos.stockpile.shared.Money;
import dev.jceballos.stockpile.shared.ProductId;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;

public class ProductFormDialog {

    private static final String[] SUPPORTED_CURRENCIES = {"USD", "EUR", "COP", "ARS", "MXN"};

    private final RegisterProductCommandHandler registerProductCommandHandler;
    private final UpdateProductCommandHandler updateProductCommandHandler;
    private final DeleteProductCommandHandler deleteProductCommandHandler;
    private final Optional<ProductView> existingProduct;

    private final TextField skuField = new TextField();
    private final TextField nameField = new TextField();
    private final TextArea descriptionArea = new TextArea();
    private final TextField priceField = new TextField();
    private final ComboBox<String> currencyCombo = new ComboBox<>();
    private final TextField stockField = new TextField();
    private final Label errorLabel = new Label();

    private Stage dialogStage;
    private boolean saved = false;

    public ProductFormDialog(RegisterProductCommandHandler registerProductCommandHandler,
                             UpdateProductCommandHandler updateProductCommandHandler,
                             DeleteProductCommandHandler deleteProductCommandHandler,
                             Optional<ProductView> existingProduct) {
        this.registerProductCommandHandler = Objects.requireNonNull(registerProductCommandHandler);
        this.updateProductCommandHandler = Objects.requireNonNull(updateProductCommandHandler);
        this.deleteProductCommandHandler = Objects.requireNonNull(deleteProductCommandHandler);
        this.existingProduct = Objects.requireNonNull(existingProduct);
    }

    /**
     * Muestra el díalogo y bloquea hasta que se cierra.
     *
     * @param owner la ventana dueña (para centrar y bloquear correctamente)
     * @return true si se guardo un producto, false si se cancelo
     */
    public boolean showAndWait(Window owner) {
        boolean editMode = existingProduct.isPresent();

        dialogStage = new Stage();
        dialogStage.initModality(Modality.WINDOW_MODAL);
        if (owner != null) {
            dialogStage.initOwner(owner);
        }
        dialogStage.setTitle(editMode ? "Editar producto" : "Registrar producto");

        GridPane form = buildForm(editMode);
        if (editMode) {
            prefillFields(existingProduct.get());
        } else {
            currencyCombo.setValue("USD");
        }

        Scene scene = new Scene(form);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        dialogStage.setScene(scene);
        dialogStage.showAndWait();

        return saved;
    }

    private GridPane buildForm(boolean editMode) {
        GridPane form = new GridPane();
        form.setHgap(12);
        form.setVgap(14);
        form.setPadding(new Insets(24));

        ColumnConstraints labelColumn = new ColumnConstraints();
        labelColumn.setPrefWidth(110);
        ColumnConstraints fieldColumn = new ColumnConstraints();
        fieldColumn.setHgrow(Priority.ALWAYS);
        form.getColumnConstraints().addAll(labelColumn, fieldColumn);

        int row = 0;

        form.add(new Label("SKU"), 0, row);
        skuField.setDisable(editMode);
        form.add(skuField, 1, row++);

        form.add(new Label("Nombre"), 0, row);
        form.add(nameField, 1, row++);

        form.add(new Label("Descripción"), 0, row);
        descriptionArea.setPrefRowCount(3);
        descriptionArea.setWrapText(true);
        form.add(descriptionArea, 1, row++);

        form.add(new Label("Precio"), 0, row);
        form.add(priceField, 1, row++);

        form.add(new Label("Moneda"), 0, row);
        currencyCombo.getItems().addAll(SUPPORTED_CURRENCIES);
        form.add(currencyCombo, 1, row++);

        if (!editMode) {
            form.add(new Label("Stock inicial"), 0, row);
            form.add(stockField, 1, row++);
        }

        errorLabel.setStyle("-fx-text-fill: -danger;");
        errorLabel.setWrapText(true);
        form.add(errorLabel, 1, row++);

        Button saveButton = new Button(editMode ? "Guardar cambios" : "Registrar");
        Button cancelButton = new Button("Cancelar");
        saveButton.getStyleClass().add("primary-button");
        cancelButton.getStyleClass().add("button");
        saveButton.setOnAction(e -> onSave(editMode));
        cancelButton.setOnAction(e -> dialogStage.close());

        HBox buttonBar;
        if (editMode) {
            Button deleteButton = new Button("Eliminar");
            deleteButton.getStyleClass().add("danger-button");
            deleteButton.setOnAction(e -> onDelete());

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            buttonBar = new HBox(10, deleteButton, spacer, cancelButton, saveButton);
        } else {
            buttonBar = new HBox(10, cancelButton, saveButton);
        }
        buttonBar.setAlignment(Pos.CENTER_RIGHT);
        form.add(buttonBar, 1, row);

        return form;
    }

    private void prefillFields(ProductView product) {
        skuField.setText(product.productId().value());
        nameField.setText(product.name());
        descriptionArea.setText(product.description());
        priceField.setText(product.price().amount().toPlainString());
        currencyCombo.setValue(product.price().currency().getCurrencyCode());
    }

    private void onSave(boolean editMode) {
        try {
            String name = nameField.getText();
            String description = descriptionArea.getText();
            BigDecimal price = new BigDecimal(priceField.getText().trim());
            Money money = Money.of(price, currencyCombo.getValue());

            if (editMode) {
                updateProductCommandHandler.handle(new UpdateProductCommand(
                        existingProduct.get().productId(), name, description, money));
            } else {
                ProductId productId = new ProductId(skuField.getText());
                int stock = Integer.parseInt(stockField.getText().trim());
                registerProductCommandHandler.handle(new RegisterProductCommand(
                        productId, name, description, money, stock));
            }

            saved = true;
            dialogStage.close();
        } catch (NumberFormatException e) {
            errorLabel.setText("Precio y stock deben ser valores numéricos válidos.");
        } catch (RuntimeException e) {
            errorLabel.setText(e.getMessage());
        }
    }

    private void onDelete() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "¿Eliminar el producto \"" + existingProduct.get().name() + "\"? Esta acción no se puede deshacer.",
                ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Confirmar eliminación");
        confirm.initOwner(dialogStage);

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.YES) {
            deleteProductCommandHandler.handle(new DeleteProductCommand(existingProduct.get().productId()));
            saved = true;
            dialogStage.close();
        }
    }

}
