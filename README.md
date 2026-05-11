<div align="center">

# 🐾 Sanos y Salvos — BFF & API Gateway

[![Java](https://img.shields.io/badge/Java-21-orange?style=for-the-badge&logo=openjdk)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.14-brightgreen?style=for-the-badge&logo=springboot)](https://spring.io/projects/spring-boot)
[![Spring Security](https://img.shields.io/badge/Spring%20Security-JWT-blue?style=for-the-badge&logo=springsecurity)](https://spring.io/projects/spring-security)
[![Docker](https://img.shields.io/badge/Docker-Multi--stage-2496ED?style=for-the-badge&logo=docker)](https://www.docker.com/)
[![Maven](https://img.shields.io/badge/Maven-Wrapper-C71A36?style=for-the-badge&logo=apachemaven)](https://maven.apache.org/)

</div>

---

Este microservicio actúa como el **Backend For Frontend (BFF)** y **API Gateway** de la plataforma Sanos y Salvos. Es el único punto de entrada público para la aplicación web, encargado de orquestar las peticiones, cruzar datos en memoria de los microservicios internos y gestionar las políticas de seguridad de forma centralizada.

---

## 🚀 Tecnologías y Herramientas

| Componente | Tecnología | Detalle |
|---|---|---|
| **Lenguaje** | Java 21 (JDK 21) | — |
| **Framework** | Spring Boot 3.5.14 | — |
| **Cliente HTTP** | RestClient | API moderna y fluida introducida en Spring 6.1 |
| **Seguridad** | Spring Security + JWT | Preparado para Clerk |
| **Build** | Maven Wrapper (`./mvnw`) | — |
| **Contenerización** | Docker | Imagen multi-stage |
| **Lombok** | Lombok | Código limpio y sin boilerplate en DTOs |
| **Monitoreo** | Spring Boot Actuator | Health checks y observabilidad |

---

## 🏛️ Arquitectura del Proyecto

A diferencia de los microservicios de dominio, el BFF no se conecta a una base de datos propia. Implementa una **arquitectura orientada a la orquestación**:
src/
└── main/

├── java/com/sanosysalvos/bff_gateway/

│ ├── BffGatewayApplication.java

│ ├── config/

│ │ ├── CorsConfig.java

│ │ └── SecurityConfig.java

│ ├── controller/

│ │ └── BffController.java

│ ├── service/

│ │ └── OrquestadorService.java

│ └── dto/

│ ├── MascotaConsolidadaDTO.java

│ ├── MascotaDTO.java

│ └── UbicacionDTO.java

└── resources/

└── application.properties


### Capas principales

- **Capa de Configuración (`config`)**: Define las políticas de `SecurityConfig` y `CorsConfig`.
- **Capa de Presentación (`controller`)**: Expone los endpoints consolidados consumidos por la interfaz gráfica.
- **Capa de Orquestación (`service`)**: Realiza llamadas asíncronas a las APIs internas y cruces de datos en memoria.
- **Capa de Transferencia (`dto`)**: Contiene objetos como `MascotaConsolidadaDTO` para la vista.

---

## 🛠️ Instalación y Configuración

### Requisitos Previos

- Docker Desktop instalado y en ejecución.
- Java 21 instalado, solo si se ejecuta sin Docker.
- Microservicios de Mascotas y Geolocalización ejecutándose en la misma red.

### 1. Configurar Variables de Entorno

Edita `src/main/resources/application.properties` o crea tu `.env` a partir del `.env.example`:

| Propiedad | Descripción | Ejemplo |
|---|---|---|
| `server.port` | Puerto del BFF | `8082` |
| `mascotas.api.url` | URL del MS de Mascotas | `http://sanos_ms_mascotas:8080/api/v1/mascotas` |
| `geolocalizacion.api.url` | URL del MS Geolocalización | `http://sanos_ms_geolocalizacion:8081/api/v1/geolocalizacion` |

> **Nota:** En un entorno local sin Docker, las URLs deben apuntar a `localhost`, por ejemplo: `http://localhost:8080/api/v1/mascotas`.

---

## 🐳 Ejecución con Docker

### 2. Ejecutar con Docker

```bash
Construir la imagen
docker build -t sanos-y-salvos-bff .

Ejecutar el contenedor en la red compartida
docker run -d
-p 8082:8082
--env-file .env
--network sanos-network
--name sanos_bff_gateway
sanos-y-salvos-bff

### 3. Ejecutar sin Docker

```bash
./mvnw spring-boot:run
```

> La API estará disponible en: `http://localhost:8082/api/v1/bff`

---

## 📡 Documentación de la API

**Base URL:** `http://localhost:8082/api/v1/bff`

| Método | Endpoint | Acceso | Descripción |
|---|---|---|---|
| `GET` | `/dashboard` | Público | Devuelve la lista unificada de mascotas con sus coordenadas espaciales. |

### Ejemplo de respuesta para `GET /api/v1/bff/dashboard`

El BFF consolida las respuestas de los microservicios de Mascotas y Geolocalización en un único JSON estructurado:

```json
[
  {
    "idMascota": 1,
    "nombre": "Firulais",
    "raza": "Mestizo",
    "estado": "PERDIDA",
    "fotoBytes": "[base64_string]",
    "coordenadas": {
      "latitud": -33.4156,
      "longitud": -70.5953
    }
  }
]
```

---

## 💡 Decisiones de Diseño Clave

- **Único Punto de Entrada:** Centraliza las peticiones del frontend, evitando que React conozca IPs o puertos internos.
- **Reducción de Peticiones:** El BFF cruza los datos en memoria usando un `Map<Integer, UbicacionDTO>`, logrando una eficiencia algorítmica O(n).
- **Tolerancia a Fallos:** Si Geolocalización cae, el BFF devuelve mascotas con coordenadas nulas para mantener operativa la interfaz.
- **CORS Centralizado:** Las políticas de Cross-Origin Resource Sharing se manejan exclusivamente aquí.
- **Seguridad Zero Trust:** Los microservicios internos no son accesibles desde internet; solo el BFF valida el token Bearer JWT.

---

## 🔍 Salud y Monitoreo

Spring Boot Actuator está habilitado. Puedes verificar el estado del servicio en:

```bash
GET http://localhost:8082/actuator/health
```

---

## 🧪 Ejecutar Tests

```bash
./mvnw test
```

---

## 🌿 Ramas

| Rama | Descripción |
|---|---|
| `main` | Versión estable en producción |
| `develop` | Rama activa de desarrollo |

---

## 👥 Equipo de Desarrollo — LMC S.A.

| Integrante |
|---|---|
| Renato Barriga |
| Matías González |
| Cristóbal Véliz |

> Este proyecto es parte del caso semestral: **"Sanos y Salvos – Plataforma Inteligente para la recuperación de mascotas perdidas"**.
