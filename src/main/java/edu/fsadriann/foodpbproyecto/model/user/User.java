package edu.fsadriann.foodpbproyecto.model.user;

import edu.fsadriann.app.linkedlist.singly.singly.LinkedList;
import edu.fsadriann.foodpbproyecto.model.product.Product;

/**
 * Modelo del cliente en el sistema Food UPB.
 *
 * <p>Almacena datos de contacto y preferencias del cliente.
 * La lógica de negocio reside en {@link UserService}.
 *
 * <p>Nota de deuda técnica: {@code telefono} es {@code int}.
 * Migrar a {@code String} en fases futuras para soportar prefijos internacionales.
 *
 * @author fsadriann
 */
public class User {

    /** Identificador único del cliente (asignado externamente, ej. UUID). */
    private final String id;

    /** Nombres del cliente. */
    private String nombres;

    /** Apellidos del cliente. */
    private String apellidos;

    /** Rol del cliente en el sistema. */
    private Rol rol;

    /** Cédula de ciudadanía — clave de búsqueda primaria. */
    private String cedula;

    /** {@code true} si el cliente tiene membresía premium (domicilio gratis). */
    private boolean isPremium;

    /**
     * Número de teléfono. Deuda técnica: limitado a 2.147.483.647.
     * TODO: migrar a {@code String}.
     */
    private int telefono;

    /** Dirección de entrega principal. */
    private String direccion;

    /** Lista de productos favoritos del cliente. */
    private LinkedList<Product> favProductos;

    // ── Constructores ─────────────────────────────────────────────────────────

    /**
     * Crea un cliente con todos sus datos.
     *
     * @param id           identificador único
     * @param nombres      nombre(s) del cliente
     * @param apellidos    apellido(s) del cliente
     * @param rol          rol en el sistema
     * @param cedula       cédula de ciudadanía (clave única)
     * @param isPremium    {@code true} si tiene membresía premium
     * @param telefono     número de teléfono de contacto
     * @param direccion    dirección principal de entrega
     * @param favProductos lista de favoritos (puede ser {@code null} — se inicializa vacía)
     */
    public User(String id, String nombres, String apellidos, Rol rol,
                String cedula, boolean isPremium, int telefono,
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

    // ── Getters ───────────────────────────────────────────────────────────────

    /** @return identificador único del cliente */
    public String getId()          { return id; }

    /** @return nombre(s) del cliente */
    public String getNombres()     { return nombres; }

    /** @return apellido(s) del cliente */
    public String getApellidos()   { return apellidos; }

    /** @return nombre completo (nombres + apellidos) */
    public String getNombreCompleto() {
        return (nombres != null ? nombres : "") + " " + (apellidos != null ? apellidos : "");
    }

    /** @return rol del cliente en el sistema */
    public Rol getRol()            { return rol; }

    /** @return cédula de ciudadanía */
    public String getCedula()      { return cedula; }

    /** @return {@code true} si el cliente es premium */
    public boolean isPremium()     { return isPremium; }

    /** @return número de teléfono de contacto */
    public int getTelefono()       { return telefono; }

    /** @return dirección principal de entrega */
    public String getDireccion()   { return direccion; }

    /** @return lista de productos favoritos */
    public LinkedList<Product> getFavProductos() { return favProductos; }

    // ── Setters ───────────────────────────────────────────────────────────────

    /** @param nombres nuevos nombres del cliente */
    public void setNombres(String nombres)         { this.nombres = nombres; }

    /** @param apellidos nuevos apellidos del cliente */
    public void setApellidos(String apellidos)     { this.apellidos = apellidos; }

    /** @param premium {@code true} para activar membresía premium */
    public void setPremium(boolean premium)        { this.isPremium = premium; }

    /** @param telefono nuevo número de teléfono */
    public void setTelefono(int telefono)          { this.telefono = telefono; }

    /** @param direccion nueva dirección de entrega */
    public void setDireccion(String direccion)     { this.direccion = direccion; }

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

