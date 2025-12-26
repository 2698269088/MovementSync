package xin.bbtt.Block;

import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.stream.Collectors;

public record BlockState(String blockName, int stateId, Map<String, String> properties) {

    public String getProperty(String key) {
        return properties.get(key);
    }

    @Override
    public @NotNull String toString() {
        String props = properties.entrySet().stream()
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.joining(","));

        return String.format("%s[%s] (id=%d)", blockName, props, stateId);
    }
}