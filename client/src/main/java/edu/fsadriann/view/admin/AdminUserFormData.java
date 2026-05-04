package edu.fsadriann.view.admin;

public class AdminUserFormData {
    private final String nombre;
    private final String apellido;
    private final String telefono;
    private final String correo;
    private final String contrasena;
    private final String rol;
    private final String direccionCompleta;

    public AdminUserFormData(String nombre,
                             String apellido,
                             String telefono,
                             String correo,
                             String contrasena,
                             String rol,
                             String direccionCompleta) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.telefono = telefono;
        this.correo = correo;
        this.contrasena = contrasena;
        this.rol = rol;
        this.direccionCompleta = direccionCompleta == null ? "" : direccionCompleta;
    }

    public String getNombre() {
        return nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public String getTelefono() {
        return telefono;
    }

    public String getCorreo() {
        return correo;
    }

    public String getContrasena() {
        return contrasena;
    }

    public String getRol() {
        return rol;
    }

    public String getDireccionCompleta() {
        return direccionCompleta;
    }
}
