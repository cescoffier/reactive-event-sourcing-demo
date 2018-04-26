package me.escoffier.demo.rxes.dbz;

import io.reactivex.Completable;
import io.vertx.amqpbridge.AmqpBridge;
import io.vertx.amqpbridge.AmqpConstants;
import io.vertx.core.Future;
import io.vertx.core.eventbus.MessageProducer;
import io.vertx.core.json.JsonObject;
import io.vertx.kafka.client.producer.KafkaWriteStream;
import io.vertx.kafka.client.serialization.JsonObjectDeserializer;
import io.vertx.kafka.client.serialization.JsonObjectSerializer;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.core.impl.AsyncResultCompletable;
import io.vertx.reactivex.kafka.client.consumer.KafkaConsumer;
import io.vertx.reactivex.kafka.client.consumer.KafkaConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class DebeziumVerticle extends AbstractVerticle {


    private KafkaConsumer<JsonObject, JsonObject> consumer;
    private KafkaWriteStream<String, JsonObject> kafka;
    private MessageProducer<JsonObject> amqp;
    private AmqpBridge bridge;

    @Override
    public void start() {
        consumer = KafkaConsumer.create(vertx, getKafkaConsumerConfig());
        kafka = KafkaWriteStream.create(vertx.getDelegate(), getKafkaProducerConfig());
        bridge = AmqpBridge.create(vertx.getDelegate());

        vertx.createHttpServer()
            .requestHandler(req -> {
                if (req.path().equalsIgnoreCase("/dispatch")) {
                    System.out.println("Start the dispatching");
                    req.response().end("Start dispatching data...");
                    register();
                } else {
                    req.response().setStatusCode(400).end("Go Away!");
                }
            }).listen(8084);
    }

    private void register() {
        System.out.println("Starting AMQP bridge...");
        bridge.start("activemq", 5672, x -> {
            if (x.failed()) {
                x.cause().printStackTrace();
            } else {
                System.out.println("AMQP Bridge started");
                amqp = bridge.createProducer("health-data");
            }
        });



        System.out.println("Starting Kafka consumer...");
        consumer.toFlowable()
            .map(KafkaConsumerRecord::value)
            .map(json -> json.getJsonObject("payload"))
            .map(json -> json.getJsonObject("after"))
            .map(json -> json.mapTo(Measure.class))
            .flatMapCompletable(measure -> {
                Completable c1 = dispatchToKafka(measure);
                Completable c2 = dispatchToAmqp(measure);
                return Completable.mergeArray(c1, c2)
                    .doOnError(Throwable::printStackTrace);
            })
            .subscribe();

        consumer
            .subscribe("rxes.measures.measures");
    }

    private Completable dispatchToAmqp(Measure measure) {
        return new AsyncResultCompletable(
            handler -> {
                JsonObject payload = new JsonObject();
                payload.put(AmqpConstants.BODY, measure.toJson());
                amqp.send(payload);
                handler.handle(Future.succeededFuture());
            }
        );
    }

    private Completable dispatchToKafka(Measure measure) {
        ProducerRecord<String, JsonObject> record
            = new ProducerRecord<>("health",
            null, null, measure.getUser(), measure.toJson());
        return new AsyncResultCompletable(
            handler -> {
                kafka.write(record, x -> {
                    if (x.succeeded()) {
                        handler.handle(Future.succeededFuture());
                    } else {
                        handler.handle(Future.failedFuture(x.cause()));
                    }
                });
            });
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

    private Map<String, Object> getKafkaProducerConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("bootstrap.servers", "kafka:9092");
        config.put("key.serializer", StringSerializer.class.getName());
        config.put("value.serializer", JsonObjectSerializer.class.getName());
        return config;
    }
}
