package me.escoffier.demo.alert;

import io.vertx.reactivex.core.Vertx;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class Main {

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();

        vertx.deployVerticle(AlertDispatcherVerticle.class.getName());
        vertx.deployVerticle(MeasureAnalyserVerticle.class.getName());
    }
}
