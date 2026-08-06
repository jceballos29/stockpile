# Stockpile

> Arquitectura de referencia de e-commerce en Java puro — Hexagonal + DDD + CQRS + TDD, sin frameworks pesados.

[![Java](https://img.shields.io/badge/Java-25-orange)]()
[![JavaFX](https://img.shields.io/badge/JavaFX-25-blue)]()
[![Tests](https://img.shields.io/badge/tests-130%20passing-brightgreen)]()
[![License](https://img.shields.io/badge/license-MIT-lightgrey)]()

Stockpile es un sistema de gestión de catálogo y pedidos construido como **proyecto de aprendizaje profundo**: cada
clase, cada decisión de diseño y cada línea de SQL se escribieron a mano, entendiendo el porqué antes del cómo. El
nombre viene de su corazón técnico: la reserva y restitución atómica de stock.

---

## Por qué este proyecto es distinto

No es un CRUD con capas. Es una implementación disciplinada de arquitectura hexagonal donde:

- **El dominio no conoce la infraestructura.** `Order` y `Product` no saben que existe SQLite, ni JavaFX, ni siquiera el
  concepto de "guardar".
- **La UI es genuinamente reemplazable.** Se migró de Swing a JavaFX completo sin modificar una sola línea de dominio,
  aplicación o persistencia — los 130 tests siguieron pasando sin tocarse. No es una promesa teórica: está en el
  historial de git.
- **Sin ORM, a propósito.** El mapeo objeto-relacional y la rehidratación de agregados están escritos a mano con JDBC
  puro, para entender qué hace un ORM por debajo antes de delegárselo.
- **TDD real, no cosmético.** Los tests se escribieron primero, y hay criterio explícito sobre cuándo *no* aplicar TDD
  (un enum sin comportamiento no lleva test).

## Arquitectura

```
dev.jceballos.stockpile
├── shared/                    Shared Kernel: Money, ProductId, PagedResult<T>
│   └── application/port/      UnitOfWork (transacción cross-contexto)
│
├── order/                     Bounded Context: Pedidos
│   ├── domain/                Order (Aggregate Root), OrderLine, OrderStatus
│   ├── application/
│   │   ├── port/              Repositorios + puertos hacia inventory
│   │   ├── command/           AddProductToOrder, Pay, Dispatch, Cancel
│   │   └── query/             OrderQueryHandler
│   └── infrastructure/
│       ├── persistence/       Adaptadores SQLite
│       └── integration/       Anti-Corruption Layer hacia inventory
│
├── inventory/                 Bounded Context: Catálogo e Inventario
│   ├── domain/                Product (Aggregate Root)
│   ├── application/           Register, Update, Delete, ReserveStock, RestoreStock
│   └── infrastructure/        Adaptadores SQLite
│
└── infrastructure/
    ├── persistence/           Conexión, esquema, UnitOfWork, reportes
    └── ui/                    JavaFX: Composition Root y vistas
```

**La regla que sostiene todo:** las dependencias apuntan hacia adentro. `infrastructure → application → domain`, nunca
al revés. El único lugar del proyecto que instancia clases concretas es el Composition Root (`Main.java`).

### Los dos Bounded Context

`order` e `inventory` no se conocen directamente. `order` define los puertos que necesita (`InventoryReadRepository`,
`StockReservationPort`), e implementaciones en `infrastructure/integration` actúan como **Anti-Corruption Layer**,
traduciendo vocabulario y excepciones entre ambos — incluyendo dos clases homónimas `InsufficientStockException`, una en
cada contexto, deliberadamente independientes.

## Reglas de negocio implementadas

- Un pedido acumula líneas mientras está `OPEN`; agregar el mismo producto dos veces suma cantidad en una sola línea.
- **La reserva de stock es real y atómica**, no una validación optimista: al agregar una línea, el stock se descuenta de
  verdad, dentro de una transacción que abarca ambos contextos (Unit of Work).
- Máquina de estados: `OPEN → PAID → DISPATCHED`, y `OPEN → CANCELLED` (cancelar restituye el stock reservado de cada
  línea).
- No se puede pagar un pedido vacío. Sí se puede cancelar uno vacío.
- El dinero usa `BigDecimal` y valida coherencia de moneda — nunca `double`, nunca sumas entre monedas distintas.

## Cómo ejecutarlo

**Requisitos:** JDK 25+ y Maven 3.9+

```bash
git clone https://github.com/jceballos29/stockpile.git
cd stockpile

mvn test          # 130 tests
mvn javafx:run    # levanta la aplicación de escritorio
```

La base de datos SQLite (`stockpile.db`) se crea automáticamente en la raíz al primer arranque.

## Estado actual

**Backend: completo.** Ambos contextos, con dominio, aplicación, persistencia SQLite y 130 tests.

**UI: en construcción.**

- [x] Navegación con sidebar
- [x] Catálogo: tabla con búsqueda en vivo, paginación, alta/edición/eliminación
- [ ] Pedidos: listado con filtro por estado
- [ ] Detalle de pedido con acciones
- [ ] Dashboard con estadísticas y gráficos

Ver [`docs/brief.md`](docs/brief.md) para el estado detallado y las decisiones de arquitectura registradas, y [
`docs/roadmap-v2.md`](docs/roadmap-v2.md) para la evolución planificada (Postgres, Hibernate, autenticación,
facturación).

## Stack

|                           |                                                                       |
|---------------------------|-----------------------------------------------------------------------|
| Lenguaje                  | Java 25                                                               |
| Build                     | Maven                                                                 |
| Persistencia              | SQLite vía JDBC puro (sin ORM)                                        |
| UI                        | JavaFX 25 + Ikonli (iconos)                                           |
| Tests                     | JUnit 5 + AssertJ (sin frameworks de mocking — Fakes escritos a mano) |
| Inyección de dependencias | Manual, sin framework                                                 |

## Licencia

MIT — ver [LICENSE](LICENSE).