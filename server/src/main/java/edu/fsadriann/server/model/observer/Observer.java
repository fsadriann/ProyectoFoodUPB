package edu.fsadriann.server.model.observer;

/**
 * Clase base del patrón Observer.
 * Las subclases se suscriben a un {@link Subject} y reaccionan cuando éste notifica cambios.
 */
public abstract class Observer {

    /** Sujeto al que está suscrito este observador. */
    protected Subject subject;

    /**
     * Crea el observador y lo registra automáticamente en el sujeto indicado.
     *
     * @param subject sujeto al que se suscribe
     */
    protected Observer(Subject subject) {
        this.subject = subject;
        this.subject.attach(this);
    }

    /**
     * Método invocado por el sujeto cuando ocurre un cambio.
     * Las subclases deben implementar la lógica de reacción.
     */
    public abstract void update();
}
