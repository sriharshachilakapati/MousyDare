package com.shc.ld31;

import com.radirius.mercury.math.geometry.Rectangle;
import com.radirius.mercury.scene.BasicEntity;
import com.radirius.mercury.scene.GameObject;

import java.util.ArrayList;
import java.util.List;

/**
 * A Grid based collision resolver. Reduces the number of collision
 * checks and increases performance. This class implements the broad
 * phase collision detection.
 *
 * @author Sri Harsha Chilakapati
 */
public class Grid
{
    // A spatial partitioned structure to hold elements
    private List<List<List<GameObject>>> grid;

    // Private stuff, self explanatory
    private int rows;
    private int cols;
    private int cellSize;

    // A list of short-listed entities
    private List<GameObject> retrieveList;

    /**
     * Creates and initializes the Grid
     *
     * @param mapWidth  The width of map (in pixels)
     * @param mapHeight The height of map (in pixels)
     * @param cellSize  The size of each cell (must be square)
     */
    public Grid(int mapWidth, int mapHeight, int cellSize)
    {
        this.cellSize = cellSize;

        // Calculate rows and columns
        rows = (mapHeight + cellSize - 1) / cellSize;
        cols = (mapWidth + cellSize - 1) / cellSize;

        // Create the grid
        grid = new ArrayList<>();

        // Create the cells
        for (int i = 0; i < cols; i++)
        {
            grid.add(new ArrayList<List<GameObject>>());

            for (int j = 0; j < rows; j++)
            {
                grid.get(i).add(new ArrayList<GameObject>());
            }
        }

        // Create the retrieve list
        retrieveList = new ArrayList<>();

        clear();
    }

    /**
     * Clears the Grid, removes all entities
     */
    public void clear()
    {
        for (int i = 0; i < cols; i++)
        {
            for (int j = 0; j < rows; j++)
                grid.get(i).get(j).clear();
        }
    }

    /**
     * Simplification method to insert bulk data
     *
     * @param list The list of GameObjects
     */
    public void insertAll(List<GameObject> list)
    {
        for (GameObject e : list)
            insert(e);
    }

    /**
     * Inserts an entity into the grid, by partitioning the available space
     * @param entity The entity to be added to the grid.
     */
    public void insert(GameObject entity)
    {
        Rectangle bounds = ((BasicEntity) (entity)).getBounds();

        // Forgot to set limits to boundaries!!
        int topLeftX = Math.max(0, (int) (bounds.getX()) / cellSize);
        int topLeftY = Math.max(0, (int) (bounds.getY()) / cellSize);
        int bottomRightX = Math.min(cols - 1, (int) (bounds.getX() + bounds.getWidth() - 1) / cellSize);
        int bottomRightY = Math.min(rows - 1, (int) (bounds.getY() + bounds.getHeight() - 1) / cellSize);

        for (int x = topLeftX; x <= bottomRightX; x++)
        {
            for (int y = topLeftY; y <= bottomRightY; y++)
            {
                grid.get(x).get(y).add(entity);
            }
        }
    }

    public List<GameObject> retrieve(GameObject e)
    {
        retrieveList.clear();

        Rectangle bounds = ((BasicEntity) (e)).getBounds();

        int topLeftX = Math.max(0, (int) (bounds.getX()) / cellSize);
        int topLeftY = Math.max(0, (int) (bounds.getY()) / cellSize);
        int bottomRightX = Math.min(cols - 1, (int) (bounds.getX() + bounds.getWidth() - 1) / cellSize);
        int bottomRightY = Math.min(rows - 1, (int) (bounds.getY() + bounds.getHeight() - 1) / cellSize);

        for (int x = topLeftX; x <= bottomRightX; x++)
        {
            for (int y = topLeftY; y <= bottomRightY; y++)
            {
                List<GameObject> cell = grid.get(x).get(y);

                for (GameObject retrieved : cell)
                {
                    if (retrieved != e && !retrieveList.contains(retrieved))
                        retrieveList.add(retrieved);
                }
            }
        }

        return retrieveList;
    }

    public void remove(GameObject e)
    {
        Rectangle bounds = ((BasicEntity) (e)).getBounds();

        int topLeftX = Math.max(0, (int) (bounds.getX()) / cellSize);
        int topLeftY = Math.max(0, (int) (bounds.getY()) / cellSize);
        int bottomRightX = Math.min(cols - 1, (int) (bounds.getX() + bounds.getWidth() - 1) / cellSize);
        int bottomRightY = Math.min(rows - 1, (int) (bounds.getY() + bounds.getHeight() - 1) / cellSize);

        for (int x = topLeftX; x <= bottomRightX; x++)
        {
            for (int y = topLeftY; y <= bottomRightY; y++)
            {
                if (grid.get(x).get(y).contains(e))
                    grid.get(x).get(y).remove(e);
            }
        }
    }
}
