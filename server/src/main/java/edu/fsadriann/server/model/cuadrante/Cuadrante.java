package edu.fsadriann.server.model.cuadrante;

import java.io.Serializable;

public class Cuadrante implements Serializable {

    private static final long serialVersionUID = 1L;

    private String  nombre;
    private String  descripcion;
    private double  distanciaDesdeUPB;   // km desde Food UPB → peso de la arista
    private boolean disponible;

    public Cuadrante(String nombre, String descripcion) {
        if (nombre == null || nombre.isBlank())
            throw new IllegalArgumentException("El nombre no puede ser nulo ni vacío.");
        this.nombre    = nombre.trim();
        this.descripcion = descripcion;
        this.disponible  = true;
    }

    public Cuadrante(String nombre, String descripcion, double distanciaDesdeUPB) {
        this(nombre, descripcion);
        this.distanciaDesdeUPB = distanciaDesdeUPB;
    }

    // ── Getters ───────────────────────────────────────────────────────────────

    public String  getNombre()             { return nombre; }
    public String  getDescripcion()        { return descripcion; }
    public double  getDistanciaDesdeUPB()  { return distanciaDesdeUPB; }
    public boolean isDisponible()          { return disponible; }

    // ── Setters ───────────────────────────────────────────────────────────────

    public void setDescripcion(String descripcion)           { this.descripcion       = descripcion; }
    public void setDistanciaDesdeUPB(double distancia)       { this.distanciaDesdeUPB = distancia; }
    public void setDisponible(boolean disponible)            { this.disponible         = disponible; }

    @Override
    public String toString() {
        return "[" + nombre + "] " + descripcion
                + " (" + distanciaDesdeUPB + " km desde UPB)"
                + (disponible ? "" : " — no disponible");
    }
}