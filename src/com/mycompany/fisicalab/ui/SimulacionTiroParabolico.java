package com.mycompany.fisicalab.ui;

import com.mycompany.fisicalab.core.Escenario;
import com.mycompany.fisicalab.core.MotorSimulacion;
import com.mycompany.fisicalab.core.SimuladorFrame;
import com.mycompany.fisicalab.utils.UIHelper;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

/**
 * SimulacionTiroParabolico v4.1 - Estilo Único
 * ✅ Reorganización de UI a panel inferior.
 * ✅ Estilo visual temático (campo de artillería).
 * ✅ Controles mejorados y agrupados.
 */
public class SimulacionTiroParabolico extends JPanel {
    
    private SimuladorFrame frame;
    private EscenarioTiroParabolico escenario;
    private MotorSimulacion motor;
    private DecimalFormat df = new DecimalFormat("#0.00");
    private final ReentrantLock lock = new ReentrantLock();
    
    // Parámetros
    private double velocidadInicial = 20.0;
    private double angulo = 45.0;
    private double gravedad = 9.8;
    private double alturaInicial = 0.0;
    
    // Flags
    private volatile boolean mostrarTrayectoria = true;
    private volatile boolean mostrarVectores = true;
    
    // UI
    private JButton btnLanzar, btnPausar, btnReiniciar, btnVolver;
    private JSlider sliderVelocidad, sliderAngulo, sliderGravedad;
    private JLabel labelVelocidad, labelAngulo, labelGravedad;
    private JSpinner spinnerAltura;
    private JCheckBox chkTrayectoria, chkVectores;
    
    public SimulacionTiroParabolico(SimuladorFrame frame) {
        this.frame = frame;
        setBackground(UIHelper.COLOR_FONDO);
        
        escenario = new EscenarioTiroParabolico(this);
        motor = new MotorSimulacion(30);
        
        inicializarComponentes();
        configurarTeclado();
        
        Timer repaintTimer = new Timer(16, e -> escenario.repaint());
        repaintTimer.start();
    }
    
