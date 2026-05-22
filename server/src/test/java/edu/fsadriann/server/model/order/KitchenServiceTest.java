package edu.fsadriann.server.model.order;

import edu.fsadriann.server.model.product.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.rmi.RemoteException;

import static org.junit.jupiter.api.Assertions.*;

class KitchenServiceTest {

    private KitchenService kitchenService;
    private OrderService   orderService;

    @BeforeEach
    void setUp() {
        orderService   = new OrderService(null);
        kitchenService = new KitchenService(orderService);
    }

    private Order pedidoEnPreparacion(String cedula, boolean premium, boolean complejo)
            throws RemoteException {
        Order pedido = orderService.crearPedido(cedula, premium);
        Product p = new Product("k-" + cedula, "Producto", "PLATO_PRINCIPAL", 10000, complejo);
        orderService.agregarProducto(pedido.getOrderId(), p);
        orderService.enviarPedidoACocina(pedido);
        return orderService.buscarPedido(pedido.getOrderId());
    }

    // ── encolarPedido ─────────────────────────────────────────────────────────

    @Test
    void encolarPedido_exitoso_aumentaTamano() throws RemoteException {
        Order pedido = pedidoEnPreparacion("k1", false, false);
        kitchenService.encolarPedido(pedido);
        assertEquals(1, kitchenService.tamanoCola());
    }

    @Test
    void encolarPedido_falla_null() {
        assertThrows(IllegalArgumentException.class,
                () -> kitchenService.encolarPedido(null));
    }

    @Test
    void encolarPedido_falla_cancelado() throws RemoteException {
        Order pedido = orderService.crearPedido("k2", false);
        orderService.cancelarPedido(pedido.getOrderId());
        Order cancelado = orderService.buscarPedido(pedido.getOrderId());
        assertThrows(IllegalStateException.class,
                () -> kitchenService.encolarPedido(cancelado));
    }

    @Test
    void colaVacia_inicialmente() {
        assertTrue(kitchenService.colaVacia());
    }

    // ── procesarSiguientePedido ───────────────────────────────────────────────

    @Test
    void procesarSiguientePedido_pedidoSimple_asignaFogon() throws RemoteException {
        Order pedido = pedidoEnPreparacion("k3", false, false);
        kitchenService.encolarPedido(pedido);
        Order procesado = kitchenService.procesarSiguientePedido();
        assertNotNull(procesado);
        assertTrue(kitchenService.colaVacia());
    }

    @Test
    void procesarSiguientePedido_pedidoComplejo_usaFogonGrande() throws RemoteException {
        Order pedido = pedidoEnPreparacion("k4", false, true);
        kitchenService.encolarPedido(pedido);
        kitchenService.procesarSiguientePedido();
        assertTrue(kitchenService.obtenerEstadoFogones()
                .contains("FOGON_GRANDE"));
    }

    @Test
    void procesarSiguientePedido_colaVacia_devuelveNull() {
        assertNull(kitchenService.procesarSiguientePedido());
    }

    @Test
    void prioridad_premiumSaleAnteStandard() throws RemoteException {
        Order std     = pedidoEnPreparacion("k5", false, false);
        Order premium = pedidoEnPreparacion("k6", true,  false);
        kitchenService.encolarPedido(std);
        kitchenService.encolarPedido(premium);
        Order primero = kitchenService.procesarSiguientePedido();
        assertNotNull(primero);
        assertTrue(primero.isPremium());
    }

    // ── marcarPedidoListo ─────────────────────────────────────────────────────

    @Test
    void marcarPedidoListo_exitoso() throws RemoteException {
        Order pedido = pedidoEnPreparacion("k7", false, false);
        kitchenService.encolarPedido(pedido);
        kitchenService.procesarSiguientePedido();
        assertTrue(kitchenService.marcarPedidoListo(pedido.getOrderId()));
    }

    @Test
    void marcarPedidoListo_falla_pedidoNoEnFogon() {
        assertThrows(IllegalStateException.class,
                () -> kitchenService.marcarPedidoListo("id-no-en-fogon"));
    }

    @Test
    void marcarPedidoListo_falla_idNull() {
        assertThrows(IllegalArgumentException.class,
                () -> kitchenService.marcarPedidoListo(null));
    }

    // ── Estado fogones ────────────────────────────────────────────────────────

    @Test
    void obtenerEstadoFogones_contieneLosCuatroFogones() {
        String estado = kitchenService.obtenerEstadoFogones();
        assertTrue(estado.contains("FOGON_GRANDE"));
        assertTrue(estado.contains("FOGON_NORMAL_1"));
        assertTrue(estado.contains("FOGON_NORMAL_2"));
        assertTrue(estado.contains("FOGON_NORMAL_3"));
    }

    @Test
    void obtenerEstadoFogones_inicialmenteTodosLibres() {
        String estado = kitchenService.obtenerEstadoFogones();
        assertEquals(4, contarOcurrencias(estado, "LIBRE"));
    }

    private int contarOcurrencias(String texto, String patron) {
        int count = 0;
        int idx   = 0;
        while ((idx = texto.indexOf(patron, idx)) != -1) {
            count++;
            idx += patron.length();
        }
        return count;
    }
}
