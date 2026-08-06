package dev.jceballos.stockpile.shared.application.port;

import dev.jceballos.stockpile.shared.ProductSalesView;

import java.util.List;

/**
 * Puerto de reportes: cruza order_lines (order) y products (inventory)
 * a propósito -- lectura pura, sin mutación. Los reportes/analytics son
 * la excepción reconocida en DDD al aislamiento estricto entre
 * contextos que sostenemos en el resto del proyecto.
 */
public interface SalesReportRepository {

    List<ProductSalesView> topSellingProducts(int limit);
}