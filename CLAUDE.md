# Ecommerce Microservices — CLAUDE.md

## Stack

- Java 21, Spring Boot 4.0.3, Spring Cloud 2025.1.0
- PostgreSQL (orders, payments, products)
- MongoDB (customers, cart, notifications)
- Kafka (comunicación asíncrona Payment → Notification)
- Redis (rate limiting en API Gateway)
- Feign + Resilience4j (comunicación síncrona entre microservicios con circuit breaker)
- Eureka (service discovery), Config Server (configuración centralizada)
- Zipkin + Micrometer Tracing, Prometheus + Grafana
- Lombok, Swagger/OpenAPI (springdoc)

---

## Microservicios

| Servicio | Puerto | Base de datos | Comunicación saliente |
|---|---|---|---|
| customer-microservice | 8091 | MongoDB | — |
| product-microservice | 8092 | PostgreSQL | — |
| cart-microservice | 8093 | MongoDB | Feign → customer, product |
| order-microservice | 8094 | PostgreSQL | Feign → customer, product, cart, payment |
| payment-microservice | 8095 | PostgreSQL | Kafka → notification |
| notification-microservice | 8096 | MongoDB | Feign → customer |
| api-gateway | 8222 | — | Redis, Eureka |
| discovery-server | 8761 | — | — |
| config-server | 8888 | — | — |

---

## Estructura de paquetes

Cada microservicio agrupa las clases **por dominio**, no por capa:

```
com.mmenendez.microservices.{servicio}
├── {dominio}/           → entidad, controller, service, repository, mapper, request/response
│   ├── Entidad.java
│   ├── EntidadController.java
│   ├── EntidadService.java
│   ├── EntidadRepository.java
│   ├── EntidadMapper.java
│   ├── EntidadRequest.java   (record)
│   └── EntidadResponse.java  (record)
├── {servicioExterno}/   → Feign client, fallback, response DTO del servicio externo
│   ├── XClient.java
│   ├── XClientFallback.java
│   └── XResponse.java
├── exceptions/
│   ├── XException.java
│   └── XExceptionHandler.java
└── kafka/               (si el servicio consume Kafka)
    ├── XEvent.java
    └── XConsumer.java
```

---

## Reglas por capa

### Controller
- Recibe request, delega al service, devuelve `ResponseEntity`
- No tiene lógica de negocio
- Extrae headers `X-Customer-Id` y `X-Customer-Role` con `@RequestHeader` cuando necesita autorización
- Siempre tiene `@Tag`, `@Operation` y `@ApiResponse` de Swagger

```java
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Tag(name = "Orders", description = "Order management endpoints")
public class OrderController {

    private final OrderService orderService;

    @GetMapping("/{id}")
    @Operation(summary = "Get order by ID")
    @ApiResponse(responseCode = "200", description = "Order found")
    @ApiResponse(responseCode = "400", description = "Order not found")
    public ResponseEntity<OrderResponse> getOrderById(
            @PathVariable Long id,
            @RequestHeader("X-Customer-Id") String requestingCustomerId,
            @RequestHeader("X-Customer-Role") String requestingRole) {
        return ResponseEntity.ok(orderService.getOrderById(id, requestingCustomerId, requestingRole));
    }
}
```

### Service
- Clase directa con `@Service` y `@RequiredArgsConstructor`. **No hay interfaz + implementación.**
- Agregar `@Slf4j` siempre que el servicio loguee
- `@Transactional` en métodos que modifican múltiples entidades
- Nunca devuelve entidades, siempre convierte con el mapper

```java
@Service
@RequiredArgsConstructor
@Slf4j
public class CartItemService {
    private final CartRepository cartRepository;
    private final CustomerClient customerClient;
    // ...
}
```

### Repository
- MongoDB: `extends MongoRepository<Entidad, String>`
- PostgreSQL: `extends JpaRepository<Entidad, Long>` o `JpaRepository<Entidad, Integer>`
- Métodos simples sin comentario; queries con `@Query` sí necesitan comentario

### Mapper
- Clase `@Service` con conversión manual. **No se usa MapStruct.**
- Un mapper por dominio: `CartMapper`, `OrderMapper`, etc.

```java
@Service
public class CartMapper {
    public CartResponse toCartResponse(Cart cart) {
        // conversión manual
    }
}
```

---

## DTOs — records

Todos los DTOs son Java records. No se usa `@Data` ni clases convencionales para DTOs.

```java
// Request
public record CartItemRequest(
    @NotNull(message = "Variant ID is required") Integer variantId,
    @NotNull(message = "Quantity is required") @Min(1) Integer quantity
) {}

// Response
public record CartResponse(
    String id,
    String customerId,
    List<CartItemResponse> cartItems
) {}
```

