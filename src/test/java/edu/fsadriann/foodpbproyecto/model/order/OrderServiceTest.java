package edu.fsadriann.foodpbproyecto.model.order;

import edu.fsadriann.foodpbproyecto.model.product.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.rmi.RemoteException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests de OrderService para RF-02, RF-03 y RF-04.
 *
 * <ul>
 * <li>RF-02 – crearPedido, enviarPedidoACocina</li>
 * <li>RF-03 – calcularFactura (subtotal + IVA + domicilio)</li>
 * <li>RF-04 – buscarPedido, modificarPedido, cancelarPedido, agregar/quitar
 * producto</li>
 * </ul>
 *
 * @author fsadriann
 */
@DisplayName("OrderService – RF-02, RF-03 y RF-04")
class OrderServiceTest {

    private OrderService service;

    private static final String CEDULA = "1001";
    private static final double IVA = 0.19;
    private static final double DOMI_STD = 5_000.0;

    // ── Fixture ───────────────────────────────────────────────────────────────

    @BeforeEach
    void setUp() {
        service = new OrderService();
    }

    private Product producto(String id, int precio, int cantidad) {
        Product p = new Product(id, "Producto-" + id, "PLATO_PRINCIPAL", precio, false);
        p.setCantidad(cantidad);
        return p;
    }

    // ── RF-02: crearPedido ────────────────────────────────────────────────────

    @Test
    @DisplayName("RF-02 | Crear pedido estándar → estado PENDIENTE")
    void crear_pedidoEstandar_estadoPendiente() throws RemoteException {
        Order o = service.crearPedido(CEDULA, false);
        assertNotNull(o);
        assertNotNull(o.getOrderId(), "Debe tener UUID asignado");
        assertEquals(EstadoPedido.PENDIENTE, o.getEstado());
        assertFalse(o.isPremium());
        assertEquals(CEDULA, o.getCedulaCliente());
    }

    @Test
    @DisplayName("RF-02 | Crear pedido premium → isPremium true")
    void crear_pedidoPremium_flagCorrecto() throws RemoteException {
        Order o = service.crearPedido(CEDULA, true);
        assertTrue(o.isPremium());
    }

    @Test
    @DisplayName("RF-02 | Crear pedido con cédula nula → excepción")
    void crear_cedulaNula_lanzaExcepcion() {
        assertThrows(IllegalArgumentException.class,
                () -> service.crearPedido(null, false));
    }

    @Test
    @DisplayName("RF-02 | Crear pedido con cédula vacía → excepción")
    void crear_cedulaVacia_lanzaExcepcion() {
        assertThrows(IllegalArgumentException.class,
                () -> service.crearPedido("   ", false));
    }

    @Test
    @DisplayName("RF-02 | IDs de pedidos distintos entre creaciones")
    void crear_dosVeces_idsDistintos() throws RemoteException {
        Order o1 = service.crearPedido(CEDULA, false);
        Order o2 = service.crearPedido(CEDULA, false);
        assertNotEquals(o1.getOrderId(), o2.getOrderId());
    }

    // ── RF-02: enviarPedidoACocina ────────────────────────────────────────────

    @Test
    @DisplayName("RF-02 | Enviar a cocina con producto → estado EN_PREPARACION")
    void enviarCocina_conProducto_estadoEnPreparacion() throws RemoteException {
        Order o = service.crearPedido(CEDULA, false);
        o.agregarProducto(producto("P1", 20_000, 1));
        service.enviarPedidoACocina(o);
        assertEquals(EstadoPedido.EN_PREPARACION, o.getEstado());
    }

    @Test
    @DisplayName("RF-02 | Enviar a cocina sin productos → excepción")
    void enviarCocina_sinProductos_lanzaExcepcion() throws RemoteException {
        Order o = service.crearPedido(CEDULA, false);
        assertThrows(IllegalStateException.class,
                () -> service.enviarPedidoACocina(o));
    }

