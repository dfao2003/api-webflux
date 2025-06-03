<div align="center">
  ![UPS Logo](https://upload.wikimedia.org/wikipedia/commons/thumb/b/b0/Logo_Universidad_Polit%C3%A9cnica_Salesiana_del_Ecuador.png/640px-Logo_Universidad_Polit%C3%A9cnica_Salesiana_del_Ecuador.png)
<div>

# Spring Boot WebFlux API con Firebase

Este proyecto tiene como objetivo desarrollar una API eficiente y reactiva usando **Spring Boot WebFlux** con integración a **Firebase (Authentication y Firestore)**. El sistema está diseñado para soportar operaciones concurrentes, validaciones, manejo adecuado de errores, y funcionalidades en tiempo real como likes, comentarios y feed.

---

## 🚀 Tecnologías Utilizadas

- Spring Boot WebFlux
- Firebase Admin SDK (Authentication, Firestore, Storage)
- Project Reactor (`Mono<T>` y `Flux<T>`)
- WebClient (HTTP reactivo y no bloqueante)

---

## ⚙️ Dependencias del Proyecto

- `spring-boot-starter-webflux`: Framework para aplicaciones web reactivas.
- `firebase-admin`: SDK oficial para conectarse a los servicios de Firebase.
- `lombok`: Reduce el código repetitivo con anotaciones para getters, setters, etc.

---

## 📁 Estructura del Proyecto

```text
src/main/java/
├── config/           # Configuración de Firebase y herramientas necesarias
├── controller/       # Controladores REST
├── enviroments/      # Variables y configuraciones específicas del entorno
├── repository/       # Acceso a datos y comunicación con servicios externos
├── security/         # Filtros y validaciones de autenticación
└── service/          # Servicios para lógica de negocio como almacenamiento de imágenes
```

---

## 🔄 Programación Reactiva con WebFlux

- **Mono<T>**: Representa 0 o 1 elemento.
- **Flux<T>**: Representa 0 a N elementos (stream).
- **Operadores útiles**:
  - `.map()`, `.flatMap()`, `.filter()`, `.zip()`
- **Manejo de errores**:
  - `.onErrorResume()`, `.onErrorReturn()`, `.doOnError()`
- **Herramientas**:
  - `WebClient`: Cliente HTTP no bloqueante

---

## 🔐 Conexión a Firebase

- Se usa el SDK `firebase-admin`.
- Para conectarse, se requiere descargar la clave privada JSON desde la consola de Firebase.
- La clave se carga en el backend para configurar la conexión a Authentication y Firestore.

---

## 👤 Autenticación: Login y Registro

### 📥 SignUp (Registro)

1. Se recibe un objeto `User` desde el cuerpo de la solicitud.
2. Se llama a `repositoryUser.signIn(User)` que retorna un `Mono<String>` (token de Firebase).
3. Se verifica en Firestore si el usuario ya existe.
   - Como es una operación bloqueante, se envuelve en `Mono.fromCallable()` y se ejecuta en `Schedulers.boundedElastic()`.
4. Se devuelve una respuesta `200 OK` o un error controlado.
5. Se usan operadores como `.flatMap()` y `.onErrorResume()` para mantener el flujo reactivo.

#### Clases clave:
- `repositoryUser.signIn(User)`
- `FirestoreClient.getFirestore()`

---

### 🔑 Login

- Mismo enfoque reactivo que el registro.
- Devuelve el token de Firebase si las credenciales son válidas.

---

## 🖼️ Publicación de Post e Imagen

### Endpoint `/post`

- Recibe un objeto `Post` vía `@RequestBody`.
- Llama a `repository.publicPost(request)` que retorna un `Mono<String>`.
- Devuelve `ResponseEntity` con estado 200 si es exitoso.

### Endpoint `/filter`

- Recibe un objeto `ImagenRequest` con los parámetros de filtrado.
- Llama a `repository.applyFilter(req)` que retorna un `Mono<String>` con la imagen procesada.

---

## 🧠 Lógica de `ImageRepository`

Responsable de:

1. **Publicar imágenes**:
   - Usa `FirebaseStorageService` para convertir imágenes a base64 y subirlas a Firebase Storage.
   - Guarda metadata en Firestore.
2. **Aplicar filtros**:
   - Se comunica con un servidor Flask usando `WebClient`.
   - Se envía la imagen codificada y se recibe la imagen filtrada.

---

## 🧾 FirebaseStorageService

- Convierte imagen base64 → archivo.
- Sube la imagen a un bucket público de Firebase Storage.
- Devuelve la URL pública de la imagen.
- Todo el proceso se maneja de forma reactiva con `Mono`.

---

## 🌍 Clase Enviroment (`Data`)

Contenedor de constantes y configuraciones, como:

- Clave API de Firebase
- URL base para llamadas REST

---

## 🔐 Seguridad: `FirebaseAuthenticationFilter`

Filtro que:

- Permite acceso libre a `/api/login` y `/api/signup`.
- Para otras rutas:
  - Extrae el token del header `Authorization`.
  - Usa `verifyIdTokenAsync()` de Firebase para validar el token de forma reactiva.
  - Si es válido, guarda el `UID` del usuario en la solicitud.
  - Si falla, devuelve `401 Unauthorized`.

Este enfoque garantiza una autenticación reactiva, segura y escalable.

---

## 📌 Notas Finales

- Toda la lógica está diseñada para ser **no bloqueante**.
- Operaciones como Firebase Auth y Firestore, que son bloqueantes, se manejan de forma adecuada con `Schedulers.boundedElastic()`.
- Se garantiza alta concurrencia y bajo consumo de recursos.

---

## 🧪 Pruebas y Desarrollo

- Endpoints `/api/signup`, `/api/login`, `/img/post`, `/img/filter` están listos para pruebas.
- Requiere conexión a internet y configuración previa del Firebase Admin SDK.

