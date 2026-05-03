package edu.fsadriann.server.model.user;

import edu.fsadriann.app.linkedlist.singly.singly.LinkedList;
import edu.fsadriann.server.model.order.Order;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface UserInterface extends Remote {
    String login(String correo, String contrasena) throws RemoteException;
    void logout(String sesionId) throws RemoteException;
    boolean validarSesion(String sesionId) throws RemoteException;
    User buscarClientePorTelefono(String telefono) throws RemoteException;
    User registrarCliente(User user) throws RemoteException;
    boolean actualizarCliente(User user) throws RemoteException;
    boolean eliminarCliente(String cedula) throws RemoteException;
    LinkedList<Order> getPedidosFrecuentes(String cedula) throws RemoteException;
    boolean cambiarContrasena(String cedula, String actual, String nueva) throws RemoteException;
    boolean actualizarPerfil(User user) throws RemoteException;
}