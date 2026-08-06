# Stockpile — Brief y Roadmap Consolidado

**Repositorio:** https://github.com/jceballos29/stockpile
**Estado:** v1 en desarrollo — backend completo, UI en construcción **Tests:** 130 pasando · **Commits:** 41

---

## 1. Qué es Stockpile

Arquitectura de referencia de e-commerce construida en Java puro, con el objetivo explícito de **aprender en
profundidad** el lenguaje y los principios de arquitectura de software aplicándolos a un sistema real, línea por línea,
entendiendo el porqué de cada decisión antes de escribirla.

**Nombre:** describe el corazón técnico del sistema — la reserva y restitución atómica de stock (`Product.reserve()`/
`restore()`).

## 2. Principios aplicados

| Principio                          | Cómo se aplica en el código                                                                                                                         |
|------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------|
| **Clean / Hexagonal Architecture** | Dominio aislado de infraestructura; los puertos los define quien los consume (DIP)                                                                  |
| **Domain-Driven Design**           | Bounded Contexts (`order`, `inventory`), Aggregate Roots, Value Objects auto-validados, Shared Kernel mínimo, Anti-Corruption Layer entre contextos |
| **CQRS**                           | Comandos y consultas separados a nivel de puertos y handlers, en ambos contextos                                                                    |
| **SOLID**                          | SRP, OCP, ISP, DIP justificados explícitamente en cada paso                                                                                         |
| **SoC**                            | El principio "paraguas": Hexagonal separa capas, CQRS separa lectura/escritura, los Bounded Context separan subdominios                             |
| **DRY**                            | `PagedResult<T>` genérico reutilizable, `requireStatus()` centraliza guardas de estado, Named Constructors evitan repetir validación                |
| **KISS**                           | Enum simple en vez de State Pattern (4 estados no lo justifican); una `Connection` en vez de un pool; upsert explícito en vez de ORM                |
| **YAGNI**                          | `OrderLine` como VO inmutable en vez de Entity; Domain Events evaluados y descartados por falta de consumidor real; Unit of Work en vez de Saga     |
| **TDD**                            | Todo el dominio y la aplicación escritos test-primero; Fakes escritos a mano, sin frameworks de mocking                                             |
| **Conventional Commits**           | El historial de git como documentación viva                                                                                                         |

## 3. Stack

Java 25 (Maven) · SQLite vía JDBC puro (sin ORM) · JavaFX 25 + Ikonli (UI) · JUnit 5 + AssertJ · DI manual, sin
frameworks

`groupId: dev.jceballos` · `artifactId: stockpile` · paquete base `dev.jceballos.stockpile`

---

# v1 — Estado actual

## ✅ Completado

### Shared Kernel

- `Money` (BigDecimal + Currency, invariantes de precisión y coherencia de moneda)
- `ProductId`, `PagedResult<T>` genérico, `ProductSalesView`
- `UnitOfWork` (puerto de transacción cross-contexto)

### Contexto `order` — dominio

- `Order` (Aggregate Root): `open()`, `addLine()` con invariante de stock, máquina de estados completa
  (`OPEN → PAID → DISPATCHED`, `OPEN → CANCELLED`), `calculateTotal()`, `createdAt`, `reconstitute()`
- `OrderLine`, `OrderId`, `OrderStatus`
- `InsufficientStockException`, `InvalidOrderStateException`

### Contexto `order` — aplicación

- Puertos: `OrderWriteRepository`, `OrderReadRepository`, `OrderQuery`, `OrderView`/`OrderLineView`,
  `InventoryReadRepository`, `StockReservationPort`
- Comandos: `AddProductToOrder`, `PayOrder`, `DispatchOrder`, `CancelOrder` (+ handlers)
- Consultas: `OrderQueryHandler`

### Contexto `inventory` — dominio y aplicación

- `Product` (Aggregate Root): `register()`, `reserve()`, `restore()`, `updateDetails()`, `description`, `reconstitute()`
- Puertos: `ProductWriteRepository` (con `deleteById`), `ProductReadRepository`, `ProductQuery`, `ProductView`
- Comandos: `RegisterProduct`, `UpdateProduct`, `DeleteProduct`, `ReserveStock`, `RestoreStock` (+ handlers)
- Consultas: `ProductQueryHandler`

