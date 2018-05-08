package me.escoffier.demo.web;

import io.vertx.amqpbridge.AmqpConstants;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.bridge.PermittedOptions;
import io.vertx.ext.web.handler.sockjs.BridgeOptions;
import io.vertx.reactivex.amqpbridge.AmqpBridge;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.core.eventbus.Message;
import io.vertx.reactivex.core.eventbus.MessageConsumer;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.ext.web.RoutingContext;
import io.vertx.reactivex.ext.web.handler.StaticHandler;
import io.vertx.reactivex.ext.web.handler.sockjs.SockJSHandler;

import java.util.concurrent.TimeUnit;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("Starting web application");
        Vertx vertx = Vertx.vertx();
        Router router = Router.router(vertx);
        SockJSHandler sockjs = getSockJsHandler(vertx);
        router.get("/init").handler(rc -> init(vertx, rc));
        router.get("/eventbus/*").handler(sockjs);
        router.get("/*").handler(StaticHandler.create());

        vertx.createHttpServer()
            .requestHandler(router::accept)
            .listen(8080);
    }

    private static void init(Vertx vertx, RoutingContext rc) {
        AmqpBridge bridge = AmqpBridge.create(vertx);

        // Start the bridge, then use the event loop thread to process things thereafter.
        bridge.start("broker-amq-amqp", 5672, "admin", "admin", res -> {
            if (!res.succeeded()) {
                System.out.println("Bridge startup failed: " + res.cause());
                rc.response().setStatusCode(500).end(res.cause().getMessage());
                return;
            } else {
                System.out.println("Web App AMQP Bridge started");
                rc.response().end("Web app initialized");
            }

            // Set up a consumer using the bridge, register a handler for it.
            MessageConsumer<JsonObject> consumer = bridge
                .createConsumer("health-data");
            consumer.toFlowable()
                .map(Message::body)
                .sample(50, TimeUnit.MILLISECONDS)
                .forEach(json -> {
                    JsonObject payload = json.getJsonObject(AmqpConstants.BODY);
                    vertx.eventBus().publish("health", payload);
                });
        });
    }

    private static SockJSHandler getSockJsHandler(Vertx vertx) {
        BridgeOptions options = new BridgeOptions()
            .addInboundPermitted(new PermittedOptions().setAddress("health"))
            .addOutboundPermitted(new PermittedOptions().setAddress("health"));
        return SockJSHandler.create(vertx).bridge(options);
    }
}