    private void configurarTeclado() {
        InputMap inputMap = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = getActionMap();
        
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), "lanzar");
        actionMap.put("lanzar", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (motor.isEnEjecucion()) {
                    pausarSimulacion();
                } else if (btnLanzar.isEnabled()) {
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
        
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "arriba");
        actionMap.put("arriba", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                lock.lock();
                try {
                    if (angulo < 90) angulo += 1;
                    sliderAngulo.setValue((int) angulo);
                } finally { lock.unlock(); }
            }
        });
        
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "abajo");
        actionMap.put("abajo", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                lock.lock();
                try {
                    if (angulo > 0) angulo -= 1;
                    sliderAngulo.setValue((int) angulo);
                } finally { lock.unlock(); }
            }
        });
        
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "derecha");
        actionMap.put("derecha", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                lock.lock();
                try {
                    if (velocidadInicial < 50) velocidadInicial += 1;
                    sliderVelocidad.setValue((int) velocidadInicial);
                } finally { lock.unlock(); }
            }
        });
        
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), "izquierda");
        actionMap.put("izquierda", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                lock.lock();
                try {
                    if (velocidadInicial > 5) velocidadInicial -= 1;
                    sliderVelocidad.setValue((int) velocidadInicial);
                } finally { lock.unlock(); }
            }
        });
    }
    
    private void inicializarComponentes() {
        setLayout(new BorderLayout(0, 0));
        
        JPanel panelControles = crearPanelControles();
        
        add(escenario, BorderLayout.CENTER);
        add(panelControles, BorderLayout.SOUTH);
    }
    
    private JPanel crearPanelControles() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(40, 55, 71));
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(2, 0, 0, 0, new Color(52, 73, 94)),
            BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 8, 5, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Columna 0: Velocidad
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(crearEtiqueta("Velocidad (m/s)", Color.WHITE), gbc);
        gbc.gridy = 1;
        sliderVelocidad = new JSlider(5, 50, 20);
        labelVelocidad = new JLabel(df.format(velocidadInicial), SwingConstants.CENTER);
        panel.add(crearPanelSlider(sliderVelocidad, labelVelocidad, UIHelper.COLOR_EXITO), gbc);
        sliderVelocidad.addChangeListener(e -> {
            lock.lock();
            try {
                velocidadInicial = sliderVelocidad.getValue();
                labelVelocidad.setText(df.format(velocidadInicial));
            } finally { lock.unlock(); }
        });

        // Columna 1: Ángulo
        gbc.gridx = 1; gbc.gridy = 0;
        panel.add(crearEtiqueta("Ángulo (°)", Color.WHITE), gbc);
        gbc.gridy = 1;
        sliderAngulo = new JSlider(0, 90, 45);
        labelAngulo = new JLabel(df.format(angulo), SwingConstants.CENTER);
        panel.add(crearPanelSlider(sliderAngulo, labelAngulo, UIHelper.COLOR_PRIMARIO), gbc);
        sliderAngulo.addChangeListener(e -> {
            lock.lock();
            try {
                angulo = sliderAngulo.getValue();
                labelAngulo.setText(df.format(angulo));
            } finally { lock.unlock(); }
        });

        // Columna 2: Altura y Gravedad
        gbc.gridx = 2; gbc.gridy = 0; gbc.gridheight = 2;
        JPanel panelSpinners = new JPanel();
        panelSpinners.setLayout(new BoxLayout(panelSpinners, BoxLayout.Y_AXIS));
        panelSpinners.setOpaque(false);
        spinnerAltura = new JSpinner(new SpinnerNumberModel(0.0, 0.0, 100.0, 1.0));
        spinnerAltura.addChangeListener(e -> {
            lock.lock();
            try { alturaInicial = (Double) spinnerAltura.getValue(); } 
            finally { lock.unlock(); }
        });
        panelSpinners.add(crearPanelSpinner("Altura (m)", spinnerAltura));
        panelSpinners.add(Box.createRigidArea(new Dimension(0, 5)));
        
        sliderGravedad = new JSlider(1, 200, 98);
        labelGravedad = new JLabel(df.format(gravedad));
        sliderGravedad.addChangeListener(e -> {
            lock.lock();
            try {
                gravedad = sliderGravedad.getValue() / 10.0;
                labelGravedad.setText(df.format(gravedad));
                MotorSimulacion.setGravedad(gravedad);
            } finally { lock.unlock(); }
        });
        panelSpinners.add(crearPanelSpinner("Gravedad (m/s²)", sliderGravedad, labelGravedad));
        gbc.insets = new Insets(0, 8, 0, 8);
        panel.add(panelSpinners, gbc);
        gbc.insets = new Insets(5, 8, 5, 8);
        gbc.gridheight = 1;

        // Separador
        gbc.gridx = 3; gbc.gridy = 0; gbc.gridheight = 2;
        panel.add(new JSeparator(SwingConstants.VERTICAL), gbc);
        gbc.gridheight = 1;

        // Columna 4: Opciones
        gbc.gridx = 4; gbc.gridy = 0; gbc.gridheight = 2;
        JPanel panelOpciones = new JPanel();
        panelOpciones.setLayout(new BoxLayout(panelOpciones, BoxLayout.Y_AXIS));
        panelOpciones.setOpaque(false);
        chkTrayectoria = new JCheckBox("Trayectoria", true);
        chkTrayectoria.addActionListener(e -> mostrarTrayectoria = chkTrayectoria.isSelected());
        estilizarCheckBox(chkTrayectoria);
        panelOpciones.add(chkTrayectoria);
        chkVectores = new JCheckBox("Vectores", true);
        chkVectores.addActionListener(e -> mostrarVectores = chkVectores.isSelected());
        estilizarCheckBox(chkVectores);
        panelOpciones.add(chkVectores);
        panel.add(panelOpciones, gbc);
        gbc.gridheight = 1;

        // Separador
        gbc.gridx = 5; gbc.gridy = 0; gbc.gridheight = 2;
        panel.add(new JSeparator(SwingConstants.VERTICAL), gbc);
        gbc.gridheight = 1;

        // Columna 6: Botones
        gbc.gridx = 6; gbc.gridy = 0; gbc.gridheight = 2; gbc.fill = GridBagConstraints.BOTH;
        JPanel panelBotones = new JPanel(new GridLayout(2, 2, 5, 5));
        panelBotones.setOpaque(false);
        btnLanzar = crearBoton("Lanzar", UIHelper.COLOR_EXITO);
        btnLanzar.addActionListener(e -> iniciarSimulacion());
        btnPausar = crearBoton("Pausar", UIHelper.COLOR_ADVERTENCIA);
        btnPausar.setEnabled(false);
        btnPausar.addActionListener(e -> pausarSimulacion());
        btnReiniciar = crearBoton("Reiniciar", UIHelper.COLOR_SECUNDARIO);
        btnReiniciar.addActionListener(e -> reiniciarSimulacion());
        btnVolver = crearBoton("Volver", UIHelper.COLOR_PELIGRO);
        btnVolver.addActionListener(e -> {
            motor.detener();
            MotorSimulacion.resetGravedad();
            frame.mostrarMenuPrincipal();
        });
        panelBotones.add(btnLanzar);
        panelBotones.add(btnPausar);
        panelBotones.add(btnReiniciar);
        panelBotones.add(btnVolver);
        panel.add(panelBotones, gbc);

        return panel;
    }
    
    private void iniciarSimulacion() {
        escenario.reiniciar();
        motor = new MotorSimulacion(30);
        motor.iniciar(e -> SwingUtilities.invokeLater(() -> {
            escenario.actualizar();
            escenario.repaint();
        }));
        
        btnLanzar.setEnabled(false);
        btnPausar.setEnabled(true);
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
        motor.detener();
        motor.reiniciar();
        escenario.reiniciar();
        escenario.repaint();
        
        btnLanzar.setEnabled(true);
        btnPausar.setEnabled(false);
        btnPausar.setText("⏸️ Pausar");
    }
    
    private JLabel crearEtiqueta(String texto, Color color) {
        JLabel l = new JLabel(texto);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        l.setForeground(color);
        return l;
    }
    
    private JButton crearBoton(String texto, Color color) {
        JButton btn = UIHelper.crearBotonRedondeado(texto, color);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setPreferredSize(new Dimension(80, 30));
        return btn;
    }

    private JPanel crearPanelSlider(JSlider slider, JLabel label, Color color) {
        JPanel p = new JPanel(new BorderLayout(5, 0));
        p.setOpaque(false);
        label.setForeground(color);
        label.setFont(new Font("Consolas", Font.BOLD, 14));
        p.add(slider, BorderLayout.CENTER);
        p.add(label, BorderLayout.EAST);
        return p;
    }

    private JPanel crearPanelSpinner(String texto, JComponent spinner) {
        JPanel p = new JPanel(new BorderLayout(10, 0));
        p.setOpaque(false);
        JLabel l = crearEtiqueta(texto, Color.WHITE);
        p.add(l, BorderLayout.WEST);
        p.add(spinner, BorderLayout.CENTER);
        return p;
    }

    private JPanel crearPanelSpinner(String texto, JSlider slider, JLabel label) {
        JPanel p = new JPanel(new BorderLayout(10, 0));
        p.setOpaque(false);
        label.setForeground(Color.WHITE);
        label.setFont(new Font("Consolas", Font.BOLD, 12));
        JLabel l = crearEtiqueta(texto, Color.WHITE);
        p.add(l, BorderLayout.WEST);
        p.add(slider, BorderLayout.CENTER);
        p.add(label, BorderLayout.EAST);
        return p;
    }

    private void estilizarCheckBox(JCheckBox chk) {
        chk.setOpaque(false);
        chk.setForeground(Color.WHITE);
        chk.setFont(new Font("Segoe UI", Font.PLAIN, 12));
    }
    
    // ESCENARIO
    private class EscenarioTiroParabolico extends JPanel {
        private double posX = 0;
        private double posY = 0;
        private double velX = 0;
        private double velY = 0;
        private double tiempo = 0;
        private boolean impacto = false;
        private List<Point> trayectoria = new ArrayList<>();
        private MotorSimulacion motor;
        private SimulacionTiroParabolico parent;
        
        public EscenarioTiroParabolico(SimulacionTiroParabolico parent) {
            this.parent = parent;
            this.motor = SimulacionTiroParabolico.this.motor;
            setDoubleBuffered(true);
        }
        
        public void reiniciar() {
            lock.lock();
            try {
                posX = 0;
                posY = alturaInicial;
                tiempo = 0;
                impacto = false;
                trayectoria.clear();
                
                double rad = Math.toRadians(angulo);
                velX = velocidadInicial * Math.cos(rad);
                velY = velocidadInicial * Math.sin(rad);
            } finally {
                lock.unlock();
            }
        }
        
        public void actualizar() {
            if (motor == null || impacto) return;
            
            lock.lock();
            try {
                tiempo = motor.getTiempoTranscurrido();
                posX = velX * tiempo;
                posY = alturaInicial + velY * tiempo - 0.5 * gravedad * tiempo * tiempo;
            } finally {
                lock.unlock();
            }
            
            if (posY >= 0 && trayectoria.size() < 500) {
                int sx = 80 + (int)(posX * 6);
                int sy = (getHeight() - 60) - (int)(posY * 6);
                if (sx < getWidth() && sy > 0) {
                    trayectoria.add(new Point(sx, sy));
                }
            }
            
            if (posY < 0) {
                impacto = true;
                motor.detener();
            }
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            int w = getWidth();
            int h = getHeight();
            
            // FONDO
            dibujarFondo(g2d, w, h);
            
            // CUADRÍCULA
            dibujarCuadricula(g2d, w, h);
            
            // SUELO
            dibujarSuelo(g2d, w, h);
            
            // CAÑÓN
            dibujarCanon(g2d, w, h);
            
            // TRAYECTORIA
            if (mostrarTrayectoria && trayectoria.size() > 1) {
                g2d.setColor(new Color(241, 196, 15, 220)); // Amarillo para trayectoria
                g2d.setStroke(new BasicStroke(2.5f));
                Path2D path = new Path2D.Double();
                path.moveTo(trayectoria.get(0).x, trayectoria.get(0).y);
                for (int i = 1; i < trayectoria.size(); i++) {
                    path.lineTo(trayectoria.get(i).x, trayectoria.get(i).y);
                }
                g2d.draw(path);
            }
            
            // OBJETO
            if (!impacto && posY >= 0) {
                dibujarProyectil(g2d, w, h);
            } else if (impacto) {
                dibujarImpacto(g2d, w, h);
            }
            
            // INFO
            dibujarInfo(g2d, w, h);
        }
        
        private void dibujarFondo(Graphics2D g2d, int w, int h) {
            // Gradiente para el cielo
            GradientPaint cielo = new GradientPaint(0, 0, new Color(135, 206, 250), 0, h, new Color(229, 231, 233));
            g2d.setPaint(cielo);
            g2d.fillRect(0, 0, w, h);
        }
        
        private void dibujarCuadricula(Graphics2D g2d, int w, int h) {
            g2d.setColor(new Color(200, 200, 200, 60));
            g2d.setStroke(new BasicStroke(0.5f));
            
            int espaciado = 40;
            for (int x = 0; x < w; x += espaciado) {
                g2d.drawLine(x, 0, x, h);
            }
            for (int y = 0; y < h; y += espaciado) {
                g2d.drawLine(0, y, w, y);
            }
        }
        
        private void dibujarSuelo(Graphics2D g2d, int w, int h) {
            int suelo = h - 60;
            // Textura de tierra
            g2d.setColor(new Color(92, 64, 51));
            g2d.fillRect(0, suelo, w, 60);
            // Césped con gradiente
            GradientPaint cesped = new GradientPaint(0, suelo - 10, new Color(88, 178, 88), 0, suelo, new Color(60, 120, 60));
            g2d.setPaint(cesped);
            g2d.fillRect(0, suelo - 10, w, 10);
        }
        
        private void dibujarCanon(Graphics2D g2d, int w, int h) {
            int suelo = h - 60;
            int canonX = 80;
            int canonY = suelo - (int)(alturaInicial * 6);
            
            double rad = Math.toRadians(angulo);
            
            // Guardar transformación original
            AffineTransform original = g2d.getTransform();
            g2d.translate(canonX, canonY);
            g2d.rotate(-rad);
            
            // Dibujar el cañón (cuerpo y tubo)
            g2d.setColor(new Color(84, 109, 122));
            g2d.fillRect(-15, -10, 40, 20);
            g2d.setColor(new Color(40, 55, 71));
            g2d.fillRect(25, -6, 30, 12);
            
            // Ruedas
            g2d.setColor(new Color(40, 55, 71));
            g2d.fillOval(-15, 5, 20, 20);
            g2d.setColor(Color.DARK_GRAY);
            g2d.fillOval(-10, 10, 10, 10);
            
            // Restaurar transformación
            g2d.setTransform(original);
            
            // Ángulo
            g2d.setColor(new Color(241, 196, 15, 150));
            g2d.setStroke(new BasicStroke(2));
            g2d.fillArc(canonX - 25, canonY - 25, 50, 50, 0, (int) angulo);
        }
        
        private void dibujarProyectil(Graphics2D g2d, int w, int h) {
            int suelo = h - 60;
            int objX = 80 + (int)(posX * 6);
            int objY = suelo - (int)(posY * 6);
            
            // Sombra en el suelo
            int sombraX = objX;
            int sombraY = suelo - 5;
            int sombraAncho = 15 - (int)(posY / 20);
            if (sombraAncho > 0) {
                g2d.setColor(new Color(0, 0, 0, 40));
                g2d.fillOval(sombraX - sombraAncho / 2, sombraY, sombraAncho, 5);
            }
            
            // Proyectil (bala de cañón)
            g2d.setColor(new Color(40, 55, 71));
            g2d.fillOval(objX - 8, objY - 8, 16, 16);
            
            // Brillo
            g2d.setColor(new Color(255, 255, 255, 100));
            g2d.fillOval(objX - 4, objY - 4, 6, 6);
            
            // Vectores
            if (mostrarVectores && motor.isEnEjecucion()) {
                double vyActual = velY - gravedad * tiempo;
                
                // Vector Vx (verde)
                dibujarVector(g2d, objX, objY, (int)(velX * 3), 0, new Color(39, 174, 96));
                // Vector Vy (azul)
                dibujarVector(g2d, objX, objY, 0, (int)(-vyActual * 3), new Color(41, 128, 185));
            }
        }
        
        private void dibujarImpacto(Graphics2D g2d, int w, int h) {
            int suelo = h - 60;
            int impX = 80 + (int)(posX * 6);
            
            // Cráter
            g2d.setColor(new Color(92, 64, 51).darker());
            g2d.fillOval(impX - 15, suelo - 8, 30, 10);
            
            // Partículas de tierra
            g2d.setColor(new Color(111, 84, 72));
            for (int i = 0; i < 10; i++) {
                double ang = Math.random() * Math.PI;
                int dist = 5 + (int)(Math.random() * 15);
                g2d.fillOval(impX + (int)(Math.cos(ang) * dist), suelo - (int)(Math.sin(ang) * dist), 3, 3);
            }
        }
        
        private void dibujarVector(Graphics2D g2d, int x, int y, int dx, int dy, Color color) {
            g2d.setColor(color);
            g2d.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2d.drawLine(x, y, x + dx, y + dy);
            
            // Flecha
            double angle = Math.atan2(dy, dx);
            int arrowSize = 8;
            g2d.drawLine(x + dx, y + dy, 
                         (int)(x + dx - arrowSize * Math.cos(angle - Math.PI / 6)), 
                         (int)(y + dy - arrowSize * Math.sin(angle - Math.PI / 6)));
            g2d.drawLine(x + dx, y + dy, 
                         (int)(x + dx - arrowSize * Math.cos(angle + Math.PI / 6)), 
                         (int)(y + dy - arrowSize * Math.sin(angle + Math.PI / 6)));
        }

        private void dibujarInfo(Graphics2D g2d, int w, int h) {
            double vyActual = velY - gravedad * tiempo;
            double rapidez = Math.sqrt(velX * velX + vyActual * vyActual);
            
            String[] datos = {
                "Tiempo: " + df.format(tiempo) + " s",
                "Posición X: " + df.format(posX) + " m",
                "Posición Y: " + df.format(Math.max(0, posY)) + " m",
                "Velocidad X: " + df.format(velX) + " m/s",
                "Velocidad Y: " + df.format(vyActual) + " m/s",
                "Rapidez: " + df.format(rapidez) + " m/s"
            };
            
            g2d.setFont(new Font("Consolas", Font.BOLD, 12));
            FontMetrics fm = g2d.getFontMetrics();
            int panelW = 240;
            int panelH = (datos.length * (fm.getHeight() + 2)) + 20;
            int panelX = w - panelW - 20;
            int panelY = 20;
            
            g2d.setColor(new Color(255, 255, 255, 200));
            g2d.fillRoundRect(panelX, panelY, panelW, panelH, 15, 15);
            
            g2d.setColor(Color.BLACK);
            int y = panelY + fm.getAscent() + 10;
            for (String linea : datos) {
                g2d.drawString(linea, panelX + 15, y);
                y += fm.getHeight() + 2;
            }
        }
    }
}
