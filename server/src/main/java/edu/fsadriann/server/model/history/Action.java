package edu.fsadriann.server.model.history;

/**
 * Representa una acción registrada en el historial del sistema.
 * Almacena la descripción de la acción y el momento en que ocurrió.
 */
public class Action {

    private String description;
    private long   timestamp;

    /**
     * Crea una acción con su descripción y la marca de tiempo actual.
     *
     * @param description descripción de la acción realizada
     */
    public Action(String description) {
        this.description = description;
        this.timestamp   = System.currentTimeMillis();
    }

    /** @return descripción de la acción */
    public String getDescription() {
        return description;
    }

    /** @return fecha y hora de la acción en formato {@code yyyy-MM-dd HH:mm:ss} */
    public String getTimestamp() {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(new java.util.Date(timestamp));
    }
}