    @Test
    @DisplayName("RF-02 | Enviar a cocina ya EN_PREPARACION → excepción")
    void enviarCocina_estadoInvalido_lanzaExcepcion() throws RemoteException {
        Order o = service.crearPedido(CEDULA, false);
        o.agregarProducto(producto("P1", 20_000, 1));
        service.enviarPedidoACocina(o);
        assertThrows(IllegalStateException.class,
                () -> service.enviarPedidoACocina(o));
    }

    // ── RF-03: calcularFactura ────────────────────────────────────────────────

    @Test
    @DisplayName("RF-03 | Factura cliente estándar: subtotal + IVA + domicilio")
    void factura_clienteEstandar_calculos_correctos() throws RemoteException {
        Order o = service.crearPedido(CEDULA, false);
        o.agregarProducto(producto("P1", 20_000, 2)); // 40.000
        o.agregarProducto(producto("P2", 10_000, 1)); // 10.000 → subtotal = 50.000

        double total = service.calcularFactura(o);

        double subtotalEsperado = 50_000.0;
        double ivaEsperado = subtotalEsperado * IVA; // 9.500
        double domiEsperado = DOMI_STD; // 5.000
        double totalEsperado = subtotalEsperado + ivaEsperado + domiEsperado; // 64.500

        assertEquals(subtotalEsperado, o.getSubtotal(), 0.01);
        assertEquals(ivaEsperado, o.getImpuesto(), 0.01);
        assertEquals(domiEsperado, o.getCostoDomi(), 0.01);
        assertEquals(totalEsperado, total, 0.01);
    }

    @Test
    @DisplayName("RF-03 | Factura cliente premium: domicilio = $0")
    void factura_clientePremium_domicilioGratis() throws RemoteException {
        Order o = service.crearPedido(CEDULA, true);
        o.agregarProducto(producto("P1", 30_000, 1));

        service.calcularFactura(o);

        assertEquals(0.0, o.getCostoDomi(), 0.01, "Premium no paga domicilio");
        assertEquals(30_000 * (1 + IVA), o.getTotal(), 0.01);
    }

    @Test
    @DisplayName("RF-03 | Factura pedido vacío → total = domicilio")
    void factura_carritoVacio_totalEsDomicilio() throws RemoteException {
        Order o = service.crearPedido(CEDULA, false);
        service.calcularFactura(o);
        assertEquals(0.0, o.getSubtotal(), 0.01);
        assertEquals(0.0, o.getImpuesto(), 0.01);
        assertEquals(DOMI_STD, o.getTotal(), 0.01);
    }

    @Test
    @DisplayName("RF-03 | Factura pedido nulo → excepción")
    void factura_pedidoNulo_lanzaExcepcion() {
        assertThrows(IllegalArgumentException.class,
                () -> service.calcularFactura(null));
    }

    // ── RF-04: buscarPedido ───────────────────────────────────────────────────

    @Test
    @DisplayName("RF-04 | Buscar pedido existente → retorna pedido")
    void buscar_pedidoExistente_retornaPedido() throws RemoteException {
        Order o = service.crearPedido(CEDULA, false);
        Order encontrado = service.buscarPedido(o.getOrderId());
        assertNotNull(encontrado);
        assertEquals(o.getOrderId(), encontrado.getOrderId());
    }

    @Test
    @DisplayName("RF-04 | Buscar pedido inexistente → null")
    void buscar_pedidoInexistente_retornaNull() throws RemoteException {
        assertNull(service.buscarPedido("uuid-que-no-existe"));
    }

    @Test
    @DisplayName("RF-04 | Buscar con ID nulo → null sin excepción")
    void buscar_idNulo_retornaNull() throws RemoteException {
        assertNull(service.buscarPedido(null));
    }

    // ── RF-04: modificarPedido ────────────────────────────────────────────────

    @Test
    @DisplayName("RF-04 | Modificar en PENDIENTE → éxito")
    void modificar_estadoPendiente_retornaTrue() throws RemoteException {
        Order o = service.crearPedido(CEDULA, false);
        o.agregarProducto(producto("P1", 10_000, 1));
        assertTrue(service.modificarPedido(o));
    }

