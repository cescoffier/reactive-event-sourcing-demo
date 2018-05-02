package me.escoffier.demo.alert;

import me.escoffier.fluid.framework.Fluid;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class Main {

    public static void main(String[] args) {
        Fluid fluid = Fluid.create();
        fluid.deploy(new AlertDispatcher(fluid.vertx()))
            .deploy(TemperatureAlert.class)
            .deploy(GlucoseAlert.class)
            .deploy(BloodPressureAlert.class);
    }
}
