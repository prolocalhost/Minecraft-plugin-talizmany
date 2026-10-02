package pl.localhost.talizmany.model;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum TalismanType {

    VAMPIRISM("vampirism", "Talizman Wampiryzmu"),
    PHOENIX("phoenix", "Talizman Feniksa"),
    THUNDER("thunder", "Talizman Gromu"),
    WIND_DASH("wind_dash", "Talizman Wiatru"),
    TITAN("titan", "Talizman Tytana");

    private final String key;
    private final String defaultName;

    TalismanType(String key, String defaultName) {
        this.key = key;
        this.defaultName = defaultName;
    }

    public String getKey() {
        return key;
    }

    public String getDefaultName() {
        return defaultName;
    }

    public static TalismanType fromKey(String input) {
        if (input == null) return null;
        String normalized = input.trim().toLowerCase().replace("-", "_");
        for (TalismanType type : values()) {
            if (type.key.equalsIgnoreCase(normalized) || type.name().equalsIgnoreCase(normalized)) {
                return type;
            }
        }
        return null;
    }

    public static List<String> getKeys() {
        return Arrays.stream(values())
                .map(TalismanType::getKey)
                .collect(Collectors.toList());
    }
}
