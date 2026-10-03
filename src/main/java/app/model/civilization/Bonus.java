package app.model.civilization;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Временный бонус цивилизации.
 */
@Getter
@RequiredArgsConstructor
public enum Bonus {
    SPEED_UP("Ускорение"),
    COST_REDUCTION("Снижение стоимости захвата"),
    EXTRA_RANGE("Дополнительный захват");

    private final String description;
}