module com.mertblk.eegui {
    requires javafx.controls;
    requires javafx.fxml;

    requires MaterialFX;
    requires com.fazecast.jSerialComm;

    opens com.mertblk.eegui to javafx.fxml;
    exports com.mertblk.eegui;
}