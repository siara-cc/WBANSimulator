package wban.simulate.view;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.Hashtable;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;

import wban.simulate.Battery;
import wban.simulate.SensorNode;
import wban.simulate.Simulator;
import wban.simulate.config.BaseStationConfig;
import wban.simulate.config.SensorNodeConfig;
import wban.simulate.path.LineSegment;
import wban.simulate.path.PathSet;
import wban.simulate.path.Point;
import wban.simulate.util.Util;

public class SwingViewer extends JPanel implements Runnable, ActionListener, MouseMotionListener, MouseListener {

    static final long serialVersionUID = 474227420717631309L;

    static final String strMIAddBaseStation = "Add Base Station";
    static final String strMIAddSensorCluster = "Add Sensor Cluster";
    static final String strMIChangeDetails = "Change details";
    static final String strMIChargeBattery = "Charge battery";
    static final String strMIStopCharging = "Stop Charging";
    static final String strMIBatteryDown = "Battery down";
    static final String strMISendData = "Send data";
    static final String strMILoadConfig = "Load from Configuration";
    static final String strMISaveConfig = "Save Configuration";
    static final String strMIStartSimulation = "Start Simulation";
    static final String strMIStopSimulation = "Stop Simulation";

    final JPopupMenu popMenu = new JPopupMenu();
    final Border lineBorder = BorderFactory.createLineBorder(Color.black);

    ImageIcon baseStationImg = new ImageIcon(this.getClass().getResource("/images/BaseStationIcon.png"));
    ImageIcon sensorClusterNormalImg = new ImageIcon(this.getClass().getResource("/images/SensorClusterIcon.png"));
    ImageIcon sensorClusterBatteryChargingImg = new ImageIcon(
            this.getClass().getResource("/images/SensorClusterBatteryChargingIcon.png"));
    ImageIcon sensorClusterBatteryDownImg = new ImageIcon(
            this.getClass().getResource("/images/SensorClusterBatteryDownIcon.png"));

    JLabel currentIcon = null;
    int currentX, currentY;

    Simulator simulator = Simulator.getInstance();
    Timer timer = new Timer();

    JFrame f = null;

    private boolean isTimerSet = false;

    public SwingViewer() {
        JMenuItem miChangeDetails = new JMenuItem(strMIChangeDetails);
        JMenuItem miChargeBattery = new JMenuItem(strMIChargeBattery);
        JMenuItem miStopCharging = new JMenuItem(strMIStopCharging);
        JMenuItem miBatteryDown = new JMenuItem(strMIBatteryDown);
        JMenuItem miSendData = new JMenuItem(strMISendData);
        miChangeDetails.addActionListener(this);
        miChargeBattery.addActionListener(this);
        miStopCharging.addActionListener(this);
        miBatteryDown.addActionListener(this);
        miSendData.addActionListener(this);
        popMenu.add(miChangeDetails);
        popMenu.add(miChargeBattery);
        popMenu.add(miStopCharging);
        popMenu.add(miBatteryDown);
        popMenu.add(miSendData);
    }

    public void run() {
        f = new JFrame("WBAN Simulation Viewer");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        f.setBounds(0, 0, (int) screenSize.getWidth(), (int) screenSize.getHeight());
        this.setLayout(null);
        this.setBackground(Color.WHITE);
        this.setSize((int) screenSize.getWidth(), (int) screenSize.getHeight());

        JMenuBar menuBar = new JMenuBar();
        JMenu menuMain = new JMenu("Main");
        menuBar.add(menuMain);
        f.setJMenuBar(menuBar);
        JMenuItem miLoadConfig = new JMenuItem(strMILoadConfig);
        miLoadConfig.addActionListener(this);
        menuMain.add(miLoadConfig);
        JMenuItem miSaveConfig = new JMenuItem(strMISaveConfig);
        miSaveConfig.addActionListener(this);
        menuMain.add(miSaveConfig);
        JMenuItem miStartSimulation = new JMenuItem(strMIStartSimulation);
        miStartSimulation.addActionListener(this);
        menuMain.add(miStartSimulation);
        JMenuItem miStopSimulation = new JMenuItem(strMIStopSimulation);
        miStopSimulation.addActionListener(this);
        menuMain.add(miStopSimulation);

        final JPopupMenu popMenu = new JPopupMenu();
        JMenuItem miBase = new JMenuItem(strMIAddBaseStation);
        miBase.addActionListener(this);
        JMenuItem miSensorCluster = new JMenuItem(strMIAddSensorCluster);
        miSensorCluster.addActionListener(this);
        popMenu.add(miBase);
        popMenu.add(miSensorCluster);
        this.add(popMenu);
        this.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent evt) {
                if (true) { //evt.isPopupTrigger()) {
                    popMenu.show(evt.getComponent(), evt.getX(), evt.getY());
                    currentX = evt.getX();
                    currentY = evt.getY();
                }
            }

