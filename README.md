# Restaurante API — Spring Boot + MySQL

Proyecto académico: **tema 7: Restaurante**, entidades **Plato** y **Pedido**, relación **muchos a muchos (N:N)**. Incluye CRUD completo, arquitectura en capas, validaciones, manejo de errores y pruebas automatizadas.

> **Fase 2**: esta entrega contiene el proyecto local de Spring Boot. El Dockerfile, el despliegue cloud y la colección de Postman se incorporarán en las fases siguientes.

## Requisitos

- **JDK 17** (también puedes utilizar JDK 21)
- **IntelliJ IDEA** con Maven (incluido en IntelliJ)
- **MySQL 8** (por ejemplo desde XAMPP)
- Puerto **3306** disponible para MySQL y puerto **8080** para la API

Spring Boot: **3.5.16**. Archivo Maven: `pom.xml`.

## 1. Crear la base de datos en XAMPP

1. Abre el panel de **XAMPP**.
2. Pulsa **Start** en **MySQL** (Apache solo se necesita si vas a usar phpMyAdmin).
3. Entra a `http://localhost/phpmyadmin` con Apache iniciado, abre la pestaña **SQL** y ejecuta:

```sql
CREATE DATABASE IF NOT EXISTS restaurante_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
```

También encontrarás el archivo `database/01_crear_base_de_datos.sql`.

**No necesitas crear tablas manualmente**. Hibernate creará `plato`, `pedido` y `pedido_plato` cuando ejecutes la API con éxito. Debes tener permisos de creación de tablas.

## 2. Abrir en IntelliJ IDEA

1. Extrae el ZIP `restaurante-api-fase2.zip`.
2. Abre IntelliJ IDEA → **File > Open** (Archivo > Abrir).
3. Selecciona la carpeta **`restaurante-api`**, no solo `src`.
4. Si IntelliJ pregunta, confía en el proyecto (**Trust Project**).
5. Espera a que Maven descargue las dependencias (requiere internet).
6. En **File > Project Structure > Project SDK** selecciona Java 17 o Java 21.
7. Comprueba que `pom.xml` aparece con el icono Maven; si no, clic derecho → **Add as Maven Project**.
8. Abre `src/main/java/com/proyectonuclear/restaurante/RestauranteApiApplication.java`.
9. Presiona el botón **Run** (triángulo verde) junto al método `main`.

Si tu MySQL local usa el usuario `root` sin contraseña, la aplicación funciona con los valores predeterminados. **Si tiene contraseña, no la guardes en el código**: configura la variable de entorno `DB_PASSWORD` en la configuración de ejecución de IntelliJ.

### Variables de entorno

| Variable | Valor predeterminado | Explicación |
|---|---|---|
| `DB_HOST` | `localhost` | Servidor MySQL |
| `DB_PORT` | `3306` | Puerto de MySQL |
| `DB_NAME` | `restaurante_db` | Base de datos |
| `DB_USER` | `root` | Usuario de MySQL |
| `DB_PASSWORD` | vacío | Contraseña de MySQL |
| `PORT` | `8080` | Puerto HTTP de Spring |

En IntelliJ: **Run > Edit Configurations > RestauranteApiApplication > Environment variables**. Si usas `DB_USER=miusuario;DB_PASSWORD=secreta` en Windows, IntelliJ admite la edición mediante tabla de variables.

## 3. Verificar la ejecución

En la consola deberías ver una línea similar a:

`Tomcat started on port 8080`

Abre en el navegador:

- `http://localhost:8080/api/platos`
- `http://localhost:8080/api/pedidos`

Antes de registrar datos ambos deberían mostrar una lista vacía: `[]`.

## 4. Endpoints

| Método | URL | Función |
|---|---|---|
| POST | `/api/platos` | Crear plato |
| GET | `/api/platos` | Listar platos |
| GET | `/api/platos/{id}` | Consultar plato |
| PUT | `/api/platos/{id}` | Actualizar plato |
| DELETE | `/api/platos/{id}` | Eliminar plato |
| POST | `/api/pedidos` | Crear pedido |
| GET | `/api/pedidos` | Listar pedidos |
| GET | `/api/pedidos/{id}` | Consultar pedido |
| PUT | `/api/pedidos/{id}` | Actualizar pedido |
| DELETE | `/api/pedidos/{id}` | Eliminar pedido |
| PATCH | `/api/pedidos/{id}/estado` | Cambiar estado de pedido |

### Ejemplos JSON para Postman o Thunder Client

**1. POST** `http://localhost:8080/api/platos`

```json
{
  "nombre": "Hamburguesa especial",
  "descripcion": "Hamburguesa con queso y papas",
  "precio": 18000.00,
  "disponible": true
}
```

**2. POST** `http://localhost:8080/api/platos`

```json
{
  "nombre": "Papas grandes",
  "descripcion": "Porcion de papas fritas",
  "precio": 12000.00,
  "disponible": true
}
```

