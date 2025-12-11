# 📘 MyBabyWallet - Documentación Técnica

**Versión:** 2.0.0 (Release Enterprise)
**Fecha:** 11-11-2025
**Asignatura:** Desarrollo de Aplicaciones Móviles
**Desarrollador:**
* Nibaldo Araya

---

## 1. Descripción del Proyecto
**MyBabyWallet** es una solución móvil nativa desarrollada en Android (Kotlin) diseñada para la gestión financiera familiar. La aplicación permite a múltiples usuarios llevar un control detallado de sus ingresos y gastos, enriqueciendo la información con evidencia fotográfica y geolocalización.

Además, el sistema cuenta con capacidades de integración empresarial, conectándose a APIs externas para conversión de divisas y sincronizando datos con un microservicio propio basado en Spring Boot.

---

## 2. Arquitectura de Software

El proyecto sigue estrictamente el patrón de diseño **MVVM (Model-View-ViewModel)** bajo los principios de **Clean Architecture** para garantizar la escalabilidad y mantenibilidad del código.

### Diagrama de Componentes
* **Capa de Presentación (UI):** Desarrollada 100% en **Jetpack Compose** (Material Design 3). Gestiona la interacción con el usuario, animaciones y navegación.
* **Capa de Lógica (ViewModel):** `WalletViewModel` y `LoginViewModel`. Actúan como intermediarios, exponiendo `LiveData` y manejando el estado de la UI (State Hoisting).
* **Capa de Datos (Data):**
    * **Local:** Repositorios Room (SQLite) para persistencia offline.
    * **Remota:** Retrofit Client para consumo de servicios REST.

---

## 3. Stack Tecnológico

### 📱 Cliente Android
* **Lenguaje:** Kotlin 1.9
* **UI Toolkit:** Jetpack Compose
* **Inyección de Dependencias:** ViewModel Factory (Nativo)
* **Base de Datos Local:** Room Database (SQLite)
* **Red:** Retrofit 2 + GSON
* **Manejo de Imágenes:** Coil + CameraX
* **Geolocalización:** Google Play Services (FusedLocationProvider)
* **Testing:** JUnit 4

### ☁️ Backend (Microservicio)
* **Framework:** Spring Boot 3.x (Java 17)
* **Base de Datos:** H2 Database (In-Memory)
* **API:** REST Controller (@RestController)
* **ORM:** Spring Data JPA / Hibernate

---

## 4. Funcionalidades Implementadas

### A. Gestión de Usuarios y Seguridad
* **Login/Registro:** Sistema de autenticación con validación de credenciales en base de datos local.
* **Sesión Multi-usuario:** Aislamiento de datos; las transacciones se filtran por `usuarioId`, asegurando que cada usuario vea solo sus propios registros.

### B. Recursos Nativos del Dispositivo
1.  **Cámara y Almacenamiento:**
    * Captura de fotografías de boletas/comprobantes.
    * Almacenamiento en directorio privado de la app.
    * Visualización con **Zoom** mediante un PopUp personalizado.
2.  **GPS y Mapas:**
    * Obtención de coordenadas (Latitud/Longitud) en tiempo real.
    * Integración con **Google Maps** mediante `Intents` para visualizar el lugar exacto de la compra.

### C. Conectividad y Servicios
1.  **API Externa (Conversor de Divisas):**
    * Conexión a `https://mindicador.cl/api` para obtener el valor del **Dólar** y la **UF** en tiempo real.
    * Cálculo de conversión integrado en la pantalla principal.
2.  **Microservicio Propio (Nube):**
    * Endpoint propio desarrollado en Spring Boot: `POST /api/transacciones/sincronizar`.
    * Botón de sincronización que envía los gastos locales al servidor central.

### D. Experiencia de Usuario (UX)
* **Animaciones:** Transiciones de color (Verde/Rojo) y escala en el saldo según el estado financiero.
* **Navegación Fluida:** Transiciones animadas (`Slide`) entre Login, Registro y Home.

---

## 5. Endpoints y Contratos de Interfaz

### Cliente Retrofit (`ApiService.kt`)
La aplicación consume los siguientes servicios:

| Método | URL Relativa | Descripción |
| :--- | :--- | :--- |
| `GET` | `/dolar` | Obtiene valor del día (API Externa). |
| `GET` | `/uf` | Obtiene valor de la UF (API Externa). |
| `POST` | `/api/transacciones/sincronizar` | Envía lista de gastos al Backend Spring Boot. |

---

## 6. Aseguramiento de Calidad (QA)

### Pruebas Unitarias (`WalletUtilsTest.kt`)
Se implementaron pruebas unitarias automatizadas validando la lógica de negocio crítica:
* ✅ Cálculo matemático de saldos (Ingresos - Gastos).
* ✅ Validación de entradas de texto (evitar caracteres no numéricos).
* ✅ Detección de estados financieros negativos (Deuda).

---

## 7. Generación y Despliegue (Build)

El proyecto está configurado para generar un artefacto instalable seguro:
* **Tipo:** APK Firmado (Signed Release).
* **Esquema de Firma:** V1 (JAR Signing) + V2 (Full APK Signature).
* **Ofuscación:** Reglas de **ProGuard/R8** configuradas para proteger el código sin romper la serialización de modelos (`@Keep`).

### Pasos para ejecución
1. Ejecutar el microservicio Spring Boot (Puerto 8080).
2. Instalar `app-release.apk` en dispositivo Android.
3. Asegurar conexión a la misma red Wi-Fi (si se usa dispositivo físico).