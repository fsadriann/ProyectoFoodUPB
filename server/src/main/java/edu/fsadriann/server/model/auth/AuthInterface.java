package edu.fsadriann.server.model.auth;

import java.rmi.RemoteException;

/**
 * Contrato para las operaciones de autenticación y gestión de sesiones.
 */
public interface AuthInterface {

    /**
     * Autentica al usuario y devuelve un identificador de sesión.
     *
     * @param usuario    correo o nombre de usuario
     * @param contrasena contraseña de acceso
     * @return identificador de sesión activa, o {@code null} si las credenciales son inválidas
     */
    String login(String usuario, String contrasena) throws RemoteException;

    /**
     * Cierra la sesión asociada al identificador dado.
     *
     * @param sesionId identificador de la sesión a cerrar
     */
    void logout(String sesionId) throws RemoteException;

    /**
     * Verifica si una sesión está activa.
     *
     * @param sesionId identificador de sesión a validar
     * @return {@code true} si la sesión existe y es válida
     */
    boolean validarSesion(String sesionId) throws RemoteException;
}
