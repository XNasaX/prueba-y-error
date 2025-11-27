package com.mycompany.fisicalab.juego;

import com.mycompany.fisicalab.core.MotorSimulacion; // Necesario para getGravedad()
import com.mycompany.fisicalab.core.SimulationResult; // Importar SimulationResult
import java.util.ArrayList;
import java.util.List;

/**
 * Misiones específicas para Caída Libre
 * Versión 3.0
 */
public class MisionCaidaLibre extends Mision {
    
    private TipoMisionCaida tipoMision;
    
    public enum TipoMisionCaida {
        CAIDA_SIMPLE,          // Caída básica
        GRAVEDAD_DIFERENTE,    // Cambiar gravedad
        PREDICCION_TIEMPO,     // Predecir tiempo
        ENERGIA_CONSERVADA,    // Verificar energía
        LANZAMIENTO_ARRIBA     // Velocidad inicial positiva
    }
    
    public MisionCaidaLibre(String id, String nombre, String descripcion, int dificultad,
                            TipoMisionCaida tipo, double objetivo, double tolerancia) {
        super(id, nombre, descripcion, "CAIDA_LIBRE", dificultad);
        this.tipoMision = tipo;
        this.valorObjetivo = objetivo;
        this.tolerancia = tolerancia;
        configurarMision();
    }
    
    private void configurarMision() {
        switch (tipoMision) {
            case CAIDA_SIMPLE:
                this.objetivo = "Soltar desde " + valorObjetivo + " metros";
                this.restricciones = new String[]{
                    "• Gravedad: 9.8 m/s²",
                    "• Observa el tiempo de caída"
                };
                break;
                
            case GRAVEDAD_DIFERENTE:
                this.objetivo = "Caer en gravedad " + valorObjetivo + " m/s²";
                this.restricciones = new String[]{
                    "• Ajusta la gravedad correctamente",
                    "• Observa las diferencias"
                };
                break;
                
            case PREDICCION_TIEMPO:
                this.objetivo = "Predecir tiempo: " + String.format("%.2f", valorObjetivo) + " segundos";
                this.restricciones = new String[]{
                    "• Calcula antes de soltar",
                    "• t = √(2h/g)"
                };
                break;
                
            case ENERGIA_CONSERVADA:
                this.objetivo = "Verificar conservación de energía";
                this.restricciones = new String[]{
                    "• Activa visualización de energía",
                    "• Ep + Ek = constante"
                };
                break;
                
            case LANZAMIENTO_ARRIBA:
                this.objetivo = "Alcanzar altura de " + valorObjetivo + " metros";
                this.restricciones = new String[]{
                    "• Velocidad inicial positiva",
                    "• Observa subida y bajada"
                };
                break;
        }
    }
    
    @Override
    public boolean evaluar(SimulationResult result) {
        double valorEvaluado;
        double diferencia;

        switch (tipoMision) {
            case CAIDA_SIMPLE:
            case LANZAMIENTO_ARRIBA:
                valorEvaluado = result.getFinalPosition(); // Asumiendo que el objetivo es la altura final o máxima
                diferencia = Math.abs(valorEvaluado - valorObjetivo);
                return diferencia <= tolerancia;
                
            case GRAVEDAD_DIFERENTE:
                // Esta misión es más compleja, ya que el "resultado" no es un valor directo de SimulationResult.
                // Podría requerir que la simulación pase la gravedad usada, o que la misión acceda a MotorSimulacion.getGravedad().
                // Para simplificar, si la simulación llega a este punto, asumimos que la gravedad fue configurada.
                // Esto es un placeholder y necesitaría una implementación más robusta si la gravedad es un resultado a evaluar.
                return Math.abs(MotorSimulacion.getGravedad() - valorObjetivo) <= tolerancia; // Evaluar la gravedad configurada
                
            case PREDICCION_TIEMPO:
                valorEvaluado = result.getFinalTime();
                diferencia = Math.abs(valorEvaluado - valorObjetivo);
                return diferencia <= tolerancia;
                
            case ENERGIA_CONSERVADA:
                // Esta misión es para verificar un concepto, no un valor numérico directo.
                // Podría requerir un resultado booleano de la simulación o una evaluación más compleja.
                return true; // Placeholder, necesita lógica real
                
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
                case CAIDA_SIMPLE:
                case LANZAMIENTO_ARRIBA:
                    valorParaPrecision = result.getFinalPosition();
                    break;
                case PREDICCION_TIEMPO:
                    valorParaPrecision = result.getFinalTime();
                    break;
                case GRAVEDAD_DIFERENTE:
                    valorParaPrecision = MotorSimulacion.getGravedad(); // Asumiendo que la gravedad es el valor a comparar
                    break;
                case ENERGIA_CONSERVADA:
                    valorParaPrecision = 1.0; // Si es true, la precisión es 1.0 (placeholder)
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
     * Crea las 5 misiones de Caída Libre para v3.0
     */
    public static List<MisionCaidaLibre> crearMisionesIniciales() {
        List<MisionCaidaLibre> misiones = new ArrayList<>();
        
        // Misión 1: Primera Caída (Fácil)
        MisionCaidaLibre m1 = new MisionCaidaLibre(
            "CAIDA_01",
            "Primera Caída",
            "Suelta un objeto desde 20 metros. ¡Tu primer experimento con gravedad!",
            1,
            TipoMisionCaida.CAIDA_SIMPLE,
            20.0,  // Altura
            5.0    // Tolerancia
        );
        m1.setDesbloqueada(true);
        misiones.add(m1);
        
        // Misión 2: Gravedad Lunar (Normal)
        MisionCaidaLibre m2 = new MisionCaidaLibre(
            "CAIDA_02",
            "Gravedad Lunar",
            "Experimenta la gravedad de la Luna (1.6 m/s²). ¿Cuánto tarda en caer 50m?",
            2,
            TipoMisionCaida.GRAVEDAD_DIFERENTE,
            1.6,   // Gravedad lunar
            0.2    // Tolerancia
        );
        misiones.add(m2);
        
        // Misión 3: Predicción Exacta (Medio)
        MisionCaidaLibre m3 = new MisionCaidaLibre(
            "CAIDA_03",
            "Predicción Exacta",
            "Calcula el tiempo de caída desde 100m ANTES de soltar. ¡Usa la fórmula!",
            3,
            TipoMisionCaida.PREDICCION_TIEMPO,
            4.52,  // Tiempo correcto: √(2*100/9.8)
            0.3    // Tolerancia: ±0.3s
        );
        misiones.add(m3);
        
        // Misión 4: Energía Perfecta (Difícil)
        MisionCaidaLibre m4 = new MisionCaidaLibre(
            "CAIDA_04",
            "Energía Perfecta",
            "Verifica que la energía se conserva. El error debe ser menor al 2%.",
            4,
            TipoMisionCaida.ENERGIA_CONSERVADA,
            2.0,   // Error máximo permitido (%)
            0.5
        );
        misiones.add(m4);
        
        // Misión 5: Lanzamiento Hacia Arriba (Muy Difícil)
        MisionCaidaLibre m5 = new MisionCaidaLibre(
            "CAIDA_05",
            "Lanzamiento Arriba",
            "Lanza hacia arriba y alcanza exactamente 30m de altura máxima. ¡Cálculo perfecto!",
            5,
            TipoMisionCaida.LANZAMIENTO_ARRIBA,
            30.0,  // Altura objetivo
            2.0    // Tolerancia: ±2m
        );
        misiones.add(m5);
        
        return misiones;
    }
    
    public TipoMisionCaida getTipoMision() {
        return tipoMision;
    }
}
