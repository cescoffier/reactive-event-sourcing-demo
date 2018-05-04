package me.escoffier.demo.rxes;

import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.AbstractVerticle;
import me.escoffier.demo.rxes.measures.Patient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class PatientVerticle extends AbstractVerticle {
    private final Patient patient;
    private final Logger log;

    PatientVerticle(Patient patient) {
        this.log = LogManager.getLogger("Patient-" + patient.name());
        this.patient = patient;
    }

    @Override
    public void start() {
        vertx.setPeriodic(2000, x -> {
            JsonObject measure = patient.measure();
            log.info("Generating measures: {}", measure.encode());
            vertx.eventBus().send("measures", measure);
        });
    }
}
