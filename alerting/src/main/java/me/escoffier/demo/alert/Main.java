package me.escoffier.demo.alert;

import me.escoffier.fluid.framework.Fluid;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class Main {

    public static void main(String[] args) {
        Fluid fluid = new Fluid();
        fluid.deploy(new AlertDispatcher(fluid.vertx()));
        fluid.deploy(TemperatureAlert.class);
        fluid.deploy(GlucoseAlert.class);
        fluid.deploy(BloodPressureAlert.class);
    }
}
