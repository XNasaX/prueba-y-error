package com.mycompany.fisicalab.ui;

import com.mycompany.fisicalab.core.Escenario;
import com.mycompany.fisicalab.core.MotorSimulacion;
import com.mycompany.fisicalab.core.SimuladorFrame;
import com.mycompany.fisicalab.core.SimulationResult;
import com.mycompany.fisicalab.juego.Mision;
import com.mycompany.fisicalab.utils.UIHelper;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Path2D;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.*;
import javax.swing.event.ChangeListener;

/**
 * Simulación de Movimiento Rectilíneo Uniforme (MRU) v4.0
 * MEJORAS: Sin bugs, modificación en tiempo real, interfaz profesional
 */
public class SimulacionMRU extends JPanel {
    
    private SimuladorFrame frame;
    private EscenarioMRU escenario;
    private MotorSimulacion motor;
    private DecimalFormat df = new DecimalFormat("#0.00");
    private Mision misionActiva; // La misión que se está ejecutando
    private Consumer<SimulationResult> onSimulationEnd; // Callback al finalizar la simulación
    
    // Componentes UI
    private JButton btnIniciar, btnPausar, btnReiniciar, btnVolver;
    private JSlider sliderVelocidad, sliderVelocidadSim, sliderDistancia;
    private JLabel labelVelocidad, labelVelocidadSim, labelDistancia;
    private JSpinner spinnerPosicionInicial, spinnerTiempoObjetivo;
    private JCheckBox chkMostrarVectores, chkModoInfinito, chkMostrarGrafica;
    private JPanel panelGrafica;
    
    // Parámetros
    private double velocidad = 5.0;
    private double posicionInicial = 0.0;
    private double distanciaObjetivo = 50.0;
    private double tiempoObjetivo = 0.0;
    private int velocidadSimulacion = 30;
    private boolean mostrarVectores = true;
    private boolean modoInfinito = false;
    private boolean mostrarGrafica = true;
    
    public SimulacionMRU(SimuladorFrame frame) {
        this(frame, null, null); // Constructor por defecto sin misión ni callback
    }

    public SimulacionMRU(SimuladorFrame frame, Mision mision, Consumer<SimulationResult> onSimulationEnd) {
        this.frame = frame;
        this.misionActiva = mision;
        this.onSimulationEnd = onSimulationEnd;
        setLayout(new BorderLayout(10, 10));
        setBackground(UIHelper.COLOR_FONDO);
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        motor = new MotorSimulacion(velocidadSimulacion);
        escenario = new EscenarioMRU(900, 500);
        
        inicializarComponentes();
        configurarTeclado();

        // Si hay una misión, configurar el escenario con sus parámetros
        if (misionActiva != null) {
            // Asumiendo que las misiones MRU usan valorObjetivo como distancia
            // y que la velocidad inicial es 0 por defecto en la simulación
            escenario.setDistanciaObjetivo(misionActiva.getValorObjetivo());
            sliderDistancia.setValue((int) misionActiva.getValorObjetivo()); // Actualizar UI
            labelDistancia.setText(df.format(misionActiva.getValorObjetivo()) + " m");
            
            // Si la misión es de tiempo exacto o contra reloj, configurar tiempo objetivo
            if (misionActiva instanceof com.mycompany.fisicalab.juego.MisionMRU) {
                com.mycompany.fisicalab.juego.MisionMRU mruMision = (com.mycompany.fisicalab.juego.MisionMRU) misionActiva;
                if (mruMision.getTipoMision() == com.mycompany.fisicalab.juego.MisionMRU.TipoMisionMRU.TIEMPO_EXACTO ||
                    mruMision.getTipoMision() == com.mycompany.fisicalab.juego.MisionMRU.TipoMisionMRU.CONTRA_RELOJ) {
                    escenario.setTiempoObjetivo(misionActiva.getValorObjetivo());
                    spinnerTiempoObjetivo.setValue(misionActiva.getValorObjetivo()); // Actualizar UI
                }
            }
            // Deshabilitar controles que la misión debe fijar
            spinnerPosicionInicial.setEnabled(false);
            sliderDistancia.setEnabled(false);
            spinnerTiempoObjetivo.setEnabled(false);
            chkModoInfinito.setEnabled(false);
        }
    }
    
