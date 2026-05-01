package edu.fsadriann.foodpbproyecto.model.admin;

import edu.fsadriann.foodpbproyecto.model.user.Rol;
import edu.fsadriann.foodpbproyecto.model.order.Order;
import edu.fsadriann.foodpbproyecto.model.user.User;
import edu.fsadriann.model.list.List;

import java.rmi.RemoteException;

public interface AdminInterface {
    boolean crearOperador(User operador) throws RemoteException;
    boolean editarOperador(User operador) throws RemoteException;
    boolean eliminarOperador(String cedula) throws RemoteException;
    boolean asignarRol(String cedula, Rol rol) throws RemoteException;
    List<Order> generarReporte(String filtroFecha, String filtroEstado, String filtroCuadrante) throws RemoteException;
    List<String> verBitacoraAuditoria() throws RemoteException;     // RF-12
}
