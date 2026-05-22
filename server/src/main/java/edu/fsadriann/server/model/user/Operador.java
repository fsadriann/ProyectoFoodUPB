package edu.fsadriann.server.model.user;

/**
 * Representa a un operador del sistema Food UPB.
 * Extiende {@link AbstractUser} con el rol fijo {@link Rol#OPERADOR}.
 */
public class Operador extends AbstractUser {

    /**
     * Crea un operador con sus credenciales de acceso.
     *
     * @param id         identificador único del operador
     * @param nombres    nombres del operador
     * @param apellidos  apellidos del operador
     * @param correo     correo electrónico para iniciar sesión
     * @param contrasena contraseña de acceso
     */
    public Operador(int id, String nombres, String apellidos,
                    String correo, String contrasena) {
        super(id, nombres, apellidos, correo, contrasena, Rol.OPERADOR);
    }
}