    private void configurarTeclado() {
        InputMap inputMap = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = getActionMap();
        
        // ESPACIO - Iniciar/Pausar
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), "iniciarPausar");
        actionMap.put("iniciarPausar", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (motor != null && motor.isEnEjecucion()) {
                    pausarSimulacion();
                } else if (btnIniciar.isEnabled()) {
                    iniciarSimulacion();
                }
            }
        });
        
        // R - Reiniciar
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_R, 0), "reiniciar");
        actionMap.put("reiniciar", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                reiniciarSimulacion();
            }
        });
        
        // V - Toggle vectores
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_V, 0), "toggleVectores");
        actionMap.put("toggleVectores", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                chkMostrarVectores.setSelected(!chkMostrarVectores.isSelected());
            }
        });
        
        // G - Toggle gráfica
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_G, 0), "toggleGrafica");
        actionMap.put("toggleGrafica", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                chkMostrarGrafica.setSelected(!chkMostrarGrafica.isSelected());
            }
        });
    }
    
    private void inicializarComponentes() {
        // Panel superior con título
        JPanel panelSuperior = new JPanel(new BorderLayout());
        panelSuperior.setOpaque(false);
        panelSuperior.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        
        JLabel titulo = new JLabel("🏃 Movimiento Rectilíneo Uniforme");
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 26));
        titulo.setForeground(UIHelper.COLOR_PRIMARIO);
        
        JLabel version = new JLabel("v4.0");
        version.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        version.setForeground(new Color(127, 140, 141));
        
        panelSuperior.add(titulo, BorderLayout.WEST);
        panelSuperior.add(version, BorderLayout.EAST);
        
        // Panel de controles lateral
        JPanel panelControles = crearPanelControles();
        JScrollPane scrollControles = new JScrollPane(panelControles);
        scrollControles.setPreferredSize(new Dimension(320, 500));
        scrollControles.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(189, 195, 199), 2),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        scrollControles.getVerticalScrollBar().setUnitIncrement(16);
        
        // Panel central con escenario
        JPanel panelCentral = new JPanel(new BorderLayout());
        panelCentral.setOpaque(false);
        panelCentral.add(escenario, BorderLayout.CENTER);
        
        // Panel de gráfica (opcional)
        panelGrafica = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (mostrarGrafica) {
                    dibujarGrafica((Graphics2D) g);
                }
            }
        };
        panelGrafica.setPreferredSize(new Dimension(900, 150));
        panelGrafica.setBackground(Color.WHITE);
        panelGrafica.setBorder(BorderFactory.createTitledBorder("Gráfica Posición-Tiempo"));
        panelCentral.add(panelGrafica, BorderLayout.SOUTH);
        
        add(panelSuperior, BorderLayout.NORTH);
        add(scrollControles, BorderLayout.WEST);
        add(panelCentral, BorderLayout.CENTER);
    }
    
    private JPanel crearPanelControles() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        // SECCIÓN: Parámetros principales
        panel.add(crearSeccion("⚙️ PARÁMETROS PRINCIPALES"));
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        // Velocidad (modificable en tiempo real)
        panel.add(crearEtiqueta("Velocidad (m/s):"));
        sliderVelocidad = crearSliderConEtiqueta(1, 30, 5, e -> {
            velocidad = sliderVelocidad.getValue();
            labelVelocidad.setText(df.format(velocidad) + " m/s");
            if (motor != null && motor.isEnEjecucion()) {
                escenario.setVelocidad(velocidad);
            }
        });
        labelVelocidad = new JLabel(df.format(velocidad) + " m/s");
        labelVelocidad.setFont(new Font("Monospaced", Font.BOLD, 13));
        labelVelocidad.setForeground(UIHelper.COLOR_PRIMARIO);
        panel.add(sliderVelocidad);
        panel.add(labelVelocidad);
        panel.add(Box.createRigidArea(new Dimension(0, 12)));
        
        // Posición inicial
        panel.add(crearEtiqueta("Posición Inicial (m):"));
        spinnerPosicionInicial = new JSpinner(new SpinnerNumberModel(0.0, -100.0, 100.0, 1.0));
        spinnerPosicionInicial.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        spinnerPosicionInicial.setFont(new Font("Monospaced", Font.PLAIN, 13));
        spinnerPosicionInicial.addChangeListener(e -> {
            posicionInicial = (Double) spinnerPosicionInicial.getValue();
            if (!motor.isEnEjecucion()) {
                escenario.setPosicionInicial(posicionInicial);
            }
        });
        panel.add(spinnerPosicionInicial);
        panel.add(Box.createRigidArea(new Dimension(0, 12)));
        
        // Distancia objetivo
        panel.add(crearEtiqueta("Distancia Objetivo (m):"));
        sliderDistancia = crearSliderConEtiqueta(10, 200, 50, e -> {
            distanciaObjetivo = sliderDistancia.getValue();
            labelDistancia.setText(df.format(distanciaObjetivo) + " m");
            escenario.setDistanciaObjetivo(distanciaObjetivo);
        });
        labelDistancia = new JLabel(df.format(distanciaObjetivo) + " m");
        labelDistancia.setFont(new Font("Monospaced", Font.BOLD, 13));
        labelDistancia.setForeground(UIHelper.COLOR_EXITO);
        panel.add(sliderDistancia);
        panel.add(labelDistancia);
        panel.add(Box.createRigidArea(new Dimension(0, 12)));
        
        // Tiempo objetivo
        panel.add(crearEtiqueta("Tiempo Objetivo (s, 0=sin límite):"));
        spinnerTiempoObjetivo = new JSpinner(new SpinnerNumberModel(0.0, 0.0, 999.0, 1.0));
        spinnerTiempoObjetivo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        spinnerTiempoObjetivo.setFont(new Font("Monospaced", Font.PLAIN, 13));
        spinnerTiempoObjetivo.addChangeListener(e -> {
            tiempoObjetivo = (Double) spinnerTiempoObjetivo.getValue();
            escenario.setTiempoObjetivo(tiempoObjetivo);
        });
        panel.add(spinnerTiempoObjetivo);
        panel.add(Box.createRigidArea(new Dimension(0, 15)));
        
        // SECCIÓN: Simulación
        panel.add(crearSeccion("🎮 CONTROL DE SIMULACIÓN"));
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        // Velocidad de simulación
        panel.add(crearEtiqueta("Velocidad de Simulación:"));
        sliderVelocidadSim = crearSliderConEtiqueta(10, 100, 30, e -> {
            velocidadSimulacion = sliderVelocidadSim.getValue();
            String vel = velocidadSimulacion < 30 ? "⚡ Rápida" : 
                        velocidadSimulacion > 50 ? "🐌 Lenta" : "⚙️ Normal";
            labelVelocidadSim.setText(vel);
            if (motor != null) {
                motor.setIntervalMs(velocidadSimulacion);
            }
        });
        labelVelocidadSim = new JLabel("⚙️ Normal");
        labelVelocidadSim.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        panel.add(sliderVelocidadSim);
        panel.add(labelVelocidadSim);
        panel.add(Box.createRigidArea(new Dimension(0, 15)));
        
        // SECCIÓN: Opciones visuales
        panel.add(crearSeccion("👁️ OPCIONES VISUALES"));
        panel.add(Box.createRigidArea(new Dimension(0, 8)));
        
        chkMostrarVectores = new JCheckBox("Mostrar vectores (V)", true);
        estilizarCheckBox(chkMostrarVectores);
        chkMostrarVectores.addActionListener(e -> {
            mostrarVectores = chkMostrarVectores.isSelected();
            escenario.setMostrarVectores(mostrarVectores);
        });
        panel.add(chkMostrarVectores);
        
        chkMostrarGrafica = new JCheckBox("Mostrar gráfica (G)", true);
        estilizarCheckBox(chkMostrarGrafica);
        chkMostrarGrafica.addActionListener(e -> {
            mostrarGrafica = chkMostrarGrafica.isSelected();
            panelGrafica.setVisible(mostrarGrafica);
            panelGrafica.repaint();
        });
        panel.add(chkMostrarGrafica);
        
        chkModoInfinito = new JCheckBox("Modo infinito (bucle)", false);
        estilizarCheckBox(chkModoInfinito);
        chkModoInfinito.addActionListener(e -> {
            modoInfinito = chkModoInfinito.isSelected();
            escenario.setModoInfinito(modoInfinito);
        });
        panel.add(chkModoInfinito);
        
        panel.add(Box.createRigidArea(new Dimension(0, 20)));
        
        // Botones de control
        btnIniciar = crearBoton("▶️ Iniciar (SPACE)", UIHelper.COLOR_EXITO);
        btnIniciar.addActionListener(e -> iniciarSimulacion());
        panel.add(btnIniciar);
        panel.add(Box.createRigidArea(new Dimension(0, 8)));
        
        btnPausar = crearBoton("⏸️ Pausar", UIHelper.COLOR_ADVERTENCIA);
        btnPausar.setEnabled(false);
        btnPausar.addActionListener(e -> pausarSimulacion());
        panel.add(btnPausar);
        panel.add(Box.createRigidArea(new Dimension(0, 8)));
        
        btnReiniciar = crearBoton("🔄 Reiniciar (R)", UIHelper.COLOR_SECUNDARIO);
        btnReiniciar.addActionListener(e -> reiniciarSimulacion());
        panel.add(btnReiniciar);
        panel.add(Box.createRigidArea(new Dimension(0, 15)));
        
        // Info de controles
        JTextArea infoControles = new JTextArea(
            "⌨️ CONTROLES RÁPIDOS:\n\n" +
            "ESPACIO → Iniciar/Pausar\n" +
            "R → Reiniciar\n" +
            "V → Toggle vectores\n" +
            "G → Toggle gráfica\n\n" +
            "📐 ECUACIÓN MRU:\n" +
            "x = x₀ + v·t\n" +
            "v = constante\n" +
            "a = 0 m/s²"
        );
        infoControles.setEditable(false);
        infoControles.setFont(new Font("Consolas", Font.PLAIN, 11));
        infoControles.setBackground(new Color(236, 240, 241));
        infoControles.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(189, 195, 199)),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        infoControles.setLineWrap(true);
        infoControles.setWrapStyleWord(true);
        panel.add(infoControles);
        
        panel.add(Box.createVerticalGlue());
        panel.add(Box.createRigidArea(new Dimension(0, 15)));
        
        btnVolver = crearBoton("🏠 Volver al Menú", UIHelper.COLOR_PELIGRO);
        btnVolver.addActionListener(e -> volverAlMenu());
        panel.add(btnVolver);
        
        return panel;
    }
    
    private void iniciarSimulacion() {
        escenario.setVelocidad(velocidad);
        escenario.setPosicionInicial(posicionInicial);
        escenario.setDistanciaObjetivo(distanciaObjetivo);
        escenario.setTiempoObjetivo(tiempoObjetivo);
        escenario.reiniciar();
        
        motor = new MotorSimulacion(velocidadSimulacion);
        motor.iniciar(e -> {
            escenario.actualizar();
            escenario.repaint();
            if (mostrarGrafica) {
                panelGrafica.repaint();
            }
        });
        
        btnIniciar.setEnabled(false);
        btnPausar.setEnabled(true);
        spinnerPosicionInicial.setEnabled(false);
        sliderDistancia.setEnabled(false);
        spinnerTiempoObjetivo.setEnabled(false);
        chkModoInfinito.setEnabled(false);
    }
    
    private void pausarSimulacion() {
        if (motor.isEnEjecucion()) {
            motor.pausar();
            btnPausar.setText("▶️ Reanudar");
        } else {
            motor.reanudar();
            btnPausar.setText("⏸️ Pausar");
        }
    }
    
    private void reiniciarSimulacion() {
        if (motor != null) {
            motor.detener();
            motor.reiniciar();
        }
        escenario.reiniciar();
        escenario.repaint();
        panelGrafica.repaint();
        
        btnIniciar.setEnabled(true);
        btnPausar.setEnabled(false);
        btnPausar.setText("⏸️ Pausar");
        spinnerPosicionInicial.setEnabled(true);
        sliderDistancia.setEnabled(true);
        spinnerTiempoObjetivo.setEnabled(true);
        chkModoInfinito.setEnabled(true);
    }
    
    private void volverAlMenu() {
        if (motor != null) {
            motor.detener();
        }
        frame.mostrarMenuPrincipal();
    }
    
    private void dibujarGrafica(Graphics2D g2d) {
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        int w = panelGrafica.getWidth();
        int h = panelGrafica.getHeight();
        
        // Ejes
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(2));
        int margen = 40;
        g2d.drawLine(margen, h - margen, w - margen, h - margen); // Eje X
        g2d.drawLine(margen, margen, margen, h - margen); // Eje Y
        
        // Etiquetas
        g2d.setFont(new Font("Arial", Font.BOLD, 11));
        g2d.drawString("t (s)", w - margen - 30, h - margen + 25);
        g2d.drawString("x (m)", margen - 30, margen);
        
        // Dibujar línea de la gráfica (MRU es una recta)
        List<Point> datosCrudos = escenario.obtenerPuntosGrafica();
        if (datosCrudos.size() > 1) {
            g2d.setColor(UIHelper.COLOR_PRIMARIO);
            g2d.setStroke(new BasicStroke(3));

            double maxTiempo = escenario.getMaxTiempoGrafica();
            double maxPosicion = escenario.getMaxPosicionGrafica();
            double minPosicion = escenario.getMinPosicionGrafica();

            // Asegurar que haya un rango para evitar división por cero
            if (maxTiempo == 0) maxTiempo = 1.0;
            if (maxPosicion == minPosicion) {
                maxPosicion += 1.0;
                minPosicion -= 1.0;
            }
            
            // Escala para el eje X (tiempo)
            double escalaX = (w - 2 * margen) / maxTiempo;
            // Escala para el eje Y (posición)
            double escalaY = (h - 2 * margen) / (maxPosicion - minPosicion);

            Path2D path = new Path2D.Double();
            boolean firstPoint = true;

            for (Point p : datosCrudos) {
                double tiempo = p.getX() / 100.0; // Convertir de vuelta a valor real
                double posicion = p.getY() / 100.0; // Convertir de vuelta a valor real

                int screenX = margen + (int) (tiempo * escalaX);
                int screenY = h - margen - (int) ((posicion - minPosicion) * escalaY);

                if (firstPoint) {
                    path.moveTo(screenX, screenY);
                    firstPoint = false;
                } else {
                    path.lineTo(screenX, screenY);
                }
            }
            g2d.draw(path);
        }
    }
    
    // Métodos auxiliares UI
    private JLabel crearSeccion(String texto) {
        JLabel label = new JLabel(texto);
        label.setFont(new Font("Segoe UI", Font.BOLD, 14));
        label.setForeground(new Color(52, 73, 94));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }
    
    private JLabel crearEtiqueta(String texto) {
        JLabel label = new JLabel(texto);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        label.setForeground(new Color(44, 62, 80));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }
    
    private JSlider crearSliderConEtiqueta(int min, int max, int val, ChangeListener listener) {
        JSlider slider = new JSlider(min, max, val);
        slider.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        slider.setAlignmentX(Component.LEFT_ALIGNMENT);
        slider.addChangeListener(listener);
        return slider;
    }
    
    private JButton crearBoton(String texto, Color color) {
        JButton btn = UIHelper.crearBotonRedondeado(texto, color);
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        return btn;
    }
    
    private void estilizarCheckBox(JCheckBox chk) {
        chk.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        chk.setFocusPainted(false);
        chk.setAlignmentX(Component.LEFT_ALIGNMENT);
    }
    
    // Clase interna del escenario
    private class EscenarioMRU extends Escenario {
        private double posX, v, x0, distancia, tLimite, tiempo;
        private boolean objetivo, vectores, infinito;
        private List<Point> datosGrafica;
        private double maxTiempoGrafica = 0.0;
        private double maxPosicionGrafica = 0.0;
        private double minPosicionGrafica = 0.0;
        
        public EscenarioMRU(int w, int h) {
            super(w, h);
            this.motor = SimulacionMRU.this.motor;
            this.vectores = true;
            this.datosGrafica = new ArrayList<>();
            reiniciar();
        }
        
        public double getMaxTiempoGrafica() { return maxTiempoGrafica; }
        public double getMaxPosicionGrafica() { return maxPosicionGrafica; }
        public double getMinPosicionGrafica() { return minPosicionGrafica; }
        
        public void setVelocidad(double vel) { this.v = vel; }
        public void setPosicionInicial(double x) { this.x0 = x; }
        public void setDistanciaObjetivo(double d) { this.distancia = d; }
        public void setTiempoObjetivo(double t) { this.tLimite = t; }
        public void setMostrarVectores(boolean m) { this.vectores = m; }
        public void setModoInfinito(boolean m) { this.infinito = m; }
        
        public void reiniciar() {
            posX = x0;
            tiempo = 0;
            objetivo = false;
            datosGrafica.clear();
            maxTiempoGrafica = 0.0;
            maxPosicionGrafica = x0;
            minPosicionGrafica = x0;
        }
        
        public List<Point> obtenerPuntosGrafica() {
            return new ArrayList<>(datosGrafica);
        }
        
        @Override
        public void actualizar() {
            if (motor == null) return;
            
            tiempo = motor.getTiempoTranscurrido();
            posX = MotorSimulacion.calcularPosicionMRU(x0, v, tiempo);
            
            // Actualizar límites para escalado dinámico de la gráfica
            if (tiempo > maxTiempoGrafica) maxTiempoGrafica = tiempo;
            if (posX > maxPosicionGrafica) maxPosicionGrafica = posX;
            if (posX < minPosicionGrafica) minPosicionGrafica = posX;

            // Guardar datos para gráfica (mantener un número limitado de puntos para rendimiento)
            // Usamos una LinkedList si queremos un buffer circular, pero para una gráfica que crece, ArrayList está bien.
            // Si queremos limitar a los últimos N puntos, podríamos hacer:
            // if (datosGrafica.size() > MAX_GRAPH_POINTS) datosGrafica.remove(0);
            // Por ahora, solo agregamos. El límite de 200 puntos se manejará en la lógica de dibujo si es necesario.
            datosGrafica.add(new Point((int) (tiempo * 100), (int) (posX * 100))); // Guardar valores reales, no pixel
            
            // Verificar objetivo
            if (!infinito && (posX - x0) >= distancia) {
                objetivo = true;
                motor.detener();
            }
            
            if (tLimite > 0 && tiempo >= tLimite) {
                motor.detener();
                finalizarSimulacion();
            }
            
            // Modo infinito
            if (infinito && metrosAPixeles(posX - x0) > ancho - 100) {
                motor.reiniciar();
                reiniciar();
            }

            // Si el motor se detiene por cualquier razón (objetivo, tiempo, manual)
            if (!motor.isEnEjecucion() && (objetivo || (tLimite > 0 && tiempo >= tLimite))) {
                finalizarSimulacion();
            }
        }

        private void finalizarSimulacion() {
            if (onSimulationEnd != null && !motor.isEnEjecucion()) { // Asegurarse de que solo se llame una vez al detenerse
                double finalTime = motor.getTiempoTranscurrido();
                double finalPos = posX;
                double avgVel = (finalPos - x0) / (finalTime > 0 ? finalTime : 1);
                SimulationResult result = new SimulationResult(finalTime, finalPos, avgVel);
                onSimulationEnd.accept(result);
                onSimulationEnd = null; // Prevenir llamadas múltiples
            }
        }
        
        @Override
        protected void dibujar(Graphics2D g2d) {
            // Fondo degradado
            GradientPaint gp = new GradientPaint(0, 0, new Color(236, 240, 241),
                                                  0, alto, new Color(255, 255, 255));
            g2d.setPaint(gp);
            g2d.fillRect(0, 0, ancho, alto);
            
            dibujarCuadricula(g2d, 50);
            
            // Línea de piso mejorada
            int pisoY = alto - 100;
            g2d.setColor(new Color(52, 73, 94));
            g2d.setStroke(new BasicStroke(4));
            g2d.drawLine(0, pisoY, ancho, pisoY);
            
            // Marcadores de distancia
            g2d.setFont(new Font("Arial", Font.PLAIN, 10));
            for (int i = 0; i <= 200; i += 10) {
                int x = 50 + metrosAPixeles(i);
                if (x < ancho) {
                    g2d.setColor(new Color(149, 165, 166));
                    g2d.drawLine(x, pisoY - 5, x, pisoY + 5);
                    if (i % 20 == 0) {
                        g2d.drawString(i + "m", x - 10, pisoY + 20);
                    }
                }
            }
            
            // Línea de inicio
            int inicioX = 50 + metrosAPixeles(x0);
            g2d.setColor(new Color(46, 204, 113));
            g2d.setStroke(new BasicStroke(3, BasicStroke.CAP_BUTT, 
                         BasicStroke.JOIN_BEVEL, 0, new float[]{10, 5}, 0));
            g2d.drawLine(inicioX, pisoY - 80, inicioX, pisoY);
            g2d.setFont(new Font("Arial", Font.BOLD, 13));
            g2d.drawString("INICIO", inicioX - 25, pisoY - 85);
            
            // Línea de meta
            if (!infinito) {
                int metaX = inicioX + metrosAPixeles(distancia);
                if (metaX < ancho) {
                    g2d.setColor(objetivo ? new Color(46, 204, 113) : new Color(231, 76, 60));
                    g2d.drawLine(metaX, pisoY - 80, metaX, pisoY);
                    g2d.drawString("META", metaX - 20, pisoY - 85);
                }
            }
            
            // Objeto móvil con efecto 3D
            int objX = 50 + metrosAPixeles(posX);
            int objY = pisoY - 25;
            
            if (objX >= 0 && objX < ancho) {
                Color colorObj = objetivo ? new Color(46, 204, 113) : new Color(52, 152, 219);
                
                // Sombra
                g2d.setColor(new Color(0, 0, 0, 50));
                g2d.fillOval(objX - 17, objY - 12, 34, 34);
                
                // Objeto principal
                g2d.setColor(colorObj);
                g2d.fillOval(objX - 15, objY - 15, 30, 30);
                
                // Brillo
                g2d.setColor(new Color(255, 255, 255, 120));
                g2d.fillOval(objX - 10, objY - 10, 12, 12);
                
                // Borde
                g2d.setColor(colorObj.darker());
                g2d.setStroke(new BasicStroke(2));
                g2d.drawOval(objX - 15, objY - 15, 30, 30);
                
                // Vector de velocidad
                if (vectores && v > 0 && motor.isEnEjecucion()) {
                    g2d.setColor(new Color(39, 174, 96));
                    g2d.setStroke(new BasicStroke(3));
                    int longFlecha = (int)(v * 12);
                    g2d.drawLine(objX + 15, objY, objX + 15 + longFlecha, objY);
                    g2d.drawLine(objX + 15 + longFlecha, objY, 
                                objX + 15 + longFlecha - 8, objY - 5);
                    g2d.drawLine(objX + 15 + longFlecha, objY, 
                                objX + 15 + longFlecha - 8, objY + 5);
                    
                    g2d.setFont(new Font("Arial", Font.BOLD, 12));
                    g2d.drawString("v=" + df.format(v), objX + 15 + longFlecha + 5, objY - 10);
                }
                
                // Trayectoria
                g2d.setColor(new Color(41, 128, 185, 100));
                g2d.setStroke(new BasicStroke(2));
                g2d.drawLine(inicioX, pisoY - 25, objX, pisoY - 25);
            }
            
            // Panel de información (esquina superior)
            dibujarPanelInfo(g2d);
        }
        
        private void dibujarPanelInfo(Graphics2D g2d) {
            double distRecorrida = posX - x0;
            double tEst = distancia / (v > 0 ? v : 1);
            double vMedia = distRecorrida / (tiempo > 0 ? tiempo : 1);
            
            String estado = objetivo ? "✓ META ALCANZADA" : 
                           (tLimite > 0 && tiempo >= tLimite) ? "⏰ TIEMPO LÍMITE" :
                           infinito ? "∞ MODO INFINITO" : "🏃 EN MOVIMIENTO";
            
            String[] datos = {
                "⏱️ Tiempo: " + df.format(tiempo) + " s " + estado,
                "📍 Posición: " + df.format(posX) + " m",
                "📏 Distancia: " + df.format(distRecorrida) + " m",
                "➡️ Velocidad: " + df.format(v) + " m/s (constante)",
                "📊 Velocidad media: " + df.format(vMedia) + " m/s",
                "⚡ Aceleración: 0.00 m/s² (MRU)",
                "🎯 Objetivo: " + df.format(distancia) + " m",
                "⏰ T.Estimado: " + df.format(tEst) + " s"
            };
            
            int panelX = 20;
            int panelY = 20;
            int panelW = 280;
            int panelH = (datos.length * 20) + 20;
            
            // Fondo semi-transparente
            g2d.setColor(new Color(255, 255, 255, 240));
            g2d.fillRoundRect(panelX, panelY, panelW, panelH, 15, 15);
            
            // Borde
            g2d.setColor(UIHelper.COLOR_PRIMARIO);
            g2d.setStroke(new BasicStroke(2));
            g2d.drawRoundRect(panelX, panelY, panelW, panelH, 15, 15);
            
            // Texto
            g2d.setColor(Color.BLACK);
            g2d.setFont(new Font("Monospaced", Font.PLAIN, 11));
            FontMetrics fm = g2d.getFontMetrics();
            int y = panelY + 16;
            
            for (String linea : datos) {
                g2d.drawString(linea, panelX + 12, y);
                y += 20;
            }
            
            // Versión en esquina inferior derecha
            g2d.setColor(new Color(127, 140, 141));
            g2d.setFont(new Font("Arial", Font.ITALIC, 11));
            g2d.drawString("v4.0", ancho - 40, alto - 10);
        }
    }
}
