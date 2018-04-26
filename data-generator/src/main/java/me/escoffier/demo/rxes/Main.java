package me.escoffier.demo.rxes;

import io.vertx.reactivex.core.Vertx;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class Main {

    public static void main(String[] args) {

        System.out.println("==== ENVIRONMENT ===");
        System.getenv().entrySet().forEach(e -> System.out.println("\t" + e.getKey() + "=" + e.getValue()));
        System.out.println("====================");

        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(DataGeneratorVerticle.class.getName());
        vertx.deployVerticle(DatabaseVerticle.class.getName());
    }

}
