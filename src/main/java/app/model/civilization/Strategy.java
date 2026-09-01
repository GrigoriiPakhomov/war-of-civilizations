package app.model.civilization;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Стратегия выбора клетки для захвата.
 */
@Getter
@RequiredArgsConstructor
public enum Strategy {
    NEUTRAL_FIRST("Сначала нейтральные клетки"),
    WEAK_FIRST("Сначала слабые противники"),
    RANDOM("Случайный выбор");

    private final String description;
}