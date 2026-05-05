package edu.fsadriann.server.model.cuadrante;

import edu.fsadriann.app.graph.matrixGraph;
import edu.fsadriann.app.linkedlist.singly.singly.LinkedList;
import edu.fsadriann.model.iterator.Iterator;

import java.rmi.RemoteException;

public class CuadranteService implements CuadranteInterface {

    private static final int CAPACIDAD_MAX = 50;
    private final matrixGraph<String> mapa;
    private final LinkedList<Cuadrante> cuadrantes;
    private final CuadranteRepository repository = new CuadranteRepository();
    private static final String NODO_ORIGEN = "UPB";

    public CuadranteService() {
        this.mapa       = new matrixGraph<>(CAPACIDAD_MAX);
        this.cuadrantes = new LinkedList<>();
        cargarDatos();
    }

    private void cargarDatos() {
        // Cargar nodos
        for (Cuadrante c : repository.findAllCuadrantes()) {
            mapa.addVortex(c.getNombre());
            cuadrantes.add(c);
        }
        // Cargar conexiones
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
            repository.saveCuadrante(cuadrante);  // ← persiste
        }
        return agregado;
    }

    @Override
    public boolean editarCuadrante(Cuadrante cuadrante) throws RemoteException{
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
    public Cuadrante buscarCuadrante(String nombre) throws RemoteException {
        if (nombre == null) return null;
        Iterator<Cuadrante> it = cuadrantes.iterator();
        while (it.hasNext()) {
            Cuadrante c = it.next();
            if (c != null && c.getNombre().equals(nombre)) return c;
        }
        return null;
    }

    @Override
    public LinkedList<Cuadrante> listarCuadrantes() throws RemoteException {
        return cuadrantes;
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
            if (ab && ba) repository.saveConexion(nombreA, nombreB, distanciaKm); // ← persiste
            return ab && ba;
        } catch (Exception e) {
            return false;
        }
    }

    // ── Grafo — rutas ─────────────────────────────────────────────────────────

    @Override
    public LinkedList<String> calcularRutaMasCorta(String origen, String destino) throws RemoteException {
        if (!validarNodos(origen, destino)) return null;
        if (origen.equals(destino)) return null; // misma ubicación
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


    private boolean validarNodos(String a, String b) {
        if (a == null || b == null) return false;
        return mapa.searchVertex(a) != -1 && mapa.searchVertex(b) != -1;
    }
}