    @Test
    @DisplayName("RF-04 | Modificar en EN_PREPARACION → excepción")
    void modificar_estadoEnPreparacion_lanzaExcepcion() throws RemoteException {
        Order o = service.crearPedido(CEDULA, false);
        o.agregarProducto(producto("P1", 10_000, 1));
        service.enviarPedidoACocina(o);
        assertThrows(IllegalStateException.class, () -> service.modificarPedido(o));
    }

    // ── RF-04: cancelarPedido ─────────────────────────────────────────────────

    @Test
    @DisplayName("RF-04 | Cancelar desde PENDIENTE → estado CANCELADO")
    void cancelar_desdePendiente_exitoso() throws RemoteException {
        Order o = service.crearPedido(CEDULA, false);
        assertTrue(service.cancelarPedido(o.getOrderId()));
        assertEquals(EstadoPedido.CANCELADO,
                service.buscarPedido(o.getOrderId()).getEstado());
    }

    @Test
    @DisplayName("RF-04 | Cancelar desde EN_PREPARACION → estado CANCELADO")
    void cancelar_desdeEnPreparacion_exitoso() throws RemoteException {
        Order o = service.crearPedido(CEDULA, false);
        o.agregarProducto(producto("P1", 10_000, 1));
        service.enviarPedidoACocina(o);
        assertTrue(service.cancelarPedido(o.getOrderId()));
        assertEquals(EstadoPedido.CANCELADO,
                service.buscarPedido(o.getOrderId()).getEstado());
    }

    @Test
    @DisplayName("RF-04 | Cancelar desde LISTO → IllegalStateException")
    void cancelar_desdeListo_lanzaExcepcion() throws RemoteException {
        // Crear pedido, enviarlo a cocina (EN_PREPARACION) y simular que cocina lo
        // terminó
        Order o = service.crearPedido("LISTO-TEST", false);
        o.agregarProducto(producto("P1", 5_000, 1));
        service.enviarPedidoACocina(o); // → EN_PREPARACION

        // buscarPedido retorna la misma referencia que está en la DoublyLinkedList
        Order ref = service.buscarPedido(o.getOrderId());
        ref.setEstado(EstadoPedido.LISTO); // simular cocina terminó

        // Ahora cancelar debe lanzar IllegalStateException
        assertThrows(IllegalStateException.class,
                () -> service.cancelarPedido(ref.getOrderId()));
    }

    @Test
    @DisplayName("RF-04 | Cancelar pedido inexistente → false")
    void cancelar_pedidoInexistente_retornaFalse() throws RemoteException {
        assertFalse(service.cancelarPedido("uuid-inventado"));
    }

    // ── RF-04: agregar / quitar producto ─────────────────────────────────────

    @Test
    @DisplayName("RF-04 | Agregar producto en PENDIENTE → cantProductos sube")
    void agregar_productoEnPendiente_incrementaCantidad() throws RemoteException {
        Order o = service.crearPedido(CEDULA, false);
        assertEquals(0, o.getCantProductos());
        service.agregarProducto(o.getOrderId(), producto("P1", 10_000, 1));
        assertEquals(1, service.buscarPedido(o.getOrderId()).getCantProductos());
    }

    @Test
    @DisplayName("RF-04 | Agregar producto en EN_PREPARACION → excepción")
    void agregar_productoEnPreparacion_lanzaExcepcion() throws RemoteException {
        Order o = service.crearPedido(CEDULA, false);
        o.agregarProducto(producto("P1", 10_000, 1));
        service.enviarPedidoACocina(o);
        assertThrows(IllegalStateException.class,
                () -> service.agregarProducto(o.getOrderId(), producto("P2", 5_000, 1)));
    }

    @Test
    @DisplayName("RF-04 | Quitar producto existente en PENDIENTE → éxito")
    void quitar_productoExistente_retornaTrue() throws RemoteException {
        Order o = service.crearPedido(CEDULA, false);
        Product p = producto("P1", 10_000, 1);
        service.agregarProducto(o.getOrderId(), p);
        assertTrue(service.quitarProducto(o.getOrderId(), p));
        assertEquals(0, service.buscarPedido(o.getOrderId()).getCantProductos());
    }

