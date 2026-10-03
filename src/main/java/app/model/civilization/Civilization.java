package app.model.civilization;

import app.model.cell.Cell;
import app.model.cell.CellType;
import app.model.world.World;
import app.service.SimulationConfig;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.awt.Color;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Поток-цивилизация, который самостоятельно выполняет захваты и эволюцию.
 */
@Getter
@Slf4j
public class Civilization extends Thread {
    private final String civilizationName;
    private final Color color;
    private final Strategy strategy;
    private final World world;
    private int energy;
    private final Set<Cell> territory;
    private final AtomicBoolean active;
    private final Random random;
    private volatile boolean civilizationAlive;
    private Bonus activeBonus;
    private int bonusTurnsLeft;
    private int evolutionCooldown;

    /**
     * Создаёт цивилизацию.
     *
     * @param civilizationName имя цивилизации
     * @param color цвет цивилизации
     * @param strategy стратегия поведения
     * @param world игровой мир
     * @param startCell стартовая клетка
     */
    public Civilization(String civilizationName, Color color, Strategy strategy, World world, Cell startCell) {
        this.civilizationName = civilizationName;
        this.color = color;
        this.strategy = strategy;
        this.world = world;
        this.energy = 100;
        this.territory = Collections.synchronizedSet(new HashSet<>());
        this.active = new AtomicBoolean(true);
        this.random = new Random(ThreadLocalRandom.current().nextLong());
        this.civilizationAlive = true;
        this.activeBonus = null;
        this.bonusTurnsLeft = 0;
        this.evolutionCooldown = 0;

        if (startCell != null && startCell.isPassable()) {
            startCell.capture(civilizationName);
            territory.add(startCell);
        }

        setName("civilization-" + civilizationName);
    }

    /**
     * Проверяет, жива ли цивилизация как игровая сущность.
     *
     * @return true, если цивилизация активна
     */
    public boolean isCivilizationAlive() {
        return civilizationAlive;
    }

    /**
     * Возвращает соседние доступные клетки для захвата.
     *
     * @return список подходящих соседних клеток
     */
    public List<Cell> getNeighborCells() {
        List<Cell> neighbors = new ArrayList<>();
        synchronized (territory) {
            for (Cell cell : territory) {
                checkAndAddNeighbor(neighbors, cell.getX() + 1, cell.getY());
                checkAndAddNeighbor(neighbors, cell.getX() - 1, cell.getY());
                checkAndAddNeighbor(neighbors, cell.getX(), cell.getY() + 1);
                checkAndAddNeighbor(neighbors, cell.getX(), cell.getY() - 1);
            }
        }
        return neighbors;
    }

