package edu.fsadriann.server.model.cuadrante;

import edu.fsadriann.app.linkedlist.singly.singly.LinkedList;

public interface CuadranteInterface {
    boolean agregarCuadrante(Cuadrante cuadrante);
    boolean editarCuadrante(Cuadrante cuadrante);
    Cuadrante buscarCuadrante(String nombre);
    LinkedList<Cuadrante> listarCuadrantes();
    boolean conectarCuadrantes(String nombreA, String nombreB, double distanciaKm);
    LinkedList<String> calcularRutaMasCorta(String origen, String destino);
    double calcularDistancia(String origen, String destino);
    LinkedList<String> calcularRutaPorSaltos(String origen, String destino);
    boolean existeRuta(String origen, String destino);
    LinkedList<String> vecinosDirectos(String nombre);
    int numeroCuadrantes();
    int numeroConexiones();
    String verMatrizAdyacencia();
}
