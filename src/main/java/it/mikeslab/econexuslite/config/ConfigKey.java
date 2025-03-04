package it.mikeslab.econexuslite.config;

import it.mikeslab.commons.api.config.ConfigurableEnum;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ConfigKey implements ConfigurableEnum {
    DEBUG_MODE("settings.debug-mode", false),
    MONGO_LOGGING("settings.mongo-logging", false),
    ANIMATION_INTERVAL("settings.animation-interval", 2);

    private final String path;
    private final Object defaultValue;

}
