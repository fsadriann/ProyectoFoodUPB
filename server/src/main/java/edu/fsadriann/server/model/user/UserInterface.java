package edu.fsadriann.server.model.user;

import edu.fsadriann.app.linkedlist.singly.singly.LinkedList;
import edu.fsadriann.server.model.order.Order;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Contrato remoto para la gestión de usuarios y autenticación en Food UPB.
 */
public interface UserInterface extends Remote {

    /**
     * Autentica a un usuario y retorna su rol si las credenciales son válidas.
     *
     * @param correo     correo electrónico del usuario
     * @param contrasena contraseña de acceso
     * @return rol del usuario autenticado, o {@code null} si las credenciales son incorrectas
     */
    Rol login(String correo, String contrasena) throws RemoteException;

    /**
     * Cierra la sesión activa del usuario.
     *
     * @param sesionId identificador de la sesión a cerrar
     */
    void logout(String sesionId) throws RemoteException;

    /**
     * Verifica si una sesión está activa.
     *
     * @param sesionId identificador de sesión a validar
     * @return {@code true} si la sesión es válida
     */
    boolean validarSesion(String sesionId) throws RemoteException;

    /**
     * Busca un cliente por su número de teléfono.
     *
     * @param telefono número de teléfono del cliente
     * @return el usuario encontrado, o {@code null} si no existe
     */
    User buscarClientePorTelefono(String telefono) throws RemoteException;

    /**
     * Registra un nuevo cliente en el sistema.
     *
     * @param user datos del cliente a registrar
     * @return el usuario registrado
     */
    User registrarCliente(User user) throws RemoteException;

    /**
     * Retorna todos los usuarios registrados en el sistema.
     *
     * @return lista de usuarios
     */
    LinkedList<User> listarUsuarios() throws RemoteException;

    /**
     * @return cantidad total de usuarios registrados
     */
    int getTotalUsuarios() throws RemoteException;

    /**
     * Actualiza los datos de un cliente existente.
     *
     * @param user datos actualizados del cliente
     * @return {@code true} si se actualizó exitosamente
     */
    boolean actualizarCliente(User user) throws RemoteException;

    /**
     * Elimina un cliente del sistema por su cédula.
     *
     * @param cedula cédula del cliente a eliminar
     * @return {@code true} si se eliminó exitosamente
     */
    boolean eliminarCliente(String cedula) throws RemoteException;

    /**
     * Retorna los pedidos más frecuentes de un cliente (hasta 10).
     *
     * @param cedula cédula del cliente
     * @return lista de pedidos ordenados por cantidad de productos
     */
    LinkedList<Order> getPedidosFrecuentes(String cedula) throws RemoteException;

    /**
     * Cambia la contraseña de un usuario.
     *
     * @param cedula  cédula del usuario
     * @param actual  contraseña actual
     * @param nueva   nueva contraseña
     * @return {@code true} si el cambio fue exitoso
     */
    boolean cambiarContrasena(String cedula, String actual,
                               String nueva) throws RemoteException;

    /**
     * Actualiza el perfil del usuario autenticado.
     *
     * @param user datos actualizados del usuario
     * @return {@code true} si se actualizó exitosamente
     */
    boolean actualizarPerfil(User user) throws RemoteException;

    /**
     * Registra las credenciales de acceso para un cliente existente.
     *
     * @param cedula     cédula del cliente
     * @param contrasena contraseña a asignar
     */
    void registrarCredencial(String cedula, String contrasena) throws RemoteException;
}
