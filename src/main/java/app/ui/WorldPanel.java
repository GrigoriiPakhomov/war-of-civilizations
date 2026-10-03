package app.ui;

import app.model.cell.Cell;
import app.model.cell.CellType;
import app.model.civilization.Civilization;
import app.model.world.World;
import app.service.SimulationConfig;

import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.HashMap;
import java.util.Map;

/**
 * Swing-панель для отрисовки карты мира в реальном времени.
 */
public class WorldPanel extends JPanel {
    private static final Color GRID_COLOR = new Color(40, 40, 40);
    private static final Color WALL_COLOR = Color.DARK_GRAY;
    private static final Color WATER_COLOR = new Color(30, 100, 220);
    private static final Color EMPTY_COLOR = new Color(245, 245, 245);
    private static final Color MEAL_COLOR = new Color(80, 170, 80);

    private final World world;

    /**
     * Создаёт панель визуализации мира.
     *
     * @param world игровой мир
     */
    public WorldPanel(World world) {
        this.world = world;
        int size = world.getSize() * SimulationConfig.CELL_SIZE;
        setPreferredSize(new Dimension(size, size));
        setBackground(Color.WHITE);
    }

    /**
     * Отрисовывает карту мира.
     *
     * @param graphics графический контекст
     */
    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g2 = (Graphics2D) graphics.create();
        g2.setStroke(new BasicStroke(1f));

        Map<String, Color> colorMap = new HashMap<>();
        for (Civilization civilization : world.getCivilizations()) {
            colorMap.put(civilization.getCivilizationName(), civilization.getColor());
        }

        for (int x = 0; x < world.getSize(); x++) {
            for (int y = 0; y < world.getSize(); y++) {
                Cell cell = world.getGrid()[x][y];
                g2.setColor(resolveCellColor(cell, colorMap));
                g2.fillRect(x * SimulationConfig.CELL_SIZE, y * SimulationConfig.CELL_SIZE,
                        SimulationConfig.CELL_SIZE, SimulationConfig.CELL_SIZE);
                g2.setColor(GRID_COLOR);
                g2.drawRect(x * SimulationConfig.CELL_SIZE, y * SimulationConfig.CELL_SIZE,
                        SimulationConfig.CELL_SIZE, SimulationConfig.CELL_SIZE);
            }
        }
        g2.dispose();
    }

    private Color resolveCellColor(Cell cell, Map<String, Color> colorMap) {
        if (cell.getType() == CellType.WATER) {
            return WATER_COLOR;
        }
        if (cell.getType() == CellType.WALL) {
            return WALL_COLOR;
        }
        if (cell.getCivilizationName() != null) {
            return colorMap.getOrDefault(cell.getCivilizationName(), Color.LIGHT_GRAY);
        }
        if (cell.getType() == CellType.MEAL) {
            return MEAL_COLOR;
        }
        return EMPTY_COLOR;
    }
}
