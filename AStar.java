import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;

public class AStar {

    // An inner class to represent a single tile (or node) on our grid
    static class Node implements Comparable<Node> {
        int x, y; // Grid coordinates (not pixels)
        Node parent; // The node we came from to get here
        int gCost; // Cost from the starting node
        int hCost; // Heuristic cost (estimated distance) to the end node
        int fCost; // gCost + hCost

        Node(int x, int y) {
            this.x = x;
            this.y = y;
        }

        // The PriorityQueue will use this to sort nodes by their fCost
        @Override
        public int compareTo(Node other) {
            return Integer.compare(this.fCost, other.fCost);
        }
    }

    /**
     * Finds the shortest path from a start to an end point using the A* algorithm.
     * @param grid A 2D boolean array where 'true' represents a wall.
     * @param startX The starting column.
     * @param startY The starting row.
     * @param endX The target column.
     * @param endY The target row.
     * @return A List of Nodes representing the path, or null if no path is found.
     */
    public static List<Node> findPath(boolean[][] grid, int startX, int startY, int endX, int endY) {
        int width = grid[0].length;
        int height = grid.length;

        // openList contains nodes to be evaluated, sorted by their fCost
        PriorityQueue<Node> openList = new PriorityQueue<>();
        // closedList contains nodes that have already been evaluated
        HashSet<Node> closedList = new HashSet<>();
        
        Node startNode = new Node(startX, startY);
        Node endNode = new Node(endX, endY);

        openList.add(startNode);

        while (!openList.isEmpty()) {
            Node currentNode = openList.poll(); // Get the node with the lowest fCost
            closedList.add(currentNode);

            // Path found!
            if (currentNode.x == endNode.x && currentNode.y == endNode.y) {
                return retracePath(currentNode);
            }

            // Check all neighbors (up, down, left, right)
            for (int i = -1; i <= 1; i++) {
                for (int j = -1; j <= 1; j++) {
                    if (Math.abs(i) == Math.abs(j)) continue; // Skip diagonals

                    int neighborX = currentNode.x + i;
                    int neighborY = currentNode.y + j;

                    // Check if neighbor is valid
                    if (neighborX < 0 || neighborX >= width || neighborY < 0 || neighborY >= height) continue;
                    if (grid[neighborY][neighborX]) continue; // It's a wall

                    Node neighbor = new Node(neighborX, neighborY);
                    if (isNodeInSet(closedList, neighbor)) continue;

                    int newGCost = currentNode.gCost + 1;

                    if (newGCost < neighbor.gCost || !isNodeInQueue(openList, neighbor)) {
                        neighbor.gCost = newGCost;
                        neighbor.hCost = calculateHeuristic(neighbor, endNode);
                        neighbor.fCost = neighbor.gCost + neighbor.hCost;
                        neighbor.parent = currentNode;

                        if (!isNodeInQueue(openList, neighbor)) {
                            openList.add(neighbor);
                        }
                    }
                }
            }
        }
        return null; // No path found
    }

    // Helper method to trace the path back from the end node to the start
    private static List<Node> retracePath(Node endNode) {
        List<Node> path = new ArrayList<>();
        Node currentNode = endNode;
        while (currentNode != null) {
            path.add(currentNode);
            currentNode = currentNode.parent;
        }
        // Reverse the path to get it from start to end
        java.util.Collections.reverse(path);
        return path;
    }

    // Heuristic calculation (Manhattan distance)
    private static int calculateHeuristic(Node a, Node b) {
        return Math.abs(a.x - b.x) + Math.abs(a.y - b.y);
    }
    
    // Helper methods to check if a node exists in a collection (since we don't override equals/hashCode)
    private static boolean isNodeInQueue(PriorityQueue<Node> queue, Node node) {
        for (Node n : queue) if (n.x == node.x && n.y == node.y) return true;
        return false;
    }

    private static boolean isNodeInSet(HashSet<Node> set, Node node) {
        for (Node n : set) if (n.x == node.x && n.y == node.y) return true;
        return false;
    }
}
