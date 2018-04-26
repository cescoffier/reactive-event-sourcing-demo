package me.escoffier.demo.rxes.measures;

import io.vertx.core.json.JsonObject;
import me.escoffier.demo.rxes.gen.BoundedRandomVariable;

import java.util.Objects;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class BodyTemperatureGenerator implements Generator {

    private final BoundedRandomVariable generator;
    private final String name;

    public BodyTemperatureGenerator(
        String name,
        double minValue,
        double maxValue,
        double stdDeviation
    ) {
        this.name = Objects.requireNonNull(name);
        this.generator = new BoundedRandomVariable(stdDeviation, minValue, maxValue);
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public JsonObject generate() {
        return new JsonObject()
            .put(NAME, name)
            .put(TIMESTAMP, System.currentTimeMillis())
            .put("value", generator.next());
    }
}
