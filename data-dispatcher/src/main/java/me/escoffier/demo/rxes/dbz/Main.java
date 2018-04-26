package me.escoffier.demo.rxes.dbz;

import io.vertx.reactivex.core.Vertx;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class Main {

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();

        vertx.deployVerticle(DebeziumVerticle.class.getName());
    }
}