    @Test
    @DisplayName("RF-04 | Historial de pedidos de un cliente")
    void historial_clienteConPedidos_retornaLista() throws RemoteException {
        service.crearPedido(CEDULA, false);
        service.crearPedido(CEDULA, true);
        service.crearPedido("9999", false); // otro cliente

        var historial = service.getPedidosPorCliente(CEDULA);
        assertNotNull(historial);

        // Contar usando Iterator tipado del JAR
        int count = 0;
        edu.fsadriann.model.iterator.Iterator<Order> it = historial.iterator();
        while (it.hasNext()) {
            Order o = it.next();
            if (o != null && CEDULA.equals(o.getCedulaCliente()))
                count++;
        }
        assertEquals(2, count, "Solo deben aparecer los pedidos del cliente 1001");
    }

    // ── RF-13: asignarCuadranteDestino ───────────────────────────────────────

    /** Construye un CuadranteService mínimo con UPB y Cabecera conectados. */
    private edu.fsadriann.foodpbproyecto.model.cuadrante.CuadranteService mapaBasico() {
        edu.fsadriann.foodpbproyecto.model.cuadrante.CuadranteService cs =
                new edu.fsadriann.foodpbproyecto.model.cuadrante.CuadranteService();
        cs.agregarCuadrante(new edu.fsadriann.foodpbproyecto.model.cuadrante.Cuadrante("UPB",      "Campus UPB"));
        cs.agregarCuadrante(new edu.fsadriann.foodpbproyecto.model.cuadrante.Cuadrante("Cabecera", "Barrio Cabecera"));
        cs.agregarCuadrante(new edu.fsadriann.foodpbproyecto.model.cuadrante.Cuadrante("Laureles", "Barrio Laureles"));
        cs.conectarCuadrantes("UPB",      "Laureles",  1.2);
        cs.conectarCuadrantes("Laureles", "Cabecera",  2.0);
        return cs;
    }

    @Test
    @DisplayName("RF-13 | Asignar cuadrante existente → true")
    void asignar_cuadranteExistente_retornaTrue() throws RemoteException {
        var cs = mapaBasico();
        Order o = service.crearPedido(CEDULA, false);
        assertTrue(service.asignarCuadranteDestino(o.getOrderId(), "Cabecera", cs));
    }

    @Test
    @DisplayName("RF-13 | Asignar guarda cuadranteDestino en el pedido")
    void asignar_guardaEnPedido() throws RemoteException {
        var cs = mapaBasico();
        Order o = service.crearPedido(CEDULA, false);
        service.asignarCuadranteDestino(o.getOrderId(), "Cabecera", cs);
        assertEquals("Cabecera",
                service.buscarPedido(o.getOrderId()).getCuadranteDestino());
    }

    @Test
    @DisplayName("RF-13 | Reasignar cuadrante → actualiza al nuevo")
    void asignar_reasignar_actualizaCorrectamente() throws RemoteException {
        var cs = mapaBasico();
        Order o = service.crearPedido(CEDULA, false);
        service.asignarCuadranteDestino(o.getOrderId(), "Cabecera", cs);
        service.asignarCuadranteDestino(o.getOrderId(), "Laureles", cs);
        assertEquals("Laureles",
                service.buscarPedido(o.getOrderId()).getCuadranteDestino());
    }

    @Test
    @DisplayName("RF-13 | Cuadrante no registrado → false")
    void asignar_cuadranteInexistente_retornaFalse() throws RemoteException {
        var cs = mapaBasico();
        Order o = service.crearPedido(CEDULA, false);
        assertFalse(service.asignarCuadranteDestino(o.getOrderId(), "NoExiste", cs));
    }

    @Test
    @DisplayName("RF-13 | Pedido inexistente → false")
    void asignar_pedidoInexistente_retornaFalse() {
        var cs = mapaBasico();
        assertFalse(service.asignarCuadranteDestino("uuid-inventado", "Cabecera", cs));
    }

