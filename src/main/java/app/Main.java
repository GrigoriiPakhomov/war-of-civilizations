package app;

import app.service.SimulationService;

/**
 * Точка входа в приложение симуляции войны цивилизаций.
 */
public class Main {

    public static void main(String[] args) {
        new SimulationService().startInteractive();
    }
}
