package edu.fsadriann.server.model.user;

/**
 * Base abstracta para los tipos de usuario que acceden al sistema con correo y contraseña.
 */
public abstract class AbstractUser {

    private int    id;
    private String nombres;
    private String apellidos;
    private String correo;
    private String contrasena;
    private Rol    rol;

    /**
     * Crea un usuario base con credenciales de acceso.
     *
     * @param id         identificador numérico del usuario
     * @param nombres    nombres del usuario
     * @param apellidos  apellidos del usuario
     * @param correo     correo electrónico (debe contener '@')
     * @param contrasena contraseña de acceso
     * @param rol        rol asignado al usuario
     */
    public AbstractUser(int id, String nombres, String apellidos,
                        String correo, String contrasena, Rol rol) {
        if (correo == null || !correo.contains("@")) {
            throw new IllegalArgumentException("Correo no válido:" + correo);
        }
        this.id         = id;
        this.nombres    = nombres;
        this.apellidos  = apellidos;
        this.correo     = correo;
        this.contrasena = contrasena;
        this.rol        = rol;
    }

    /** @return identificador numérico del usuario */
    public int getId() { return id; }

    /** @return nombre completo (nombres + apellidos) */
    public String getNombreCompleto() { return nombres + " " + apellidos; }

    /** @return correo electrónico del usuario */
    public String getCorreo()     { return correo; }

    /** @return contraseña almacenada */
    public String getContrasena() { return contrasena; }

    /** @return rol del usuario en el sistema */
    public Rol getRol()           { return rol; }

    /** @param contrasena nueva contraseña del usuario */
    public void setContrasena(String contrasena) { this.contrasena = contrasena; }

    /**
     * Verifica si la contraseña ingresada coincide con la almacenada.
     *
     * @param intento contraseña a verificar
     * @return {@code true} si coincide
     */
    public boolean verificarContrasena(String intento) {
        return this.contrasena != null && this.contrasena.equals(intento);
    }

    @Override
    public String toString() {
        return "[" + rol + "] " + getNombreCompleto()
                + " (id=" + id + ", correo=" + correo + ")";
    }
}