    /**
     * Основной цикл жизни цивилизации.
     */
    @Override
    public void run() {
        while (active.get() && civilizationAlive && !Thread.currentThread().isInterrupted()) {
            try {
                Thread.sleep(nextDelay());

                if (Thread.currentThread().isInterrupted() || !civilizationAlive) {
                    break;
                }

                makeMove();

                if (evolutionCooldown > 0) {
                    evolutionCooldown--;
                }

                if (territory.size() >= SimulationConfig.EVOLUTION_MIN_COMPONENT_SIZE) {
                    checkAndPerformEvolution();
                }

                if (energy <= 0 && random.nextInt(100) < 1) {
                    die();
                }

                updateBonuses();

                if (random.nextBoolean()) {
                    energy += random.nextInt(10) + 1;
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                log.error("Ошибка в потоке цивилизации {}", civilizationName, e);
            }
        }

        active.set(false);
    }

    private int nextDelay() {
        int delay = 100 + random.nextInt(401);
        if (activeBonus == Bonus.SPEED_UP) {
            delay = Math.max(50, delay / 2);
        }
        return delay;
    }

    private void makeMove() {
        if (energy < 20 || !civilizationAlive || Thread.currentThread().isInterrupted()) {
            return;
        }

        synchronized (world) {
            Cell target = chooseTarget();
            if (target == null || !target.isPassable()) {
                return;
            }

            int cost = activeBonus == Bonus.COST_REDUCTION ? Math.max(1, (int) (20 * 0.1)) : 20;
            if (energy < cost) {
                return;
            }

            captureCell(target, cost);

            if (activeBonus == Bonus.EXTRA_RANGE) {
                Cell extraTarget = chooseTarget();
                if (extraTarget != null && extraTarget.isPassable() && energy >= cost) {
                    captureCell(extraTarget, cost);
                }
            }
        }
    }

    private void captureCell(Cell target, int cost) {
        energy -= cost;
        target.capture(civilizationName);
        territory.add(target);

        if (target.getType() == CellType.MEAL) {
            energy += 10;
        }

        energy = Math.max(0, energy - 1);
        world.logCapture(civilizationName, target);
    }

    private Cell chooseTarget() {
        List<Cell> neighbors = getNeighborCells();
        if (neighbors.isEmpty()) {
            return null;
        }

        return switch (strategy) {
            case NEUTRAL_FIRST -> chooseNeutralFirst(neighbors);
            case WEAK_FIRST -> chooseWeakFirst(neighbors);
            case RANDOM -> neighbors.get(random.nextInt(neighbors.size()));
        };
    }

    private void checkAndAddNeighbor(List<Cell> neighbors, int x, int y) {
        Cell cell = world.getCell(x, y);
        if (cell != null
                && cell.isPassable()
                && !civilizationName.equals(cell.getCivilizationName())
                && !territory.contains(cell)) {
            neighbors.add(cell);
        }
    }

    private Cell chooseNeutralFirst(List<Cell> neighbors) {
        List<Cell> neutralCells = neighbors.stream().filter(Cell::isNeutral).toList();
        if (!neutralCells.isEmpty()) {
            return neutralCells.get(random.nextInt(neutralCells.size()));
        }
        return neighbors.get(random.nextInt(neighbors.size()));
    }

    private Cell chooseWeakFirst(List<Cell> neighbors) {
        Map<String, Integer> civilizationSizes = new HashMap<>();

        for (Cell neighbor : neighbors) {
            String owner = neighbor.getCivilizationName();
            if (owner != null && !owner.equals(civilizationName)) {
                civilizationSizes.put(owner, world.getCivilizationSize(owner));
            }
        }

        if (civilizationSizes.isEmpty()) {
            return chooseNeutralFirst(neighbors);
        }

        String weakestCivilization = civilizationSizes.entrySet().stream()
                .min(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);

        if (weakestCivilization != null) {
            List<Cell> weakCells = neighbors.stream()
                    .filter(cell -> weakestCivilization.equals(cell.getCivilizationName()))
                    .toList();

            if (!weakCells.isEmpty()) {
                return weakCells.get(random.nextInt(weakCells.size()));
            }
        }

        return neighbors.get(random.nextInt(neighbors.size()));
    }

    private void checkAndPerformEvolution() {
        if (evolutionCooldown > 0 || !world.canCreateEvolution(SimulationConfig.MAX_TOTAL_CIVILIZATIONS)) {
            return;
        }

        synchronized (world) {
            List<Set<Cell>> components = findConnectedComponents();
            if (components.size() <= 1) {
                return;
            }

            Set<Cell> smallestComponent = components.stream()
                    .min(Comparator.comparingInt(Set::size))
                    .orElse(null);

            if (smallestComponent == null || smallestComponent.size() < SimulationConfig.EVOLUTION_MIN_COMPONENT_SIZE) {
                return;
            }

            int remainingSize = territory.size() - smallestComponent.size();
            if (remainingSize < SimulationConfig.EVOLUTION_MIN_COMPONENT_SIZE) {
                return;
            }

            String newName = world.nextEvolutionName(civilizationName);
            Color newColor = new Color(random.nextInt(256), random.nextInt(256), random.nextInt(256));
            Cell startCell = smallestComponent.iterator().next();
            Civilization newCivilization = new Civilization(newName, newColor, strategy, world, startCell);

            for (Cell cell : smallestComponent) {
                territory.remove(cell);
                cell.reassignOwner(newName);
                newCivilization.getTerritory().add(cell);
            }

            evolutionCooldown = SimulationConfig.EVOLUTION_COOLDOWN_TURNS;
            newCivilization.evolutionCooldown = SimulationConfig.EVOLUTION_COOLDOWN_TURNS;

            world.addCivilization(newCivilization);
            world.logEvolution(civilizationName, newName, smallestComponent.size());
            newCivilization.start();
        }
    }

    private List<Set<Cell>> findConnectedComponents() {
        Set<Cell> visited = new HashSet<>();
        List<Set<Cell>> components = new ArrayList<>();

        synchronized (territory) {
            for (Cell start : territory) {
                if (!visited.contains(start)) {
                    Set<Cell> component = new HashSet<>();
                    Queue<Cell> queue = new LinkedList<>();
                    queue.add(start);
                    visited.add(start);

                    while (!queue.isEmpty()) {
                        Cell current = queue.poll();
                        component.add(current);

                        checkAndAddToComponent(queue, visited, current.getX() + 1, current.getY());
                        checkAndAddToComponent(queue, visited, current.getX() - 1, current.getY());
                        checkAndAddToComponent(queue, visited, current.getX(), current.getY() + 1);
                        checkAndAddToComponent(queue, visited, current.getX(), current.getY() - 1);
                    }

                    components.add(component);
                }
            }
        }

        return components;
    }

    private void checkAndAddToComponent(Queue<Cell> queue, Set<Cell> visited, int x, int y) {
        Cell cell = world.getCell(x, y);
        if (cell != null && territory.contains(cell) && !visited.contains(cell)) {
            visited.add(cell);
            queue.add(cell);
        }
    }

    private void die() {
        synchronized (world) {
            active.set(false);
            civilizationAlive = false;

            synchronized (territory) {
                for (Cell cell : territory) {
                    cell.liberate();
                }
                territory.clear();
            }

            world.logDeath(civilizationName);
            world.removeCivilization(this);
        }
    }

    private void updateBonuses() {
        if (activeBonus != null) {
            bonusTurnsLeft--;
            if (bonusTurnsLeft <= 0) {
                activeBonus = null;
                bonusTurnsLeft = 0;
            }
        }

        if (activeBonus == null && random.nextInt(100) < 5) {
            Bonus[] bonuses = Bonus.values();
            activeBonus = bonuses[random.nextInt(bonuses.length)];
            bonusTurnsLeft = 3;
            world.logBonus(civilizationName, activeBonus);
        }
    }
}