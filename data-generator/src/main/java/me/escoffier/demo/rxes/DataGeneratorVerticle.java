package me.escoffier.demo.rxes;

import io.vertx.reactivex.core.AbstractVerticle;
import me.escoffier.demo.rxes.measures.BloodGlucoseGenerator;
import me.escoffier.demo.rxes.measures.BloodPressureGenerator;
import me.escoffier.demo.rxes.measures.BodyTemperatureGenerator;
import me.escoffier.demo.rxes.measures.Patient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class DataGeneratorVerticle extends AbstractVerticle {

    private static final Logger LOGGER = LogManager.getLogger("Data-Generator");

    @Override
    public void start() {
        createPatients();
    }

    private void createPatients() {
        LOGGER.info("Create patients");

        Patient marius = new Patient(
            "marius",
            new BloodPressureGenerator("pressure", 100, 140, 3, 60, 90, 3),
            new BloodGlucoseGenerator("glucose", 85, 140, 5),
            new BodyTemperatureGenerator("temperature", 36, 39, 0.5)
        );

        vertx.getDelegate()
            .deployVerticle(new PatientVerticle(marius));

        Patient clement = new Patient(
            "clement",
            new BloodPressureGenerator("pressure", 100, 170, 3, 60, 100, 3),
            new BloodGlucoseGenerator("glucose", 80, 140, 5),
            new BodyTemperatureGenerator("temperature", 36.4, 38, 0.3)
        );

        vertx.getDelegate()
            .deployVerticle(new PatientVerticle(clement));
    }
}
