package edu.fsadriann.foodpbproyecto.model.user;

import edu.fsadriann.foodpbproyecto.model.Rol;

/**
 * Representa un operador del sistema Food UPB.
 *
 * <p>El operador interactúa con clientes, registra pedidos y los envía a cocina.
 * Toda la lógica de negocio reside en la capa Service; esta clase es solo el modelo.
 *
 * @author fsadriann
 */
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
