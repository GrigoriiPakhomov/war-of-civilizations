package app.model.world;

import app.model.cell.Cell;
import app.model.cell.CellType;
import app.model.civilization.Bonus;
import app.model.civilization.Civilization;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Игровой мир симуляции.
 */
@Getter
@Slf4j
public class World {
    private final int size;
    private final Cell[][] grid;
    private final List<Civilization> civilizations;
    private final Map<String, Integer> civilizationSizes;
    private final Map<String, Integer> evolutionCounters;
    private final List<String> logMessages;
    private volatile boolean gameOver;
    private String winner;
    private final int totalPassableCells;

    /**
     * Создаёт игровой мир указанного размера.
     *
     * @param size размер стороны карты
     * @param waterPercentage доля воды
     * @param wallPercentage доля стен
     */
    public World(int size, double waterPercentage, double wallPercentage) {
        this.size = size;
        this.grid = new Cell[size][size];
        this.civilizations = new CopyOnWriteArrayList<>();
        this.civilizationSizes = new ConcurrentHashMap<>();
        this.evolutionCounters = new ConcurrentHashMap<>();
        this.logMessages = Collections.synchronizedList(new ArrayList<>());
        initializeGrid(waterPercentage, wallPercentage);
        this.totalPassableCells = countPassableCells();
    }

    /**
     * Возвращает клетку по координатам.
     *
     * @param x координата X
     * @param y координата Y
     * @return клетка или null
     */
    public synchronized Cell getCell(int x, int y) {
        if (x >= 0 && x < size && y >= 0 && y < size) {
            return grid[x][y];
        }
        return null;
    }

    /**
     * Добавляет цивилизацию в мир.
     *
     * @param civilization новая цивилизация
     */
    public synchronized void addCivilization(Civilization civilization) {
        civilizations.add(civilization);
        civilizationSizes.put(civilization.getCivilizationName(), civilization.getTerritory().size());
        logMessage("Цивилизация " + civilization.getCivilizationName() + " родилась");
    }

    /**
     * Удаляет цивилизацию из мира.
     *
     * @param civilization удаляемая цивилизация
     */
    public synchronized void removeCivilization(Civilization civilization) {
        civilizations.remove(civilization);
        civilizationSizes.remove(civilization.getCivilizationName());
        checkGameOver();
    }

    /**
     * Возвращает размер цивилизации по имени.
     *
     * @param civilizationName имя цивилизации
     * @return количество клеток
     */
    public synchronized int getCivilizationSize(String civilizationName) {
        return civilizationSizes.getOrDefault(civilizationName, 0);
    }

    /**
     * Генерирует имя эволюционировавшей цивилизации на основе корневого имени.
     *
     * @param civilizationName текущее имя цивилизации
     * @return новое имя с порядковым номером
     */
    public synchronized String nextEvolutionName(String civilizationName) {
        String rootName = civilizationName.split("-")[0];
        int nextNumber = evolutionCounters.merge(rootName, 1, Integer::sum);
        return rootName + "-" + nextNumber;
    }

    /**
     * Логирует захват клетки.
     *
     * @param civilizationName имя цивилизации
     * @param cell захваченная клетка
     */
    public synchronized void logCapture(String civilizationName, Cell cell) {
        logMessage(civilizationName + " захватил клетку (" + cell.getX() + ", " + cell.getY() + ")");
        updateCivilizationSizes(civilizationName);
        checkGameOver();
    }

    /**
     * Логирует эволюцию цивилизации.
     *
     * @param parentName исходная цивилизация
     * @param childName новая цивилизация
     * @param size размер отделившейся части
     */
    public synchronized void logEvolution(String parentName, String childName, int size) {
        logMessage(parentName + " эволюционировала: создана " + childName + " (" + size + " клеток)");
    }

    /**
     * Логирует гибель цивилизации.
     *
     * @param civilizationName имя цивилизации
     */
    public synchronized void logDeath(String civilizationName) {
        logMessage(civilizationName + " погибла");
    }

