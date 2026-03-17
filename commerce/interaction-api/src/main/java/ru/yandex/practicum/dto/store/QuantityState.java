package ru.yandex.practicum.dto.store;

public enum QuantityState {
    ENDED,
    FEW,
    ENOUGH,
    MANY;

    public static QuantityState fromQuantity(Integer quantity) {
        if (quantity == null) {
            return ENDED;
        }
        if (quantity > 100) {
            return MANY;
        } else if (quantity > 10) {
            return ENOUGH;
        } else if (quantity > 0) {
            return FEW;
        } else {
            return ENDED;
        }
    }
}
