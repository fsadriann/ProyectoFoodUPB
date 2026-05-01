package edu.fsadriann.foodpbproyecto.model.delivery;

import edu.fsadriann.foodpbproyecto.model.cuadrante.Cuadrante;
import edu.fsadriann.foodpbproyecto.model.cuadrante.CuadranteService;
import edu.fsadriann.foodpbproyecto.model.order.EstadoPedido;
import edu.fsadriann.foodpbproyecto.model.order.Order;
import edu.fsadriann.foodpbproyecto.model.order.OrderService;
import edu.fsadriann.foodpbproyecto.model.product.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.rmi.RemoteException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests de DeliveryService para RF-13 (flujo de entrega).
 *
 * <p>Cubre: asignación de repartidor, ciclo LISTO→EN_CAMINO→ENTREGADO,
 * prevención de duplicados, validaciones de entrada y casos borde.
 *
 * <p><b>Fixture de estado LISTO:</b> los tests crean un Order en PENDIENTE
 * y lo elevan a LISTO directamente con {@code setEstado()} para no depender
 * de {@code KitchenService} en estos tests unitarios.
 *
 * @author fsadriann
 */
@DisplayName("DeliveryService – RF-13 Entrega")
class DeliveryServiceTest {

    private OrderService    orderService;
    private DeliveryService deliveryService;
    private CuadranteService cuadranteService;

    private static final String REPARTIDOR = "REP-01";

    // ── Fixture ───────────────────────────────────────────────────────────────

    @BeforeEach
    void setUp() {
        orderService    = new OrderService();
        deliveryService = new DeliveryService();
        cuadranteService = buildMapa();
    }

    /** Mapa mínimo: UPB → Laureles → Cabecera */
    private CuadranteService buildMapa() {
        CuadranteService cs = new CuadranteService();
        cs.agregarCuadrante(new Cuadrante("UPB",      "Campus UPB"));
        cs.agregarCuadrante(new Cuadrante("Laureles",  "Barrio Laureles"));
        cs.agregarCuadrante(new Cuadrante("Cabecera",  "Barrio Cabecera"));
        cs.conectarCuadrantes("UPB",      "Laureles", 1.2);
        cs.conectarCuadrantes("Laureles", "Cabecera", 2.0);
        return cs;
    }

    /** Crea un pedido ya en LISTO con cuadranteDestino asignado. Listo para asignar. */
    private Order pedidoListo(String cedula) throws RemoteException {
        Order o = orderService.crearPedido(cedula, false);
        o.agregarProducto(new Product("P1", "Plato", "PLATO_PRINCIPAL", 25_000, true));
        orderService.asignarCuadranteDestino(o.getOrderId(), "Cabecera", cuadranteService);
        o.setEstado(EstadoPedido.LISTO); // bypass kitchen para test unitario
        return o;
    }

    // ── asignarPedidoARepartidor ──────────────────────────────────────────────

    @Test
    @DisplayName("RF-13 | Asignar pedido LISTO → Delivery creado")
    void asignar_pedidoListo_retornaDelivery() throws RemoteException {
        Order o = pedidoListo("1001");
        Delivery d = deliveryService.asignarPedidoARepartidor(
                o.getOrderId(), REPARTIDOR, cuadranteService, orderService);
        assertNotNull(d);
    }

    @Test
    @DisplayName("RF-13 | Delivery tiene ruta calculada (no nula)")
    void asignar_deliveryContieneRuta() throws RemoteException {
        Order o = pedidoListo("1001");
        Delivery d = deliveryService.asignarPedidoARepartidor(
                o.getOrderId(), REPARTIDOR, cuadranteService, orderService);
        assertNotNull(d);
        assertNotNull(d.getRuta(), "La ruta no puede ser null");
    }

    @Test
    @DisplayName("RF-13 | Delivery tiene repartidorId correcto")
    void asignar_repartidorIdCorrecto() throws RemoteException {
        Order o = pedidoListo("1001");
        Delivery d = deliveryService.asignarPedidoARepartidor(
                o.getOrderId(), REPARTIDOR, cuadranteService, orderService);
        assertNotNull(d);
        assertEquals(REPARTIDOR, d.getRepartidorId());
    }

    @Test
    @DisplayName("RF-13 | Delivery tiene orderId correcto")
    void asignar_orderIdCorrecto() throws RemoteException {
        Order o = pedidoListo("1001");
        Delivery d = deliveryService.asignarPedidoARepartidor(
                o.getOrderId(), REPARTIDOR, cuadranteService, orderService);
        assertNotNull(d);
        assertEquals(o.getOrderId(), d.getOrderId());
    }

