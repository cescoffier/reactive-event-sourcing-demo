package me.escoffier.demo.alert;

import io.reactivex.Maybe;
import io.reactivex.flowables.ConnectableFlowable;
import io.reactivex.flowables.GroupedFlowable;
import io.vertx.core.json.JsonObject;
import io.vertx.kafka.client.serialization.JsonObjectDeserializer;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.kafka.client.consumer.KafkaConsumer;
import io.vertx.reactivex.kafka.client.consumer.KafkaConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class MeasureAnalyserVerticle extends AbstractVerticle {

    private KafkaConsumer<String, JsonObject> consumer;

    @Override
    public void start() {
        // retrieve stuff from kafka
        consumer = KafkaConsumer.create(vertx, getKafkaConsumerConfig());

        System.out.println("Starting Kafka consumer...");

        ConnectableFlowable<JsonObject> values = consumer.toFlowable().map(KafkaConsumerRecord::value).publish();

        // Check temperature
        values
            .flatMapMaybe(json -> {
                String patient = json.getString("patient");
                double temp = json.getDouble("temperature");

                if (temp >= 38) {
                    return Maybe.just(new JsonObject().put("patient", patient).put("alert",
                        "Temperature reached an abnormal level: " + temp));
                } else {
                    return Maybe.empty();
                }
            })
            .subscribe(
                alert -> vertx.eventBus().send("alerts", alert)
                // Do nothing is empty
            );

        values
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
            })
            .subscribe(
                alert -> vertx.eventBus().send("alerts", alert)
                // Do nothing is empty
            );

        values
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
            })
            .subscribe(
                alert -> vertx.eventBus().send("alerts", alert)
                // Do nothing is empty
            );
        
        values.connect();

        consumer.subscribe("health");
    }
    
    private Map<String, String> getKafkaConsumerConfig() {
        Map<String, String> config = new HashMap<>();
        config.put("bootstrap.servers", "kafka:9092");
        config.put("key.deserializer", StringDeserializer.class.getName());
        config.put("value.deserializer", JsonObjectDeserializer.class.getName());
        config.put("group.id", "vertx-client");
        config.put("auto.offset.reset", "earliest");
        config.put("enable.auto.commit", "false");
        return config;
    }
}
