package app.model.cell;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 * Клетка игрового мира с координатами, типом и владельцем.
 */
@Getter
@ToString
@RequiredArgsConstructor
@EqualsAndHashCode(of = {"x", "y"})
public class Cell {
    private final int x;
    private final int y;
    private final CellType type;
    private String civilizationName;

    /**
     * Проверяет, является ли клетка проходимой.
     *
     * @return true, если клетка не является водой или стеной
     */
    public boolean isPassable() {
        return type != CellType.WATER && type != CellType.WALL;
    }

    /**
     * Проверяет, является ли клетка нейтральной.
     *
     * @return true, если клетка проходима и не имеет владельца
     */
    public boolean isNeutral() {
        return isPassable() && civilizationName == null;
    }

    /**
     * Назначает клетке владельца.
     *
     * @param ownerName имя цивилизации-владельца
     */
    public synchronized void capture(String ownerName) {
        if (isPassable()) {
            this.civilizationName = ownerName;
        }
    }

    /**
     * Освобождает клетку.
     */
    public synchronized void liberate() {
        if (isPassable()) {
            this.civilizationName = null;
        }
    }

    /**
     * Переназначает владельца клетки без дополнительных проверок сценария.
     * Используется при эволюции.
     *
     * @param ownerName новое имя владельца
     */
    public synchronized void reassignOwner(String ownerName) {
        this.civilizationName = ownerName;
    }
}