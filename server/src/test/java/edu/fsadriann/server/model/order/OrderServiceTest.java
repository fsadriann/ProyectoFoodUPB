package edu.fsadriann.server.model.order;

import edu.fsadriann.app.linkedlist.singly.singly.LinkedList;
import edu.fsadriann.model.iterator.Iterator;
import edu.fsadriann.server.model.product.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.rmi.RemoteException;

import static org.junit.jupiter.api.Assertions.*;

class OrderServiceTest {

    private OrderService service;

    @BeforeEach
    void setUp() {
        service = new OrderService(null);
    }

    private Product producto(String id, String nombre, int precio, boolean complejo) {
        return new Product(id, nombre, "PLATO_PRINCIPAL", precio, complejo);
    }

    private int contar(LinkedList<Order> lista) {
        int count = 0;
        Iterator<Order> it = lista.iterator();
        while (it.hasNext()) {
            if (it.next() != null) count++;
        }
        return count;
    }

    // ── Creación ──────────────────────────────────────────────────────────────

    @Test
    void crearPedido_exitoso_estadoPendiente() throws RemoteException {
        Order pedido = service.crearPedido("111", false);
        assertNotNull(pedido);
        assertEquals("111", pedido.getCedulaCliente());
        assertEquals(EstadoPedido.PENDIENTE, pedido.getEstado());
    }

    @Test
    void crearPedido_falla_cedulaNula() {
        assertThrows(IllegalArgumentException.class, () -> service.crearPedido(null, false));
    }

    @Test
    void crearPedido_falla_cedulaVacia() {
        assertThrows(IllegalArgumentException.class, () -> service.crearPedido("  ", false));
    }

    // ── Carrito ───────────────────────────────────────────────────────────────

    @Test
    void agregarProducto_exitoso() throws RemoteException {
        Order pedido = service.crearPedido("222", false);
        assertTrue(service.agregarProducto(pedido.getOrderId(), producto("p1", "Burger", 18000, false)));
    }

    @Test
    void agregarProducto_falla_pedidoInexistente() throws RemoteException {
        assertFalse(service.agregarProducto("id-no-existe", producto("p2", "Pizza", 22000, false)));
    }

    @Test
    void quitarProducto_exitoso() throws RemoteException {
        Order pedido = service.crearPedido("333", false);
        Product p = producto("p3", "Gaseosa", 5000, false);
        service.agregarProducto(pedido.getOrderId(), p);
        assertTrue(service.quitarProducto(pedido.getOrderId(), p));
    }

    @Test
    void quitarProducto_falla_productoNoEnCarrito() throws RemoteException {
        Order pedido = service.crearPedido("444", false);
        assertFalse(service.quitarProducto(pedido.getOrderId(), producto("p4", "Papas", 8000, false)));
    }

    @Test
    void cambiarCantidadProducto_exitoso() throws RemoteException {
        Order pedido = service.crearPedido("555", false);
        Product p = producto("p5", "Papas", 8000, false);
        service.agregarProducto(pedido.getOrderId(), p);
        assertTrue(service.cambiarCantidadProducto(pedido.getOrderId(), "p5", 3));
    }

    @Test
    void cambiarCantidadProducto_falla_pedidoInexistente() throws RemoteException {
        assertFalse(service.cambiarCantidadProducto("id-no-existe", "p5", 2));
    }

    // ── Transiciones de estado ────────────────────────────────────────────────

    @Test
    void transicion_completa_estados() throws RemoteException {
        Order pedido = service.crearPedido("666", false);
        service.agregarProducto(pedido.getOrderId(), producto("p6", "Bandeja", 28000, true));

        service.enviarPedidoACocina(pedido);
        assertEquals(EstadoPedido.EN_PREPARACION,
                service.buscarPedido(pedido.getOrderId()).getEstado());

        service.marcarListo(pedido.getOrderId());
        assertEquals(EstadoPedido.LISTO,
                service.buscarPedido(pedido.getOrderId()).getEstado());

        service.marcarEnCamino(pedido.getOrderId());
        assertEquals(EstadoPedido.EN_CAMINO,
                service.buscarPedido(pedido.getOrderId()).getEstado());

        service.marcarEntregado(pedido.getOrderId());
        assertEquals(EstadoPedido.ENTREGADO,
                service.buscarPedido(pedido.getOrderId()).getEstado());
    }

