import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.text.DecimalFormat;
import java.util.concurrent.locks.ReentrantLock;

public class Display extends JFrame implements Runnable {
    //Max weight of 255, with three colors of 255 values each leads to 3 rgb value per 1 weight
    private final int COLOR_INCREMENT = (255 * 3) / Floor.MAX_WEIGHT;
    private final int STATION_SIZE = 75;
    private static ReentrantLock lock = new ReentrantLock();
    private static boolean interruptFlag = false;
    private static Floor floor;
    private JPanel window, panelSolution, panelTry, labelsTop, labelsBottom;
    private JLabel solutionLabel, tryLabel, solutionAffinity, tryAffinity;
    private DrawPanel[][] solutionFloor = new DrawPanel[Floor.FLOOR_SIDE][Floor.FLOOR_SIDE];
    private DrawPanel[][] tryFloor = new DrawPanel[Floor.FLOOR_SIDE][Floor.FLOOR_SIDE];
    private DecimalFormat df = new DecimalFormat("#.###");

    public interface Drawable {
        void draw(Graphics g);
    }

    public class DrawPanel extends JPanel {
        private Drawable drawable;

        public DrawPanel() {}

        public void setDrawable(Drawable drawable) {
            this.drawable = drawable;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (drawable != null) {
                drawable.draw(g);
            }
        }
    }

    public class Rectangle implements Drawable {
        private int x, y;
        private JPanel surface;
        private Color color;
        public Rectangle(int x, int y, JPanel surface, Color color) {
            this.x = x;
            this.y = y;
            this.surface = surface;
            this.color = color;
        }
        @Override
        public void draw(Graphics g) {
            g.setColor(color);
            g.fillRect(x, y, surface.getWidth(), surface.getHeight());
        }
    }

    public Display(Floor f) {
        initComponents(f);
    }

    private void initComponents(Floor f) {
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        //Each box is 75x75, 50 across the top and bottom, one box space down the center
        this.setSize((Floor.FLOOR_SIDE * STATION_SIZE * 2) + STATION_SIZE, (Floor.FLOOR_SIDE * STATION_SIZE) + 100);

        window = new JPanel();
        window.setLayout(new BorderLayout());
        window.setSize(this.getWidth(), this.getHeight());

        labelsTop = new JPanel();
        labelsTop.setLayout(new BorderLayout());

        solutionLabel = new JLabel("Best Solution");
        solutionLabel.setFont(new Font("Arial", Font.BOLD, 20));
        solutionLabel.setBorder(new EmptyBorder(window.getHeight()/50, window.getHeight()/3, window.getHeight()/50, 0));
        tryLabel = new JLabel("Current Try");
        tryLabel.setFont(new Font("Arial", Font.BOLD, 20));
        tryLabel.setBorder(new EmptyBorder(window.getHeight()/50, 0, window.getHeight()/50, window.getHeight()/3));

        labelsTop.add(solutionLabel, BorderLayout.LINE_START);
        labelsTop.add(tryLabel, BorderLayout.LINE_END);
        window.add(labelsTop, BorderLayout.PAGE_START);

        labelsBottom = new JPanel();
        labelsBottom.setLayout(new BorderLayout());

        solutionAffinity = new JLabel("Affinity: " + df.format(f.getAffinity()));
        solutionAffinity.setFont(new Font("Arial", Font.PLAIN, 15));
        solutionAffinity.setBorder(new EmptyBorder(window.getHeight()/50, window.getHeight()/3, window.getHeight()/50, 0));
        tryAffinity = new JLabel("Affinity: 0.000");
        tryAffinity.setFont(new Font("Arial", Font.PLAIN, 15));
        tryAffinity.setBorder(new EmptyBorder(window.getHeight()/50, 0, window.getHeight()/50, window.getHeight()/3));

        labelsBottom.add(solutionAffinity, BorderLayout.LINE_START);
        labelsBottom.add(tryAffinity, BorderLayout.LINE_END);
        window.add(labelsBottom, BorderLayout.PAGE_END);
        window.add(labelsTop, BorderLayout.PAGE_START);

        panelSolution = new JPanel();
        panelTry = new JPanel();
        GridLayout gridFloor = new GridLayout(Floor.FLOOR_SIDE, Floor.FLOOR_SIDE, 1, 1);
        panelSolution.setLayout(gridFloor);
        panelSolution.setPreferredSize(new Dimension(Floor.FLOOR_SIDE * STATION_SIZE, Floor.FLOOR_SIDE * STATION_SIZE));
        panelSolution.setBackground(Color.BLACK);
        panelTry.setLayout(gridFloor);
        panelTry.setPreferredSize(new Dimension(Floor.FLOOR_SIDE * STATION_SIZE, Floor.FLOOR_SIDE * STATION_SIZE));
        panelTry.setBackground(Color.BLACK);
        for (int i = 0; i < Floor.FLOOR_SIDE; i++) {
            for (int j = 0; j < Floor.FLOOR_SIDE; j++) {
                DrawPanel panel = new DrawPanel();
                panel.setPreferredSize(new Dimension(STATION_SIZE, STATION_SIZE));
                solutionFloor[i][j] = panel;
                panelSolution.add(panel);
            }
        }
        for (int i = 0; i < Floor.FLOOR_SIDE; i++) {
            for (int j = 0; j < Floor.FLOOR_SIDE; j++) {
                DrawPanel panel = new DrawPanel();
                panel.setPreferredSize(new Dimension(STATION_SIZE, STATION_SIZE));
                tryFloor[i][j] = panel;
                panelTry.add(panel);
            }
        }
        window.add(panelSolution, BorderLayout.LINE_START);
        window.add(panelTry, BorderLayout.LINE_END);

        this.add(window);
        this.setLocationRelativeTo(null);
        this.setResizable(false);
    }

