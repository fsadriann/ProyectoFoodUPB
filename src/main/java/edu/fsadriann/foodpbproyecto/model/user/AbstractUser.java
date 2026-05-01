package edu.fsadriann.foodpbproyecto.model.user;

public abstract class AbstractUser {
    private int id;
    private String nombres;
    private String apellidos;
    private String correo;
    private String contrasena;
    private Rol rol;

    public AbstractUser(int id, String nombres, String apellidos, String correo, String contrasena, Rol rol) {
        if (correo == null || !correo.contains("@")){
            throw new IllegalArgumentException("Correo no válido:" + correo);
        }
        this.id = id;
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.correo = correo;
        this.contrasena = contrasena;
        this.rol = rol;
    }

    public int getId() {
        return id;
    }
    public String getNombreCompleto() {
        return nombres + " " + apellidos;
    }
    public String getCorreo() {
        return correo;
    }
    public String getContrasena() {
        return contrasena;
    }
    public Rol getRol() {
        return rol;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }

    public boolean verificarContrasena(String intento){
        return this.contrasena != null && this.contrasena.equals(intento);
    }

    @Override
    public String toString(){
        return "[" + rol + "] " + getNombreCompleto()
                + " (id=" + id + ", correo=" + correo + ")";
    }
}
