package edu.fsadriann.server.model.user;

import com.google.gson.annotations.Expose;
import edu.fsadriann.app.linkedlist.singly.singly.LinkedList;
import edu.fsadriann.server.model.product.Product;
import java.io.Serializable;

public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    @Expose private final String id;
    @Expose private String nombres;
    @Expose private String apellidos;
    @Expose private Rol rol;
    @Expose private String cedula;
    @Expose private boolean isPremium;
    @Expose private String telefono;
    @Expose private String direccion;
    // favProductos sin @Expose → Gson lo ignora
    private LinkedList<Product> favProductos;

    public User(String id, String nombres, String apellidos, Rol rol,
                String cedula, boolean isPremium, String telefono,
                String direccion, LinkedList<Product> favProductos) {
        this.id           = id;
        this.nombres      = nombres;
        this.apellidos    = apellidos;
        this.rol          = rol;
        this.cedula       = cedula;
        this.isPremium    = isPremium;
        this.telefono     = telefono;
        this.direccion    = direccion;
        this.favProductos = (favProductos != null) ? favProductos : new LinkedList<>();
    }

    public String getId()          { return id; }
    public String getNombres()     { return nombres; }
    public String getApellidos()   { return apellidos; }
    public String getNombreCompleto() {
        return (nombres != null ? nombres : "") + " " + (apellidos != null ? apellidos : "");
    }
    public Rol getRol()            { return rol; }
    public String getCedula()      { return cedula; }
    public boolean isPremium()     { return isPremium; }
    public String getTelefono()    { return telefono; }
    public String getDireccion()   { return direccion; }
    public LinkedList<Product> getFavProductos() { return favProductos; }

    public void setNombres(String nombres)     { this.nombres = nombres; }
    public void setApellidos(String apellidos) { this.apellidos = apellidos; }
    public void setPremium(boolean premium)    { this.isPremium = premium; }
    public void setTelefono(String telefono)   { this.telefono = telefono; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", nombre='" + getNombreCompleto().trim() + '\'' +
                ", cedula='" + cedula + '\'' +
                ", premium=" + isPremium +
                ", tel=" + telefono +
                '}';
    }
}