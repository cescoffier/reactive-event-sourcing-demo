package me.escoffier.demo.rxes.dbz;

import io.reactivex.Flowable;
import io.vertx.core.json.JsonObject;
import me.escoffier.fluid.annotations.Inbound;
import me.escoffier.fluid.annotations.Outbound;
import me.escoffier.fluid.annotations.Transformation;

import java.util.UUID;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class AlertBranchMediator {

    @Transformation
    @Outbound("health")
    public Flowable<JsonObject> process(@Inbound("measures") Flowable<JsonObject> measures) {
        return measures.map(json -> json
            .put("correlation-id", UUID.randomUUID().toString()));
    }

}
