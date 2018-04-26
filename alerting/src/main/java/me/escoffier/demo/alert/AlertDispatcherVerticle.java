package me.escoffier.demo.alert;

import com.twilio.Twilio;
import com.twilio.type.PhoneNumber;
import io.reactivex.Completable;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.CompletableHelper;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.core.buffer.Buffer;
import io.vertx.reactivex.core.eventbus.Message;
import io.vertx.reactivex.core.eventbus.MessageConsumer;

import java.util.concurrent.TimeUnit;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class AlertDispatcherVerticle extends AbstractVerticle {

    private PhoneNumber sender;
    private PhoneNumber to;

    @Override
    public void start(Future<Void> future) {

        Completable c1 = vertx.fileSystem()
            .rxReadFile("secrets.json")
            .map(Buffer::toJsonObject)
            .doOnSuccess(json -> {
                Twilio.init(json.getString("account"), json.getString("token"));
                sender = new PhoneNumber(json.getString("sender"));
                to = new PhoneNumber(json.getString("to"));
            })
            .toCompletable();


        MessageConsumer<JsonObject> consumer = vertx.eventBus().consumer("alerts");
        consumer.toFlowable()
            .map(Message::body)
            .sample(20, TimeUnit.SECONDS)
            .doOnNext(json -> System.out.println("ALERT!!!! " + json.encode()))
            .map(json -> "Patient: " + json.getString("patient") + "\n" + json.getString("alert"))
            .flatMapCompletable(this::sendTextMessage)
            .subscribe();

        Completable c2 = consumer.rxCompletionHandler();

        Completable.mergeArray(c1, c2)
            .doOnComplete(() -> System.out.println("Alert dispatcher ready to serve"))
            .subscribe(CompletableHelper.toObserver(future));
    }

    private Completable sendTextMessage(String txt) {
        return vertx.rxExecuteBlocking(future -> {
            com.twilio.rest.api.v2010.account.Message.creator(to, sender, txt).create();
            System.out.println("Alert sent!");
            future.complete();
        }).toCompletable();
    }
}