    @Test
    void cancelarPedido_exitoso_desdePendiente() throws RemoteException {
        Order pedido = service.crearPedido("777", false);
        assertTrue(service.cancelarPedido(pedido.getOrderId()));
        assertEquals(EstadoPedido.CANCELADO,
                service.buscarPedido(pedido.getOrderId()).getEstado());
    }

    @Test
    void cancelarPedido_exitoso_desdeEnPreparacion() throws RemoteException {
        Order pedido = service.crearPedido("888", false);
        service.agregarProducto(pedido.getOrderId(), producto("p7", "Arroz", 20000, false));
        service.enviarPedidoACocina(pedido);
        assertTrue(service.cancelarPedido(pedido.getOrderId()));
    }

    @Test
    void cancelarPedido_falla_desdeEntregado() throws RemoteException {
        Order pedido = service.crearPedido("999", false);
        service.agregarProducto(pedido.getOrderId(), producto("p8", "Limonada", 6000, false));
        service.enviarPedidoACocina(pedido);
        service.marcarListo(pedido.getOrderId());
        service.marcarEnCamino(pedido.getOrderId());
        service.marcarEntregado(pedido.getOrderId());
        assertThrows(IllegalStateException.class,
                () -> service.cancelarPedido(pedido.getOrderId()));
    }

    @Test
    void enviarPedidoACocina_falla_sinProductos() throws RemoteException {
        Order pedido = service.crearPedido("101", false);
        assertThrows(IllegalStateException.class,
                () -> service.enviarPedidoACocina(pedido));
    }

    // ── Factura ───────────────────────────────────────────────────────────────

    @Test
    void calcularFactura_premium_dominioGratis() throws RemoteException {
        Order pedido = service.crearPedido("102", true);
        service.agregarProducto(pedido.getOrderId(), producto("p9", "Jugo", 6000, false));
        double total = service.calcularFactura(pedido);
        // 6000 + IVA 19% + 0 domicilio = 7140
        assertEquals(7140.0, total, 0.01);
    }

    @Test
    void calcularFactura_standard_dominioFallback() throws RemoteException {
        Order pedido = service.crearPedido("103", false);
        service.agregarProducto(pedido.getOrderId(), producto("p10", "Brownie", 7000, false));
        double total = service.calcularFactura(pedido);
        // 7000 + IVA 19% + 5000 fallback = 13330
        assertEquals(13330.0, total, 0.01);
    }

    // ── Cuadrante destino ─────────────────────────────────────────────────────

    @Test
    void asignarCuadranteDestino_exitoso() throws RemoteException {
        Order pedido = service.crearPedido("104", false);
        assertTrue(service.asignarCuadranteDestino(pedido.getOrderId(), "Laureles"));
        assertEquals("Laureles",
                service.buscarPedido(pedido.getOrderId()).getCuadranteDestino());
    }

    @Test
    void asignarCuadranteDestino_falla_cuadranteVacio() {
        Order pedido;
        try {
            pedido = service.crearPedido("105", false);
            assertThrows(IllegalArgumentException.class,
                    () -> service.asignarCuadranteDestino(pedido.getOrderId(), ""));
        } catch (RemoteException ignored) {}
    }

    // ── Consultas ─────────────────────────────────────────────────────────────

    @Test
    void buscarPedido_inexistente_devuelveNull() throws RemoteException {
        assertNull(service.buscarPedido("id-no-existe"));
    }

    @Test
    void getPedidosPorCliente_filtraCorrectamente() throws RemoteException {
        service.crearPedido("cedula-A", false);
        service.crearPedido("cedula-A", false);
        service.crearPedido("cedula-B", false);
        assertEquals(2, contar(service.getPedidosPorCliente("cedula-A")));
        assertEquals(1, contar(service.getPedidosPorCliente("cedula-B")));
    }

    @Test
    void getPedidosEnPreparacion_filtraCorrectamente() throws RemoteException {
        Order p1 = service.crearPedido("ced-x", false);
        service.agregarProducto(p1.getOrderId(), producto("px1", "Pizza", 22000, false));
        service.enviarPedidoACocina(p1);

        service.crearPedido("ced-y", false);

        LinkedList<Order> enPrep = service.getPedidosEnPreparacion();
        assertEquals(1, contar(enPrep));
    }
}
