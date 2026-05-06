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
    @Expose private Rol    rol;
    @Expose private String cedula;
    @Expose private boolean isPremium;
    @Expose private String telefono;
    @Expose private String direccion;

    /**
     * Cuadrante de entrega asociado a la dirección del cliente.
     * Se usa para asignarlo automáticamente al crear un pedido y
     * calcular el costo de domicilio por distancia desde UPB.
     * {@code null} si el cliente no tiene cuadrante asignado.
     */
    @Expose private String cuadrante;

    // favProductos sin @Expose → Gson lo ignora
    private LinkedList<Product> favProductos;

    // ── Constructor completo ──────────────────────────────────────────────────

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
        this.cuadrante    = null;
    }

    /**
     * Constructor extendido que incluye el cuadrante de entrega.
     * Útil al reconstruir usuarios desde JSON que ya tengan cuadrante guardado.
     */
    public User(String id, String nombres, String apellidos, Rol rol,
                String cedula, boolean isPremium, String telefono,
                String direccion, LinkedList<Product> favProductos,
                String cuadrante) {
        this(id, nombres, apellidos, rol, cedula, isPremium,
                telefono, direccion, favProductos);
        this.cuadrante = cuadrante;
    }

    // ── Getters ───────────────────────────────────────────────────────────────

    public String  getId()               { return id; }
    public String  getNombres()          { return nombres; }
    public String  getApellidos()        { return apellidos; }
    public Rol     getRol()              { return rol; }
    public String  getCedula()           { return cedula; }
    public boolean isPremium()           { return isPremium; }
    public String  getTelefono()         { return telefono; }
    public String  getDireccion()        { return direccion; }
    public String  getCuadrante()        { return cuadrante; }
    public LinkedList<Product> getFavProductos() { return favProductos; }

    public String getNombreCompleto() {
        return (nombres != null ? nombres : "") + " "
                + (apellidos != null ? apellidos : "");
    }

    // ── Setters ───────────────────────────────────────────────────────────────

    public void setNombres(String nombres)       { this.nombres    = nombres; }
    public void setApellidos(String apellidos)   { this.apellidos  = apellidos; }
    public void setPremium(boolean premium)      { this.isPremium  = premium; }
    public void setTelefono(String telefono)     { this.telefono   = telefono; }
    public void setDireccion(String direccion)   { this.direccion  = direccion; }

    /**
     * Asigna el cuadrante de entrega del cliente.
     * El nombre debe corresponder a un cuadrante registrado en {@code CuadranteService}.
     *
     * @param cuadrante nombre del cuadrante (ej. "Cabecera", "Lagos") o {@code null} para limpiar
     */
    public void setCuadrante(String cuadrante)   { this.cuadrante  = cuadrante; }

    // ── toString ──────────────────────────────────────────────────────────────

    @Override
    public String toString() {
        return "User{" +
                "id='"       + id                          + '\'' +
                ", nombre='" + getNombreCompleto().trim()  + '\'' +
                ", cedula='" + cedula                      + '\'' +
                ", premium=" + isPremium                          +
                ", tel="     + telefono                           +
                ", cuadrante='" + (cuadrante != null ? cuadrante : "sin asignar") + '\'' +
                '}';
    }
}