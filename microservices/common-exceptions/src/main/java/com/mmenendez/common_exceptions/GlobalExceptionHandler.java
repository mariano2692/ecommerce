package com.mmenendez.common_exceptions;

import java.util.HashMap;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import lombok.extern.slf4j.Slf4j;

@Component
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

        @ExceptionHandler(MethodArgumentNotValidException.class) 
    public ResponseEntity<ErrorResponse> handleException(MethodArgumentNotValidException exception) { 
        
        var errors = new HashMap<String, String>();
        exception.getBindingResult().getFieldErrors().forEach(error -> {
            var fieldName = ((FieldError) error).getField();
            var errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        log.warn("Validation errors: {}", exception.toString());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(errors)); 
    }

    @ExceptionHandler(Exception.class) 
    public ResponseEntity<ErrorResponse> handleException(Exception exception) { 
        var errors = new HashMap<String, String>(); 
        var fieldName = "message"; 
        var errorMessage = "An error has occurred. Please contact the administrator or try again later.";
        errors.put(fieldName, errorMessage); 
        log.error("Error: {}", exception.toString());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse(errors)); 
    }


}

/** 
1️⃣ Flujo normal de una request

Cuando llega una request HTTP ocurre esto:

Filtro de seguridad (por ejemplo JWT)

DispatcherServlet

Controller

Service

Repository

Respuesta HTTP

Si todo sale bien, el controller devuelve el ResponseEntity.

2️⃣ ¿Qué pasa cuando ocurre una excepción?

Si en cualquier parte del flujo ocurre un throw:

throw new CartException("Cart not found");

Spring hace esto:

La excepción sube por la pila (controller → dispatcher).

El DispatcherServlet busca un manejador de excepciones.

Spring detecta clases anotadas con
@RestControllerAdvice.

Busca un método @ExceptionHandler compatible con la excepción.

Ejecuta ese método y devuelve la respuesta HTTP que definiste.

3️⃣ En tu caso específico

Tenés dos handlers:

Global
@RestControllerAdvice
public class GlobalExceptionHandler

Este maneja:

MethodArgumentNotValidException

Exception (cualquier error)

Específico del cart microservice
@RestControllerAdvice(basePackages = "com.mmenendez.microservices.cart_microservice")
public class CartExceptionHandler extends GlobalExceptionHandler

Este maneja:

CartException

FeignException

4️⃣ Ejemplo real de cuándo se ejecuta

Supongamos este controller:

@PostMapping
public CartResponse create(@Valid @RequestBody CartRequest request)
Caso 1 — Error de validación
{
  "quantity": -1
}

@Valid falla → Spring lanza:

MethodArgumentNotValidException

Spring ejecuta:

handleException(MethodArgumentNotValidException exception)
Caso 2 — Error de negocio

En tu service:

if(cart == null){
    throw new CartException("Cart not found");
}

Spring ejecuta:

handle(CartException exception)

del CartExceptionHandler.

Caso 3 — Error llamando otro microservicio

Si usás OpenFeign y el otro servicio falla:

FeignException

Se ejecuta:

handleFeignException(FeignException exception)
Caso 4 — Error inesperado

Por ejemplo:

NullPointerException

Spring ejecuta:

handleException(Exception exception)

del GlobalExceptionHandler.

5️⃣ Por qué funciona sin que lo llames

Porque Spring MVC tiene un componente interno llamado:

ExceptionHandlerExceptionResolver

que:

detecta @ControllerAdvice

registra todos los @ExceptionHandler

intercepta las excepciones automáticamente.

6️⃣ Orden de prioridad en tu caso

Spring busca handlers así:

1️⃣ Handler específico (CartException)
2️⃣ Handler del controller
3️⃣ Handler del @RestControllerAdvice del paquete
4️⃣ Handler global (Exception.class)

7️⃣ Resultado final que recibe el cliente

Tu API devolvería algo como:

{
  "errors": {
    "cart": "Cart not found"
  }
}

con HTTP:

400 BAD_REQUEST

✅ Resumen

Cuando una request falla:

Controller / Service lanza excepción
        ↓
DispatcherServlet la captura
        ↓
Spring busca @RestControllerAdvice
        ↓
ejecuta @ExceptionHandler correspondiente
        ↓
devuelve ResponseEntity al cliente
*/