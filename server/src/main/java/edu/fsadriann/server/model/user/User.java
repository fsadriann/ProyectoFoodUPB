package edu.fsadriann.server.model.user;

import com.google.gson.annotations.Expose;
import edu.fsadriann.app.linkedlist.singly.singly.LinkedList;
import edu.fsadriann.server.model.product.Product;

import java.io.Serializable;

/**
 * Representa a un usuario del sistema Food UPB (cliente, operador, admin, etc.).
 * Los campos anotados con {@code @Expose} se persisten en JSON; {@code favProductos} no.
 */
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    @Expose private final String  id;
    @Expose private String  nombres;
    @Expose private String  apellidos;
    @Expose private Rol     rol;
    @Expose private String  cedula;
    @Expose private boolean isPremium;
    @Expose private String  telefono;
    @Expose private String  direccion;

    /**
     * Cuadrante de entrega asociado a la dirección del cliente.
     * Se usa para calcular el costo de domicilio por distancia desde UPB.
     * Es {@code null} si el cliente no tiene cuadrante asignado.
     */
    @Expose private String cuadrante;

    private LinkedList<Product> favProductos;

    /**
     * Crea un usuario con su información completa.
     *
     * @param id           correo o identificador de sesión
     * @param nombres      nombres del usuario
     * @param apellidos    apellidos del usuario
     * @param rol          rol en el sistema
     * @param cedula       número de cédula
     * @param isPremium    {@code true} si tiene membresía premium
     * @param telefono     número de teléfono
     * @param direccion    dirección de domicilio
     * @param favProductos lista de productos favoritos
     */
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
     * Crea un usuario incluyendo el cuadrante de entrega.
     * Se usa al reconstruir usuarios desde JSON que ya tengan cuadrante guardado.
     */
    public User(String id, String nombres, String apellidos, Rol rol,
                String cedula, boolean isPremium, String telefono,
                String direccion, LinkedList<Product> favProductos,
                String cuadrante) {
        this(id, nombres, apellidos, rol, cedula, isPremium, telefono, direccion, favProductos);
        this.cuadrante = cuadrante;
    }

    /** @return identificador del usuario (correo o ID de sesión) */
    public String  getId()           { return id; }

    /** @return nombres del usuario */
    public String  getNombres()      { return nombres; }

    /** @return apellidos del usuario */
    public String  getApellidos()    { return apellidos; }

    /** @return rol del usuario en el sistema */
    public Rol     getRol()          { return rol; }

    /** @return cédula del usuario */
    public String  getCedula()       { return cedula; }

    /** @return {@code true} si el usuario tiene membresía premium */
    public boolean isPremium()       { return isPremium; }

    /** @return número de teléfono */
    public String  getTelefono()     { return telefono; }

    /** @return dirección de domicilio */
    public String  getDireccion()    { return direccion; }

    /** @return nombre del cuadrante de entrega asignado, o {@code null} */
    public String  getCuadrante()    { return cuadrante; }

    /** @return lista de productos favoritos del usuario */
    public LinkedList<Product> getFavProductos() { return favProductos; }

    /** @return nombre completo (nombres + apellidos) */
    public String getNombreCompleto() {
        return (nombres != null ? nombres : "") + " "
                + (apellidos != null ? apellidos : "");
    }

    /** @param nombres nuevos nombres del usuario */
    public void setNombres(String nombres)     { this.nombres   = nombres; }

    /** @param apellidos nuevos apellidos del usuario */
    public void setApellidos(String apellidos) { this.apellidos = apellidos; }

    /** @param premium {@code true} para activar membresía premium */
    public void setPremium(boolean premium)    { this.isPremium = premium; }

    /** @param telefono nuevo número de teléfono */
    public void setTelefono(String telefono)   { this.telefono  = telefono; }

    /** @param direccion nueva dirección de domicilio */
    public void setDireccion(String direccion) { this.direccion = direccion; }

    /**
     * Asigna el cuadrante de entrega del cliente.
     *
     * @param cuadrante nombre del cuadrante registrado en {@code CuadranteService},
     *                  o {@code null} para limpiar la asignación
     */
    public void setCuadrante(String cuadrante) { this.cuadrante = cuadrante; }

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
