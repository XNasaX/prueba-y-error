package com.mycompany.fisicalab.juego;

import com.mycompany.fisicalab.core.SimulationResult; // Importar SimulationResult
import java.util.ArrayList;
import java.util.List;

/**
 * Misiones específicas para Tiro Parabólico
 * Versión 3.0
 */
public class MisionTiroParabolico extends Mision {
    
    private TipoMisionTiro tipoMision;
    
    public enum TipoMisionTiro {
        LANZAMIENTO_SIMPLE,    // Lanzamiento básico
        DIANA_FIJA,           // Alcanzar distancia exacta
        ANGULO_OPTIMO,        // Encontrar ángulo óptimo
        DIANAS_MULTIPLES,     // Varias dianas consecutivas
        TIRO_LIMITADO         // Con restricciones
    }
    
    public MisionTiroParabolico(String id, String nombre, String descripcion, int dificultad,
                                TipoMisionTiro tipo, double objetivo, double tolerancia) {
        super(id, nombre, descripcion, "TIRO_PARABOLICO", dificultad);
        this.tipoMision = tipo;
        this.valorObjetivo = objetivo;
        this.tolerancia = tolerancia;
        configurarMision();
    }
    
    private void configurarMision() {
        switch (tipoMision) {
            case LANZAMIENTO_SIMPLE:
                this.objetivo = "Alcanzar más de " + valorObjetivo + " metros";
                this.restricciones = new String[]{
                    "• Usa cualquier ángulo",
                    "• Ajusta la velocidad"
                };
                break;
                
            case DIANA_FIJA:
                this.objetivo = "Impactar en X = " + valorObjetivo + " metros";
                this.restricciones = new String[]{
                    "• Margen: ±" + tolerancia + "m",
                    "• Ajusta ángulo y velocidad"
                };
                break;
                
            case ANGULO_OPTIMO:
                this.objetivo = "Encontrar el ángulo de máximo alcance";
                this.restricciones = new String[]{
                    "• Pista: cerca de 45°",
                    "• Velocidad fija: 20 m/s"
                };
                break;
                
            case DIANAS_MULTIPLES:
                this.objetivo = "Impactar " + (int)valorObjetivo + " dianas seguidas";
                this.restricciones = new String[]{
                    "• Sin fallar ninguna",
                    "• Diferentes distancias"
                };
                break;
                
            case TIRO_LIMITADO:
                this.objetivo = "Alcanzar " + valorObjetivo + "m con restricciones";
                this.restricciones = new String[]{
                    "• Velocidad máxima limitada",
                    "• Requiere estrategia"
                };
                break;
        }
    }
    
    @Override
    public boolean evaluar(SimulationResult result) {
        double valorEvaluado;
        double diferencia;

        switch (tipoMision) {
            case LANZAMIENTO_SIMPLE:
                valorEvaluado = result.getFinalPosition(); // Asumiendo que la posición final es el alcance
                return valorEvaluado >= valorObjetivo;
                
            case DIANA_FIJA:
            case TIRO_LIMITADO:
                valorEvaluado = result.getFinalPosition(); // Posición X final
                diferencia = Math.abs(valorEvaluado - valorObjetivo);
                return diferencia <= tolerancia;
                
            case ANGULO_OPTIMO:
                // Esta misión es más compleja, ya que el "resultado" no es un valor directo de SimulationResult.
                // Debería evaluar si el ángulo usado en la simulación está dentro de la tolerancia del ángulo óptimo.
                // Por ahora, asumiremos que el valorObjetivo es el ángulo óptimo y que la simulación lo configuró.
                // Esto es un placeholder y necesitaría una implementación más robusta.
                return true; // Placeholder, necesita lógica real
                
            case DIANAS_MULTIPLES:
                // Esta misión requiere un manejo de estado más complejo (cuántas dianas se han acertado).
                // SimulationResult solo da el resultado de un tiro.
                return false; // Placeholder, necesita lógica real
                
            default:
                return false;
        }
    }

