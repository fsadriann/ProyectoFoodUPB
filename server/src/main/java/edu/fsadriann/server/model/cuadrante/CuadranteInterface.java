package edu.fsadriann.server.model.cuadrante;

import edu.fsadriann.app.linkedlist.singly.singly.LinkedList;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Contrato remoto para la gestión del mapa de cuadrantes y cálculo de rutas.
 */
public interface CuadranteInterface extends Remote {

    /**
     * Registra un nuevo cuadrante en el mapa.
     *
     * @param cuadrante cuadrante a agregar
     * @return {@code true} si se agregó exitosamente
     */
    boolean agregarCuadrante(Cuadrante cuadrante) throws RemoteException;

    /**
     * Actualiza la información de un cuadrante existente.
     *
     * @param cuadrante cuadrante con datos actualizados
     * @return {@code true} si se editó exitosamente
     */
    boolean editarCuadrante(Cuadrante cuadrante) throws RemoteException;

    /**
     * Elimina un cuadrante del mapa por su nombre.
     *
     * @param nombre nombre del cuadrante a eliminar
     * @return {@code true} si se eliminó exitosamente
     */
    boolean eliminarCuadrante(String nombre) throws RemoteException;

    /**
     * Busca un cuadrante por su nombre.
     *
     * @param nombre nombre del cuadrante
     * @return el cuadrante encontrado, o {@code null} si no existe
     */
    Cuadrante buscarCuadrante(String nombre) throws RemoteException;

    /**
     * Retorna todos los cuadrantes registrados en el mapa.
     *
     * @return lista de cuadrantes
     */
    LinkedList<Cuadrante> listarCuadrantes() throws RemoteException;

    /**
     * Conecta dos cuadrantes con una distancia en kilómetros.
     *
     * @param nombreA     nombre del primer cuadrante
     * @param nombreB     nombre del segundo cuadrante
     * @param distanciaKm distancia en km entre ambas zonas (debe ser mayor que 0)
     * @return {@code true} si la conexión se creó exitosamente
     */
    boolean conectarCuadrantes(String nombreA, String nombreB,
                                double distanciaKm) throws RemoteException;

    /**
     * Retorna todas las conexiones (aristas) del mapa.
     *
     * @return lista de conexiones entre cuadrantes
     */
    LinkedList<CuadranteRepository.ConexionEntry> listarConexiones() throws RemoteException;

    /**
     * Calcula la ruta más corta entre dos cuadrantes usando el algoritmo de Dijkstra.
     *
     * @param origen  nombre del cuadrante de origen
     * @param destino nombre del cuadrante de destino
     * @return lista ordenada de cuadrantes en la ruta, o {@code null} si no hay camino
     */
    LinkedList<String> calcularRutaMasCorta(String origen,
                                             String destino) throws RemoteException;

    /**
     * Calcula la distancia total en km entre dos cuadrantes por la ruta más corta.
     *
     * @param origen  nombre del cuadrante de origen
     * @param destino nombre del cuadrante de destino
     * @return distancia en km, o {@code -1} si no hay camino
     */
    double calcularDistancia(String origen, String destino) throws RemoteException;

    /**
     * Calcula la ruta con menor número de paradas entre dos cuadrantes.
     *
     * @param origen  nombre del cuadrante de origen
     * @param destino nombre del cuadrante de destino
     * @return lista de cuadrantes en la ruta por saltos, o {@code null} si no hay camino
     */
    LinkedList<String> calcularRutaPorSaltos(String origen,
                                              String destino) throws RemoteException;

    /**
     * Verifica si existe alguna ruta entre dos cuadrantes.
     *
     * @param origen  nombre del cuadrante de origen
     * @param destino nombre del cuadrante de destino
     * @return {@code true} si existe al menos un camino
     */
    boolean existeRuta(String origen, String destino) throws RemoteException;

    /**
     * Retorna los cuadrantes directamente conectados al cuadrante indicado.
     *
     * @param nombre nombre del cuadrante
     * @return lista de vecinos directos, o {@code null} si el cuadrante no existe
     */
    LinkedList<String> vecinosDirectos(String nombre) throws RemoteException;

    /**
     * @return cantidad total de cuadrantes en el mapa
     */
    int numeroCuadrantes() throws RemoteException;

    /**
     * @return cantidad total de conexiones en el mapa
     */
    int numeroConexiones() throws RemoteException;

    /**
     * @return representación en texto de la matriz de adyacencia del grafo
     */
    String verMatrizAdyacencia() throws RemoteException;
}
