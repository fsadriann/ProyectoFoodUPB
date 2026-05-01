package edu.fsadriann.foodpbproyecto.model.cuadrante;

import edu.fsadriann.app.linkedlist.singly.singly.LinkedList;

/**
 * Contrato del servicio de cuadrantes y rutas. RF-06.
 *
 * <p><b>Modelo de operación:</b>
 * <ol>
 *   <li>Registrar cuadrantes con {@link #agregarCuadrante(Cuadrante)}.</li>
 *   <li>Conectarlos con {@link #conectarCuadrantes(String, String, double)}
 *       — la conexión es <em>bidireccional</em> automáticamente.</li>
 *   <li>Calcular rutas con {@link #calcularRutaMasCorta(String, String)}
 *       (Dijkstra — usa distancias reales en km).</li>
 * </ol>
 *
 * <p><b>Sin checked exceptions:</b> los errores del JAR se absorben
 * internamente. El API retorna {@code false/null/-1} cuando una operación
 * no es posible, o lanza {@link IllegalArgumentException} para entradas inválidas.
 *
 * @author fsadriann
 */
public interface CuadranteInterface {

    // ── CRUD ──────────────────────────────────────────────────────────────────

    /**
     * Registra un cuadrante como nodo en el grafo de rutas.
     *
     * @param cuadrante cuadrante a registrar
     * @return {@code true} si se registró; {@code false} si ya existía
     * @throws IllegalArgumentException si el cuadrante o su nombre es nulo/vacío
     */
    boolean agregarCuadrante(Cuadrante cuadrante);

    /**
     * Actualiza descripción, coordenadas o disponibilidad de un cuadrante existente.
     *
     * @param cuadrante cuadrante con datos actualizados (el nombre es la clave)
     * @return {@code true} si se encontró y actualizó; {@code false} si no existe
     */
    boolean editarCuadrante(Cuadrante cuadrante);

    /**
     * Busca un cuadrante por su nombre (ID único).
     *
     * @param nombre nombre del cuadrante
     * @return cuadrante encontrado, o {@code null} si no existe
     */
    Cuadrante buscarCuadrante(String nombre);

    /**
     * Retorna todos los cuadrantes registrados.
     *
     * @return lista del JAR con todos los cuadrantes; vacía si no hay ninguno
     */
    LinkedList<Cuadrante> listarCuadrantes();

    // ── Grafo — construcción ──────────────────────────────────────────────────

    /**
     * Conecta dos cuadrantes con una arista bidireccional ponderada.
     *
     * <p>Crea automáticamente A→B y B→A con el mismo peso, por lo que
     * no es necesario llamar este método dos veces.
     *
     * @param nombreA     cuadrante origen (debe existir)
     * @param nombreB     cuadrante destino (debe existir)
     * @param distanciaKm distancia entre ellos en kilómetros (debe ser &gt; 0)
     * @return {@code true} si la conexión se creó; {@code false} si algún nodo
     *         no existe o hubo error interno del grafo
     * @throws IllegalArgumentException si distanciaKm &le; 0
     */
    boolean conectarCuadrantes(String nombreA, String nombreB, double distanciaKm);

    // ── Grafo — rutas ─────────────────────────────────────────────────────────

    /**
     * Calcula la ruta de menor distancia entre dos cuadrantes usando <b>Dijkstra</b>.
     *
     * <p>Siempre usa pesos (km). Para el número mínimo de saltos
     * ver {@link #calcularRutaPorSaltos(String, String)}.
     *
     * @param origen  nombre del cuadrante de partida
     * @param destino nombre del cuadrante de llegada
     * @return lista ordenada de nombres de cuadrantes en la ruta óptima,
     *         o {@code null} si no existe camino o las entradas son inválidas
     */
    LinkedList<String> calcularRutaMasCorta(String origen, String destino);

    /**
     * Calcula la distancia total de la ruta óptima (Dijkstra) en kilómetros.
     *
     * @param origen  cuadrante de partida
     * @param destino cuadrante de llegada
     * @return distancia en km, o {@code -1.0} si no hay camino o entrada inválida
     */
    double calcularDistancia(String origen, String destino);

    /**
     * Calcula la ruta con menor número de saltos (BFS, ignora pesos).
     * Método secundario — para entregas, preferir {@link #calcularRutaMasCorta}.
     *
     * @param origen  cuadrante de partida
     * @param destino cuadrante de llegada
     * @return lista de nombres en la ruta por saltos, o {@code null} si no existe
     */
    LinkedList<String> calcularRutaPorSaltos(String origen, String destino);

    /**
     * Indica si existe algún camino entre dos cuadrantes.
     *
     * @param origen  cuadrante de partida
     * @param destino cuadrante de llegada
     * @return {@code true} si hay al menos un camino; {@code false} en caso contrario
     */
    boolean existeRuta(String origen, String destino);

    /**
     * Retorna los cuadrantes directamente conectados al dado.
     *
     * @param nombre nombre del cuadrante
     * @return lista de vecinos directos, o {@code null} si el cuadrante no existe
     */
    LinkedList<String> vecinosDirectos(String nombre);

    // ── Consultas del grafo ───────────────────────────────────────────────────

    /** @return número de cuadrantes registrados */
    int numeroCuadrantes();

    /** @return número de conexiones únicas (aristas bidireccionales cuentan como 1) */
    int numeroConexiones();

    /**
     * Retorna la matriz de adyacencia como texto, útil para depuración.
     *
     * @return representación textual de la matriz del JAR
     */
    String verMatrizAdyacencia();
}
