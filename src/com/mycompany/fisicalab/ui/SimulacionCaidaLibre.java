package com.mycompany.fisicalab.ui;

import com.mycompany.fisicalab.core.Escenario;
import com.mycompany.fisicalab.core.MotorSimulacion;
import com.mycompany.fisicalab.core.SimuladorFrame;
import com.mycompany.fisicalab.utils.UIHelper;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Path2D;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Simulación de Caída Libre v4.0
 * MEJORAS: Sin bugs, parámetros en tiempo real, sin congelamiento, interfaz profesional
 */
public class SimulacionCaidaLibre extends JPanel {
    
    private SimuladorFrame frame;
    private EscenarioCaidaLibre escenario;
    private MotorSimulacion motor;
    private DecimalFormat df = new DecimalFormat("#0.00");
    
    // UI
    private JButton btnSoltar, btnPausar, btnReiniciar, btnVolver;
    private JSlider sliderAltura, sliderGravedad, sliderVelocidadSim, sliderMasa;
    private JLabel labelAltura, labelGravedad, labelVelocidadSim, labelMasa;
    private JSpinner spinnerVelocidadInicial;
    private JCheckBox chkMostrarVectores, chkMostrarEnergia;
    private JPanel panelEnergia;
    
    // Parámetros
    private double alturaInicial = 50.0;
    private double velocidadInicial = 0.0;
    private double gravedad = 9.8;
    private double masa = 1.0;
    private int velocidadSimulacion = 30;
    private boolean mostrarVectores = true;
    private boolean mostrarEnergia = false;
    
    public SimulacionCaidaLibre(SimuladorFrame frame) {
        this.frame = frame;
        setLayout(new BorderLayout(10, 10));
        setBackground(UIHelper.COLOR_FONDO);
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        motor = new MotorSimulacion(velocidadSimulacion);
        escenario = new EscenarioCaidaLibre(900, 600);
        
        inicializarComponentes();
        configurarTeclado();
    }
    
