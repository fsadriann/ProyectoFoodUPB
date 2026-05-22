package edu.fsadriann.server.model.observer;

/**
 * Contrato del patrón Observer para el sujeto que emite notificaciones.
 * Las clases que implementen esta interfaz pueden registrar y notificar observadores.
 */
public interface Observable {

    /**
     * Registra un observador para recibir notificaciones.
     *
     * @param observer observador a registrar
     */
    void attach(Observer observer);

    /**
     * Elimina un observador previamente registrado.
     *
     * @param observer observador a eliminar
     */
    void detach(Observer observer);

    /**
     * Notifica a todos los observadores registrados sobre un cambio de estado.
     */
    void notifyObservers();
}
