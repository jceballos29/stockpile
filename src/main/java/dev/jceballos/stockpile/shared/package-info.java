/**
 * Shared Kernel: Value Objects mínimos y deliberadamente estables,
 * compartidos entre los Bounded Context {@code order} e {@code inventory}.
 * <p>
 * Solo pertenece aca lo que ambos contextos necesitan como vocabulario
 * común (ver {@code brief.md}, secciones 2 y 5). No debería crecer más
 * allá de eso: cualquier tipo con reglas de negocio propias de un solo
 * contexto pertenece al paquete {@code domain} de ese contexto, no aca.
 */
package dev.jceballos.stockpile.shared;