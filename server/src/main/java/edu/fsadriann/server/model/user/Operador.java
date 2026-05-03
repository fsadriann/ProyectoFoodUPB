package edu.fsadriann.server.model.user;

public class Operador extends AbstractUser {

    /**
     * Crea un operador con credenciales de acceso al sistema.
     *
     * @param id        identificador único del operador
     * @param nombres   nombres del operador
     * @param apellidos apellidos del operador
     * @param correo    correo electrónico (usado para login)
     * @param contrasena contraseña de acceso
     */
    public Operador(int id, String nombres, String apellidos,
                    String correo, String contrasena) {
        super(id, nombres, apellidos, correo, contrasena, Rol.OPERADOR);
    }
}
