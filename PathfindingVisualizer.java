import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.sql.*;
import java.util.List;
import javax.swing.Timer;

public class PathfindingVisualizer extends JPanel {
    private static final int GRID_SIZE = 20;
    private static final int CELL_SIZE = 30;
    private int[][] grid = new int[GRID_SIZE][GRID_SIZE];
    private Point start = null;
    private Point end = null;
    private boolean selectingStart = true;
    private Map<Point, Boolean> safeZones = new HashMap<>();

    public PathfindingVisualizer() {
        setPreferredSize(new Dimension(GRID_SIZE * CELL_SIZE, GRID_SIZE * CELL_SIZE));
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int x = e.getX() / CELL_SIZE;
                int y = e.getY() / CELL_SIZE;
                Point clickedPoint = new Point(x, y);
                if (SwingUtilities.isLeftMouseButton(e)) {
                    if (selectingStart) {
                        start = clickedPoint;
                        selectingStart = false;
                    } else {
                        end = clickedPoint;
                        selectingStart = true;
                    }
                } else if (SwingUtilities.isRightMouseButton(e)) {
                    safeZones.put(clickedPoint, true);
                }
                repaint();
            }
        });
    }

    public void generateWalls() {
        Random rand = new Random();
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                if (!safeZones.containsKey(new Point(i, j)) && !isStartOrEnd(i, j)) {
                    grid[i][j] = (rand.nextDouble() < 0.3) ? 1 : 0;
                }
            }
        }
        repaint();
    }

    private boolean isStartOrEnd(int x, int y) {
        return (start != null && start.x == x && start.y == y) || (end != null && end.x == x && end.y == y);
    }
    private Connection connectToDatabase() {
        String url = "jdbc:mysql://localhost:3306/pathfinder";
        String user = "root";
        String password = "mathur";
        try {
            return DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }




    private void storePathInDatabase(List<Point> path, String algorithm) {
        if (path == null || path.isEmpty()) {
            System.out.println("No path to store.");
            return;
        }

        // Ensure algorithm name is valid
        if (algorithm == null || algorithm.trim().isEmpty()) {
            System.err.println("Invalid algorithm name.");
            return;
        }

        // Fix: Ensure valid table name
        String tableName = "paths_" + algorithm.toLowerCase().replaceAll("[^a-zA-Z0-9_]", "");

        String createTableSQL = "CREATE TABLE IF NOT EXISTS " + tableName + " (id INT AUTO_INCREMENT PRIMARY KEY, x INT, y INT)";
        String insertSQL = "INSERT INTO " + tableName + " (x, y) VALUES (?, ?)";

        try (Connection conn = connectToDatabase();
             Statement createStmt = conn.createStatement();
             PreparedStatement stmt = conn.prepareStatement(insertSQL)) {

            // Create table if it does not exist
            createStmt.executeUpdate(createTableSQL);

            // Insert points into database
            for (Point p : path) {
                stmt.setInt(1, p.x);
                stmt.setInt(2, p.y);
                stmt.addBatch();
            }
            stmt.executeBatch();

            System.out.println("Path stored successfully in table " + tableName);
        } catch (SQLException e) {
            System.err.println("Error storing path in database: " + e.getMessage());
            e.printStackTrace();
        }
    }



    public void findAndDisplayPath(String algorithm) {
        if (start == null || end == null) {
            System.out.println("Please select both start and end points.");
            return;
        }
        resetPath();
        List<Point> path = switch (algorithm) {
            case "A*" -> AStar.findPath(grid, start, end);
            case "Dijkstra" -> Dijkstra.findPath(grid, start, end);
            case "BFS" -> BFS.findPath(grid, start, end);
            case "DFS" -> DFS.findPath(grid, start, end);
            default -> null;
        };
        if (path != null) {
            animatePath(path);
            storePathInDatabase(path, algorithm);
        }
    }

    private void animatePath(List<Point> path) {
        Iterator<Point> iterator = path.iterator();
        Timer timer = new Timer(100, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (iterator.hasNext()) {
                    Point p = iterator.next();
                    if (!isStartOrEnd(p.x, p.y)) {
                        grid[p.x][p.y] = 2;
                        SwingUtilities.invokeLater(() -> repaint(p.x * CELL_SIZE, p.y * CELL_SIZE, CELL_SIZE, CELL_SIZE));
                    }
                } else {
                    ((Timer) e.getSource()).stop();
                }
            }
        });
        timer.start();
    }

    private void resetPath() {
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                if (grid[i][j] == 2) {
                    grid[i][j] = 0;
                    repaint(i * CELL_SIZE, j * CELL_SIZE, CELL_SIZE, CELL_SIZE);
                }
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                if (grid[i][j] == 1) g.setColor(Color.BLACK);
                else if (grid[i][j] == 2) g.setColor(Color.YELLOW);
                else if (safeZones.containsKey(new Point(i, j))) g.setColor(Color.GREEN);
                else g.setColor(Color.WHITE);
                g.fillRect(i * CELL_SIZE, j * CELL_SIZE, CELL_SIZE, CELL_SIZE);
                g.setColor(Color.GRAY);
                g.drawRect(i * CELL_SIZE, j * CELL_SIZE, CELL_SIZE, CELL_SIZE);
            }
        }
        if (start != null) {
            g.setColor(Color.BLUE);
            g.fillRect(start.x * CELL_SIZE, start.y * CELL_SIZE, CELL_SIZE, CELL_SIZE);
        }
        if (end != null) {
            g.setColor(Color.RED);
            g.fillRect(end.x * CELL_SIZE, end.y * CELL_SIZE, CELL_SIZE, CELL_SIZE);
        }
    }
    public static void main(String[] args) {
        JFrame frame = new JFrame("Pathfinding Visualizer");
        PathfindingVisualizer panel = new PathfindingVisualizer();
        JComboBox<String> algorithmSelector = new JComboBox<>(new String[]{"A*", "Dijkstra", "BFS", "DFS"});
        JButton findPathButton = new JButton("Find Path");
        JButton generateWallsButton = new JButton("Generate Walls");

        findPathButton.addActionListener(e -> panel.findAndDisplayPath((String) algorithmSelector.getSelectedItem()));
        generateWallsButton.addActionListener(e -> panel.generateWalls());

        JPanel controlPanel = new JPanel();
        controlPanel.add(algorithmSelector);
        controlPanel.add(findPathButton);
        controlPanel.add(generateWallsButton);

        frame.setLayout(new BorderLayout());
        frame.add(controlPanel, BorderLayout.NORTH);
        frame.add(panel, BorderLayout.CENTER);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

}