    private void draw(Floor floor, boolean isSolution) {
        if(floor != null) {
            Station[][] stations = floor.getStations();
            for (int i = 0; i < stations.length; i++) {
                for (int j = 0; j < stations[i].length; j++) {
                    if (isSolution) {
                        DrawPanel panel = solutionFloor[i][j];
                        panel.setDrawable(new Rectangle(0, 0, panel, getColor(stations[i][j])));
                    } else {
                        DrawPanel panel = tryFloor[i][j];
                        panel.setDrawable(new Rectangle(0, 0, panel, getColor(stations[i][j])));
                        tryAffinity.setText("Affinity: " + df.format(floor.getAffinity()));
                    }
                }
            }
            if(isSolution) {
                solutionAffinity.setText("Affinity: " + df.format(floor.getAffinity()));
            } else {
                tryAffinity.setText("Affinity: " + df.format(floor.getAffinity()));
            }
        }
    }

    private Color getColor(Station station) {
        if(station == null) {
            return new Color(255, 255, 255);
        }
        int weight = station.getWeight();
        int r = 255;
        int g = 255;
        int b = 255;
        for(int i = 0; i < weight; i++) {
            if(r > COLOR_INCREMENT) {
                r -= COLOR_INCREMENT;
            } else if (r != 0) {
                int decrement = COLOR_INCREMENT - r;
                r = 0;
                g -= decrement;
            } else {
                if(g > COLOR_INCREMENT) {
                    g -= COLOR_INCREMENT;
                } else if(g != 0) {
                    int decrement = COLOR_INCREMENT - g;
                    g = 0;
                    b -= decrement;
                } else {
                    if(b > COLOR_INCREMENT) {
                        b -= COLOR_INCREMENT;
                    } else {
                        b = 0;
                    }
                }
            }
        }
        return new Color(r, g, b);
    }

    public static void interrupt(Floor f) {
        for(;;) {
            if(lock.tryLock()) {
                interruptFlag = true;
                floor = new Floor(f);
                lock.unlock();
                break;
            }
        }
    }

    private boolean isInterrupted() {
        boolean ret;
        for(;;) {
            if(lock.tryLock()) {
                ret = interruptFlag;
                interruptFlag = false;
                lock.unlock();
                break;
            }
        }
        return ret;
    }

    @Override
    public void run() {
        this.setVisible(true);
        Main.setReady(true);
        for(;;) {
            try {
                if (isInterrupted()) {
                    draw(floor, true);
                }
                draw(Solution.getUncheckedFloor(), false);
                Thread.sleep(250);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
