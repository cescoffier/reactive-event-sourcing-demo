package me.escoffier.demo.alert;

import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.vertx.core.json.JsonObject;
import me.escoffier.fluid.annotations.Inbound;
import me.escoffier.fluid.annotations.Outbound;
import me.escoffier.fluid.annotations.Transformation;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class TemperatureAlert {


    @Transformation
    @Outbound("alerts")
    public Flowable<JsonObject> detect(@Inbound("health") Flowable<JsonObject> measures) {
        return measures
            .flatMapMaybe(json -> {
                String patient = json.getString("patient");
                double temp = json.getDouble("temperature");

                if (temp >= 38) {
                    return Maybe.just(new JsonObject().put("patient", patient).put("alert",
                        "Temperature reached an abnormal level: " + temp));
                } else {
                    return Maybe.empty();
                }
            });

    }
}
