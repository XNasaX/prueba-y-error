package com.mycompany.fisicalab.juego;

import com.mycompany.fisicalab.core.SimulationResult; // Importar la nueva clase
import java.io.Serializable;

/**
 * Clase base abstracta para todas las misiones del juego
 * Versión 3.0
 */
public abstract class Mision implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    // Identificación
    protected String id;
    protected String nombre;
    protected String descripcion;
    protected String tipoSimulacion; // "MRU", "CAIDA_LIBRE", "TIRO_PARABOLICO"
    
    // Dificultad y puntuación
    protected int dificultad; // 1-5 estrellas máximas posibles
    protected int puntosBase; // Puntos al completar con 1 estrella
    
    // Estado
    protected boolean desbloqueada;
    protected boolean completada;
    protected int estrellasObtenidas; // 0-3
    protected int mejorPuntuacion;
    
    // Restricciones y objetivos
    protected String objetivo; // Descripción del objetivo
    protected String[] restricciones; // Lista de restricciones
    protected double valorObjetivo; // Valor a alcanzar
    protected double tolerancia; // Margen de error permitido
    
    // Constructor
    public Mision(String id, String nombre, String descripcion, String tipo, int dificultad) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.tipoSimulacion = tipo;
        this.dificultad = dificultad;
        this.puntosBase = dificultad * 100;
        this.desbloqueada = false;
        this.completada = false;
        this.estrellasObtenidas = 0;
        this.mejorPuntuacion = 0;
    }
    
    /**
     * Evalúa si la misión fue completada
     * @param result El objeto SimulationResult con los resultados obtenidos de la simulación
     * @return true si se completó el objetivo básico
     */
    public abstract boolean evaluar(SimulationResult result);
    
    /**
     * Calcula cuántas estrellas se obtienen según la precisión
     * @param precision Precisión del resultado (0.0 a 1.0)
     * @return Número de estrellas obtenidas (1-3)
     */
    public int calcularEstrellas(double precision) {
        if (precision >= 0.95) {
            return 3; // Perfecto
        } else if (precision >= 0.80) {
            return 2; // Bien
        } else if (precision >= 0.70) {
            return 1; // Completado
        } else {
            return 0; // Fallido
        }
    }
    
    /**
     * Calcula los puntos obtenidos según las estrellas
     * @param estrellas Estrellas obtenidas (1-3)
     * @return Puntos totales
     */
    public int calcularPuntos(int estrellas) {
        switch (estrellas) {
            case 3:
                return (int)(puntosBase * 2.0) + 50; // Bonus perfecto
            case 2:
                return (int)(puntosBase * 1.5);
            case 1:
                return puntosBase;
            default:
                return 0;
        }
    }
    
    /**
     * Calcula la precisión del resultado
     * @param valorObtenido El valor específico del resultado a comparar con el objetivo
     * @return Precisión (0.0 a 1.0)
     */
    protected double calcularPrecision(double valorObtenido) {
        double diferencia = Math.abs(valorObtenido - valorObjetivo);
        // Evitar división por cero si tolerancia y valorObjetivo son 0
        double divisor = (tolerancia > 0) ? tolerancia : ((valorObjetivo != 0) ? Math.abs(valorObjetivo) : 1.0);
        double precision = 1.0 - (diferencia / divisor);
        return Math.max(0.0, Math.min(1.0, precision));
    }
    
    /**
     * Registra el resultado de un intento
     */
    public void registrarResultado(SimulationResult result) {
        boolean exito = evaluar(result);
        
        if (exito) {
            // La precisión y los puntos se calcularán en las subclases
            // ya que dependen del tipo de misión y del valor objetivo.
            // Por ahora, solo marcamos como completada y asignamos estrellas/puntos básicos.
            // Esto se refinará en MisionMRU.
            if (!completada) {
                completada = true;
                // Las estrellas y puntuación se establecerán en la subclase
            }
        }
    }
    
    // Getters y Setters
    public String getId() { return id; }
    public String getNombre() { return nombre; }
    public String getDescripcion() { return descripcion; }
    public String getTipoSimulacion() { return tipoSimulacion; }
    public int getDificultad() { return dificultad; }
    public int getPuntosBase() { return puntosBase; }
    public boolean isDesbloqueada() { return desbloqueada; }
    public void setDesbloqueada(boolean desbloqueada) { this.desbloqueada = desbloqueada; }
    public boolean isCompletada() { return completada; }
    public int getEstrellasObtenidas() { return estrellasObtenidas; }
    public int getMejorPuntuacion() { return mejorPuntuacion; }
    public String getObjetivo() { return objetivo; }
    public String[] getRestricciones() { return restricciones; }
    public double getValorObjetivo() { return valorObjetivo; } // Nuevo getter
    
    /**
     * Retorna un String con las estrellas visuales
     */
    public String getEstrellasString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 3; i++) {
            if (i < estrellasObtenidas) {
                sb.append("⭐");
            } else {
                sb.append("☆");
            }
        }
        return sb.toString();
    }
    
    /**
     * Retorna el icono según dificultad
     */
    public String getIconoDificultad() {
        switch (dificultad) {
            case 1: return "😊 Fácil";
            case 2: return "🙂 Normal";
            case 3: return "😐 Medio";
            case 4: return "😰 Difícil";
            case 5: return "😱 Muy Difícil";
            default: return "❓";
        }
    }
    
    @Override
    public String toString() {
        return String.format("%s [%s] - %s %s", 
                           nombre, 
                           tipoSimulacion, 
                           completada ? "✓" : "✗",
                           getEstrellasString());
    }
}
