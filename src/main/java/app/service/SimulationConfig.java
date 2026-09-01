package app.service;

import lombok.Getter;

import java.awt.Color;
import java.util.List;

/**
 * Конфигурация симуляции с основными ограничениями и стартовыми данными.
 */
@Getter
public final class SimulationConfig {
    public static final int MAX_SIMULATION_TIME_MS = 120_000;
    public static final int MIN_WORLD_SIZE = 5;
    public static final int MAX_WORLD_SIZE = 100;
    public static final int MIN_CIVILIZATIONS = 1;
    public static final int MAX_CIVILIZATIONS = 8;
    public static final int MONITOR_INTERVAL_MS = 1_000;
    public static final int STATS_INTERVAL_MS = 5_000;
    public static final int JOIN_TIMEOUT_MS = 200;
    public static final int MAX_TOTAL_CIVILIZATIONS = 64;
    public static final int EVOLUTION_MIN_COMPONENT_SIZE = 2;
    public static final int EVOLUTION_COOLDOWN_TURNS = 5;
    public static final int CELL_SIZE = 22;

    public static final List<String> START_NAMES = List.of(
            "Alpha", "Beta", "Gamma", "Delta",
            "Epsilon", "Zeta", "Eta", "Theta"
    );

    public static final List<Color> START_COLORS = List.of(
            Color.RED,
            Color.BLUE,
            Color.GREEN,
            Color.ORANGE,
            Color.MAGENTA,
            Color.CYAN,
            Color.PINK,
            Color.YELLOW
    );

    private SimulationConfig() {
    }
}