**3. POST** `http://localhost:8080/api/pedidos` (utiliza los ID reales de los platos creados)

```json
{
  "nombreCliente": "Carlos Perez",
  "platoIds": [1, 2]
}
```

**4. PATCH** `http://localhost:8080/api/pedidos/1/estado`

```json
{
  "estado": "EN_PREPARACION"
}
```

> Al crear/actualizar envía `Content-Type: application/json`. Los ID en los ejemplos son ilustrativos; utiliza los ID que tu base de datos haya generado.

## 5. Reglas de negocio

1. Los platos deben tener nombre, disponibilidad y precio positivo, con 2 decimales como máximo.
2. Cada pedido debe tener nombre del cliente y al menos un plato existente.
3. No se aceptan ID de platos repetidos dentro de un pedido (HTTP 409).
4. Los platos nuevos incluidos deben estar disponibles (HTTP 409).
5. Un pedido inicia `PENDIENTE`; después puede pasar a `EN_PREPARACION` o `CANCELADO`. De `EN_PREPARACION` puede pasar a `ENTREGADO` o `CANCELADO`. Los estados finales no pueden cambiar.
6. Solo los pedidos pendientes pueden editarse. Si solamente cambia el nombre del cliente, se conserva el total histórico. Si cambia el conjunto de platos, el total se recalcula con los precios actuales.
7. Modificar el precio de un plato **no actualiza los totales históricos** de pedidos ya guardados.
8. No se permite eliminar un plato asociado a algún pedido (HTTP 409); puedes marcarlo `disponible: false`.
9. Se permite eliminar pedidos desde su endpoint `DELETE`, independientemente de su estado, por requerimiento del CRUD académico.
10. La fecha de los pedidos se genera en horario `America/Bogota`.

### Estados HTTP

- `200`: consulta, actualización o cambio de estado exitoso.
- `201`: recurso creado; devuelve cabecera `Location`.
- `204`: recurso eliminado, sin contenido.
- `400`: error de validación o JSON mal formado.
- `404`: recurso no encontrado.
- `409`: regla de negocio incumplida.

## 6. Pruebas automatizadas

Desde IntelliJ ejecuta la clase `RestauranteApiIntegrationTest` o escribe en la terminal de IntelliJ:

```bash
mvn test
```

Las pruebas usan H2 en memoria: **no modifican tu base de datos MySQL**. Incluyen:

- Creación de pedidos con múltiples platos, relación N:N y cálculo del total.
- Protección del total histórico al cambiar el precio de un plato.
- Rechazo de duplicados y eliminación de un plato incluido en pedidos.
- Validación de entradas y transiciones de estado.

## 7. Organización del código

```text
src/main/java/com/proyectonuclear/restaurante/
├── RestauranteApiApplication.java
├── controller/
│   ├── PlatoController.java
│   └── PedidoController.java
├── service/
│   ├── PlatoService.java
│   └── PedidoService.java
├── repository/
│   ├── PlatoRepository.java
│   └── PedidoRepository.java
├── entity/
│   ├── Plato.java
│   ├── Pedido.java
│   └── EstadoPedido.java
├── dto/
│   ├── PlatoRequest.java
│   ├── PlatoResponse.java
│   ├── PlatoResumenResponse.java
│   ├── PedidoRequest.java
│   ├── PedidoResponse.java
│   └── PedidoEstadoRequest.java
└── exception/
    ├── RecursoNoEncontradoException.java
    ├── ReglaNegocioException.java
    ├── RespuestaError.java
    └── ManejadorGlobalExcepciones.java
```

## 8. Problemas habituales

- **Communications link failure**: MySQL está apagado o el host/puerto es incorrecto.
- **Access denied for user**: usuario o contraseña incorrectos; verifica `DB_USER` y `DB_PASSWORD`.
- **Unknown database 'restaurante_db'**: aún no creaste la base de datos; ejecuta el SQL de la sección 1.
- **Port 8080 was already in use**: libera el puerto o cambia la variable `PORT`, por ejemplo `8081`.
- **Could not resolve dependencies**: verifica internet, abre la ventana Maven y pulsa **Reload All Maven Projects**.
- **Unsupported Java version**: configura el SDK de IntelliJ en Java 17 o 21 y recarga Maven.
- **GET / muestra 404**: la API no define una página web raíz; usa `/api/platos` o `/api/pedidos`.

## Siguientes fases

- **Fase 3**: colección de Postman, solicitudes CRUD y pruebas de funcionamiento.
- **Fase 4**: Dockerfile y docker-compose, construcción y verificación de contenedores.
- **Fase 5**: despliegue en Railway y conexión a MySQL cloud.
- **Fase 6**: README de despliegue y evidencias JSON.

**Nota de verificación**: el proyecto se entrega como código fuente y pruebas listas para ejecutarse. No se afirma haberlo compilado o ejecutado contra MySQL real sin disponer de Maven y el servidor de base de datos en el entorno de generación.
