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
public class BloodPressureAlert {


    @Transformation
    @Outbound("alerts")
    public Flowable<JsonObject> detect(@Inbound("health") Flowable<JsonObject> measures) {
        return measures
            .flatMapMaybe(json -> {
                String patient = json.getString("patient");
                double systolic = json.getDouble("systolic");
                double diastolic = json.getDouble("diastolic");
                if (diastolic <= 65  || diastolic >= 90  || systolic <= 110  || systolic >= 150) {
                    return Maybe.just(new JsonObject().put("patient", patient).put("alert",
                        "Blood pressure issue: " + systolic + " " + diastolic));
                } else {
                    return Maybe.empty();
                }
            });
    }
}
