# Roadmap v2 — Stockpile: E-commerce Completo

**Contexto:** este documento extiende `brief.md`/`plan.md` (v1) con el alcance completo solicitado por la cátedra. v1
(arquitectura hexagonal, DDD, CQRS, TDD, `order`+`inventory`, SQLite, Swing) queda como **base funcional** sobre la que
se construye todo lo demás -- no se descarta nada de lo hecho.

**Fecha límite:** fin de mes. Este plan está diseñado para llegar con un sistema funcional de punta a punta, priorizando
lo que hace que el proyecto se sienta y funcione como un e-commerce completo, dejando el pulido opcional para el final
(primero candidato a recortar si el tiempo aprieta).

---

## Semana 1 — Infraestructura nueva + Identidad (la base de todo lo demás)

**Por qué primero:** "el dashboard es solo para admin" no se puede construir sin que exista, antes, un sistema de
usuarios y roles. Todo lo demás depende de esto.

| Día | Contenido                                                                                                                                                                           |
|-----|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| 1-2 | Docker Compose: Postgres 18. Migrar la conexión de SQLite a Postgres. ORM confirmado: **Hibernate** (pedido explícito de la cátedra)                                                |
| 3-5 | Bounded Context `identity`: `User` (email, password hasheado, rol), `RegisterUserCommand`/`LoginCommand` + handlers, sesión simple en memoria (no hace falta JWT para este alcance) |
| 6-7 | Guard de autorización: solo `ADMIN` accede al dashboard existente. Pantalla de login como primera ventana de la app                                                                 |

## Semana 2 — Catálogo ampliado + Carrito expuesto

| Día   | Contenido                                                                                                                      |
|-------|--------------------------------------------------------------------------------------------------------------------------------|
| 8-9   | `Category`: nuevo concepto simple en `inventory` (id, nombre), `Product` referencia una categoría                              |
| 10-11 | Configuración de entorno: moneda por defecto del sistema (una tabla `settings` simple)                                         |
| 12-14 | Exponer `Order(OPEN)` como "carrito" en la UI de usuario (ya existe casi todo el backend -- es mayormente trabajo de interfaz) |

## Semana 3 — Facturación y pagos simulados

| Día   | Contenido                                                                                                                                                                                              |
|-------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| 15-17 | Bounded Context `billing`: `Invoice`, `PaymentMethod` (enum: tarjeta/transferencia/efectivo), `SimulatedPaymentGateway` (puerto + adaptador que siempre "aprueba", con posibilidad de simular rechazo) |
| 18-21 | Generación de factura PDF (Apache PDFBox) a partir de un pedido pagado                                                                                                                                 |

## Semana 4 (o antes, si sobra tiempo) — Extras, en orden de recorte si falta tiempo

| Prioridad           | Contenido                                                         |
|---------------------|-------------------------------------------------------------------|
| 1 (recortar último) | Imágenes de producto/perfil con storage (Azurite/MinIO en Docker) |
| 2                   | Carga masiva de productos vía Excel (Apache POI)                  |
| 3                   | Exportar métricas de ventas (reusa el puerto de reportes de v1)   |

---

## Decisiones confirmadas

1. **ORM: Hibernate** -- pedido explícito de la cátedra, no negociable. Se usa recién en la migración de la Semana 1 de
   v2; v1 (Stockpile actual) sigue con JDBC puro tal como está, sin tocar.
2. **Redis: descartado.** No fue pedido explícitamente y no tiene un caso de uso real en esta arquitectura (app de
   escritorio, un solo backend).