package edu.fsadriann.server.model.cuadrante;

import edu.fsadriann.app.graph.Graph;
import edu.fsadriann.app.linkedlist.singly.singly.LinkedList;
import edu.fsadriann.model.iterator.Iterator;

import java.rmi.RemoteException;

/**
 * Servicio que gestiona el mapa de cuadrantes de entrega usando un grafo ponderado.
 * Cada cuadrante es un nodo del grafo y cada conexión tiene como peso la distancia en km.
 * Permite calcular rutas óptimas con Dijkstra y rutas por menor número de saltos con BFS.
 */
public class CuadranteService implements CuadranteInterface {

    private static final int    CAPACIDAD_MAX = 50;
    private static final String NODO_ORIGEN   = "UPB";

    private final Graph<String>              mapa;
    private final LinkedList<Cuadrante>      cuadrantes;
    private final CuadranteRepository        repository = new CuadranteRepository();

    /**
     * Crea el servicio y carga los cuadrantes y conexiones desde el archivo JSON.
     */
    public CuadranteService() {
        this.mapa       = new Graph<>(CAPACIDAD_MAX);
        this.cuadrantes = new LinkedList<>();
        cargarDatos();
    }

    private void cargarDatos() {
        Iterator<Cuadrante> itC = repository.findAllCuadrantes().iterator();
        while (itC.hasNext()) {
            Cuadrante c = itC.next();
            if (c == null) continue;
            mapa.addVortex(c.getNombre());
            cuadrantes.add(c);
        }
        Iterator<CuadranteRepository.ConexionEntry> itE = repository.findAllConexiones().iterator();
        while (itE.hasNext()) {
            CuadranteRepository.ConexionEntry conn = itE.next();
            if (conn == null) continue;
            try {
                mapa.addEdgeWithWeight(conn.origen,  conn.destino, conn.distancia);
                mapa.addEdgeWithWeight(conn.destino, conn.origen,  conn.distancia);
            } catch (Exception ignored) {}
        }
    }

    /**
     * Registra un nuevo cuadrante en el mapa y lo persiste en el archivo JSON.
     *
     * @param cuadrante cuadrante a agregar
     * @return {@code true} si se agregó exitosamente; {@code false} si ya existe
     * @throws IllegalArgumentException si el cuadrante o su nombre son nulos o vacíos
     */
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

    /**
     * Actualiza la descripción, distancia y disponibilidad de un cuadrante existente.
     *
     * @param cuadrante cuadrante con datos actualizados
     * @return {@code true} si se editó exitosamente
     */
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

    /**
     * Elimina un cuadrante del mapa y sus conexiones. No se puede eliminar el nodo origen "UPB".
     *
     * @param nombre nombre del cuadrante a eliminar
     * @return {@code true} si se eliminó exitosamente
     */
    @Override
    public boolean eliminarCuadrante(String nombre) throws RemoteException {
        if (nombre == null || nombre.isBlank()) return false;
        if (NODO_ORIGEN.equals(nombre)) return false;
        if (mapa.searchVertex(nombre) == -1) return false;

        repository.deleteCuadrante(nombre);
        repository.deleteConexionesDe(nombre);

        mapa.removeVortex(nombre);
        return true;
    }

    /**
     * Busca un cuadrante por su nombre en la lista en memoria.
     *
     * @param nombre nombre del cuadrante
     * @return el cuadrante encontrado, o {@code null} si no existe
     */
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
     * Retorna todos los cuadrantes que existen tanto en la lista en memoria como en el grafo.
     *
     * @return lista de cuadrantes activos
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

