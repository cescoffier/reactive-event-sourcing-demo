package me.escoffier.demo.rxes.dbz;

import io.reactivex.Completable;
import io.vertx.amqpbridge.AmqpBridgeOptions;
import io.vertx.amqpbridge.AmqpConstants;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.amqpbridge.AmqpBridge;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.core.eventbus.Message;
import io.vertx.reactivex.core.eventbus.MessageConsumer;
import me.escoffier.fluid.framework.Fluid;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class Main {

    private static final Logger LOGGER = LogManager.getLogger("Data-Dispatcher");

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();

        // Because we need to wait until debezium is fully initialized, we start the dispatcher afterwards.
        vertx.createHttpServer()
            .requestHandler(req -> {
                if (req.path().equalsIgnoreCase("/dispatch")) {
                    startFluidMediation(vertx)
                        .subscribe(
                            () -> {
                                LOGGER.info("Data dispatcher started...");
                                req.response().end("Dispatcher started");
                            },
                            err -> {
                                LOGGER.error("Data dispatcher failed to start", err);
                                req.response().setStatusCode(500).end(err.getMessage());
                            }
                        );
                } else {
                    req.response().setStatusCode(400).end("Go Away!");
                }
            }).listen(8084);
    }

    private static Completable startFluidMediation(Vertx vertx) {
        Fluid fluid = Fluid.create(vertx);
        return setupAmqpBridge(vertx)
            .doOnComplete(() -> fluid.deploy(CDCEventReceiver.class))
            .doOnComplete(() -> fluid.deploy(AlertBranchMediator.class));
    }

    private static Completable setupAmqpBridge(Vertx vertx) {
        AmqpBridge bridge = AmqpBridge.create(vertx);
        LOGGER.info("Starting AMQP bridge...");
        return bridge.rxStart("broker-amq-amqp", 5672, "admin", "admin")
            .map(ab -> ab.createProducer("health-data"))
            .flatMapCompletable(producer -> {
                MessageConsumer<JsonObject> data = vertx.eventBus().consumer("measures");
                data.toFlowable()
                    .map(Message::body)
                    .forEach(json -> {
                        JsonObject payload = new JsonObject();
                        payload.put(AmqpConstants.BODY, json);
                        producer.send(payload);
                    });
                return data.rxCompletionHandler();
            })
            .doOnComplete(() -> LOGGER.info("AMQP bridge started"));
    }
}