    @Test
    @DisplayName("RF-13 | Pedido ya asignado → null (no duplica)")
    void asignar_duplicado_retornaNull() throws RemoteException {
        Order o = pedidoListo("1001");
        deliveryService.asignarPedidoARepartidor(
                o.getOrderId(), REPARTIDOR, cuadranteService, orderService);
        // Segunda asignación al mismo pedido
        assertNull(deliveryService.asignarPedidoARepartidor(
                o.getOrderId(), "REP-02", cuadranteService, orderService));
    }

    @Test
    @DisplayName("RF-13 | Pedido no LISTO (PENDIENTE) → null")
    void asignar_pedidoNoListo_retornaNull() throws RemoteException {
        Order o = orderService.crearPedido("1001", false); // PENDIENTE
        assertNull(deliveryService.asignarPedidoARepartidor(
                o.getOrderId(), REPARTIDOR, cuadranteService, orderService));
    }

    @Test
    @DisplayName("RF-13 | Pedido sin cuadranteDestino → null")
    void asignar_pedidoSinCuadrante_retornaNull() throws RemoteException {
        Order o = orderService.crearPedido("1001", false);
        o.setEstado(EstadoPedido.LISTO); // LISTO pero sin cuadranteDestino
        assertNull(deliveryService.asignarPedidoARepartidor(
                o.getOrderId(), REPARTIDOR, cuadranteService, orderService));
    }

    @Test
    @DisplayName("RF-13 | Pedido inexistente → null")
    void asignar_pedidoInexistente_retornaNull() {
        assertNull(deliveryService.asignarPedidoARepartidor(
                "uuid-inventado", REPARTIDOR, cuadranteService, orderService));
    }

    @Test
    @DisplayName("RF-13 | repartidorId nulo → IllegalArgumentException")
    void asignar_repartidorNull_lanzaExcepcion() throws RemoteException {
        Order o = pedidoListo("1001");
        assertThrows(IllegalArgumentException.class,
                () -> deliveryService.asignarPedidoARepartidor(
                        o.getOrderId(), null, cuadranteService, orderService));
    }

    @Test
    @DisplayName("RF-13 | repartidorId vacío → IllegalArgumentException")
    void asignar_repartidorVacio_lanzaExcepcion() throws RemoteException {
        Order o = pedidoListo("1001");
        assertThrows(IllegalArgumentException.class,
                () -> deliveryService.asignarPedidoARepartidor(
                        o.getOrderId(), "   ", cuadranteService, orderService));
    }

    @Test
    @DisplayName("RF-13 | Destino sin ruta en grafo → null")
    void asignar_rutaImposible_retornaNull() throws RemoteException {
        // Cuadrante Isla sin conexión
        cuadranteService.agregarCuadrante(new Cuadrante("Isla", "Zona desconectada"));
        Order o = orderService.crearPedido("1001", false);
        orderService.asignarCuadranteDestino(o.getOrderId(), "Isla", cuadranteService);
        o.setEstado(EstadoPedido.LISTO);
        assertNull(deliveryService.asignarPedidoARepartidor(
                o.getOrderId(), REPARTIDOR, cuadranteService, orderService));
    }

    // ── iniciarEntrega ────────────────────────────────────────────────────────

    @Test
    @DisplayName("RF-13 | Iniciar entrega → Order pasa a EN_CAMINO")
    void iniciar_pedidoAsignado_estadoEnCamino() throws RemoteException {
        Order o = pedidoListo("1001");
        deliveryService.asignarPedidoARepartidor(
                o.getOrderId(), REPARTIDOR, cuadranteService, orderService);
        assertTrue(deliveryService.iniciarEntrega(o.getOrderId(), orderService));
        assertEquals(EstadoPedido.EN_CAMINO,
                orderService.buscarPedido(o.getOrderId()).getEstado());
    }

    @Test
    @DisplayName("RF-13 | Iniciar entrega sin asignar → false")
    void iniciar_sinAsignar_retornaFalse() throws RemoteException {
        Order o = pedidoListo("1001");
        assertFalse(deliveryService.iniciarEntrega(o.getOrderId(), orderService));
    }

