package edu.fsadriann.view.admin;

public class AdminUserFormData {
    private final String  nombre;
    private final String  apellido;
    private final String  telefono;
    private final String  correo;
    private final String  contrasena;
    private final String  rol;
    private final String  direccionCompleta;
    private final boolean isPremium;
    private final String  cuadrante;

    public AdminUserFormData(String nombre, String apellido, String telefono,
                             String correo, String contrasena, String rol,
                             String direccionCompleta) {
        this(nombre, apellido, telefono, correo, contrasena, rol, direccionCompleta, false, null);
    }

    public AdminUserFormData(String nombre, String apellido, String telefono,
                             String correo, String contrasena, String rol,
                             String direccionCompleta, boolean isPremium, String cuadrante) {
        this.nombre            = nombre;
        this.apellido          = apellido;
        this.telefono          = telefono;
        this.correo            = correo;
        this.contrasena        = contrasena;
        this.rol               = rol;
        this.direccionCompleta = direccionCompleta == null ? "" : direccionCompleta;
        this.isPremium         = isPremium;
        this.cuadrante         = cuadrante;
    }

    public String  getNombre()            { return nombre; }
    public String  getApellido()          { return apellido; }
    public String  getTelefono()          { return telefono; }
    public String  getCorreo()            { return correo; }
    public String  getContrasena()        { return contrasena; }
    public String  getRol()               { return rol; }
    public String  getDireccionCompleta() { return direccionCompleta; }
    public boolean isPremium()            { return isPremium; }
    public String  getCuadrante()         { return cuadrante; }
}