            public void mouseReleased(MouseEvent evt) {
            }
        });
        JScrollPane scrollPane = new JScrollPane(this);
        scrollPane.setAutoscrolls(true);
        scrollPane.setWheelScrollingEnabled(true);
        f.getContentPane().add(scrollPane);
        f.setVisible(true);
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        return screenSize;
    }

    private void drawLineSegment(Point from, Point to, Color c, int size, String label, Graphics2D g) {
        g.setColor(c);
        g.setStroke(new BasicStroke(size));
        // double lineLen = Util.distanceBetween(ls.getFrom(),
        // ls.getTo());
        int x1 = from.getX();
        int y1 = from.getY();
        int x2 = to.getX();
        int y2 = to.getY();
        g.drawLine(x1, y1, x2, y2);
        int sx = (int) ((x1 + x2) / 2.1);
        int sy = (int) ((y1 + y2) / 2.1);
        int cx = (int) ((x1 + x2) / 2);
        int cy = (int) ((y1 + y2) / 2);
        int d = 10;
        double angle = Util.angle360(from, to);
        double anglePlus45 = angle + 45;
        if (anglePlus45 > 360)
            anglePlus45 = anglePlus45 % 360;
        double angleMinus45 = angle - 45;
        if (angleMinus45 < 360)
            angleMinus45 = angleMinus45 + 360;
        anglePlus45 = Math.toRadians(anglePlus45);
        angleMinus45 = Math.toRadians(angleMinus45);
        int ax1 = (int) (cx - d * Math.cos(anglePlus45));
        int ay1 = (int) (cy - d * Math.sin(anglePlus45));
        int ax2 = (int) (cx - d * Math.cos(angleMinus45));
        int ay2 = (int) (cy - d * Math.sin(angleMinus45));
        g.drawLine(cx, cy, ax1, ay1);
        g.drawLine(cx, cy, ax2, ay2);
        g.drawString(label, sx, sy);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        PathSet currentPathSet = simulator.getCurrentPathSet();
        if (currentPathSet != null) {
            Hashtable<Point, List<LineSegment>> hmLines = currentPathSet.getAllPaths();
            for (List<LineSegment> lstLS : hmLines.values()) {
                for (LineSegment ls : lstLS) {
                    drawLineSegment(ls.getFrom(), ls.getTo(), Color.BLACK, 1, ls.getLabel(), (Graphics2D) g);
                }
            }
            List<Point[]> shortestPath = currentPathSet.getShortestPath();
            for (Point[] line : shortestPath) {
                String label = String.valueOf(Math.round(Util.distanceBetween(line[0], line[1])));
                drawLineSegment(line[0], line[1], Color.RED, 3, label, (Graphics2D) g);
            }
        }
    }

    private JLabel addBaseStation(int x, int y) {
        JLabel icon = new JLabel(baseStationImg);
        icon.setBounds(x, y, baseStationImg.getIconWidth(), baseStationImg.getIconHeight());
        icon.addMouseListener(this);
        icon.addMouseMotionListener(this);
        this.add(icon);
        if (currentIcon != null)
            currentIcon.setBorder(null);
        currentIcon = icon;
        icon.setBorder(lineBorder);
        return icon;
    }

    public JLabel addSensorCluster(int x, int y, short state) {
        ImageIcon img = null;
        switch (state) {
        case SensorNodeConfig.ST_DISCHARGING:
            img = sensorClusterNormalImg;
            break;
        case SensorNodeConfig.ST_CHARGING:
            img = sensorClusterBatteryChargingImg;
            break;
        case SensorNodeConfig.ST_DOWN:
            img = sensorClusterBatteryDownImg;
            break;
        }
        JLabel icon = new JLabel(img);
        icon.setBounds(x, y, img.getIconWidth(), img.getIconHeight());
        icon.addMouseListener(this);
        icon.addMouseMotionListener(this);
        this.add(icon);
        if (currentIcon != null)
            currentIcon.setBorder(null);
        currentIcon = icon;
        icon.setBorder(lineBorder);
        popMenu.setVisible(false);
        popMenu.revalidate();
        return icon;
    }

    public void actionPerformed(ActionEvent e) {
        String strCommand = e.getActionCommand();
        if (strCommand.equals(strMIAddBaseStation)) {
            if (simulator.getConfig().getAllBSConfig().size() > 0)
                return;
            JLabel icon = addBaseStation(currentX, currentY);
            BaseStationConfig bsConfig = new BaseStationConfig();
            bsConfig.setBSPosX(currentX);
            bsConfig.setBSPosY(currentY);
            bsConfig.setBSMidX(currentX + currentIcon.getWidth() / 2);
            bsConfig.setBSMidY(currentY + currentIcon.getHeight() / 2);
            int bsIdx = simulator.getConfig().addBaseStation(bsConfig);
            icon.setToolTipText(String.valueOf(bsIdx));
        } else if (strCommand.equals(strMIAddSensorCluster)) {
            JLabel icon = addSensorCluster(currentX, currentY, SensorNodeConfig.ST_DISCHARGING);
            SensorNodeConfig slaveConfig = new SensorNodeConfig();
            slaveConfig.setState(SensorNodeConfig.ST_DISCHARGING);
            slaveConfig.setSlavePosX(currentX);
            slaveConfig.setSlavePosY(currentY);
            slaveConfig.setSlaveMidX(currentX + currentIcon.getWidth() / 2);
            slaveConfig.setSlaveMidY(currentY + currentIcon.getHeight() / 2);
            slaveConfig.setBatteryVolt(4.2);
            int slaveIdx = simulator.getConfig().addSensorNodeConfig(slaveConfig);
            icon.setToolTipText(String.valueOf(slaveIdx));
        } else if (strCommand.equals(strMIChangeDetails)) {
            int idx = Integer.valueOf(currentIcon.getToolTipText());
            SensorNodeConfig sensorConfig = simulator.getConfig().getSensorNodeConfig(idx);
            if (sensorConfig.getState() != SensorNodeConfig.ST_CHARGING) {
               JDialog jd = new JDialog(f, "Enter battery voltage", true);
               JTextField tf = new JTextField(String.valueOf(sensorConfig.getBatteryVolt()));
               jd.add(tf);
               jd.setBounds(100, 100, 300, 200);
               jd.setVisible(true);
               double bVolt = Double.parseDouble(tf.getText());
               sensorConfig.setBatteryVolt(bVolt);
               if (bVolt > Battery.batteryDownThreshold) {
                  simulator.getConfig().getSensorNodeConfig(idx).setState(SensorNodeConfig.ST_DISCHARGING);
                  currentIcon.setIcon(sensorClusterNormalImg);
               } else {
                  simulator.getConfig().getSensorNodeConfig(idx).setState(SensorNodeConfig.ST_DOWN);
                  currentIcon.setIcon(sensorClusterBatteryDownImg);
               }
               this.revalidate();
            }
        } else if (strCommand.equals(strMIChargeBattery)) {
            currentIcon.setIcon(sensorClusterBatteryChargingImg);
            this.revalidate();
            int idx = Integer.valueOf(currentIcon.getToolTipText());
            SensorNodeConfig sensorConfig = simulator.getConfig().getSensorNodeConfig(idx);
            sensorConfig.setState(SensorNodeConfig.ST_CHARGING);
            sensorConfig.setBatteryVolt(4.2);
        } else if (strCommand.equals(strMIStopCharging)) {
            currentIcon.setIcon(sensorClusterNormalImg);
            this.revalidate();
            int idx = Integer.valueOf(currentIcon.getToolTipText());
            SensorNodeConfig sensorConfig = simulator.getConfig().getSensorNodeConfig(idx);
            sensorConfig.setState(SensorNodeConfig.ST_DISCHARGING);
            sensorConfig.setBatteryVolt(Battery.batteryDownThreshold+1);
        } else if (strCommand.equals(strMIBatteryDown)) {
            currentIcon.setIcon(sensorClusterBatteryDownImg);
            this.revalidate();
            int idx = Integer.valueOf(currentIcon.getToolTipText());
            SensorNodeConfig sensorConfig = simulator.getConfig().getSensorNodeConfig(idx);
            sensorConfig.setState(SensorNodeConfig.ST_DOWN);
            sensorConfig.setBatteryVolt(Battery.batteryDownThreshold);
        } else if (strCommand.equals(strMISendData)) {
            int id = Integer.valueOf(currentIcon.getToolTipText());
            SensorNodeConfig sc = simulator.getConfig().getSensorNodeConfig(id);
            simulator.simulateDataTransmission(sc);
        } else if (strCommand.equals(strMILoadConfig)) {
            simulator.loadConfig();
            redrawConfig();
        } else if (strCommand.equals(strMISaveConfig)) {
            simulator.saveConfig();
        } else if (strCommand.equals(strMIStartSimulation)) {
            if (isTimerSet)
                return;
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    SensorNode sn = simulator.simulateNext();
                    SensorNodeConfig sensorConfig = sn.getSensorNodeConfig();
                    double bVolt = sensorConfig.getBatteryVolt();
                    if (bVolt > Battery.batteryDownThreshold) {
                        sensorConfig.setState(SensorNodeConfig.ST_DISCHARGING);
                        currentIcon.setIcon(sensorClusterNormalImg);
                     } else {
                        sensorConfig.setState(SensorNodeConfig.ST_DOWN);
                        currentIcon.setIcon(sensorClusterBatteryDownImg);
                     }
                    repaint();
                }
            }, 0, simulator.getConfig().getDataSendFrequencyMillis());
            System.out.println("Started");
            isTimerSet = true;
        } else if (strCommand.equals(strMIStopSimulation)) {
            timer.cancel();
            isTimerSet = false;
            System.out.println("Stopped");
        }
        this.repaint();
    }

    private void redrawConfig() {
        for (Component c : this.getComponents()) {
            if (c instanceof JLabel) {
                JLabel l = (JLabel) c;
                Icon i = l.getIcon();
                if (i.equals(baseStationImg) || i.equals(sensorClusterNormalImg)
                        || i.equals(sensorClusterBatteryChargingImg) || i.equals(sensorClusterBatteryDownImg)) {
                    this.remove(c);
                }
            }
        }
        List<SensorNodeConfig> lstSlaveConfig = simulator.getConfig().getAllSlaveConfig();
        for (SensorNodeConfig sc : lstSlaveConfig) {
            JLabel label = addSensorCluster(sc.getSlavePosX(), sc.getSlavePosY(), sc.getState());
            label.setToolTipText(String.valueOf(lstSlaveConfig.indexOf(sc)));
        }
        List<BaseStationConfig> lstBSConfig = simulator.getConfig().getAllBSConfig();
        for (BaseStationConfig bsc : lstBSConfig) {
            JLabel label = addBaseStation(bsc.getBSPosX(), bsc.getBSPosY());
            label.setToolTipText(String.valueOf(lstBSConfig.indexOf(bsc)));
        }
    }

    public void mouseMoved(MouseEvent e) {
    }

    public void mouseDragged(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();
        if (currentIcon != null) {
            x += currentIcon.getX();
            y += currentIcon.getY();
            if (x >= 0 && x <= this.getWidth() && y >= 0 && y <= this.getHeight()) {
                currentIcon.setBounds(x, y, currentIcon.getWidth(), currentIcon.getHeight());
                if (currentIcon.getIcon().equals(baseStationImg)) {
                    int bsIdx = Integer.parseInt(currentIcon.getToolTipText());
                    BaseStationConfig bsConfig = simulator.getConfig().getAllBSConfig().get(bsIdx);
                    bsConfig.setBSPosX(x);
                    bsConfig.setBSPosY(y);
                    bsConfig.setBSMidX(x + currentIcon.getWidth() / 2);
                    bsConfig.setBSMidY(y + currentIcon.getHeight() / 2);
                } else {
                    int idx = Integer.parseInt(currentIcon.getToolTipText());
                    SensorNodeConfig slaveConfig = simulator.getConfig().getAllSlaveConfig().get(idx);
                    slaveConfig.setSlavePosX(x);
                    slaveConfig.setSlavePosY(y);
                    slaveConfig.setSlaveMidX(x + currentIcon.getWidth() / 2);
                    slaveConfig.setSlaveMidY(y + currentIcon.getHeight() / 2);
                }
            }
        }
    }

    public void mouseClicked(MouseEvent e) {
    }

    public void mousePressed(MouseEvent e) {
        if (e.getComponent() instanceof JLabel) {
            if (currentIcon != null) {
                currentIcon.setBorder(null);
            }
            currentIcon = (JLabel) e.getComponent();
            currentIcon.setBorder(lineBorder);
            Icon img = currentIcon.getIcon();
            if (/*e.isPopupTrigger() && */img != null && !img.equals(baseStationImg)) {
                popMenu.setVisible(true);
                popMenu.show(e.getComponent(), e.getX(), e.getY());
            }
        }
    }

    public void mouseReleased(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new SwingViewer());
    }

}
