# RescuePathFinder

## Pathfinding Visualizer

### Overview
**RescuePathFinder** is a Java application designed to visually demonstrate how various pathfinding algorithms work, including **A\***, **Dijkstra’s**, **Breadth-First Search (BFS)**, and **Depth-First Search (DFS)**. Users can interact with a grid-based interface by placing start/end points, generating obstacles, and viewing algorithmically determined paths in action.

### Key Features
- **Interactive Start/End Selection**: Simply click on the grid to define where the path should begin (blue) and end (red).
- **Random Wall Creation**: Add random barriers to increase complexity.
- **Safe Zone Tagging**: Right-click to highlight safe zones within the grid.
- **Supported Algorithms**:
  - A* (A-Star)
  - Dijkstra’s Algorithm
  - Breadth-First Search (BFS)
  - Depth-First Search (DFS)
- **SQL Database Connection**: Optionally log path data into a MySQL database.
- **Smooth Path Animation**: Watch the algorithm step through the grid in real-time.

### Getting Started

#### Requirements
- **Java Development Kit (JDK 17 or newer)**
- **A Java IDE** like IntelliJ IDEA
- **MySQL** (if database functionality is desired)

#### Clone the Project
```bash
git clone https://github.com/yourusername/Pathfinding-Visualizer.git
cd Pathfinding-Visualizer
```

#### Optional: Configure the Database
1. Set up a MySQL database called `pathfinder`.
2. Create a `paths` table using:
   ```sql
   CREATE TABLE paths (
       id INT AUTO_INCREMENT PRIMARY KEY,
       x INT NOT NULL,
       y INT NOT NULL
   );
   ```
3. Modify the database connection details in `PathfindingVisualizer.java`:
   ```java
   String url = "jdbc:mysql://localhost:3306/pathfinder";
   String user = "root";
   String password = "yourpassword";
   ```

#### Running the App
1. Launch the project in IntelliJ IDEA (or any Java-compatible IDE).
2. Execute the `PathfindingVisualizer` class.

### How to Use
1. Set start and end nodes with a click.
2. Use right-click to mark safe zones.
3. Click on “Generate Walls” for automatic obstacle placement.
4. Choose an algorithm from the list.
5. Hit “Find Path” to watch the path discovery process.

### Want to Contribute?
You're welcome to improve the project! Fork the repo, make your changes, and submit a pull request.

