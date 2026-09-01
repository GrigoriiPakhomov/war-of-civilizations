package app.model.cell;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * Клетка игрового мира с координатами, типом и владельцем.
 */
@Getter
@ToString
@EqualsAndHashCode(of = {"x", "y"})
public class Cell {
    private final int x;
    private final int y;
    private final CellType type;
    private String civilizationName;

    /**
     * Создаёт клетку мира.
     *
     * @param x координата X
     * @param y координата Y
     * @param type тип клетки
     */
    public Cell(int x, int y, CellType type) {
        this.x = x;
        this.y = y;
        this.type = type;
    }

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
