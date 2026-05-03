package edu.fsadriann.server.model.cuadrante;

public class Cuadrante {

    /** Identificador único. Es la clave del grafo — debe ser irrepetible. */
    private String nombre;

    /** Descripción del barrio o zona para mostrar al usuario. */
    private String descripcion;

    /**
     * Coordenada geográfica latitud.
     * Valor 0.0 si no se ha configurado; se usa en visualización Swing posterior.
     */
    private double latitud;

    /**
     * Coordenada geográfica longitud.
     * Valor 0.0 si no se ha configurado; se usa en visualización Swing posterior.
     */
    private double longitud;

    /**
     * Indica si el cuadrante está activo para recibir entregas.
     * {@code true} por defecto. Permite deshabilitar zonas sin eliminarlas del grafo.
     */
    private boolean disponible;

    // ── Constructores ─────────────────────────────────────────────────────────

    /**
     * Constructor mínimo. Coordenadas en 0.0, disponible = true.
     *
     * @param nombre      identificador único (no puede ser nulo ni vacío)
     * @param descripcion descripción de la zona
     */
    public Cuadrante(String nombre, String descripcion) {
        if (nombre == null || nombre.isBlank())
            throw new IllegalArgumentException("El nombre del cuadrante no puede ser nulo ni vacío.");
        this.nombre      = nombre.trim();
        this.descripcion = descripcion;
        this.disponible  = true;
    }

    /**
     * Constructor completo con coordenadas para visualización futura.
     *
     * @param nombre      identificador único
     * @param descripcion descripción de la zona
     * @param latitud     coordenada latitud
     * @param longitud    coordenada longitud
     */
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
