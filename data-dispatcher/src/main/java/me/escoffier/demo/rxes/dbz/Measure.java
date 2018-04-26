package me.escoffier.demo.rxes.dbz;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.vertx.core.json.JsonObject;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class Measure {

    private String name;

    private long id;

    private double systolic;

    private double diastolic;

    private double temperature;

    private double glucose;

    private String timestamp;

    public String getUser() {
        return name;
    }

    public void setUser(String name) {
        this.name = name;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public double getSystolic() {
        return systolic;
    }

    public void setSystolic(double systolic) {
        this.systolic = systolic;
    }

    public double getDiastolic() {
        return diastolic;
    }

    public void setDiastolic(double diastolic) {
        this.diastolic = diastolic;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public double getGlucose() {
        return glucose;
    }

    public void setGlucose(double glucose) {
        this.glucose = glucose;
    }

    public JsonObject toJson() {
        return new JsonObject()
            .put("patient", name)
            .put("systolic", systolic)
            .put("diastolic", diastolic)
            .put("temperature", temperature)
            .put("glucose", glucose)
            .put("timestamp", timestamp);
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
