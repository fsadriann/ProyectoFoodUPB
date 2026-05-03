package edu.fsadriann.server.model.admin;

import edu.fsadriann.server.model.user.Rol;
import edu.fsadriann.server.model.order.Order;
import edu.fsadriann.server.model.user.User;
import edu.fsadriann.app.linkedlist.singly.singly.LinkedList;

import java.rmi.RemoteException;


public interface AdminInterface {
    boolean crearOperador(User operador) throws RemoteException;
    boolean editarOperador(User operador) throws RemoteException;
    boolean eliminarOperador(String cedula) throws RemoteException;
    boolean asignarRol(String cedula, Rol rol) throws RemoteException;
    LinkedList<Order> generarReporte(String filtroFecha, String filtroEstado, String filtroCuadrante) throws RemoteException;
    LinkedList<String> verBitacoraAuditoria() throws RemoteException;
}