    /**
     * Conecta dos cuadrantes en el grafo con una arista bidireccional y persiste la conexión.
     *
     * @param nombreA     nombre del primer cuadrante
     * @param nombreB     nombre del segundo cuadrante
     * @param distanciaKm distancia en km (debe ser mayor que 0)
     * @return {@code true} si la conexión se creó exitosamente
     * @throws IllegalArgumentException si la distancia no es positiva
     */
    @Override
    public boolean conectarCuadrantes(String nombreA, String nombreB,
                                       double distanciaKm) throws RemoteException {
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

    /**
     * Retorna todas las conexiones del mapa desde el repositorio.
     *
     * @return lista de conexiones entre cuadrantes
     */
    @Override
    public LinkedList<CuadranteRepository.ConexionEntry> listarConexiones() throws RemoteException {
        LinkedList<CuadranteRepository.ConexionEntry> result = new LinkedList<>();
        Iterator<CuadranteRepository.ConexionEntry> it = repository.findAllConexiones().iterator();
        while (it.hasNext()) {
            CuadranteRepository.ConexionEntry e = it.next();
            if (e != null) result.add(e);
        }
        return result;
    }

    /**
     * Calcula la ruta de menor distancia entre dos cuadrantes usando el algoritmo de Dijkstra.
     *
     * @param origen  nombre del cuadrante de origen
     * @param destino nombre del cuadrante de destino
     * @return lista ordenada de cuadrantes en la ruta, o {@code null} si no hay camino
     */
    @Override
    public LinkedList<String> calcularRutaMasCorta(String origen,
                                                    String destino) throws RemoteException {
        if (!validarNodos(origen, destino)) return null;
        if (origen.equals(destino)) return null;
        if (!mapa.existsPath(origen, destino)) return null;
        return mapa.dijkstra(origen, destino);
    }

    /**
     * Calcula la distancia total en km entre dos cuadrantes por la ruta más corta.
     *
     * @param origen  nombre del cuadrante de origen
     * @param destino nombre del cuadrante de destino
     * @return distancia en km, o {@code -1} si no existe ruta
     */
    @Override
    public double calcularDistancia(String origen, String destino) throws RemoteException {
        if (!validarNodos(origen, destino)) return -1.0;
        if (origen.equals(destino)) return 0.0;
        if (!mapa.existsPath(origen, destino)) return -1.0;
        return mapa.dijkstraWeight(origen, destino);
    }

    /**
     * Calcula la ruta con menor número de paradas entre dos cuadrantes.
     *
     * @param origen  nombre del cuadrante de origen
     * @param destino nombre del cuadrante de destino
     * @return lista de cuadrantes en la ruta por saltos, o {@code null} si no hay camino
     */
    @Override
    public LinkedList<String> calcularRutaPorSaltos(String origen,
                                                     String destino) throws RemoteException {
        if (!validarNodos(origen, destino)) return null;
        if (origen.equals(destino)) return null;
        if (!mapa.existsPath(origen, destino)) return null;
        return mapa.shortestPath(origen, destino);
    }

    /**
     * Verifica si existe al menos una ruta entre dos cuadrantes.
     *
     * @param origen  nombre del cuadrante de origen
     * @param destino nombre del cuadrante de destino
     * @return {@code true} si hay camino disponible
     */
    @Override
    public boolean existeRuta(String origen, String destino) throws RemoteException {
        if (!validarNodos(origen, destino)) return false;
        if (origen.equals(destino)) return true;
        return mapa.existsPath(origen, destino);
    }

    /**
     * Retorna los cuadrantes directamente conectados al cuadrante indicado.
     *
     * @param nombre nombre del cuadrante
     * @return lista de vecinos directos, o {@code null} si el cuadrante no existe
     */
    @Override
    public LinkedList<String> vecinosDirectos(String nombre) throws RemoteException {
        if (nombre == null || mapa.searchVertex(nombre) == -1) return null;
        return mapa.getNeighbours(nombre);
    }

    /** @return cantidad total de cuadrantes en el mapa */
    @Override
    public int numeroCuadrantes() throws RemoteException {
        return mapa.numberVertex();
    }

    /** @return cantidad total de conexiones únicas en el mapa */
    @Override
    public int numeroConexiones() throws RemoteException {
        return mapa.numberEdges() / 2;
    }

    /** @return representación en texto de la matriz de adyacencia del grafo */
    @Override
    public String verMatrizAdyacencia() throws RemoteException {
        return mapa.seeMatAdj();
    }

    private boolean validarNodos(String a, String b) {
        if (a == null || b == null) return false;
        return mapa.searchVertex(a) != -1 && mapa.searchVertex(b) != -1;
    }
}
