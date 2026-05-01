package edu.fsadriann.foodpbproyecto.model.cuadrante;

import edu.fsadriann.app.graph.matrixGraph;
import edu.fsadriann.app.linkedlist.singly.singly.LinkedList;
import edu.fsadriann.model.iterator.Iterator;

/**
 * Servicio de cuadrantes y rutas de entrega. Implementa {@link CuadranteInterface}. RF-06.
 *
 * <p><b>Estructura de datos principal:</b> {@code matrixGraph<String>} del JAR.
 * <ul>
 *   <li>Cada nodo es el {@code nombre} del cuadrante (String, clave única).</li>
 *   <li>Las aristas tienen peso en kilómetros (double).</li>
 *   <li>Las conexiones son <em>bidireccionales</em> — {@link #conectarCuadrantes}
 *       agrega A→B y B→A con el mismo peso.</li>
 * </ul>
 *
 * <p><b>Capacidad:</b> {@code matrixGraph} internamente tiene {@code maxVerts} estático.
 * Se inicializa con {@value #CAPACIDAD_MAX} nodos máximos — suficiente para un sistema
 * de delivery universitario con múltiples barrios. Si se cambia la capacidad, debe
 * hacerse antes de cualquier otra instancia de {@code matrixGraph} en el sistema
 * (limitación del JAR: el campo es {@code static}).
 *
 * <p><b>Algoritmo de ruta:</b>
 * <ul>
 *   <li>{@link #calcularRutaMasCorta} → Dijkstra (distancia mínima en km)</li>
 *   <li>{@link #calcularRutaPorSaltos} → BFS ({@code shortestPath} del JAR, sin pesos)</li>
 * </ul>
 *
 * <p><b>Manejo de errores:</b> los {@code Exception} del JAR se absorben con
 * {@code try/catch} interno. El API retorna {@code false/null/-1.0} cuando la
 * operación no es posible.
 *
 * @author fsadriann
 */
public class CuadranteService implements CuadranteInterface {

    /**
     * Capacidad máxima de nodos del grafo.
     * El JAR usa {@code static int maxVerts}, así que este valor se aplica
     * globalmente. 50 cubre holgadamente un campus universitario con barrios.
     */
    private static final int CAPACIDAD_MAX = 50;

    /** Grafo de rutas: nodo = nombre del cuadrante, arista = distancia en km. */
    private final matrixGraph<String> mapa;

    /** Lista de objetos Cuadrante para operaciones CRUD (descripción, coordenadas, disponible). */
    private final LinkedList<Cuadrante> cuadrantes;

    public CuadranteService() {
        this.mapa       = new matrixGraph<>(CAPACIDAD_MAX);
        this.cuadrantes = new LinkedList<>();
    }

    // ── CRUD ──────────────────────────────────────────────────────────────────

    @Override
    public boolean agregarCuadrante(Cuadrante cuadrante) {
        if (cuadrante == null)
            throw new IllegalArgumentException("El cuadrante no puede ser nulo.");
        if (cuadrante.getNombre() == null || cuadrante.getNombre().isBlank())
            throw new IllegalArgumentException("El nombre del cuadrante no puede estar vacío.");

        // Nodo ya existe en el grafo → no duplicar
        if (mapa.searchVertex(cuadrante.getNombre()) != -1) return false;

        boolean agregado = mapa.addVortex(cuadrante.getNombre());
        if (agregado) cuadrantes.add(cuadrante);
        return agregado;
    }

    @Override
    public boolean editarCuadrante(Cuadrante cuadrante) {
        if (cuadrante == null)
            throw new IllegalArgumentException("El cuadrante no puede ser nulo.");
        Iterator<Cuadrante> it = cuadrantes.iterator();
        while (it.hasNext()) {
            Cuadrante c = it.next();
            if (c != null && c.getNombre().equals(cuadrante.getNombre())) {
                c.setDescripcion(cuadrante.getDescripcion());
                c.setLatitud(cuadrante.getLatitud());
                c.setLongitud(cuadrante.getLongitud());
                c.setDisponible(cuadrante.isDisponible());
                return true;
            }
        }
        return false; // no encontrado
    }