    @Test
    @DisplayName("RF-13 | Pedido CANCELADO → false")
    void asignar_pedidoCancelado_retornaFalse() throws RemoteException {
        var cs = mapaBasico();
        Order o = service.crearPedido(CEDULA, false);
        service.cancelarPedido(o.getOrderId());
        assertFalse(service.asignarCuadranteDestino(o.getOrderId(), "Cabecera", cs));
    }

    @Test
    @DisplayName("RF-13 | Nombre cuadrante nulo → IllegalArgumentException")
    void asignar_nombreNulo_lanzaExcepcion() throws RemoteException {
        var cs = mapaBasico();
        Order o = service.crearPedido(CEDULA, false);
        assertThrows(IllegalArgumentException.class,
                () -> service.asignarCuadranteDestino(o.getOrderId(), null, cs));
    }

    @Test
    @DisplayName("RF-13 | Nombre cuadrante vacío → IllegalArgumentException")
    void asignar_nombreVacio_lanzaExcepcion() throws RemoteException {
        var cs = mapaBasico();
        Order o = service.crearPedido(CEDULA, false);
        assertThrows(IllegalArgumentException.class,
                () -> service.asignarCuadranteDestino(o.getOrderId(), "  ", cs));
    }

    // ── RF-13: calcularRutaEntrega ────────────────────────────────────────────

    @Test
    @DisplayName("RF-13 | Flujo completo: crear → asignar → calcular ruta")
    void ruta_flujoCompleto_retornaRuta() throws RemoteException {
        var cs = mapaBasico();
        Order o = service.crearPedido(CEDULA, false);
        service.asignarCuadranteDestino(o.getOrderId(), "Cabecera", cs);

        var ruta = service.calcularRutaEntrega(o.getOrderId(), cs);

        assertNotNull(ruta, "Debe existir ruta UPB → Cabecera");
        // Verificar que la ruta contiene al menos 2 nodos
        edu.fsadriann.model.iterator.Iterator<String> it = ruta.iterator();
        int nodos = 0;
        while (it.hasNext()) { it.next(); nodos++; }
        assertTrue(nodos >= 2, "La ruta debe tener al menos 2 nodos");
    }

    @Test
    @DisplayName("RF-13 | Sin cuadranteDestino asignado → null")
    void ruta_sinDestino_retornaNull() throws RemoteException {
        var cs = mapaBasico();
        Order o = service.crearPedido(CEDULA, false);
        assertNull(service.calcularRutaEntrega(o.getOrderId(), cs));
    }

    @Test
    @DisplayName("RF-13 | Pedido inexistente → null")
    void ruta_pedidoInexistente_retornaNull() {
        var cs = mapaBasico();
        assertNull(service.calcularRutaEntrega("uuid-inventado", cs));
    }

    @Test
    @DisplayName("RF-13 | Pedido CANCELADO → null")
    void ruta_pedidoCancelado_retornaNull() throws RemoteException {
        var cs = mapaBasico();
        Order o = service.crearPedido(CEDULA, false);
        service.asignarCuadranteDestino(o.getOrderId(), "Cabecera", cs);
        service.cancelarPedido(o.getOrderId());
        assertNull(service.calcularRutaEntrega(o.getOrderId(), cs));
    }

    @Test
    @DisplayName("RF-13 | Origen UPB no en grafo → null")
    void ruta_origenBaseNoExiste_retornaNull() throws RemoteException {
        // Grafo sin UPB
        edu.fsadriann.foodpbproyecto.model.cuadrante.CuadranteService csSinBase =
                new edu.fsadriann.foodpbproyecto.model.cuadrante.CuadranteService();
        csSinBase.agregarCuadrante(
                new edu.fsadriann.foodpbproyecto.model.cuadrante.Cuadrante("Cabecera", "Zona"));

        Order o = service.crearPedido(CEDULA, false);
        // Asignamos directamente en el modelo (el service no lo validaría sin UPB)
        o.setCuadranteDestino("Cabecera");

        assertNull(service.calcularRutaEntrega(o.getOrderId(), csSinBase),
                "Si UPB no está en el grafo, no se puede calcular ruta");
    }

