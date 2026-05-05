package edu.fsadriann.server.model.cuadrante;

import java.io.Serializable;

public class Cuadrante implements Serializable {

    private static final long serialVersionUID = 1L;

    private String nombre;
    private String descripcion;
    private double latitud;
    private double longitud;
    private boolean disponible;

    public Cuadrante(String nombre, String descripcion) {
        if (nombre == null || nombre.isBlank())
            throw new IllegalArgumentException("El nombre del cuadrante no puede ser nulo ni vacío.");
        this.nombre      = nombre.trim();
        this.descripcion = descripcion;
        this.disponible  = true;
    }

    public Cuadrante(String nombre, String descripcion, double latitud, double longitud) {
        this(nombre, descripcion);
        this.latitud  = latitud;
        this.longitud = longitud;
    }

    // ── Getters ───────────────────────────────────────────────────────────────

    public String  getNombre()      { return nombre; }
    public String  getDescripcion() { return descripcion; }
    public double  getLatitud()     { return latitud; }
    public double  getLongitud()    { return longitud; }
    public boolean isDisponible()   { return disponible; }

    // ── Setters ───────────────────────────────────────────────────────────────

    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public void setLatitud(double latitud)         { this.latitud     = latitud; }
    public void setLongitud(double longitud)       { this.longitud    = longitud; }
    public void setDisponible(boolean disponible)  { this.disponible  = disponible; }

    @Override
    public String toString() {
        return "[" + nombre + "] " + descripcion
                + (disponible ? "" : " (no disponible)");
    }
}