    @Override
    public Cuadrante buscarCuadrante(String nombre) {
        if (nombre == null) return null;
        Iterator<Cuadrante> it = cuadrantes.iterator();
        while (it.hasNext()) {
            Cuadrante c = it.next();
            if (c != null && c.getNombre().equals(nombre)) return c;
        }
        return null;
    }

    @Override
    public LinkedList<Cuadrante> listarCuadrantes() {
        return cuadrantes;
    }

    // ── Grafo — construcción ──────────────────────────────────────────────────

    @Override
    public boolean conectarCuadrantes(String nombreA, String nombreB, double distanciaKm) {
        if (distanciaKm <= 0)
            throw new IllegalArgumentException("La distancia debe ser mayor que cero.");
        if (nombreA == null || nombreB == null) return false;
        if (nombreA.equals(nombreB)) return false; // auto-loop no permitido
        if (mapa.searchVertex(nombreA) == -1 || mapa.searchVertex(nombreB) == -1) return false;

        try {
            boolean ab = mapa.addEdgeWithWeight(nombreA, nombreB, distanciaKm);
            boolean ba = mapa.addEdgeWithWeight(nombreB, nombreA, distanciaKm);
            return ab && ba;
        } catch (Exception e) {
            return false; // absorber Exception del JAR
        }
    }

    // ── Grafo — rutas ─────────────────────────────────────────────────────────

    @Override
    public LinkedList<String> calcularRutaMasCorta(String origen, String destino) {
        if (!validarNodos(origen, destino)) return null;
        if (origen.equals(destino)) return null; // misma ubicación
        if (!mapa.existsPath(origen, destino)) return null;
        return mapa.dijkstra(origen, destino);
    }

    @Override
    public double calcularDistancia(String origen, String destino) {
        if (!validarNodos(origen, destino)) return -1.0;
        if (origen.equals(destino)) return 0.0;
        if (!mapa.existsPath(origen, destino)) return -1.0;
        return mapa.dijkstraWeight(origen, destino);
    }

    @Override
    public LinkedList<String> calcularRutaPorSaltos(String origen, String destino) {
        if (!validarNodos(origen, destino)) return null;
        if (origen.equals(destino)) return null;
        if (!mapa.existsPath(origen, destino)) return null;
        return mapa.shortestPath(origen, destino);
    }

    @Override
    public boolean existeRuta(String origen, String destino) {
        if (!validarNodos(origen, destino)) return false;
        if (origen.equals(destino)) return true;
        return mapa.existsPath(origen, destino);
    }

    @Override
    public LinkedList<String> vecinosDirectos(String nombre) {
        if (nombre == null || mapa.searchVertex(nombre) == -1) return null;
        return mapa.getNeighbours(nombre);
    }

    // ── Consultas ─────────────────────────────────────────────────────────────

    @Override
    public int numeroCuadrantes() {
        return mapa.numberVertex();
    }

    /**
     * El grafo almacena A→B y B→A como 2 aristas.
     * Dividir entre 2 para reportar conexiones únicas.
     */
    @Override
    public int numeroConexiones() {
        return mapa.numberEdges() / 2;
    }

    @Override
    public String verMatrizAdyacencia() {
        return mapa.seeMatAdj();
    }

    // ── Helper privado ────────────────────────────────────────────────────────

    /**
     * Valida que ambos nombres no sean nulos y existan como nodos en el grafo.
     *
     * @return {@code true} si ambos nodos existen
     */
    private boolean validarNodos(String a, String b) {
        if (a == null || b == null) return false;
        return mapa.searchVertex(a) != -1 && mapa.searchVertex(b) != -1;
    }
}