    // ── RF-13-C: marcarEnCamino ───────────────────────────────────────────────

    @Test
    @DisplayName("RF-13-C | LISTO → EN_CAMINO exitoso")
    void marcarEnCamino_desdeListo_exitoso() throws RemoteException {
        Order o = service.crearPedido(CEDULA, false);
        o.setEstado(EstadoPedido.LISTO); // simular cocina terminó
        assertTrue(service.marcarEnCamino(o.getOrderId()));
        assertEquals(EstadoPedido.EN_CAMINO,
                service.buscarPedido(o.getOrderId()).getEstado());
    }

    @Test
    @DisplayName("RF-13-C | Estado inválido (PENDIENTE) → IllegalStateException")
    void marcarEnCamino_estadoInvalido_lanzaExcepcion() throws RemoteException {
        Order o = service.crearPedido(CEDULA, false); // PENDIENTE
        assertThrows(IllegalStateException.class,
                () -> service.marcarEnCamino(o.getOrderId()));
    }

    @Test
    @DisplayName("RF-13-C | Pedido inexistente → false")
    void marcarEnCamino_pedidoInexistente_retornaFalse() {
        assertFalse(service.marcarEnCamino("uuid-inventado"));
    }

    // ── RF-13-D: marcarEntregado ──────────────────────────────────────────────

    @Test
    @DisplayName("RF-13-D | EN_CAMINO → ENTREGADO exitoso")
    void marcarEntregado_desdeEnCamino_exitoso() throws RemoteException {
        Order o = service.crearPedido(CEDULA, false);
        o.setEstado(EstadoPedido.EN_CAMINO); // simular en tránsito
        assertTrue(service.marcarEntregado(o.getOrderId()));
        assertEquals(EstadoPedido.ENTREGADO,
                service.buscarPedido(o.getOrderId()).getEstado());
    }

    @Test
    @DisplayName("RF-13-D | Estado inválido (LISTO, no EN_CAMINO) → IllegalStateException")
    void marcarEntregado_estadoInvalido_lanzaExcepcion() throws RemoteException {
        Order o = service.crearPedido(CEDULA, false);
        o.setEstado(EstadoPedido.LISTO); // LISTO, no EN_CAMINO
        assertThrows(IllegalStateException.class,
                () -> service.marcarEntregado(o.getOrderId()));
    }

    @Test
    @DisplayName("RF-13-D | Pedido inexistente → false")
    void marcarEntregado_pedidoInexistente_retornaFalse() {
        assertFalse(service.marcarEntregado("uuid-inventado"));
    }

    // ── A-02: marcarListo ─────────────────────────────────────────────────────

    @Test
    @DisplayName("A-02 | EN_PREPARACION → LISTO exitoso")
    void marcarListo_desdeEnPreparacion_exitoso() throws RemoteException {
        Order o = service.crearPedido(CEDULA, false);
        o.setEstado(EstadoPedido.EN_PREPARACION); // simular asignación a fogón
        assertTrue(service.marcarListo(o.getOrderId()));
        assertEquals(EstadoPedido.LISTO,
                service.buscarPedido(o.getOrderId()).getEstado());
    }

    @Test
    @DisplayName("A-02 | Estado inválido (PENDIENTE, no EN_PREPARACION) → IllegalStateException")
    void marcarListo_estadoInvalido_lanzaExcepcion() throws RemoteException {
        Order o = service.crearPedido(CEDULA, false); // PENDIENTE
        assertThrows(IllegalStateException.class,
                () -> service.marcarListo(o.getOrderId()));
    }

    @Test
    @DisplayName("A-02 | Pedido inexistente → false")
    void marcarListo_pedidoInexistente_retornaFalse() {
        assertFalse(service.marcarListo("uuid-inventado"));
    }
}
