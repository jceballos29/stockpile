package dev.jceballos.stockpile.inventory.infrastructure.persistence;

import dev.jceballos.stockpile.shared.Money;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Currency;

public class ProductRowMapper {
    record CommonProductData(String name, String description, Money price, int stock) {}

    static CommonProductData extractCommonData(ResultSet resultSet) throws SQLException {
        String name = resultSet.getString("name");
        String description = resultSet.getString("description");
        Currency currency = Currency.getInstance(resultSet.getString("currency"));
        Money price = new Money(new BigDecimal(resultSet.getString("price")), currency);
        int stock = resultSet.getInt("stock");

        return new CommonProductData(name, description, price, stock);
    }
}
