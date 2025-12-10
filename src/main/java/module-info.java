module com.mertblk.sensorui {
    requires javafx.controls;
    requires MaterialFX;
    requires com.fazecast.jSerialComm;
    requires javafx.fxml;
    requires java.sql;

    opens com.mertblk.sensorui.view to javafx.graphics;
    opens com.mertblk.sensorui.model to javafx.base;

    exports com.mertblk.sensorui;
}
