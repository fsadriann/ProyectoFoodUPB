package edu.fsadriann.server.model.admin;

import edu.fsadriann.server.model.user.Rol;
import edu.fsadriann.server.model.order.Order;
import edu.fsadriann.server.model.user.User;
import edu.fsadriann.app.linkedlist.singly.singly.LinkedList;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Contrato remoto para las operaciones de administración del sistema Food UPB.
 */
public interface AdminInterface extends Remote {

    /**
     * Registra un nuevo operador en el sistema.
     *
     * @param operador datos del operador a crear
     * @return {@code true} si se creó exitosamente
     */
    boolean crearOperador(User operador) throws RemoteException;

    /**
     * Actualiza los datos de un operador existente.
     *
     * @param operador datos actualizados del operador
     * @return {@code true} si se editó exitosamente
     */
    boolean editarOperador(User operador) throws RemoteException;

    /**
     * Elimina un operador del sistema por su cédula.
     *
     * @param cedula cédula del operador a eliminar
     * @return {@code true} si se eliminó exitosamente
     */
    boolean eliminarOperador(String cedula) throws RemoteException;

    /**
     * Asigna un nuevo rol a un usuario del sistema.
     *
     * @param cedula cédula del usuario
     * @param rol    nuevo rol a asignar
     * @return {@code true} si se asignó exitosamente
     */
    boolean asignarRol(String cedula, Rol rol) throws RemoteException;

    /**
     * Genera un reporte de pedidos aplicando los filtros indicados.
     * Los parámetros {@code null} o vacíos no aplican filtro en ese campo.
     *
     * @param filtroFecha     fecha a filtrar
     * @param filtroEstado    estado del pedido a filtrar
     * @param filtroCuadrante cuadrante de destino a filtrar
     * @return lista de pedidos que cumplen los criterios
     */
    LinkedList<Order> generarReporte(String filtroFecha, String filtroEstado,
                                     String filtroCuadrante) throws RemoteException;

    /**
     * Retorna el historial de acciones registradas en la bitácora de auditoría.
     *
     * @return lista con todas las entradas de la bitácora
     */
    LinkedList<String> verBitacoraAuditoria() throws RemoteException;

    /**
     * Retorna todos los pedidos registrados en el sistema.
     *
     * @return lista completa de pedidos
     */
    LinkedList<Order> getPedidosTodos() throws RemoteException;
}
