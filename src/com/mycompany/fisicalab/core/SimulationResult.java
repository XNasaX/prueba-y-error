package com.mycompany.fisicalab.core;

/**
 * Clase que encapsula los resultados de una simulación.
 * Esto permite pasar múltiples valores (tiempo, posición, velocidad, etc.)
 * a los métodos de evaluación de misiones.
 */
public class SimulationResult {
    private double finalTime;
    private double finalPosition;
    private double averageVelocity;
    // Puedes añadir más campos según sea necesario para otras simulaciones
    // Por ejemplo: finalHeight, maxVelocity, etc.

    public SimulationResult(double finalTime, double finalPosition, double averageVelocity) {
        this.finalTime = finalTime;
        this.finalPosition = finalPosition;
        this.averageVelocity = averageVelocity;
    }

    public double getFinalTime() {
        return finalTime;
    }

    public double getFinalPosition() {
        return finalPosition;
    }

    public double getAverageVelocity() {
        return averageVelocity;
    }
}