### Integración entre contextos (Anti-Corruption Layer)

- `InventoryStockReservationAdapter` (escritura) e `InventoryStockQueryAdapter` (lectura): traducen vocabulario y
  excepciones entre `order` e `inventory`

### Infraestructura

- `SqliteConnectionFactory` (PRAGMA foreign_keys), `SchemaInitializer`, `PersistenceException`
- `SqliteUnitOfWork` (transacción real, probada contra fallo a mitad de camino)
- 4 repositorios SQLite (write/read × order/inventory) + `SqliteSalesReportRepository`

### UI (JavaFX)

- `Main` (Composition Root) + `Launcher`, `MainView` con sidebar + navegación
- `ProductsView`: tabla con búsqueda en vivo, paginación, acciones por fila
- `ProductFormDialog`: crear/editar/eliminar con confirmación
- Paleta Material vía CSS con looked-up colors

## 🔲 Pendiente para cerrar v1

| Prioridad | Pendiente                                                                         |
|-----------|-----------------------------------------------------------------------------------|
| 1         | `OrdersView`: tabla de pedidos con filtro por estado                              |
| 2         | `OrderDetailView`: detalle + acciones (agregar línea, pagar, despachar, cancelar) |
| 3         | `DashboardView`: tarjetas de estadísticas, últimos 10 pedidos, top vendidos       |
| 4         | Gráfico de ventas (JFreeChart — decidido, aún no integrado)                       |
| 5         | `README.md` con capturas y instrucciones de ejecución                             |
| 6         | Higiene de repo: dejar de versionar `.idea/` (`git rm -r --cached .idea`)         |

---

# v2 — E-commerce completo

Requerido por la cátedra. Ver `roadmap-v2.md` para el detalle por semanas. Resumen:

| Fase            | Contenido                                                                | Estado |
|-----------------|--------------------------------------------------------------------------|--------|
| Infraestructura | Docker Compose + Postgres 18, migración de SQLite                        | 🔲     |
| ORM             | Migrar persistencia a Hibernate (pedido explícito)                       | 🔲     |
| Identidad       | Bounded Context `identity`: usuarios, roles ADMIN/USER, login            | 🔲     |
| Catálogo        | Categorías de producto, configuración de moneda del sistema              | 🔲     |
| Carrito         | Exponer `Order(OPEN)` como carrito en UI de usuario (backend ya existe)  | 🔲     |
| Facturación     | Bounded Context `billing`: `Invoice`, métodos de pago, pasarela simulada | 🔲     |
| Documentos      | Factura PDF (PDFBox), carga masiva Excel (POI), exportar métricas        | 🔲     |
| Storage         | Imágenes de producto/perfil (Azurite o MinIO en Docker)                  | 🔲     |

**Descartado explícitamente:** Redis (sin caso de uso real en esta arquitectura; no fue pedido por nombre).

---

## Decisiones de arquitectura registradas

1. **Sin ORM en v1** — decisión pedagógica: entender JDBC, el mapeo objeto-relacional y la rehidratación de agregados a
   mano antes de delegarlos. v2 migra a Hibernate por requisito de la cátedra.
2. **Reserva de stock inmediata y atómica** al agregar línea, no validación optimista sin efecto.
3. **Consistencia fuerte vía Unit of Work** entre `order` e `inventory` (misma base, monolito modular) en vez de
   eventual consistency.
4. **Domain Events: no incluidos** — evaluados contra el criterio "¿consistencia eventual + desacoplamiento real?";
   ningún caso lo requiere hoy. Punto de extensión natural documentado para v2 (notificaciones, auditoría).
5. **`Order.addLine()` conserva su validación en memoria** aunque la reserva real ocurra en `Product` — defensa en
   profundidad.
6. **`CANCELLED` solo desde `OPEN`** — cancelar un pedido pagado implicaría reembolsos, fuera de alcance de v1.
7. **Reportes cruzan contextos a propósito** (`SqliteSalesReportRepository` une `order_lines` + `products`): la
   excepción reconocida en DDD al aislamiento estricto, válida para lectura pura sin mutación.
8. **UI reemplazable sin tocar el core** — demostrado en la práctica: se migró de Swing a JavaFX sin modificar una sola
   línea de dominio, aplicación o persistencia, con los 130 tests intactos.