    @Test
    @DisplayName("RF-13 | Iniciar entrega pedido no LISTO → IllegalStateException")
    void iniciar_estadoInvalido_lanzaExcepcion() throws RemoteException {
        Order o = pedidoListo("1001");
        deliveryService.asignarPedidoARepartidor(
                o.getOrderId(), REPARTIDOR, cuadranteService, orderService);
        // Cambiar estado directamente para simular error
        o.setEstado(EstadoPedido.PENDIENTE);
        assertThrows(IllegalStateException.class,
                () -> deliveryService.iniciarEntrega(o.getOrderId(), orderService));
    }

    // ── completarEntrega ──────────────────────────────────────────────────────

    @Test
    @DisplayName("RF-13 | Completar entrega → Order pasa a ENTREGADO")
    void completar_desdeEnCamino_estadoEntregado() throws RemoteException {
        Order o = pedidoListo("1001");
        deliveryService.asignarPedidoARepartidor(
                o.getOrderId(), REPARTIDOR, cuadranteService, orderService);
        deliveryService.iniciarEntrega(o.getOrderId(), orderService);
        assertTrue(deliveryService.completarEntrega(o.getOrderId(), orderService));
        assertEquals(EstadoPedido.ENTREGADO,
                orderService.buscarPedido(o.getOrderId()).getEstado());
    }

    @Test
    @DisplayName("RF-13 | Completar sin iniciar → IllegalStateException")
    void completar_sinIniciar_lanzaExcepcion() throws RemoteException {
        Order o = pedidoListo("1001");
        deliveryService.asignarPedidoARepartidor(
                o.getOrderId(), REPARTIDOR, cuadranteService, orderService);
        // El pedido sigue en LISTO, no EN_CAMINO → excepción estricta
        assertThrows(IllegalStateException.class,
                () -> deliveryService.completarEntrega(o.getOrderId(), orderService));
    }

    @Test
    @DisplayName("RF-13 | Completar sin asignar → false")
    void completar_sinAsignar_retornaFalse() throws RemoteException {
        Order o = pedidoListo("1001");
        assertFalse(deliveryService.completarEntrega(o.getOrderId(), orderService));
    }

    // ── buscarEntrega / yaEstaAsignado ────────────────────────────────────────

    @Test
    @DisplayName("RF-13 | buscarEntrega existente → retorna Delivery")
    void buscar_existente_retornaDelivery() throws RemoteException {
        Order o = pedidoListo("1001");
        deliveryService.asignarPedidoARepartidor(
                o.getOrderId(), REPARTIDOR, cuadranteService, orderService);
        Delivery d = deliveryService.buscarEntregaPorPedido(o.getOrderId());
        assertNotNull(d);
        assertEquals(o.getOrderId(), d.getOrderId());
    }

    @Test
    @DisplayName("RF-13 | buscarEntrega inexistente → null")
    void buscar_inexistente_retornaNull() {
        assertNull(deliveryService.buscarEntregaPorPedido("no-existe"));
    }

    @Test
    @DisplayName("RF-13 | yaEstaAsignado antes/después de asignar")
    void yaEstaAsignado_verificaEstado() throws RemoteException {
        Order o = pedidoListo("1001");
        assertFalse(deliveryService.yaEstaAsignado(o.getOrderId()));
        deliveryService.asignarPedidoARepartidor(
                o.getOrderId(), REPARTIDOR, cuadranteService, orderService);
        assertTrue(deliveryService.yaEstaAsignado(o.getOrderId()));
    }

    // ── Flujo completo RF-13 ──────────────────────────────────────────────────

    @Test
    @DisplayName("RF-13 | Flujo completo: LISTO → asignar → EN_CAMINO → ENTREGADO")
    void flujoCompleto_listaEnCaminoEntregado() throws RemoteException {
        Order o = pedidoListo("1001");
        String id = o.getOrderId();

        // Asignar repartidor y verificar Delivery creado con ruta
        Delivery d = deliveryService.asignarPedidoARepartidor(
                id, REPARTIDOR, cuadranteService, orderService);
        assertNotNull(d);
        assertNotNull(d.getRuta());
        assertEquals(EstadoPedido.LISTO, orderService.buscarPedido(id).getEstado());

        // Iniciar entrega
        assertTrue(deliveryService.iniciarEntrega(id, orderService));
        assertEquals(EstadoPedido.EN_CAMINO, orderService.buscarPedido(id).getEstado());

        // Completar entrega
        assertTrue(deliveryService.completarEntrega(id, orderService));
        assertEquals(EstadoPedido.ENTREGADO, orderService.buscarPedido(id).getEstado());
    }
}
