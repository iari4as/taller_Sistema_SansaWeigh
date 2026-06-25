# Contexto y Reglas del Proyecto: Sistema SansaWeigh (tallerHDD)

Este archivo (`GEMINI.md`) sirve como la guía de contexto, directrices, reglas de desarrollo y registro de avance para el asistente de Inteligencia Artificial (Gemini) en el proyecto **SansaWeigh**.

---

## 1. Descripción del Proyecto

**SansaWeigh** es un sistema de pesaje, registro y seguimiento de paquetes diseñado para la Universidad Técnica Federico Santa María (USM). El sistema permite registrar paquetes, almacenar su información de pesaje y destino, optimizar las consultas mediante caché y proporcionar endpoints REST para la integración con básculas y otros sistemas logísticos de la universidad.

---

## 2. Pila Tecnológica (Tech Stack)

El proyecto está construido sobre el ecosistema de **Spring Boot** utilizando las siguientes tecnologías (definidas en el `pom.xml`):

*   **Lenguaje:** Java 17
*   **Framework Principal:** Spring Boot 4.1.0
*   **Base de Datos NoSQL:** MongoDB (para persistencia de paquetes y registros de pesaje)
*   **Base de Datos en Memoria / Caché:** Redis (para caché de consultas rápidas y mensajería)
*   **Exposición REST:** Spring Data REST & Spring Web MVC (para exponer repositorios y controladores REST)
*   **Herramientas de Productividad:** Lombok (para generación automática de Getters, Setters, Constructores, etc.)
*   **Pruebas:** Spring Boot Starter Test, MongoDB Test, Redis Test, WebMVC Test.

---

## 3. Arquitectura del Sistema

El proyecto sigue una arquitectura limpia dividida en capas con una estructura simplificada de paquetes (sin paquete `exceptions` independiente):

1.  **Entities (`cl.usm.tallerhdd.entities`):** Clases que representan el modelo de datos persistido en MongoDB (`paquete` y `RegistroPesaje`).
2.  **DTOs (`cl.usm.tallerhdd.dto`):** Objetos de transferencia de datos para la comunicación con APIs externas y control del ciclo de vida (`EspecificacionBalanzaDTO`, `PesajeRequestDTO`, `EstadoUpdateRequestDTO`).
3.  **Repositories (`cl.usm.tallerhdd.repositories`):** Interfaces de Spring Data MongoDB (`RegistroPesajeRepository`).
4.  **Services (`cl.usm.tallerhdd.services`):** Capa de lógica de negocio e implementación de reglas críticas (`PesajeService` y `PesajeServiceImpl`).
5.  **Controllers (`cl.usm.tallerhdd.controllers`):** Controladores REST personalizados (`PesajeController`) y el Manejador Global de Excepciones (`GlobalExceptionHandler`), el cual aloja las excepciones de negocio de forma estática e interna para simplificar la estructura.

---

## 4. Instrucciones y Reglas de Desarrollo para la IA

*   **Estilo de Código:** Utilizar siempre anotaciones de Lombok (`@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`) en las entidades y DTOs para mantener el código limpio.
*   **Inyección de Dependencias:** Preferir la inyección por constructor en lugar de `@Autowired` sobre campos, utilizando `@RequiredArgsConstructor` de Lombok.
*   **Manejo de Excepciones:** No crear un paquete `exceptions`. Las excepciones de negocio (ej: `IllegalWeighingStateException`, `ResourceNotFoundException`) deben declararse como clases estáticas internas públicas dentro del `GlobalExceptionHandler`.
*   **Gestión de Estados:** No utilizar enumeraciones (Enums). Manejar los estados y categorías como cadenas de texto (`String`) constantes o literales de acuerdo a los requerimientos del taller.

---

## 5. Modelos de Datos

