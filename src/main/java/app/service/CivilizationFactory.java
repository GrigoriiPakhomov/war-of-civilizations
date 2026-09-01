package app.service;

import app.model.cell.Cell;
import app.model.civilization.Civilization;
import app.model.civilization.Strategy;
import app.model.world.World;

import java.util.List;
import java.util.Random;

/**
 * Фабрика создания стартовых цивилизаций.
 */
public class CivilizationFactory {
    private final Random random = new Random();

    /**
     * Создаёт стартовую цивилизацию по индексу.
     *
     * @param index порядковый номер стартовой цивилизации
     * @param world игровой мир
     * @param neutralCells доступные нейтральные клетки
     * @return созданная цивилизация
     */
    public Civilization createInitialCivilization(int index, World world, List<Cell> neutralCells) {
        Strategy[] strategies = Strategy.values();
        Strategy strategy = strategies[random.nextInt(strategies.length)];
        int cellIndex = random.nextInt(neutralCells.size());
        Cell startCell = neutralCells.remove(cellIndex);

        String name = SimulationConfig.START_NAMES.get(index % SimulationConfig.START_NAMES.size());
        java.awt.Color color = SimulationConfig.START_COLORS.get(index % SimulationConfig.START_COLORS.size());

        return new Civilization(name, color, strategy, world, startCell);
    }
}
