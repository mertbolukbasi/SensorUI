module com.mertblk.eegui {
    requires javafx.controls;
    requires MaterialFX;
    requires com.fazecast.jSerialComm;
    requires javafx.fxml;
    requires java.sql;

    opens com.mertblk.eegui.view to javafx.graphics;
    opens com.mertblk.eegui.model to javafx.base;

    exports com.mertblk.eegui;
}
