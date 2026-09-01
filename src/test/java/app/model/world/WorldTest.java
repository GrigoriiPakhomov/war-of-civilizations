package app.model.world;

import app.model.cell.Cell;
import app.model.civilization.Civilization;
import app.model.civilization.Strategy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.awt.Color;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Тесты игрового мира.
 */
class WorldTest {

    @Test
    @DisplayName("Мир возвращает нейтральные клетки на пустой карте")
    void testShouldReturnNeutralCells() {
        World world = new World(5, 0.0, 0.0);

        List<Cell> neutralCells = world.getNeutralCells();

        assertThat(neutralCells).isNotEmpty();
        assertThat(neutralCells).allMatch(Cell::isNeutral);
    }

    @Test
    @DisplayName("Добавление цивилизации обновляет её размер в мире")
    void testShouldUpdateCivilizationSizeAfterAdd() {
        World world = new World(5, 0.0, 0.0);
        Civilization civilization = new Civilization("Alpha", Color.RED, Strategy.RANDOM, world, world.getCell(2, 2));

        world.addCivilization(civilization);

        assertThat(world.getCivilizationSize("Alpha")).isEqualTo(1);
    }

    @Test
    @DisplayName("Удаление цивилизации очищает запись о её размере")
    void testShouldRemoveCivilizationSizeAfterDelete() {
        World world = new World(5, 0.0, 0.0);
        Civilization civilization = new Civilization("Beta", Color.BLUE, Strategy.RANDOM, world, world.getCell(1, 1));
        world.addCivilization(civilization);

        world.removeCivilization(civilization);

        assertThat(world.getCivilizationSize("Beta")).isZero();
    }

    @Test
    @DisplayName("Имя эволюции увеличивается последовательно для корневого имени")
    void testShouldGenerateSequentialEvolutionNames() {
        World world = new World(5, 0.0, 0.0);

        assertThat(world.nextEvolutionName("Alpha")).isEqualTo("Alpha-1");
        assertThat(world.nextEvolutionName("Alpha-1")).isEqualTo("Alpha-2");
        assertThat(world.nextEvolutionName("Alpha-2")).isEqualTo("Alpha-3");
    }
}