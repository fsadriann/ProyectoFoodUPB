package edu.fsadriann.server.model.cuadrante;

import java.io.Serializable;

/**
 * Representa una zona de entrega en el mapa del sistema.
 * Cada cuadrante es un nodo del grafo de rutas gestionado por {@code CuadranteService}.
 */
public class Cuadrante implements Serializable {

    private static final long serialVersionUID = 1L;

    private String  nombre;
    private String  descripcion;
    private double  distanciaDesdeUPB;
    private boolean disponible;

    /**
     * Crea un cuadrante con nombre y descripción, marcado como disponible por defecto.
     *
     * @param nombre      nombre único que identifica la zona
     * @param descripcion descripción breve de la zona
     */
    public Cuadrante(String nombre, String descripcion) {
        if (nombre == null || nombre.isBlank())
            throw new IllegalArgumentException("El nombre no puede ser nulo ni vacío.");
        this.nombre      = nombre.trim();
        this.descripcion = descripcion;
        this.disponible  = true;
    }

    /**
     * Crea un cuadrante incluyendo la distancia desde UPB.
     *
     * @param nombre             nombre único de la zona
     * @param descripcion        descripción de la zona
     * @param distanciaDesdeUPB  distancia en kilómetros desde UPB hasta esta zona
     */
    public Cuadrante(String nombre, String descripcion, double distanciaDesdeUPB) {
        this(nombre, descripcion);
        this.distanciaDesdeUPB = distanciaDesdeUPB;
    }

    /** @return nombre del cuadrante */
    public String  getNombre()            { return nombre; }

    /** @return descripción de la zona */
    public String  getDescripcion()       { return descripcion; }

    /** @return distancia en km desde UPB */
    public double  getDistanciaDesdeUPB() { return distanciaDesdeUPB; }

    /** @return {@code true} si el cuadrante está activo para entregas */
    public boolean isDisponible()         { return disponible; }

    /** @param descripcion nueva descripción de la zona */
    public void setDescripcion(String descripcion)        { this.descripcion       = descripcion; }

    /** @param distancia distancia en km desde UPB */
    public void setDistanciaDesdeUPB(double distancia)    { this.distanciaDesdeUPB = distancia; }

    /** @param disponible {@code true} para activar, {@code false} para desactivar */
    public void setDisponible(boolean disponible)         { this.disponible         = disponible; }

    @Override
    public String toString() {
        return "[" + nombre + "] " + descripcion
                + " (" + distanciaDesdeUPB + " km desde UPB)"
                + (disponible ? "" : " — no disponible");
    }
}
