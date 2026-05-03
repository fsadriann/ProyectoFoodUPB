package edu.fsadriann.server.model.order;

/**
 * Estaciones de trabajo (fogones) disponibles en cocina.
 *
 * <ul>
 *   <li>{@link #FOGON_GRANDE} — único fogón para pedidos complejos.</li>
 *   <li>{@link #FOGON_NORMAL_1}, {@link #FOGON_NORMAL_2}, {@link #FOGON_NORMAL_3}
 *       — fogones normales para pedidos simples. Se asignan al primero libre.</li>
 * </ul>
 *
 * @author fsadriann
 */
public enum FogonCocina {

    FOGON_GRANDE,
    FOGON_NORMAL_1,
    FOGON_NORMAL_2,
    FOGON_NORMAL_3
}