    @Override
    public void registrarResultado(SimulationResult result) {
        boolean exito = evaluar(result);
        
        if (exito) {
            double valorParaPrecision;
            switch (tipoMision) {
                case LANZAMIENTO_SIMPLE:
                case DIANA_FIJA:
                case TIRO_LIMITADO:
                    valorParaPrecision = result.getFinalPosition();
                    break;
                case ANGULO_OPTIMO:
                    // Necesitaríamos el ángulo usado en la simulación para calcular la precisión
                    valorParaPrecision = valorObjetivo; // Placeholder
                    break;
                case DIANAS_MULTIPLES:
                    valorParaPrecision = result.getFinalPosition(); // Placeholder
                    break;
                default:
                    valorParaPrecision = 0; // Fallback
            }

            double precision = calcularPrecision(valorParaPrecision);
            int estrellas = calcularEstrellas(precision);
            int puntos = calcularPuntos(estrellas);
            
            if (!completada) {
                completada = true;
                estrellasObtenidas = estrellas;
                mejorPuntuacion = puntos;
            } else {
                // Actualizar si es mejor que antes
                if (estrellas > estrellasObtenidas) {
                    estrellasObtenidas = estrellas;
                }
                if (puntos > mejorPuntuacion) {
                    mejorPuntuacion = puntos;
                }
            }
        }
    }
    
    /**
     * Crea las 5 misiones de Tiro Parabólico para v3.0
     */
    public static List<MisionTiroParabolico> crearMisionesIniciales() {
        List<MisionTiroParabolico> misiones = new ArrayList<>();
        
        // Misión 1: Primer Lanzamiento (Fácil)
        MisionTiroParabolico m1 = new MisionTiroParabolico(
            "TIRO_01",
            "Primer Lanzamiento",
            "Lanza el proyectil y alcanza al menos 10 metros. ¡Tu primer tiro!",
            1,
            TipoMisionTiro.LANZAMIENTO_SIMPLE,
            10.0,  // Mínimo 10m
            0.0
        );
        m1.setDesbloqueada(true);
        misiones.add(m1);
        
        // Misión 2: Diana Fija (Normal)
        MisionTiroParabolico m2 = new MisionTiroParabolico(
            "TIRO_02",
            "Diana Fija",
            "Impacta exactamente en 25 metros. ¡Ajusta bien el ángulo y velocidad!",
            2,
            TipoMisionTiro.DIANA_FIJA,
            25.0,  // Objetivo: 25m
            2.0    // Tolerancia: ±2m
        );
        misiones.add(m2);
        
        // Misión 3: Ángulo Óptimo (Medio)
        MisionTiroParabolico m3 = new MisionTiroParabolico(
            "TIRO_03",
            "Ángulo Óptimo",
            "Descubre el ángulo que da máximo alcance con v=20 m/s. (Pista: ~45°)",
            3,
            TipoMisionTiro.ANGULO_OPTIMO,
            45.0,  // Ángulo óptimo
            3.0    // Tolerancia: ±3°
        );
        misiones.add(m3);
        
        // Misión 4: Francotirador (Difícil)
        MisionTiroParabolico m4 = new MisionTiroParabolico(
            "TIRO_04",
            "Francotirador",
            "Impacta 3 dianas consecutivas sin fallar: 15m, 30m y 45m.",
            4,
            TipoMisionTiro.DIANAS_MULTIPLES,
            3.0,   // 3 dianas
            0.0
        );
        misiones.add(m4);
        
        // Misión 5: Tiro Imposible (Muy Difícil)
        MisionTiroParabolico m5 = new MisionTiroParabolico(
            "TIRO_05",
            "Tiro Imposible",
            "Alcanza 80 metros con velocidad máxima de 25 m/s. ¡Usa el ángulo perfecto!",
            5,
            TipoMisionTiro.TIRO_LIMITADO,
            80.0,  // Objetivo: 80m
            3.0    // Tolerancia: ±3m
        );
        misiones.add(m5);
        
        return misiones;
    }
    
    public TipoMisionTiro getTipoMision() {
        return tipoMision;
    }
}
