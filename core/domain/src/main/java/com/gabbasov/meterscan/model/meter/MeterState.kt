package com.gabbasov.meterscan.model.meter

enum class MeterState {
    NOT_REQUIRED,          // Не нужно снимать показания
    REQUIRED,              // Нужно снимать показания
    SUBMITTED_TO_SERVER,   // Показания сняты и отправлены на сервер
    SAVED_LOCALLY          // Показания сняты и сохранены локально
}