    private void configurarTeclado() {
        InputMap inputMap = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = getActionMap();
        
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), "soltar");
        actionMap.put("soltar", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (motor != null && motor.isEnEjecucion()) {
                    pausarSimulacion();
                } else if (btnSoltar.isEnabled()) {
                    iniciarSimulacion();
                }
            }
        });
        
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_R, 0), "reiniciar");
        actionMap.put("reiniciar", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                reiniciarSimulacion();
            }
        });
        
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_V, 0), "toggleVectores");
        actionMap.put("toggleVectores", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                chkMostrarVectores.setSelected(!chkMostrarVectores.isSelected());
            }
        });
        
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_E, 0), "toggleEnergia");
        actionMap.put("toggleEnergia", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                chkMostrarEnergia.setSelected(!chkMostrarEnergia.isSelected());
            }
        });
    }
    
    private void inicializarComponentes() {
        // Panel superior
        JPanel panelSuperior = new JPanel(new BorderLayout());
        panelSuperior.setOpaque(false);
        panelSuperior.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        
        JLabel titulo = new JLabel("🪂 Caída Libre");
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 26));
        titulo.setForeground(UIHelper.COLOR_SECUNDARIO);
        
        JLabel version = new JLabel("v4.0");
        version.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        version.setForeground(new Color(127, 140, 141));
        
        panelSuperior.add(titulo, BorderLayout.WEST);
        panelSuperior.add(version, BorderLayout.EAST);
        
        // Panel de controles
        JPanel panelControles = crearPanelControles();
        JScrollPane scrollControles = new JScrollPane(panelControles);
        scrollControles.setPreferredSize(new Dimension(320, 600));
        scrollControles.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(189, 195, 199), 2),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        scrollControles.getVerticalScrollBar().setUnitIncrement(16);
        
        // Panel central
        JPanel panelCentral = new JPanel(new BorderLayout());
        panelCentral.setOpaque(false);
        panelCentral.add(escenario, BorderLayout.CENTER);
        
        // Panel de energía
        panelEnergia = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (mostrarEnergia) {
                    escenario.dibujarPanelEnergia((Graphics2D) g);
                }
            }
        };
        panelEnergia.setPreferredSize(new Dimension(900, 120));
        panelEnergia.setBackground(Color.WHITE);
        panelEnergia.setBorder(BorderFactory.createTitledBorder("Análisis de Energía"));
        panelCentral.add(panelEnergia, BorderLayout.SOUTH);
        
        add(panelSuperior, BorderLayout.NORTH);
        add(scrollControles, BorderLayout.WEST);
        add(panelCentral, BorderLayout.CENTER);
    }
    
    private JPanel crearPanelControles() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        // PARÁMETROS PRINCIPALES
        panel.add(crearSeccion("⚙️ PARÁMETROS"));
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        // Altura
        panel.add(crearEtiqueta("Altura Inicial (m):"));
        sliderAltura = crearSlider(5, 200, 50);
        sliderAltura.addChangeListener(e -> {
            alturaInicial = sliderAltura.getValue();
            labelAltura.setText(df.format(alturaInicial) + " m");
            if (!motor.isEnEjecucion()) {
                escenario.setAlturaInicial(alturaInicial);
            }
        });
        labelAltura = new JLabel(df.format(alturaInicial) + " m");
        labelAltura.setFont(new Font("Monospaced", Font.BOLD, 13));
        labelAltura.setForeground(UIHelper.COLOR_SECUNDARIO);
        panel.add(sliderAltura);
        panel.add(labelAltura);
        panel.add(Box.createRigidArea(new Dimension(0, 12)));
        
        // Velocidad inicial
        panel.add(crearEtiqueta("Velocidad Inicial (m/s):"));
        spinnerVelocidadInicial = new JSpinner(new SpinnerNumberModel(0.0, -50.0, 50.0, 1.0));
        spinnerVelocidadInicial.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        spinnerVelocidadInicial.setFont(new Font("Monospaced", Font.PLAIN, 13));
        spinnerVelocidadInicial.addChangeListener(e -> {
            velocidadInicial = (Double) spinnerVelocidadInicial.getValue();
            if (!motor.isEnEjecucion()) {
                escenario.setVelocidadInicial(velocidadInicial);
            }
        });
        panel.add(spinnerVelocidadInicial);
        JLabel infoVel = new JLabel("(−: abajo, +: arriba)");
        infoVel.setFont(new Font("Arial", Font.ITALIC, 10));
        infoVel.setForeground(Color.GRAY);
        panel.add(infoVel);
        panel.add(Box.createRigidArea(new Dimension(0, 12)));
        
        // Masa
        panel.add(crearEtiqueta("Masa (kg):"));
        sliderMasa = new JSlider(1, 100, 10);
        sliderMasa.setMajorTickSpacing(25);
        sliderMasa.setMinorTickSpacing(5);
        sliderMasa.setPaintTicks(true);
        sliderMasa.addChangeListener(e -> {
            masa = sliderMasa.getValue() / 10.0;
            labelMasa.setText(df.format(masa) + " kg");
            escenario.setMasa(masa);
        });
        labelMasa = new JLabel(df.format(masa) + " kg");
        labelMasa.setFont(new Font("Monospaced", Font.BOLD, 13));
        labelMasa.setForeground(UIHelper.COLOR_EXITO);
        panel.add(sliderMasa);
        panel.add(labelMasa);
        panel.add(Box.createRigidArea(new Dimension(0, 12)));
        
        // Gravedad
        panel.add(crearEtiqueta("Gravedad (m/s²):"));
        sliderGravedad = new JSlider(1, 200, 98);
        sliderGravedad.setMajorTickSpacing(50);
        sliderGravedad.setMinorTickSpacing(10);
        sliderGravedad.setPaintTicks(true);
        sliderGravedad.addChangeListener(e -> {
            gravedad = sliderGravedad.getValue() / 10.0;
            labelGravedad.setText(df.format(gravedad) + " m/s²");
            MotorSimulacion.setGravedad(gravedad);
        });
        labelGravedad = new JLabel(df.format(gravedad) + " m/s²");
        labelGravedad.setFont(new Font("Monospaced", Font.BOLD, 13));
        labelGravedad.setForeground(UIHelper.COLOR_PRIMARIO);
        panel.add(sliderGravedad);
        panel.add(labelGravedad);
        JLabel infoGrav = new JLabel("Tierra:9.8 Luna:1.6 Marte:3.7");
        infoGrav.setFont(new Font("Arial", Font.ITALIC, 9));
        infoGrav.setForeground(Color.GRAY);
        panel.add(infoGrav);
        panel.add(Box.createRigidArea(new Dimension(0, 12)));
        
        // Velocidad simulación
        panel.add(crearEtiqueta("Velocidad Simulación:"));
        sliderVelocidadSim = crearSlider(10, 100, 30);
        sliderVelocidadSim.addChangeListener(e -> {
            velocidadSimulacion = sliderVelocidadSim.getValue();
            String vel = velocidadSimulacion < 30 ? "⚡ Rápida" : 
                        velocidadSimulacion > 50 ? "🐌 Lenta" : "⚙️ Normal";
            labelVelocidadSim.setText(vel);
        });
        labelVelocidadSim = new JLabel("⚙️ Normal");
        labelVelocidadSim.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        panel.add(sliderVelocidadSim);
        panel.add(labelVelocidadSim);
        panel.add(Box.createRigidArea(new Dimension(0, 15)));
        
        // OPCIONES VISUALES
        panel.add(crearSeccion("👁️ VISUALIZACIÓN"));
        panel.add(Box.createRigidArea(new Dimension(0, 8)));
        
        chkMostrarVectores = new JCheckBox("Mostrar vectores (V)", true);
        estilizarCheckBox(chkMostrarVectores);
        chkMostrarVectores.addActionListener(e -> {
            mostrarVectores = chkMostrarVectores.isSelected();
            escenario.setMostrarVectores(mostrarVectores);
        });
        panel.add(chkMostrarVectores);
        
        chkMostrarEnergia = new JCheckBox("Mostrar energía (E)", false);
        estilizarCheckBox(chkMostrarEnergia);
        chkMostrarEnergia.addActionListener(e -> {
            mostrarEnergia = chkMostrarEnergia.isSelected();
            panelEnergia.setVisible(mostrarEnergia);
            panelEnergia.repaint();
        });
        panel.add(chkMostrarEnergia);
        
        panel.add(Box.createRigidArea(new Dimension(0, 20)));
        
        // Botones
        btnSoltar = crearBoton("🪂 Soltar (SPACE)", UIHelper.COLOR_EXITO);
        btnSoltar.addActionListener(e -> iniciarSimulacion());
        panel.add(btnSoltar);
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
        
        // Info
        JTextArea info = new JTextArea(
            "⌨️ CONTROLES:\n\n" +
            "ESPACIO → Soltar/Pausar\n" +
            "R → Reiniciar\n" +
            "V → Toggle vectores\n" +
            "E → Toggle energía\n\n" +
            "📐 ECUACIONES:\n" +
            "y = h₀ + v₀t - ½gt²\n" +
            "v = v₀ - gt\n" +
            "Eₚ = mgh\n" +
            "Eₖ = ½mv²"
        );
        info.setEditable(false);
        info.setFont(new Font("Consolas", Font.PLAIN, 11));
        info.setBackground(new Color(236, 240, 241));
        info.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(189, 195, 199)),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        info.setLineWrap(true);
        info.setWrapStyleWord(true);
        panel.add(info);
        
        panel.add(Box.createVerticalGlue());
        panel.add(Box.createRigidArea(new Dimension(0, 15)));
        
        btnVolver = crearBoton("🏠 Volver al Menú", UIHelper.COLOR_PELIGRO);
        btnVolver.addActionListener(e -> volverAlMenu());
        panel.add(btnVolver);
        
        return panel;
    }
    
    private void iniciarSimulacion() {
        escenario.setAlturaInicial(alturaInicial);
        escenario.setVelocidadInicial(velocidadInicial);
        escenario.setMasa(masa);
        escenario.reiniciar();
        
        motor = new MotorSimulacion(velocidadSimulacion);
        motor.iniciar(e -> {
            escenario.actualizar();
            escenario.repaint();
            if (mostrarEnergia) {
                panelEnergia.repaint();
            }
        });
        
        btnSoltar.setEnabled(false);
        btnPausar.setEnabled(true);
        sliderAltura.setEnabled(false);
        spinnerVelocidadInicial.setEnabled(false);
        sliderMasa.setEnabled(false);
        sliderGravedad.setEnabled(false);
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
        panelEnergia.repaint();
        
        btnSoltar.setEnabled(true);
        btnPausar.setEnabled(false);
        btnPausar.setText("⏸️ Pausar");
        sliderAltura.setEnabled(true);
        spinnerVelocidadInicial.setEnabled(true);
        sliderMasa.setEnabled(true);
        sliderGravedad.setEnabled(true);
    }
    
    private void volverAlMenu() {
        if (motor != null) {
            motor.detener();
            MotorSimulacion.resetGravedad();
        }
        frame.mostrarMenuPrincipal();
    }
    
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
    
    private JSlider crearSlider(int min, int max, int val) {
        JSlider slider = new JSlider(min, max, val);
        slider.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        slider.setAlignmentX(Component.LEFT_ALIGNMENT);
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
    
    // Escenario interno
    private class EscenarioCaidaLibre extends Escenario {
        private double posY, velY, h0, v0, m, tiempo;
        private boolean enSuelo, mostrarVect;
        private double energiaInicial;
        
        public EscenarioCaidaLibre(int w, int h) {
            super(w, h);
            this.motor = SimulacionCaidaLibre.this.motor;
            this.mostrarVect = true;
            reiniciar();
        }
        
        public void setAlturaInicial(double h) { this.h0 = h; }
        public void setVelocidadInicial(double v) { this.v0 = v; }
        public void setMasa(double masa) { this.m = masa; }
        public void setMostrarVectores(boolean m) { this.mostrarVect = m; }
        
        public void reiniciar() {
            posY = h0;
            velY = v0;
            tiempo = 0;
            enSuelo = false;
            energiaInicial = m * MotorSimulacion.getGravedad() * h0 + 0.5 * m * v0 * v0;
        }
        
        public void dibujarPanelEnergia(Graphics2D g2d) {
            int w = panelEnergia.getWidth();
            int h = panelEnergia.getHeight();
            
            double ep = m * MotorSimulacion.getGravedad() * Math.max(0, posY);
            double ek = 0.5 * m * velY * velY;
            double eTotal = ep + ek;
            double propEp = energiaInicial > 0 ? ep / energiaInicial : 0;
            double propEk = energiaInicial > 0 ? ek / energiaInicial : 0;
            
            g2d.setFont(new Font("Arial", Font.BOLD, 12));
            g2d.setColor(Color.BLACK);
            g2d.drawString("Energía Potencial", 20, 30);
            g2d.drawString("Energía Cinética", 20 + 200, 30);
            g2d.drawString("Energía Total", 20 + 400, 30);
            
            // Barras de energía
            g2d.setColor(new Color(41, 128, 185));
            g2d.fillRect(20, 40, (int)(180 * propEp), 40);
            g2d.setColor(new Color(231, 76, 60));
            g2d.fillRect(220, 40, (int)(180 * propEk), 40);
            g2d.setColor(new Color(46, 204, 113));
            g2d.fillRect(420, 40, 180, 40);
            
            g2d.setColor(new Color(189, 195, 199));
            g2d.setStroke(new BasicStroke(2));
            g2d.drawRect(20, 40, 180, 40);
            g2d.drawRect(220, 40, 180, 40);
            g2d.drawRect(420, 40, 180, 40);
            
            // Valores
            g2d.setColor(Color.BLACK);
            g2d.setFont(new Font("Monospaced", Font.PLAIN, 11));
            g2d.drawString(String.format("%.2f J (%.0f%%)", ep, propEp * 100), 30, 75);
            g2d.drawString(String.format("%.2f J (%.0f%%)", ek, propEk * 100), 230, 75);
            g2d.drawString(String.format("%.2f J", eTotal), 430, 75);
            
            // Error de conservación
            double error = Math.abs(eTotal - energiaInicial) / energiaInicial * 100;
            g2d.setColor(error < 2 ? new Color(46, 204, 113) : new Color(231, 76, 60));
            g2d.drawString("Error: " + df.format(error) + "%", 620, 75);
        }
        
        @Override
        public void actualizar() {
            if (motor == null || enSuelo) return;
            
            tiempo = motor.getTiempoTranscurrido();
            posY = h0 + v0 * tiempo - 0.5 * MotorSimulacion.getGravedad() * tiempo * tiempo;
            velY = v0 - MotorSimulacion.getGravedad() * tiempo;
            
            if (posY <= 0) {
                posY = 0;
                velY = 0;
                enSuelo = true;
                motor.detener();
            }
        }
        
        @Override
        protected void dibujar(Graphics2D g2d) {
            // Fondo degradado
            GradientPaint cielo = new GradientPaint(0, 0, new Color(135, 206, 250),
                                                     0, alto, new Color(240, 248, 255));
            g2d.setPaint(cielo);
            g2d.fillRect(0, 0, ancho, alto);
            
            // Suelo
            int sueloY = alto - 50;
            g2d.setColor(new Color(101, 67, 33));
            g2d.fillRect(0, sueloY, ancho, 50);
            g2d.setColor(new Color(76, 153, 0));
            g2d.fillRect(0, sueloY - 10, ancho, 10);
            
            // Escala de altura
            g2d.setColor(new Color(100, 100, 100));
            g2d.setFont(new Font("Arial", Font.PLAIN, 10));
            for (int i = 0; i <= 200; i += 20) {
                int y = sueloY - metrosAPixeles(i);
                if (y > 0) {
                    g2d.drawLine(40, y, 60, y);
                    if (i % 40 == 0) {
                        g2d.drawString(i + "m", 10, y + 5);
                    }
                }
            }
            
            // Línea de altura inicial
            g2d.setColor(new Color(100, 100, 100, 150));
            g2d.setStroke(new BasicStroke(2, BasicStroke.CAP_BUTT,
                         BasicStroke.JOIN_BEVEL, 0, new float[]{5}, 0));
            int altInicY = sueloY - metrosAPixeles(h0);
            g2d.drawLine(100, altInicY, ancho - 100, altInicY);
            g2d.setFont(new Font("Arial", Font.BOLD, 12));
            g2d.drawString("h₀=" + df.format(h0) + "m", ancho - 150, altInicY - 5);
            
            // Objeto cayendo
            int objX = ancho / 2;
            int objY = sueloY - metrosAPixeles(posY);
            
            Color colorObj = enSuelo ? new Color(46, 204, 113) : new Color(231, 76, 60);
            int radioObj = (int)(15 * Math.sqrt(m));
            
            // Sombra
            g2d.setColor(new Color(0, 0, 0, 50));
            g2d.fillOval(objX - radioObj + 2, objY - radioObj + 2, radioObj * 2, radioObj * 2);
            
            // Objeto
            g2d.setColor(colorObj);
            g2d.fillOval(objX - radioObj, objY - radioObj, radioObj * 2, radioObj * 2);
            
            // Brillo
            g2d.setColor(new Color(255, 255, 255, 120));
            g2d.fillOval(objX - radioObj + 4, objY - radioObj + 4, radioObj, radioObj);
            
            // Borde
            g2d.setColor(colorObj.darker());
            g2d.setStroke(new BasicStroke(2));
            g2d.drawOval(objX - radioObj, objY - radioObj, radioObj * 2, radioObj * 2);
            
            // Vectores
            if (mostrarVect && !enSuelo && Math.abs(velY) > 0.1) {
                // Vector velocidad
                g2d.setColor(new Color(39, 174, 96));
                g2d.setStroke(new BasicStroke(3));
                int longFlecha = Math.min((int)(Math.abs(velY) * 5), 100);
                int dir = velY < 0 ? 1 : -1;
                g2d.drawLine(objX, objY, objX, objY + dir * longFlecha);
                g2d.drawLine(objX, objY + dir * longFlecha,
                            objX - 5, objY + dir * longFlecha - dir * 8);
                g2d.drawLine(objX, objY + dir * longFlecha,
                            objX + 5, objY + dir * longFlecha - dir * 8);
                g2d.setFont(new Font("Arial", Font.BOLD, 12));
                g2d.drawString("v=" + df.format(Math.abs(velY)),
                              objX + 15, objY + dir * longFlecha / 2);
                
                // Vector gravedad
                g2d.setColor(new Color(155, 89, 182));
                g2d.setStroke(new BasicStroke(2, BasicStroke.CAP_BUTT,
                                             BasicStroke.JOIN_BEVEL, 0, new float[]{5}, 0));
                int gFlecha = (int)(MotorSimulacion.getGravedad() * 8);
                g2d.drawLine(objX + 30, objY, objX + 30, objY + gFlecha);
                g2d.drawLine(objX + 30, objY + gFlecha,
                            objX + 25, objY + gFlecha - 8);
                g2d.drawLine(objX + 30, objY + gFlecha,
                            objX + 35, objY + gFlecha - 8);
                g2d.drawString("g", objX + 40, objY + gFlecha / 2);
            }
            
            // Panel de información
            dibujarPanelInfo(g2d);
        }
        
        private void dibujarPanelInfo(Graphics2D g2d) {
            double ep = m * MotorSimulacion.getGravedad() * Math.max(0, posY);
            double ek = 0.5 * m * velY * velY;
            double eTotal = ep + ek;
            
            String estado = enSuelo ? "✓ EN SUELO" :
                           velY < 0 ? "⬇️ CAYENDO" : "⬆️ SUBIENDO";
            
            String[] datos = {
                "⏱️ Tiempo: " + df.format(tiempo) + " s " + estado,
                "📍 Altura: " + df.format(Math.max(0, posY)) + " m",
                "⬇️ Velocidad: " + df.format(velY) + " m/s",
                "⚡ Aceleración: " + df.format(MotorSimulacion.getGravedad()) + " m/s²",
                "⚖️ Masa: " + df.format(m) + " kg",
                "🔋 Eₚ: " + df.format(ep) + " J",
                "⚡ Eₖ: " + df.format(ek) + " J",
                "💯 E total: " + df.format(eTotal) + " J"
            };
            
            int panelX = 20;
            int panelY = 20;
            int panelW = 280;
            int panelH = (datos.length * 18) + 20;
            
            // Fondo
            g2d.setColor(new Color(255, 255, 255, 240));
            g2d.fillRoundRect(panelX, panelY, panelW, panelH, 15, 15);
            
            // Borde
            g2d.setColor(UIHelper.COLOR_SECUNDARIO);
            g2d.setStroke(new BasicStroke(2));
            g2d.drawRoundRect(panelX, panelY, panelW, panelH, 15, 15);
            
            // Texto
            g2d.setColor(Color.BLACK);
            g2d.setFont(new Font("Monospaced", Font.PLAIN, 11));
            int y = panelY + 16;
            
            for (String linea : datos) {
                g2d.drawString(linea, panelX + 12, y);
                y += 18;
            }
            
            // Versión
            g2d.setColor(new Color(127, 140, 141));
            g2d.setFont(new Font("Arial", Font.ITALIC, 11));
            g2d.drawString("v4.0", ancho - 40, alto - 10);
        }
    }
}