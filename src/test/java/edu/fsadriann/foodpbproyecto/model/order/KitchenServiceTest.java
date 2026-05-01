package edu.fsadriann.foodpbproyecto.model.order;

import edu.fsadriann.foodpbproyecto.model.product.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests de KitchenService RF-05 — Cola de cocina con estaciones reales.
 *
 * <p>Cubre: prioridad, FIFO, asignación por fogón, bloqueo por ocupación,
 * liberación con reactivación automática, transiciones de estado y validaciones.
 *
 * @author fsadriann
 */
@DisplayName("KitchenService – RF-05 Fogones reales")
class KitchenServiceTest {

    private KitchenService service;

    @BeforeEach
    void setUp() {
        service = new KitchenService();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Order pedido(String cedula, boolean premium) {
        return new Order(cedula, premium);
    }

    private Product simple(String id) {
        return new Product(id, "Simple-" + id, "BEBIDA", 5_000, false);
    }

    private Product complejo(String id) {
        return new Product(id, "Complejo-" + id, "PLATO_PRINCIPAL", 25_000, true);
    }

    private Order pedidoSimple(String cedula, boolean premium) {
        Order o = pedido(cedula, premium);
        o.agregarProducto(simple("P1"));
        return o;
    }

    private Order pedidoComplejo(String cedula, boolean premium) {
        Order o = pedido(cedula, premium);
        o.agregarProducto(complejo("P1"));
        return o;
    }

    // ── encolarPedido ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("Cola vacía al inicio")
    void colaVaciaAlInicio() {
        assertTrue(service.colaVacia());
        assertEquals(0, service.tamanoCola());
    }

    @Test
    @DisplayName("Encolar aumenta tamaño de cola")
    void encolar_aumentaTamano() {
        service.encolarPedido(pedidoSimple("1001", false));
        assertEquals(1, service.tamanoCola());
    }

    @Test
    @DisplayName("Encolar pedido nulo → IllegalArgumentException")
    void encolar_nulo_lanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> service.encolarPedido(null));
    }

    @Test
    @DisplayName("Encolar pedido CANCELADO → IllegalStateException")
    void encolar_cancelado_lanzaExcepcion() {
        Order o = pedidoSimple("1001", false);
        o.setEstado(EstadoPedido.CANCELADO);
        assertThrows(IllegalStateException.class, () -> service.encolarPedido(o));
    }

    @Test
    @DisplayName("Encolar pedido ENTREGADO → IllegalStateException")
    void encolar_entregado_lanzaExcepcion() {
        Order o = pedidoSimple("1001", false);
        o.setEstado(EstadoPedido.ENTREGADO);
        assertThrows(IllegalStateException.class, () -> service.encolarPedido(o));
    }

    // ── procesarSiguientePedido — Prioridad ───────────────────────────────────

    @Test
    @DisplayName("RF-05 | Premium procesado antes que estándar")
    void prioridad_premiumAntesQueEstandar() {
        Order estandar = pedidoSimple("E001", false);
        Order premium  = pedidoSimple("P001", true);

        service.encolarPedido(estandar); // entra primero
        service.encolarPedido(premium);  // entra después

        Order asignado = service.procesarSiguientePedido();
        assertEquals(premium.getOrderId(), asignado.getOrderId(),
                "Premium debe procesarse primero aunque llegó después");
    }

    @Test
    @DisplayName("RF-05 | Dos premiums simples → FIFO entre ellos")
    void prioridad_dosPremiumSimples_fifo() {
        Order p1 = pedidoSimple("P001", true);
        Order p2 = pedidoSimple("P002", true);
        service.encolarPedido(p1);
        service.encolarPedido(p2);

        assertEquals(p1.getOrderId(), service.procesarSiguientePedido().getOrderId());
        assertEquals(p2.getOrderId(), service.procesarSiguientePedido().getOrderId());
    }

    @Test
    @DisplayName("RF-05 | Dos estándar simples → FIFO entre ellos")
    void prioridad_dosEstandarSimples_fifo() {
        Order e1 = pedidoSimple("E001", false);
        Order e2 = pedidoSimple("E002", false);
        service.encolarPedido(e1);
        service.encolarPedido(e2);

        assertEquals(e1.getOrderId(), service.procesarSiguientePedido().getOrderId());
        assertEquals(e2.getOrderId(), service.procesarSiguientePedido().getOrderId());
    }

    @Test
    @DisplayName("RF-05 | Cola vacía → procesar retorna null")
    void procesar_colaVacia_retornaNull() {
        assertNull(service.procesarSiguientePedido());
    }

    @Test
    @DisplayName("RF-05 | Procesar pedido PENDIENTE → cambia a EN_PREPARACION")
    void procesar_pendiente_cambiaEstado() {
        Order o = pedidoSimple("1001", false);
        assertEquals(EstadoPedido.PENDIENTE, o.getEstado());

        service.encolarPedido(o);
        service.procesarSiguientePedido();

        assertEquals(EstadoPedido.EN_PREPARACION, o.getEstado());
    }

    // ── Asignación de fogones ─────────────────────────────────────────────────

    @Test
    @DisplayName("RF-05 | Pedido complejo → asignado a FOGON_GRANDE")
    void asignar_complejo_fogonGrande() {
        Order o = pedidoComplejo("1001", false);
        service.encolarPedido(o);
        Order asignado = service.procesarSiguientePedido();

        assertNotNull(asignado);
        // El estado en el pedido confirma que fue asignado
        assertEquals(EstadoPedido.EN_PREPARACION, asignado.getEstado());
        // Y el estado del fogón grande refleja ocupación
        assertTrue(service.obtenerEstadoFogones().contains("FOGON_GRANDE"));
        assertFalse(service.obtenerEstadoFogones().contains("FOGON_GRANDE   : [ LIBRE ]"));
    }

    @Test
    @DisplayName("RF-05 | Pedido simple → asignado al primer fogón normal libre")
    void asignar_simple_fogonNormal() {
        Order o = pedidoSimple("1001", false);
        service.encolarPedido(o);
        Order asignado = service.procesarSiguientePedido();

        assertNotNull(asignado);
        assertEquals(EstadoPedido.EN_PREPARACION, asignado.getEstado());
    }

    @Test
    @DisplayName("RF-05 | 3 simples ocupan los 3 fogones normales")
    void asignar_tresSimples_ocupanTresFogones() {
        Order o1 = pedidoSimple("1001", false);
        Order o2 = pedidoSimple("1002", false);
        Order o3 = pedidoSimple("1003", false);

        service.encolarPedido(o1);
        service.encolarPedido(o2);
        service.encolarPedido(o3);

        assertNotNull(service.procesarSiguientePedido()); // FOGON_NORMAL_1
        assertNotNull(service.procesarSiguientePedido()); // FOGON_NORMAL_2
        assertNotNull(service.procesarSiguientePedido()); // FOGON_NORMAL_3

        assertEquals(EstadoPedido.EN_PREPARACION, o1.getEstado());
        assertEquals(EstadoPedido.EN_PREPARACION, o2.getEstado());
        assertEquals(EstadoPedido.EN_PREPARACION, o3.getEstado());
        assertTrue(service.colaVacia());
    }

    @Test
    @DisplayName("RF-05 | 4to simple espera en cola si fogones normales llenos")
    void asignar_cuartoSimple_esperaEnCola() {
        service.encolarPedido(pedidoSimple("1001", false));
        service.encolarPedido(pedidoSimple("1002", false));
        service.encolarPedido(pedidoSimple("1003", false));
        Order cuarto = pedidoSimple("1004", false);
        service.encolarPedido(cuarto);

        service.procesarSiguientePedido();
        service.procesarSiguientePedido();
        service.procesarSiguientePedido();

        // Cuarto pedido: todos normales ocupados → no se asigna
        Order resultado = service.procesarSiguientePedido();
        assertNull(resultado, "El 4to simple no debe asignarse si no hay fogón libre");
        assertEquals(1, service.tamanoCola(), "El 4to pedido debe seguir en cola");
        assertEquals(EstadoPedido.PENDIENTE, cuarto.getEstado());
    }

    @Test
    @DisplayName("RF-05 | Complejo espera en cola si FOGON_GRANDE ocupado")
    void asignar_complejoEspera_fogonGrandeOcupado() {
        Order c1 = pedidoComplejo("1001", false);
        Order c2 = pedidoComplejo("1002", false);

        service.encolarPedido(c1);
        service.encolarPedido(c2);

        service.procesarSiguientePedido(); // c1 → FOGON_GRANDE
        Order resultado = service.procesarSiguientePedido(); // c2 → espera

        assertNull(resultado, "c2 debe esperar porque FOGON_GRANDE está ocupado");
        assertEquals(1, service.tamanoCola());
        assertEquals(EstadoPedido.PENDIENTE, c2.getEstado());
    }

    // ── marcarPedidoListo ─────────────────────────────────────────────────────

    @Test
    @DisplayName("RF-05 | Marcar listo libera fogón → estado LISTO")
    void marcarListo_libraFogon_cambiaEstado() {
        Order o = pedidoSimple("1001", false);
        service.encolarPedido(o);
        service.procesarSiguientePedido();
        assertEquals(EstadoPedido.EN_PREPARACION, o.getEstado());

        service.marcarPedidoListo(o.getOrderId());

        assertEquals(EstadoPedido.LISTO, o.getEstado());
    }

    @Test
    @DisplayName("RF-05 | Liberar fogón activa siguiente pedido en cola")
    void marcarListo_liberaFogon_activaSiguiente() {
        Order o1 = pedidoSimple("1001", false);
        Order o2 = pedidoSimple("1002", false);
        Order o3 = pedidoSimple("1003", false);
        Order o4 = pedidoSimple("1004", false); // quedará esperando

        service.encolarPedido(o1);
        service.encolarPedido(o2);
        service.encolarPedido(o3);
        service.encolarPedido(o4);

        service.procesarSiguientePedido(); // o1 → normal_1
        service.procesarSiguientePedido(); // o2 → normal_2
        service.procesarSiguientePedido(); // o3 → normal_3
        service.procesarSiguientePedido(); // o4 no asignado → cola

        assertEquals(EstadoPedido.PENDIENTE, o4.getEstado());

        // Liberar o1 → debe activar o4 automáticamente
        service.marcarPedidoListo(o1.getOrderId());

        assertEquals(EstadoPedido.LISTO, o1.getEstado());
        assertEquals(EstadoPedido.EN_PREPARACION, o4.getEstado(),
                "Al liberar o1, o4 debe asignarse automáticamente");
        assertTrue(service.colaVacia());
    }

    @Test
    @DisplayName("RF-05 | Liberar FOGON_GRANDE activa siguiente complejo")
    void marcarListo_liberaFogonGrande_activaSiguienteComplejo() {
        Order c1 = pedidoComplejo("1001", false);
        Order c2 = pedidoComplejo("1002", false);

        service.encolarPedido(c1);
        service.encolarPedido(c2);

        service.procesarSiguientePedido(); // c1 → FOGON_GRANDE
        service.procesarSiguientePedido(); // c2 → bloqueado

        service.marcarPedidoListo(c1.getOrderId()); // libera FOGON_GRANDE → activa c2

        assertEquals(EstadoPedido.LISTO, c1.getEstado());
        assertEquals(EstadoPedido.EN_PREPARACION, c2.getEstado(),
                "c2 debe asignarse al FOGON_GRANDE al liberarse");
        assertTrue(service.colaVacia());
    }

    @Test
    @DisplayName("RF-05 | marcarListo con pedidoId nulo → IllegalArgumentException")
    void marcarListo_idNulo_lanzaExcepcion() {
        assertThrows(IllegalArgumentException.class,
                () -> service.marcarPedidoListo(null));
    }

    @Test
    @DisplayName("RF-05 | marcarListo de pedido no en fogón → IllegalStateException")
    void marcarListo_noEnFogon_lanzaExcepcion() {
        assertThrows(IllegalStateException.class,
                () -> service.marcarPedidoListo("id-inexistente"));
    }

    @Test
    @DisplayName("RF-05 | marcarListo pedido no EN_PREPARACION → IllegalStateException")
    void marcarListo_estadoInvalido_lanzaExcepcion() {
        // Forzar situación interna: procesar, luego cambiar estado manualmente
        Order o = pedidoSimple("1001", false);
        service.encolarPedido(o);
        service.procesarSiguientePedido(); // EN_PREPARACION
        o.setEstado(EstadoPedido.LISTO);   // simular ya listo sin pasar por service

        assertThrows(IllegalStateException.class,
                () -> service.marcarPedidoListo(o.getOrderId()));
    }

    // ── obtenerEstadoFogones ──────────────────────────────────────────────────

    @Test
    @DisplayName("Estado de fogones contiene los 4 fogones")
    void estadoFogones_contieneLosCuatro() {
        String estado = service.obtenerEstadoFogones();
        assertTrue(estado.contains("FOGON_GRANDE"));
        assertTrue(estado.contains("FOGON_NORMAL_1"));
        assertTrue(estado.contains("FOGON_NORMAL_2"));
        assertTrue(estado.contains("FOGON_NORMAL_3"));
    }

    @Test
    @DisplayName("Estado muestra LIBRE para los 4 fogones vacíos")
    void estadoFogones_todosLibres() {
        String estado = service.obtenerEstadoFogones();
        long libres = estado.lines()
                .filter(l -> l.contains("LIBRE"))
                .count();
        assertEquals(4, libres, "Los 4 fogones deben mostrar LIBRE cuando no hay pedidos");
    }

    // ── Flujo completo ────────────────────────────────────────────────────────

    @Test
    @DisplayName("RF-05 | Flujo completo: encolar → procesar → marcarListo → siguiente automático")
    void flujoCompleto_encolararProcesarMarcarListo() {
        Order simple   = pedidoSimple("1001", true);  // premium
        Order complejo = pedidoComplejo("1002", false); // estándar

        service.encolarPedido(simple);
        service.encolarPedido(complejo);

        // Premium va primero
        Order enCocina1 = service.procesarSiguientePedido();
        assertNotNull(enCocina1);
        assertEquals(simple.getOrderId(), enCocina1.getOrderId());
        assertEquals(EstadoPedido.EN_PREPARACION, simple.getEstado());

        // Complejo al FOGON_GRANDE
        Order enCocina2 = service.procesarSiguientePedido();
        assertNotNull(enCocina2);
        assertEquals(complejo.getOrderId(), enCocina2.getOrderId());
        assertEquals(EstadoPedido.EN_PREPARACION, complejo.getEstado());

        assertTrue(service.colaVacia());

        // Marcar simple listo
        service.marcarPedidoListo(simple.getOrderId());
        assertEquals(EstadoPedido.LISTO, simple.getEstado());

        // Marcar complejo listo
        service.marcarPedidoListo(complejo.getOrderId());
        assertEquals(EstadoPedido.LISTO, complejo.getEstado());

        // Todo libre
        assertTrue(service.colaVacia());
        assertTrue(service.obtenerEstadoFogones().contains("LIBRE"));
    }
}