---

## Entidades

### MongoDB
`@Data @Builder @AllArgsConstructor @NoArgsConstructor @Document`
El `@Id` es `String`.

```java
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document
public class Cart {
    @Id
    private String id;
    private String customerId;
    private List<CartItem> items;
}
```

### PostgreSQL / JPA
`@Getter @Setter @Builder @AllArgsConstructor @NoArgsConstructor @Entity @Table(name = "...")`
No se usa `@Data` en entidades JPA (conflicto con lazy loading).
Nombre de tabla explícito en snake_case.

```java
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<OrderItem> items;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.status == null) this.status = OrderStatus.PENDING;
    }
}
```

---

## Feign Clients

Cada cliente Feign vive en un subpaquete con el nombre del servicio externo (ej: `cart/customer/`, `order/product/`).
Siempre tienen fallback. El nombre en `@FeignClient` es el nombre de Eureka en mayúsculas.

```java
@FeignClient(name = "CUSTOMER-MICROSERVICE", fallback = CustomerClientFallback.class)
public interface CustomerClient {
    @GetMapping("/api/v1/customers/{id}")
    Optional<CustomerResponse> getCustomerById(@PathVariable("id") String customerId);
}
```

### Fallbacks
`@Component` que implementa el cliente. Retornan `Optional.empty()` para que el service haga `.orElseThrow()`.

```java
@Component
public class CustomerClientFallback implements CustomerClient {
    @Override
    public Optional<CustomerResponse> getCustomerById(String customerId) {
        return Optional.empty();
    }
}
```

**Criterio para el fallback:**
- Llamada crítica para la operación → fallback retorna `Optional.empty()`, el service lanza excepción
- Llamada no crítica (ej: limpiar carrito después del pago, restock en recovery) → fallback solo loguea WARN, no falla

---

## Excepciones

### Excepción de dominio
```java
@Data
@EqualsAndHashCode(callSuper = true)
public class CartException extends RuntimeException {
    private final String message;
}
```

### Handler por microservicio
Extiende `GlobalExceptionHandler` del módulo `common-exceptions`.
Anotado con `@RestControllerAdvice(basePackages = "...")` y `@Primary`.

```java
@RestControllerAdvice(basePackages = "com.mmenendez.microservices.cart_microservice")
@Primary
@Slf4j
public class CartExceptionHandler extends GlobalExceptionHandler {

    @ExceptionHandler(CartException.class)
    public ResponseEntity<ErrorResponse> handle(CartException ex) {
        var errors = new HashMap<String, String>();
        errors.put("cart", ex.getMessage());
        log.warn("Cart error: {}", ex.toString());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(errors));
    }

    @ExceptionHandler(FeignException.class)
    public ResponseEntity<ErrorResponse> handleFeignException(FeignException ex) {
        var errors = new HashMap<String, String>();
        errors.put("Error communicating with microservice", ex.getMessage());
        log.warn("Error communicating with microservice: {}", ex.toString());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(errors));
    }
}
```

`GlobalExceptionHandler` (en `common-exceptions`) maneja `MethodArgumentNotValidException` y `Exception` genérica.

---

## Autorización

El API Gateway valida el JWT y propaga dos headers a todos los microservicios:
- `X-Customer-Id` → ID del usuario autenticado
- `X-Customer-Role` → rol (`CUSTOMER` o `ADMIN`)

Los microservicios **no validan el JWT directamente**. Leen estos headers y hacen el control de acceso a nivel de negocio en el service:

```java
// Patrón en OrderService
private void requireSelfOrAdmin(String resourceCustomerId, String requestingCustomerId, String requestingRole) {
    if (!ROLE_ADMIN.equals(requestingRole) && !resourceCustomerId.equals(requestingCustomerId)) {
        throw new UnauthorizedException("Access denied to this resource");
    }
}
```

Endpoints que no requieren auth (register, login) son públicos solo en customer-microservice.

---

## Kafka

**Productor** → `@Service` con `KafkaTemplate<String, XEvent>`:

```java
@Service
@RequiredArgsConstructor
@Slf4j
public class OrderConfirmedProducer {
    static final String TOPIC = "order-confirmed";
    private final KafkaTemplate<String, OrderConfirmedEvent> kafkaTemplate;

    public void publish(OrderConfirmedEvent event) {
        kafkaTemplate.send(TOPIC, event)
            .whenComplete((result, ex) -> {
                if (ex != null) log.error("...");
                else log.info("...");
            });
    }
}
```

