package me.escoffier.demo.alert;

import com.twilio.Twilio;
import com.twilio.type.PhoneNumber;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.core.buffer.Buffer;
import me.escoffier.fluid.annotations.Function;
import me.escoffier.fluid.annotations.Inbound;
import me.escoffier.fluid.annotations.Transformation;

import java.util.concurrent.TimeUnit;

import static io.reactivex.schedulers.Schedulers.io;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class AlertDispatcher {


    private final Vertx vertx;
    private PhoneNumber sender;
    private PhoneNumber to;

    public AlertDispatcher(Vertx vertx) {
        this.vertx = vertx;
    }

    @Transformation
    public void sendToTwilio(@Inbound("alerts") Flowable<JsonObject> alerts) {
        alerts
            .sample(1, TimeUnit.MINUTES)
            .doOnNext(json -> System.out.println("ALERT!!!! " + json.encode()))
            .map(json -> "Patient: " + json.getString("patient") + "\n" + json.getString("alert"))
            .flatMapSingle(this::initTwilio)
            .observeOn(io())
            .flatMapCompletable(this::sendTextMessage)
            .subscribe();
    }

    private Completable sendTextMessage(String txt) {
        return Completable.fromAction(() -> {
            com.twilio.rest.api.v2010.account.Message.creator(to, sender, txt).create();
            System.out.println("Alert sent!");
        });
    }

    private Single<String> initTwilio(String txt) {
        Single<String> initTwilio;
        if (sender == null && to == null) {
            initTwilio = vertx.fileSystem()
                .rxReadFile("secrets.json")
                .map(Buffer::toJsonObject)
                .doOnSuccess(json -> {
                    Twilio.init(json.getString("account"), json.getString("token"));
                    sender = new PhoneNumber(json.getString("sender"));
                    to = new PhoneNumber(json.getString("to"));
                })
                .toCompletable()
                .toSingleDefault(txt);
        } else {
            initTwilio = Single.just(txt);
        }
        return initTwilio;
    }
}
