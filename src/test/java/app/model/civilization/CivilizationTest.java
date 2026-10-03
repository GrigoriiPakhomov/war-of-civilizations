package app.model.civilization;

import app.model.cell.Cell;
import app.model.world.World;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.awt.Color;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Тесты поведения цивилизации.
 */
class CivilizationTest {

    @Test
    @DisplayName("Стартовая клетка захватывается при создании цивилизации")
    void testShouldCaptureStartCellOnCreation() {
        World world = new World(5, 0.0, 0.0);
        Cell start = world.getCell(2, 2);

        Civilization civilization = new Civilization("Alpha", Color.RED, Strategy.NEUTRAL_FIRST, world, start);

        assertThat(start.getCivilizationName()).isEqualTo("Alpha");
        assertThat(civilization.getTerritory()).contains(start);
    }

    @Test
    @DisplayName("Начальная энергия цивилизации равна 100")
    void testShouldHaveInitialEnergyHundred() {
        World world = new World(5, 0.0, 0.0);
        Civilization civilization = new Civilization("Beta", Color.BLUE, Strategy.RANDOM, world, world.getCell(1, 1));

        assertThat(civilization.getEnergy()).isEqualTo(100);
    }

    @Test
    @DisplayName("Соседи цивилизации содержат только проходимые клетки")
    void testShouldReturnOnlyPassableNeighbors() {
        World world = new World(7, 0.0, 0.0);
        Civilization civilization = new Civilization("Gamma", Color.GREEN, Strategy.NEUTRAL_FIRST, world, world.getCell(3, 3));

        List<Cell> neighbors = civilization.getNeighborCells();

        assertThat(neighbors).isNotEmpty();
        assertThat(neighbors).allMatch(Cell::isPassable);
    }

    @Test
    @DisplayName("Собственная клетка не попадает в список соседей")
    void testShouldNotIncludeOwnCellInNeighbors() {
        World world = new World(5, 0.0, 0.0);
        Cell start = world.getCell(2, 2);
        Civilization civilization = new Civilization("Delta", Color.ORANGE, Strategy.RANDOM, world, start);

        assertThat(civilization.getNeighborCells()).doesNotContain(start);
    }
}