**Evento** → record Java. Cada microservicio tiene su propia copia (no hay módulo compartido para eventos):

```java
public record OrderConfirmedEvent(Long orderId, String customerId, Double amount, String paymentMethod) {}
```

**Consumidor** → `@Service` con `@KafkaListener`:

```java
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationConsumer {
    private final NotificationService notificationService;

    @KafkaListener(topics = OrderConfirmedEvent.TOPIC, groupId = "notification-group")
    public void consume(OrderConfirmedEvent event) {
        log.info("Received OrderConfirmedEvent for order {}", event.orderId());
        notificationService.sendOrderConfirmation(...);
    }
}
```

---

## Configuración

Toda la configuración de los microservicios está en el Config Server:
`config-server/src/main/resources/config/{nombre-servicio}.yml`

Las variables sensibles van en el `.env` de la raíz y se inyectan en docker-compose como variables de entorno.
**Nunca hardcodear credenciales, URLs de base de datos ni el JWT secret en el código.**

### Patrón de config yml
```yaml
server:
  port: 8093

spring:
  data:
    mongodb:
      uri: ${SPRING_MONGODB_URI}
  cloud:
    openfeign:
      circuitbreaker:
        enabled: true

logging:
  pattern:
    console: '%d{yyyy-MM-dd HH:mm:ss} [%X{traceId:-},%X{spanId:-}] [%thread] %-5level %logger{36} - %msg%n'
  file:
    name: logs/{servicio}.log

management:
  endpoints:
    web:
      exposure:
        include: health, info, prometheus
  zipkin:
    tracing:
      endpoint: ${ZIPKIN_BASE_URL:http://localhost:9411}/api/v2/spans
  tracing:
    sampling:
      probability: 1.0

resilience4j:
  circuitbreaker:
    configs:
      default:
        sliding-window-size: 10
        minimum-number-of-calls: 5
        wait-duration-in-open-state: 30s
        failure-rate-threshold: 50
```

---

## Paginación

Para listados que pueden crecer, usar `Page<T>`:

```java
// Service
public Page<ProductResponse> getProducts(int page, int size) {
    return productRepository.findAll(PageRequest.of(page, size))
        .map(productMapper::toProductResponse);
}

// Controller
@GetMapping
public ResponseEntity<Page<ProductResponse>> getProducts(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size) {
    return ResponseEntity.ok(productService.getProducts(page, size));
}
```

---

## Logging

Usar `@Slf4j` de Lombok. Nivel de log por convención:
- `log.info` → eventos normales de negocio (orden confirmada, evento Kafka publicado)
- `log.warn` → errores de negocio manejados (CartException, FeignException, circuit breaker abierto)
- `log.error` → errores inesperados o fallas críticas (pago fallido, NullPointerException)

Los logs incluyen `traceId` y `spanId` automáticamente vía el patrón configurado en el Config Server.

---

## Convenciones de nombres

- Todo en inglés
- Clases: `PascalCase`
- Métodos y variables: `camelCase`
- Constantes: `UPPER_SNAKE_CASE`
- Tablas: `snake_case`
- DTOs: sufijo `Request` o `Response`
- Excepciones: sufijo `Exception` (ej: `CartException`, `OrderException`)
- Feign fallbacks: sufijo `Fallback` (ej: `CustomerClientFallback`)
- Kafka producers: sufijo `Producer`; consumers: sufijo `Consumer`

---

## Forma de trabajar

Antes de implementar cualquier cosa:
1. Explicame en palabras simples qué hace la funcionalidad
2. Listame qué clases vas a crear/modificar y qué hace cada una
3. Explicame cómo se conecta con lo que ya existe
4. Después implementá todo sin esperar aprobación adicional

Al implementar:
- Seguí los patrones exactos que ya existen en el proyecto (records para DTOs, `@Service` directo sin interfaz, etc.)
- Si el código nuevo puede generar dudas, agregá un comentario explicando el por qué
- Al terminar, hacé un resumen de qué creaste y cómo se conecta todo

---

## Lo que no hacer

- Lógica de negocio en el controller
- Devolver entidades JPA o documentos MongoDB directamente desde el controller
- Usar clases convencionales para DTOs (usar records)
- Crear interfaz + implementación para services (no se usa en este proyecto)
- Usar MapStruct (los mappers son manuales)
- Hardcodear credenciales, secrets o URLs
- `spring.jpa.hibernate.ddl-auto=create` (solo `update` o `validate`)
- `@Data` en entidades JPA (usar `@Getter @Setter` separados)
- Mezclar anotaciones JPA y MongoDB en la misma clase
