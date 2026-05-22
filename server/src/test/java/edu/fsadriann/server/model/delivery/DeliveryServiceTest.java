package edu.fsadriann.server.model.delivery;

import edu.fsadriann.server.model.order.EstadoPedido;
import edu.fsadriann.server.model.order.Order;
import edu.fsadriann.server.model.order.OrderService;
import edu.fsadriann.server.model.product.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.rmi.RemoteException;

import static org.junit.jupiter.api.Assertions.*;

class DeliveryServiceTest {

    private DeliveryService deliveryService;
    private OrderService    orderService;

    @BeforeEach
    void setUp() {
        orderService    = new OrderService(null);
        deliveryService = new DeliveryService(null, orderService);
    }

    private Order crearPedidoListo(String cedula) throws RemoteException {
        Order pedido = orderService.crearPedido(cedula, false);
        Product p = new Product("d-" + cedula, "Arroz", "PLATO_PRINCIPAL", 20000, false);
        orderService.agregarProducto(pedido.getOrderId(), p);
        orderService.enviarPedidoACocina(pedido);
        orderService.marcarListo(pedido.getOrderId());
        return orderService.buscarPedido(pedido.getOrderId());
    }

    private Order crearPedidoEnCamino(String cedula) throws RemoteException {
        Order pedido = crearPedidoListo(cedula);
        orderService.marcarEnCamino(pedido.getOrderId());
        return orderService.buscarPedido(pedido.getOrderId());
    }

    // ── buscarEntregaPorPedido ────────────────────────────────────────────────

    @Test
    void buscarEntregaPorPedido_null_devuelveNull() throws RemoteException {
        assertNull(deliveryService.buscarEntregaPorPedido(null));
    }

    @Test
    void buscarEntregaPorPedido_inexistente_devuelveNull() throws RemoteException {
        assertNull(deliveryService.buscarEntregaPorPedido("id-no-existe"));
    }

    // ── yaEstaAsignado ────────────────────────────────────────────────────────

    @Test
    void yaEstaAsignado_sinAsignar_devuelveFalse() throws RemoteException {
        assertFalse(deliveryService.yaEstaAsignado("id-no-existe"));
    }

    // ── completarEntrega ──────────────────────────────────────────────────────

    @Test
    void completarEntrega_exitoso_desdeEnCamino() throws RemoteException {
        Order pedido = crearPedidoEnCamino("dc1");
        assertTrue(deliveryService.completarEntrega(pedido.getOrderId()));
        assertEquals(EstadoPedido.ENTREGADO,
                orderService.buscarPedido(pedido.getOrderId()).getEstado());
    }

    @Test
    void completarEntrega_falla_pedidoNoEnCamino() throws RemoteException {
        Order pedido = crearPedidoListo("dc2");
        assertFalse(deliveryService.completarEntrega(pedido.getOrderId()));
    }

    @Test
    void completarEntrega_falla_pedidoNull() throws RemoteException {
        assertFalse(deliveryService.completarEntrega(null));
    }

    @Test
    void completarEntrega_falla_pedidoInexistente() throws RemoteException {
        assertFalse(deliveryService.completarEntrega("id-no-existe"));
    }

    // ── iniciarEntrega ────────────────────────────────────────────────────────

    @Test
    void iniciarEntrega_falla_sinAsignacionPrevia() throws RemoteException {
        Order pedido = crearPedidoListo("dc3");
        assertFalse(deliveryService.iniciarEntrega(pedido.getOrderId()));
    }

    @Test
    void iniciarEntrega_falla_pedidoNull() throws RemoteException {
        assertFalse(deliveryService.iniciarEntrega(null));
    }
}
