package me.escoffier.demo.rxes.dbz;

import io.vertx.core.json.JsonObject;
import me.escoffier.fluid.annotations.Function;
import me.escoffier.fluid.annotations.Inbound;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class CDCEventReceiver {

    @Function(outbound = "measures")
    public JsonObject transformCdcToMeasures(@Inbound("cdc") JsonObject cdc) {
        JsonObject payload = cdc.getJsonObject("payload");
        JsonObject after = payload.getJsonObject("after");
        return after.put("patient", after.getString("user"));
    }
}
