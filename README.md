# **FoodUPB — Sistema de Gestión de Domicilios**

Sistema desarrollado para la gestión integral de pedidos de comida a domicilio en el entorno universitario de UPB, orientado a la automatización del flujo completo desde la toma del pedido hasta su entrega al cliente.

## **Descripción General**

FoodUPB sistematiza los procesos de gestión de pedidos que de otra forma se realizarían de forma manual. El sistema cubre desde el registro del cliente y la creación del pedido, pasando por la preparación en cocina, hasta la asignación del repartidor y la entrega final, con soporte para múltiples roles de usuario y comunicación en red mediante Java RMI.

## **Problemática**

La gestión manual de pedidos de domicilio genera tiempos de respuesta lentos, errores humanos en la toma de pedidos y dificultad para coordinar las áreas de cocina, entrega y administración en tiempo real. FoodUPB centraliza y automatiza este flujo, eliminando la necesidad de comunicación verbal entre áreas y reduciendo los errores operativos.

## **Funcionalidades Principales**

### **Módulo Operador**
- Búsqueda de clientes por número de teléfono.
- Registro de nuevos clientes en el sistema.
- Creación de pedidos y gestión del carrito de productos.
- Generación de factura con cálculo automático de IVA y costo de domicilio por distancia.
- Selección del cuadrante de destino para la entrega.
- Envío del pedido a cocina.

### **Módulo Cocina**
- Visualización de pedidos en cola por prioridad (Premium primero).
- Asignación automática de pedidos a fogones disponibles:
  - 1 fogón grande para pedidos complejos.
  - 3 fogones normales para pedidos simples.
- Marcado de pedidos como listos para entrega.
- Actualización automática cada 8 segundos.

### **Módulo Entregas**
- Visualización de pedidos listos para despachar.
- Asignación de repartidores a pedidos.
- Cálculo de ruta óptima desde UPB hasta el cuadrante de destino (algoritmo de Dijkstra).
- Agrupación de pedidos por repartidor para entregas conjuntas.
- Registro de inicio y finalización de cada entrega.

### **Módulo Administración**
- Gestión de usuarios (clientes y usuarios operativos).
- Gestión del catálogo de productos (agregar, editar, activar/desactivar).
- Gestión de cuadrantes de entrega y conexiones entre ellos.
- Reportes de pedidos con métricas de cocina y entregas.
- Bitácora de auditoría de acciones del sistema.

## **Información contenida en un Pedido**
- Id único del pedido.
- Cédula del cliente asociado.
- Estado actual en el ciclo de vida (PENDIENTE → EN_PREPARACION → LISTO → EN_CAMINO → ENTREGADO).
- Lista de productos del carrito con cantidad y precio.
- Subtotal, IVA (19%) y costo de domicilio.
- Total final de la factura.
- Cuadrante de destino asignado.
- Marca de tiempo de creación.

## **Cuadrantes y Rutas**
- Los cuadrantes representan zonas geográficas de entrega configuradas por el administrador.
- Cada cuadrante tiene una distancia registrada desde UPB en kilómetros.
- El costo de domicilio se calcula como: `$2.000 + (distanciaKm × $800)`. Los clientes Premium tienen domicilio gratis.
- El sistema calcula la ruta más corta entre cuadrantes usando el **algoritmo de Dijkstra** sobre un grafo de matriz de adyacencia.
- El módulo de entregas calcula rutas óptimas multi-destino con un algoritmo greedy (vecino más cercano).

## **Roles del Sistema**

| Rol | Acceso |
|---|---|
| `ADMIN` | Panel de administración completo |
| `OPERADOR` | Toma de pedidos y gestión de clientes |
| `COCINA` | Cola de preparación y fogones |
| `ENTREGA` | Asignación de repartidores y rutas |

## **Estructuras de Datos Implementadas**

El proyecto utiliza estructuras de datos propias (no las de Java) para las operaciones principales:

| Estructura | Dónde se usa |
|---|---|
| `LinkedList<T>` | Almacenamiento de pedidos, usuarios, productos y cuadrantes en todos los servicios |
| `Stack<T>` | Bitácora de acciones del servidor en `History` (orden LIFO, acción más reciente primero) |
| `PriorityQueue<T>` | Cola de pedidos en cocina — los pedidos Premium tienen prioridad `0` (alta), los estándar prioridad `1` |
| `matrixGraph<T>` | Mapa de cuadrantes en `CuadranteService` — soporta Dijkstra para calcular la ruta y distancia más corta |

# **Diseño del Sistema**

## **Diagrama de Clases**
> _Proximamente_

## **Diagrama de Componentes**
> _Proximamente_

# **Notas Técnicas**
- La comunicación entre cliente y servidor se realiza mediante **Java RMI** (Remote Method Invocation).
- La persistencia de datos se maneja con archivos **JSON** usando la librería Gson.
- La configuración del servidor (IP, puerto, nombre del servicio) se define en `server/config.properties`.
- Pueden presentarse ajustes de diseño durante el desarrollo debido al carácter académico del proyecto.

# **Cómo Inicializar el Programa**

### **1. Configurar el servidor**
Edita el archivo `server/config.properties` con la IP y puerto deseados:
```properties
SERVER_IP=localhost
SERVER_PORT=1808
SERVER_NAME=foodupb
```

### **2. Iniciar el servidor**
Desde la raíz del proyecto, en la terminal:
```bash
cd server
mvn exec:java
```

### **3. Iniciar el cliente**
En otra terminal:
```bash
cd client
mvn exec:java
```

### **Credenciales de prueba**
Usa las credenciales registradas en `server/src/main/resources/data/users.json` y `credentials.json`.

# **Integrantes del Equipo de Desarrollo**
- Adrián Felipe Forero Suárez ([fsadriann](https://github.com/fsadriann))
