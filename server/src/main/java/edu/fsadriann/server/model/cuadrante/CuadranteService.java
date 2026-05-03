package edu.fsadriann.server.model.cuadrante;

import edu.fsadriann.app.graph.matrixGraph;
import edu.fsadriann.app.linkedlist.singly.singly.LinkedList;
import edu.fsadriann.model.iterator.Iterator;

public class CuadranteService implements CuadranteInterface {

    private static final int CAPACIDAD_MAX = 50;

    private final matrixGraph<String> mapa;

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

    @Override
    public int numeroConexiones() {
        return mapa.numberEdges() / 2;
    }

    @Override
    public String verMatrizAdyacencia() {
        return mapa.seeMatAdj();
    }


    private boolean validarNodos(String a, String b) {
        if (a == null || b == null) return false;
        return mapa.searchVertex(a) != -1 && mapa.searchVertex(b) != -1;
    }
}