    /**
     * Логирует получение бонуса.
     *
     * @param civilizationName имя цивилизации
     * @param bonus бонус
     */
    public synchronized void logBonus(String civilizationName, Bonus bonus) {
        logMessage(civilizationName + " получила бонус " + bonus);
    }

    /**
     * Возвращает список нейтральных клеток.
     *
     * @return нейтральные клетки
     */
    public synchronized List<Cell> getNeutralCells() {
        List<Cell> neutralCells = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                Cell cell = grid[i][j];
                if (cell.isNeutral()) {
                    neutralCells.add(cell);
                }
            }
        }
        return neutralCells;
    }

    /**
     * Выводит статистику мира в лог.
     */
    public void printStats() {
        log.info("=== СТАТИСТИКА ===");
        log.info("Всего клеток: {}", size * size);
        log.info("Проходимых клеток: {}", totalPassableCells);
        log.info("Цивилизаций: {}", civilizations.size());
        for (Civilization civilization : civilizations) {
            if (civilization.isCivilizationAlive()) {
                double percentage = totalPassableCells == 0
                        ? 0.0
                        : (double) civilization.getTerritory().size() / totalPassableCells * 100;
                log.info("{}: {} клеток ({}%), энергия: {}",
                        civilization.getCivilizationName(),
                        civilization.getTerritory().size(),
                        String.format("%.1f", percentage),
                        civilization.getEnergy());
            }
        }
    }

    /**
     * Проверяет, можно ли создать новую цивилизацию в результате эволюции.
     *
     * @param maxTotalCivs общий лимит цивилизаций
     * @return true, если лимит не превышен
     */
    public synchronized boolean canCreateEvolution(int maxTotalCivs) {
        return civilizations.size() < maxTotalCivs;
    }

    private void initializeGrid(double waterPercentage, double wallPercentage) {
        Random random = new Random();
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                double rand = random.nextDouble();
                CellType type;
                if (rand < waterPercentage) {
                    type = CellType.WATER;
                } else if (rand < waterPercentage + wallPercentage) {
                    type = CellType.WALL;
                } else if (rand < waterPercentage + wallPercentage + 0.15) {
                    type = CellType.MEAL;
                } else {
                    type = CellType.EMPTY;
                }
                grid[i][j] = new Cell(i, j, type);
            }
        }
    }

    private int countPassableCells() {
        int count = 0;
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (grid[i][j].isPassable()) {
                    count++;
                }
            }
        }
        return count;
    }

    private void updateCivilizationSizes(String civilizationName) {
        civilizations.stream()
                .filter(civ -> civ.getCivilizationName().equals(civilizationName))
                .findFirst()
                .ifPresent(civ -> civilizationSizes.put(civilizationName, civ.getTerritory().size()));
    }

    private void checkGameOver() {
        if (gameOver) {
            return;
        }

        if (totalPassableCells == 0) {
            gameOver = true;
            winner = "Ничья";
            logMessage("Нет проходимых клеток. Игра завершена вничью");
            return;
        }

        for (Civilization civilization : civilizations) {
            if (civilization.isCivilizationAlive()) {
                double percentage = (double) civilization.getTerritory().size() / totalPassableCells * 100;
                if (percentage > 90) {
                    gameOver = true;
                    winner = civilization.getCivilizationName();
                    logMessage("ПОБЕДА: " + winner + " контролирует " + String.format("%.1f", percentage) + "% территории");
                    return;
                }
            }
        }

        boolean allDead = civilizations.isEmpty() || civilizations.stream().allMatch(c -> !c.isCivilizationAlive());
        if (allDead) {
            gameOver = true;
            winner = "Ничья";
            logMessage("Все цивилизации погибли. Ничья");
        }
    }

    private void logMessage(String message) {
        String timestamped = "[" + System.currentTimeMillis() + "] " + message;
        logMessages.add(timestamped);
        log.info(timestamped);
    }
}
