package edu.fsadriann.foodpbproyecto.model.cuadrante;

import edu.fsadriann.app.linkedlist.singly.singly.LinkedList;
import edu.fsadriann.model.iterator.Iterator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests de CuadranteService para RF-06 (Rutas de entrega).
 *
 * <p>Cubre: CRUD de cuadrantes, construcción del grafo, Dijkstra,
 * BFS, vecinos, validaciones y casos borde.
 *
 * @author fsadriann
 */
@DisplayName("CuadranteService – RF-06 Rutas")
class CuadranteServiceTest {

    private CuadranteService service;

    @BeforeEach
    void setUp() {
        service = new CuadranteService();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Cuadrante c(String nombre) {
        return new Cuadrante(nombre, "Zona " + nombre);
    }

    private Cuadrante cConCoordenadas(String nombre, double lat, double lon) {
        return new Cuadrante(nombre, "Zona " + nombre, lat, lon);
    }

    /** Convierte LinkedList del JAR a String para aserciones */
    private String rutaStr(LinkedList<String> ruta) {
        if (ruta == null) return null;
        StringBuilder sb = new StringBuilder();
        Iterator<String> it = ruta.iterator();
        while (it.hasNext()) {
            if (sb.length() > 0) sb.append(" → ");
            sb.append(it.next());
        }
        return sb.toString();
    }

    /** Cuenta elementos de LinkedList del JAR */
    private int contarElementos(LinkedList<?> lista) {
        if (lista == null) return 0;
        int count = 0;
        var it = lista.iterator();
        while (it.hasNext()) { it.next(); count++; }
        return count;
    }

    // ── Cuadrante model ───────────────────────────────────────────────────────

    @Test
    @DisplayName("Cuadrante con nombre vacío → IllegalArgumentException")
    void cuadrante_nombreVacio_lanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> new Cuadrante("", "Zona"));
        assertThrows(IllegalArgumentException.class, () -> new Cuadrante("  ", "Zona"));
        assertThrows(IllegalArgumentException.class, () -> new Cuadrante(null, "Zona"));
    }

    @Test
    @DisplayName("Cuadrante disponible por defecto")
    void cuadrante_disponiblePorDefecto() {
        assertTrue(new Cuadrante("UPB", "Campus").isDisponible());
    }

    @Test
    @DisplayName("Cuadrante con coordenadas")
    void cuadrante_conCoordenadas() {
        Cuadrante cu = cConCoordenadas("UPB", 6.2008, -75.5739);
        assertEquals(6.2008,  cu.getLatitud(),  1e-6);
        assertEquals(-75.5739, cu.getLongitud(), 1e-6);
    }

    // ── agregarCuadrante ──────────────────────────────────────────────────────

    @Test
    @DisplayName("RF-06 | Registrar cuadrante → true y aparece en grafo")
    void agregar_cuadranteValido_retornaTrue() {
        assertTrue(service.agregarCuadrante(c("UPB")));
        assertEquals(1, service.numeroCuadrantes());
    }

    @Test
    @DisplayName("RF-06 | Cuadrante duplicado → false (no duplica nodo)")
    void agregar_duplicado_retornaFalse() {
        service.agregarCuadrante(c("UPB"));
        assertFalse(service.agregarCuadrante(c("UPB")));
        assertEquals(1, service.numeroCuadrantes());
    }

    @Test
    @DisplayName("RF-06 | Cuadrante nulo → IllegalArgumentException")
    void agregar_nulo_lanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> service.agregarCuadrante(null));
    }

    @Test
    @DisplayName("RF-06 | Múltiples cuadrantes → contador correcto")
    void agregar_varios_contadorCorrecto() {
        service.agregarCuadrante(c("UPB"));
        service.agregarCuadrante(c("Laureles"));
        service.agregarCuadrante(c("Estadio"));
        assertEquals(3, service.numeroCuadrantes());
    }

    // ── CRUD ──────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("RF-06 | buscarCuadrante existente → retorna objeto")
    void buscar_existente_retornaCuadrante() {
        service.agregarCuadrante(new Cuadrante("UPB", "Campus UPB"));
        Cuadrante encontrado = service.buscarCuadrante("UPB");
        assertNotNull(encontrado);
        assertEquals("Campus UPB", encontrado.getDescripcion());
    }

    @Test
    @DisplayName("RF-06 | buscarCuadrante inexistente → null")
    void buscar_inexistente_retornaNull() {
        assertNull(service.buscarCuadrante("NoExiste"));
    }

    @Test
    @DisplayName("RF-06 | buscarCuadrante nulo → null sin excepción")
    void buscar_nulo_retornaNull() {
        assertNull(service.buscarCuadrante(null));
    }

    @Test
    @DisplayName("RF-06 | editarCuadrante existente → actualiza descripción")
    void editar_existente_actualizaDatos() {
        service.agregarCuadrante(new Cuadrante("UPB", "Campus UPB"));
        assertTrue(service.editarCuadrante(new Cuadrante("UPB", "Campus Principal UPB")));
        assertEquals("Campus Principal UPB", service.buscarCuadrante("UPB").getDescripcion());
    }

    @Test
    @DisplayName("RF-06 | editarCuadrante inexistente → false")
    void editar_inexistente_retornaFalse() {
        assertFalse(service.editarCuadrante(new Cuadrante("NoExiste", "Desc")));
    }

    @Test
    @DisplayName("RF-06 | listarCuadrantes → lista con todos los registrados")
    void listar_cuadrantes_retornaLista() {
        service.agregarCuadrante(c("UPB"));
        service.agregarCuadrante(c("Laureles"));
        LinkedList<Cuadrante> lista = service.listarCuadrantes();
        assertEquals(2, contarElementos(lista));
    }

    // ── conectarCuadrantes ────────────────────────────────────────────────────

    @Test
    @DisplayName("RF-06 | Conexión bidireccional A↔B")
    void conectar_bidireccional_ambasAristas() {
        service.agregarCuadrante(c("UPB"));
        service.agregarCuadrante(c("Laureles"));
        assertTrue(service.conectarCuadrantes("UPB", "Laureles", 1.5));

        // Desde cada lado existe ruta directa
        assertTrue(service.existeRuta("UPB", "Laureles"));
        assertTrue(service.existeRuta("Laureles", "UPB"));
    }

    @Test
    @DisplayName("RF-06 | Conectar nodo A inexistente → false (sin excepción)")
    void conectar_nodoAInexistente_retornaFalse() {
        service.agregarCuadrante(c("Laureles"));
        assertFalse(service.conectarCuadrantes("NoExiste", "Laureles", 1.0));
    }

    @Test
    @DisplayName("RF-06 | Conectar nodo B inexistente → false (sin excepción)")
    void conectar_nodoBInexistente_retornaFalse() {
        service.agregarCuadrante(c("UPB"));
        assertFalse(service.conectarCuadrantes("UPB", "NoExiste", 1.0));
    }

    @Test
    @DisplayName("RF-06 | Distancia ≤ 0 → IllegalArgumentException")
    void conectar_distanciaInvalida_lanzaExcepcion() {
        service.agregarCuadrante(c("UPB"));
        service.agregarCuadrante(c("Laureles"));
        assertThrows(IllegalArgumentException.class,
                () -> service.conectarCuadrantes("UPB", "Laureles", 0));
        assertThrows(IllegalArgumentException.class,
                () -> service.conectarCuadrantes("UPB", "Laureles", -1.5));
    }

    @Test
    @DisplayName("RF-06 | Conexión aumenta contador de aristas")
    void conectar_incrementaContadorConexiones() {
        service.agregarCuadrante(c("UPB"));
        service.agregarCuadrante(c("Laureles"));
        service.agregarCuadrante(c("Estadio"));
        service.conectarCuadrantes("UPB", "Laureles", 1.2);
        service.conectarCuadrantes("Laureles", "Estadio", 0.8);
        assertEquals(2, service.numeroConexiones());
    }

    @Test
    @DisplayName("RF-06 | Conectar cuadrante consigo mismo → false (auto-loop)")
    void conectar_mismoNodo_retornaFalse() {
        service.agregarCuadrante(c("UPB"));
        assertFalse(service.conectarCuadrantes("UPB", "UPB", 1.0),
                "Un cuadrante no puede conectarse consigo mismo");
    }

    // ── calcularRutaMasCorta (Dijkstra) ───────────────────────────────────────

    @Test
    @DisplayName("RF-06 | Ruta directa A→B (Dijkstra)")
    void ruta_directa_correcto() {
        service.agregarCuadrante(c("UPB"));
        service.agregarCuadrante(c("Cabecera"));
        service.conectarCuadrantes("UPB", "Cabecera", 3.2);

        LinkedList<String> ruta = service.calcularRutaMasCorta("UPB", "Cabecera");
        assertNotNull(ruta, "Debe existir ruta directa");
        String rutaTexto = rutaStr(ruta);
        assertTrue(rutaTexto.contains("UPB") && rutaTexto.contains("Cabecera"),
                "La ruta debe contener origen y destino: " + rutaTexto);
    }

    @Test
    @DisplayName("RF-06 | Dijkstra elige ruta más corta entre dos opciones")
    void ruta_dijkstraEligeMenorDistancia() {
        // UPB→Laureles→Estadio = 1.2 + 0.8 = 2.0 km
        // UPB→Estadio directo  = 3.5 km
        service.agregarCuadrante(c("UPB"));
        service.agregarCuadrante(c("Laureles"));
        service.agregarCuadrante(c("Estadio"));
        service.conectarCuadrantes("UPB",     "Laureles", 1.2);
        service.conectarCuadrantes("Laureles", "Estadio",  0.8);
        service.conectarCuadrantes("UPB",     "Estadio",  3.5);

        double dist = service.calcularDistancia("UPB", "Estadio");
        assertEquals(2.0, dist, 0.01, "Dijkstra debe elegir la ruta más corta: 2.0 km");
    }

    @Test
    @DisplayName("RF-06 | Ruta mismo origen y destino → null")
    void ruta_mismoOrigenDestino_retornaNull() {
        service.agregarCuadrante(c("UPB"));
        assertNull(service.calcularRutaMasCorta("UPB", "UPB"));
    }

    @Test
    @DisplayName("RF-06 | Ruta imposible (grafo desconectado) → null")
    void ruta_imposible_retornaNull() {
        service.agregarCuadrante(c("UPB"));
        service.agregarCuadrante(c("Isla")); // sin conexión
        assertNull(service.calcularRutaMasCorta("UPB", "Isla"));
    }

    @Test
    @DisplayName("RF-06 | Ruta con nodo origen inexistente → null")
    void ruta_origenInexistente_retornaNull() {
        service.agregarCuadrante(c("UPB"));
        assertNull(service.calcularRutaMasCorta("NoExiste", "UPB"));
    }

    @Test
    @DisplayName("RF-06 | Ruta con nodo destino inexistente → null")
    void ruta_destinoInexistente_retornaNull() {
        service.agregarCuadrante(c("UPB"));
        assertNull(service.calcularRutaMasCorta("UPB", "NoExiste"));
    }

    @Test
    @DisplayName("RF-06 | Ruta con nulo → null sin excepción")
    void ruta_nulo_retornaNull() {
        service.agregarCuadrante(c("UPB"));
        assertNull(service.calcularRutaMasCorta(null, "UPB"));
        assertNull(service.calcularRutaMasCorta("UPB", null));
    }

    // ── calcularDistancia ─────────────────────────────────────────────────────

    @Test
    @DisplayName("RF-06 | Distancia directa correcta")
    void distancia_directa_correcta() {
        service.agregarCuadrante(c("UPB"));
        service.agregarCuadrante(c("Cabecera"));
        service.conectarCuadrantes("UPB", "Cabecera", 3.2);
        assertEquals(3.2, service.calcularDistancia("UPB", "Cabecera"), 0.01);
    }

    @Test
    @DisplayName("RF-06 | Distancia mismo origen/destino → 0.0")
    void distancia_mismoNodo_cero() {
        service.agregarCuadrante(c("UPB"));
        assertEquals(0.0, service.calcularDistancia("UPB", "UPB"), 0.001);
    }

    @Test
    @DisplayName("RF-06 | Distancia sin camino → -1.0")
    void distancia_sinCamino_retornaMinusUno() {
        service.agregarCuadrante(c("UPB"));
        service.agregarCuadrante(c("Isla"));
        assertEquals(-1.0, service.calcularDistancia("UPB", "Isla"), 0.001);
    }

    @Test
    @DisplayName("RF-06 | Distancia nodo inexistente → -1.0")
    void distancia_nodoInexistente_retornaMinusUno() {
        service.agregarCuadrante(c("UPB"));
        assertEquals(-1.0, service.calcularDistancia("UPB", "NoExiste"), 0.001);
    }

    // ── existeRuta ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("RF-06 | existeRuta con camino → true")
    void existeRuta_conCamino_true() {
        service.agregarCuadrante(c("UPB"));
        service.agregarCuadrante(c("Laureles"));
        service.conectarCuadrantes("UPB", "Laureles", 1.5);
        assertTrue(service.existeRuta("UPB", "Laureles"));
        assertTrue(service.existeRuta("Laureles", "UPB")); // bidireccional
    }

    @Test
    @DisplayName("RF-06 | existeRuta sin conexión → false")
    void existeRuta_sinCamino_false() {
        service.agregarCuadrante(c("UPB"));
        service.agregarCuadrante(c("Isla"));
        assertFalse(service.existeRuta("UPB", "Isla"));
    }

    @Test
    @DisplayName("RF-06 | existeRuta mismo nodo → true")
    void existeRuta_mismoNodo_true() {
        service.agregarCuadrante(c("UPB"));
        assertTrue(service.existeRuta("UPB", "UPB"));
    }

    // ── vecinosDirectos ───────────────────────────────────────────────────────

    @Test
    @DisplayName("RF-06 | Vecinos directos correctos tras conectar")
    void vecinos_correctosTrasConectar() {
        service.agregarCuadrante(c("UPB"));
        service.agregarCuadrante(c("Laureles"));
        service.agregarCuadrante(c("Estadio"));
        service.conectarCuadrantes("UPB", "Laureles", 1.2);
        service.conectarCuadrantes("UPB", "Estadio",  2.5);

        LinkedList<String> vecinos = service.vecinosDirectos("UPB");
        assertNotNull(vecinos);
        assertEquals(2, contarElementos(vecinos));
    }

    @Test
    @DisplayName("RF-06 | Vecinos de nodo inexistente → null")
    void vecinos_nodoInexistente_retornaNull() {
        assertNull(service.vecinosDirectos("NoExiste"));
    }

    // ── Flujo completo RF-06 ──────────────────────────────────────────────────

    @Test
    @DisplayName("RF-06 | Flujo completo: registrar, conectar, calcular ruta y distancia")
    void flujoCompleto_registrarConectarRuta() {
        // Construir mapa: UPB → Laureles → Estadio → Cabecera
        service.agregarCuadrante(new Cuadrante("UPB",      "Campus UPB"));
        service.agregarCuadrante(new Cuadrante("Laureles",  "Barrio Laureles"));
        service.agregarCuadrante(new Cuadrante("Estadio",   "Zona Estadio"));
        service.agregarCuadrante(new Cuadrante("Cabecera",  "Barrio Cabecera"));

        service.conectarCuadrantes("UPB",     "Laureles", 1.2);
        service.conectarCuadrantes("Laureles", "Estadio",  0.8);
        service.conectarCuadrantes("Estadio",  "Cabecera", 1.0);

        assertEquals(4, service.numeroCuadrantes());
        assertEquals(3, service.numeroConexiones());

        // Ruta más corta UPB → Cabecera
        LinkedList<String> ruta = service.calcularRutaMasCorta("UPB", "Cabecera");
        assertNotNull(ruta);
        String rutaTexto = rutaStr(ruta);
        assertTrue(rutaTexto.contains("UPB"),     "Debe empezar en UPB");
        assertTrue(rutaTexto.contains("Cabecera"), "Debe llegar a Cabecera");

        // Distancia total
        double dist = service.calcularDistancia("UPB", "Cabecera");
        assertEquals(3.0, dist, 0.01, "UPB→Laureles(1.2) + Laureles→Estadio(0.8) + Estadio→Cabecera(1.0) = 3.0 km");
    }
}
