package edu.fsadriann.server.model.cuadrante;

import edu.fsadriann.app.graph.matrixGraph;
import edu.fsadriann.app.linkedlist.singly.singly.LinkedList;
import edu.fsadriann.model.iterator.Iterator;

import java.rmi.RemoteException;

public class CuadranteService implements CuadranteInterface {

    private static final int    CAPACIDAD_MAX = 50;
    private static final String NODO_ORIGEN   = "UPB";


    private final matrixGraph<String>   mapa;
    private final LinkedList<Cuadrante> cuadrantes;
    private final CuadranteRepository  repository = new CuadranteRepository();

    public CuadranteService() {
        this.mapa       = new matrixGraph<>(CAPACIDAD_MAX);
        this.cuadrantes = new LinkedList<>();
        cargarDatos();
    }

    private void cargarDatos() {
        for (Cuadrante c : repository.findAllCuadrantes()) {
            mapa.addVortex(c.getNombre());
            cuadrantes.add(c);
        }
        for (CuadranteRepository.ConexionEntry conn : repository.findAllConexiones()) {
            try {
                mapa.addEdgeWithWeight(conn.origen,  conn.destino, conn.distancia);
                mapa.addEdgeWithWeight(conn.destino, conn.origen,  conn.distancia);
            } catch (Exception ignored) {}
        }
    }

    // ── CRUD ──────────────────────────────────────────────────────────────────

    @Override
    public boolean agregarCuadrante(Cuadrante cuadrante) throws RemoteException {
        if (cuadrante == null)
            throw new IllegalArgumentException("El cuadrante no puede ser nulo.");
        if (cuadrante.getNombre() == null || cuadrante.getNombre().isBlank())
            throw new IllegalArgumentException("El nombre no puede estar vacío.");
        if (mapa.searchVertex(cuadrante.getNombre()) != -1) return false;

        boolean agregado = mapa.addVortex(cuadrante.getNombre());
        if (agregado) {
            cuadrantes.add(cuadrante);
            repository.saveCuadrante(cuadrante);
        }
        return agregado;
    }

    @Override
    public boolean editarCuadrante(Cuadrante cuadrante) throws RemoteException {
        if (cuadrante == null)
            throw new IllegalArgumentException("El cuadrante no puede ser nulo.");
        Iterator<Cuadrante> it = cuadrantes.iterator();
        while (it.hasNext()) {
            Cuadrante c = it.next();
            if (c != null && c.getNombre().equals(cuadrante.getNombre())) {
                c.setDescripcion(cuadrante.getDescripcion());
                c.setDistanciaDesdeUPB(cuadrante.getDistanciaDesdeUPB());
                c.setDisponible(cuadrante.isDisponible());
                repository.saveCuadrante(c);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean eliminarCuadrante(String nombre) throws RemoteException {
        if (nombre == null || nombre.isBlank()) return false;
        if (NODO_ORIGEN.equals(nombre)) return false; // UPB nunca se elimina
        if (mapa.searchVertex(nombre) == -1) return false;

        // 1. Persistir eliminación en JSON
        repository.deleteCuadrante(nombre);
        repository.deleteConexionesDe(nombre);

        // 2. Eliminar del grafo — removeVortex elimina el nodo y todas sus aristas
        mapa.removeVortex(nombre);

        // La lista en memoria (cuadrantes) no necesita modificarse:
        // listarCuadrantes() filtra usando el grafo como fuente de verdad
        return true;
    }

    @Override
    public Cuadrante buscarCuadrante(String nombre) throws RemoteException {
        if (nombre == null) return null;
        Iterator<Cuadrante> it = cuadrantes.iterator();
        while (it.hasNext()) {
            Cuadrante c = it.next();
            if (c != null && c.getNombre().equals(nombre)) return c;
        }
        return null;
    }

    /**
     * Devuelve solo los cuadrantes que aún existen en el grafo.
     * Así no se necesita remove() en LinkedList — el grafo es la fuente de verdad.
     */
    @Override
    public LinkedList<Cuadrante> listarCuadrantes() throws RemoteException {
        LinkedList<Cuadrante> result = new LinkedList<>();
        Iterator<Cuadrante> it = cuadrantes.iterator();
        while (it.hasNext()) {
            Cuadrante c = it.next();
            if (c != null && mapa.searchVertex(c.getNombre()) != -1) {
                result.add(c);
            }
        }
        return result;
    }

    // ── Grafo — construcción ──────────────────────────────────────────────────

    @Override
    public boolean conectarCuadrantes(String nombreA, String nombreB, double distanciaKm) throws RemoteException {
        if (distanciaKm <= 0)
            throw new IllegalArgumentException("La distancia debe ser mayor que cero.");
        if (nombreA == null || nombreB == null) return false;
        if (nombreA.equals(nombreB)) return false;
        if (mapa.searchVertex(nombreA) == -1 || mapa.searchVertex(nombreB) == -1) return false;

        try {
            boolean ab = mapa.addEdgeWithWeight(nombreA, nombreB, distanciaKm);
            boolean ba = mapa.addEdgeWithWeight(nombreB, nombreA, distanciaKm);
            if (ab && ba) repository.saveConexion(nombreA, nombreB, distanciaKm);
            return ab && ba;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public LinkedList<CuadranteRepository.ConexionEntry> listarConexiones() throws RemoteException {
        LinkedList<CuadranteRepository.ConexionEntry> result = new LinkedList<>();
        for (CuadranteRepository.ConexionEntry e : repository.findAllConexiones()) {
            result.add(e);
        }
        return result;
    }

    // ── Grafo — rutas ─────────────────────────────────────────────────────────

    @Override
    public LinkedList<String> calcularRutaMasCorta(String origen, String destino) throws RemoteException {
        if (!validarNodos(origen, destino)) return null;
        if (origen.equals(destino)) return null;
        if (!mapa.existsPath(origen, destino)) return null;
        return mapa.dijkstra(origen, destino);
    }

    @Override
    public double calcularDistancia(String origen, String destino) throws RemoteException {
        if (!validarNodos(origen, destino)) return -1.0;
        if (origen.equals(destino)) return 0.0;
        if (!mapa.existsPath(origen, destino)) return -1.0;
        return mapa.dijkstraWeight(origen, destino);
    }

    @Override
    public LinkedList<String> calcularRutaPorSaltos(String origen, String destino) throws RemoteException {
        if (!validarNodos(origen, destino)) return null;
        if (origen.equals(destino)) return null;
        if (!mapa.existsPath(origen, destino)) return null;
        return mapa.shortestPath(origen, destino);
    }

    @Override
    public boolean existeRuta(String origen, String destino) throws RemoteException {
        if (!validarNodos(origen, destino)) return false;
        if (origen.equals(destino)) return true;
        return mapa.existsPath(origen, destino);
    }

    @Override
    public LinkedList<String> vecinosDirectos(String nombre) throws RemoteException {
        if (nombre == null || mapa.searchVertex(nombre) == -1) return null;
        return mapa.getNeighbours(nombre);
    }

    // ── Consultas ─────────────────────────────────────────────────────────────

    @Override
    public int numeroCuadrantes() throws RemoteException {
        return mapa.numberVertex();
    }

    @Override
    public int numeroConexiones() throws RemoteException {
        return mapa.numberEdges() / 2;
    }

    @Override
    public String verMatrizAdyacencia() throws RemoteException {
        return mapa.seeMatAdj();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private boolean validarNodos(String a, String b) {
        if (a == null || b == null) return false;
        return mapa.searchVertex(a) != -1 && mapa.searchVertex(b) != -1;
    }
}