package edu.fsadriann.foodpbproyecto.model.auth;

import java.rmi.RemoteException;

public interface AuthInterface {
    String login(String usuario, String contrasena) throws RemoteException; // retorna token/sesionId
    void logout(String sesionId) throws RemoteException;
    boolean validarSesion(String sesionId) throws RemoteException;
}

