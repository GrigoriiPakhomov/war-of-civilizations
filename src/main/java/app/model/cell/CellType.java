package app.model.cell;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Тип клетки игрового мира.
 */

@Getter
@RequiredArgsConstructor
public enum CellType {
    EMPTY("Пустая"),
    MEAL("Еда"),
    WATER("Вода"),
    WALL("Стена");

    private final String description;

//    CellType(String description) {
//        this.description = description;
//    }
}
