package dev.jceballos.stockpile.infrastructure.ui;

/**
 * Punto de entrada real del jar. Existe separado de {@code Main} a
 * propósito: JavaFX aplica una restricción especial cuando la clase
 * que extiende {@code Application} se ejecuta directamente sobre el
 * classpath plano (sin module-path configurado) -- delegar a traves de
 * una clase que NO extiende Application evita ese problema por completo.
 */
public class Launcher {

    static void main(String[] args) {
        Main.main(args);
    }
}