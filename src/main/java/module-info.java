module ship.weather.routing {
    requires com.google.gson;
    requires com.sun.istack.runtime;
    requires org.json;
    requires commons.math3;
    requires jmetal.core;
    requires jmetal.algorithm;
    opens org.example.model to com.google.gson;
    exports org.example.util to com.google.gson;
}