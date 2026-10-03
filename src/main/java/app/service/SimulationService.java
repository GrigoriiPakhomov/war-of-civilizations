package app.service;

import app.model.cell.Cell;
import app.model.civilization.Civilization;
import app.model.world.World;
import app.ui.SimulationFrame;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Scanner;

/**
 * Основной сервис запуска и управления симуляцией.
 */
@Slf4j
public class SimulationService {
    private final ConsoleInputService inputService = new ConsoleInputService();
    private final CivilizationFactory civilizationFactory = new CivilizationFactory();

    private long startTime;
    private volatile boolean simulationEnded;

    /**
     * Запускает интерактивную симуляцию с консольным вводом и графической визуализацией.
     */
    public void startInteractive() {
        simulationEnded = false;

        try (Scanner scanner = new Scanner(System.in)) {
            log.info("=== ВОЙНА ЦИВИЛИЗАЦИЙ ===");
            log.info("Симуляция захвата в чашке Петри");

            int size = inputService.readInt(scanner, "Введите размер карты (N): ",
                    SimulationConfig.MIN_WORLD_SIZE, SimulationConfig.MAX_WORLD_SIZE);

            double waterPercent;
            double wallPercent;
            while (true) {
                waterPercent = inputService.readPercent(scanner, "Введите процент воды (0-100): ");
                wallPercent = inputService.readPercent(scanner, "Введите процент стен (0-100): ");
                if (waterPercent + wallPercent < 1.0) {
                    break;
                }
                log.warn("Сумма процентов воды и стен должна быть меньше 100");
            }

            int numCivs = inputService.readInt(scanner, "Введите количество стартовых цивилизаций: ",
                    SimulationConfig.MIN_CIVILIZATIONS, SimulationConfig.MAX_CIVILIZATIONS);

            World world = new World(size, waterPercent, wallPercent);
            SimulationFrame frame = new SimulationFrame(world);
            frame.open();
            startTime = System.currentTimeMillis();

            List<Cell> neutralCells = world.getNeutralCells();
            if (neutralCells.size() < numCivs) {
                log.error("Недостаточно нейтральных клеток для размещения цивилизаций");
                frame.stopRendering();
                return;
            }

            initializeCivilizations(world, neutralCells, numCivs);

            log.info("Симуляция запущена. Нажмите Enter для остановки");
            log.info("Доступна графическая визуализация карты в отдельном окне");

            Thread monitorThread = new Thread(() -> monitorSimulation(world, frame), "simulation-monitor");
            monitorThread.setDaemon(true);
            monitorThread.start();

            scanner.nextLine();

            if (!world.isGameOver() && !simulationEnded) {
                log.info("Симуляция остановлена пользователем");
                simulationEnded = true;
                stopAllCivilizations(world);
                frame.stopRendering();
                printFinalStats(world);
            }
        } catch (Exception e) {
            log.error("Ошибка при запуске симуляции", e);
        }
    }

    /**
     * Создаёт и запускает стартовые цивилизации.
     *
     * @param world игровой мир
     * @param neutralCells доступные нейтральные клетки
     * @param numCivs количество цивилизаций
     */
    private void initializeCivilizations(World world, List<Cell> neutralCells, int numCivs) {
        for (int i = 0; i < numCivs; i++) {
            Civilization civilization = civilizationFactory.createInitialCivilization(i, world, neutralCells);
            world.addCivilization(civilization);
            civilization.start();
        }
    }

    /**
     * Следит за временем симуляции, победой и обновлением статистики.
     *
     * @param world игровой мир
     * @param frame окно визуализации
     */
    private void monitorSimulation(World world, SimulationFrame frame) {
        long lastStatsTime = System.currentTimeMillis();
        try {
            while (!simulationEnded) {
                Thread.sleep(SimulationConfig.MONITOR_INTERVAL_MS);
                long currentTime = System.currentTimeMillis();

                if (currentTime - startTime >= SimulationConfig.MAX_SIMULATION_TIME_MS) {
                    log.info("Время симуляции истекло");
                    simulationEnded = true;
                    stopAllCivilizations(world);
                    frame.stopRendering();
                    printFinalStats(world);
                    return;
                }

                if (world.isGameOver()) {
                    log.info("Симуляция завершена");
                    simulationEnded = true;
                    stopAllCivilizations(world);
                    frame.stopRendering();
                    printFinalStats(world);
                    return;
                }

                if (currentTime - lastStatsTime >= SimulationConfig.STATS_INTERVAL_MS) {
                    world.printStats();
                    lastStatsTime = currentTime;
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Мониторинг симуляции был прерван");
        }
    }

    /**
     * Останавливает все потоки цивилизаций.
     *
     * @param world игровой мир
     */
    private void stopAllCivilizations(World world) {
        for (Civilization civilization : world.getCivilizations()) {
            civilization.interrupt();
        }
        for (Civilization civilization : world.getCivilizations()) {
            try {
                civilization.join(SimulationConfig.JOIN_TIMEOUT_MS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("Ожидание завершения потоков было прервано");
                return;
            }
        }
    }

    /**
     * Выводит итоговую статистику симуляции.
     *
     * @param world игровой мир
     */
    private void printFinalStats(World world) {
        log.info("=== ФИНАЛЬНАЯ СТАТИСТИКА ===");
        log.info("Победитель: {}", world.getWinner() != null ? world.getWinner() : "не определён");
        log.info("Время симуляции: {} секунд", (System.currentTimeMillis() - startTime) / 1000);
        world.printStats();
        log.info("=== КОНЕЦ СИМУЛЯЦИИ ===");
    }
}
