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
public class GlucoseAlert {


    @Transformation
    @Outbound("alerts")
    public Flowable<JsonObject> detect(@Inbound("health") Flowable<JsonObject> measures) {
        return measures
            .flatMapMaybe(json -> {
                String patient = json.getString("patient");
                double glucose = json.getDouble("glucose");
                if (glucose <= 85) {
                    return Maybe.just(new JsonObject().put("patient", patient).put("alert",
                        "Not enough glucose in blood: " + glucose));
                } else if (glucose >= 130) {
                    return Maybe.just(new JsonObject().put("patient", patient).put("alert",
                        "Too much glucose in blood: " + glucose));
                } else {
                    return Maybe.empty();
                }
            });

    }
}