### A. Entidad `paquete`
Representa un bulto o encomienda dentro del campus.
*   `id` (String, `@Id`): Identificador único autogenerado por MongoDB.
*   `name` (String): Nombre o etiqueta descriptiva del paquete.
*   `weight` (Double): Peso medido en kilogramos.
*   `description` (String): Descripción detallada del contenido.
*   `sender` (String): Nombre o del departamento que envía.
*   `destination` (String): Destino del paquete (ej. Casa Central, San Joaquín, Vitacura, Concepción).
*   `status` (String): Estado actual del paquete (`REGISTRADO`, `PESADO`, `EN_TRANSITO`, `ENTREGADO`).
*   `createdAt` (LocalDateTime): Fecha y hora de registro.

### B. Entidad `RegistroPesaje`
Representa la medición física del peso en una balanza.
*   `id` (String, `@Id`): Identificador único autogenerado.
*   `idBalanza` (String): ID de la balanza física que tomó la muestra.
*   `idPaquete` (String): ID del paquete asociado.
*   `pesoSansas` (Double): Peso convertido a la unidad propietaria "Sansas" (1 Sansa = 1.337 kg).
*   `categoria` (String): Clasificación del peso (`LIVIANO`, `MEDIANO`, `PESADO`).
*   `estado` (String): Estado en la máquina de estados (`INGRESADO`, `PESADO`, `APROBADO`, `RECHAZADO`, `DESPACHADO`).
*   `createdAt` (LocalDateTime): Fecha de creación de la medición.
*   `updatedAt` (LocalDateTime): Fecha de modificación del estado de pesaje.

---

## 6. Plan de Trabajo y Avance del Proyecto

*   **[x] Fase 1: Corrección de Entidades Existentes**
    *   Corregir y completar `paquete.java` resolviendo errores de sintaxis y mapeo de colección de MongoDB (`collection = "paquetes"`).
*   **[x] Fase 2: Estructura Inicial y Persistencia**
    *   Crear la entidad `RegistroPesaje` con Lombok y auditoría de fechas.
    *   Crear el DTO serializable `EspecificacionBalanzaDTO` para integración de balanzas externas.
    *   Crear el repositorio `RegistroPesajeRepository` con soporte para consultas de historial filtradas por rango de fechas.
*   **[x] Fase 3: Capa de Servicio y Reglas de Negocio**
    *   Implementar la conversión de unidades (Kg a Sansas).
    *   Implementar la clasificación de peso (`LIVIANO`, `MEDIANO`, `PESADO`).
    *   Implementar la restricción horaria (bloqueo de paquetes `"PESADO"` de 20:00 a 06:00).
    *   Implementar la validación de balanza con ID primo en días impares.
    *   Implementar la máquina de estados estricta (`INGRESADO ➔ PESADO ➔ APROBADO/RECHAZADO ➔ DESPACHADO`).
*   **[x] Fase 4: Capa de API REST y Manejo de Errores**
    *   Crear `PesajeController` con endpoints POST, PUT/PATCH (para actualización de estados) y GET (historial por rango de fechas).
    *   Crear `GlobalExceptionHandler` con soporte para traducción de excepciones de negocio a respuestas JSON limpias:
        *   `IllegalWeighingStateException` ➔ HTTP 400 (Bad Request)
        *   `IllegalArgumentException` ➔ HTTP 400 (Bad Request)
        *   `ResourceNotFoundException` ➔ HTTP 404 (Not Found)
        *   `Exception` (Errores genéricos) ➔ HTTP 500 (Internal Server Error)
*   **[x] Fase 5: Refactorización Estructural**
    *   Eliminar el paquete `exceptions`.
    *   Migrar las excepciones de negocio a clases estáticas internas de `GlobalExceptionHandler`.
    *   Actualizar las referencias e importaciones de la aplicación.
    *   Verificar y compilar el proyecto exitosamente.
*   **[ ] Fase 6: Configuración e Integraciones (Pendiente)**
    *   Establecer la conexión a MongoDB y Redis en `application.properties`.
    *   Implementar el almacenamiento en caché de Redis para las balanzas consultadas frecuentemente.
    *   Desarrollar pruebas unitarias y de integración para validar el sistema de extremo a extremo.
