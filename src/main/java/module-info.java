module com.mertblk.eegui {
    requires javafx.controls;
    requires MaterialFX;
    requires com.fazecast.jSerialComm;
    requires javafx.fxml;

    opens com.mertblk.eegui.view to javafx.graphics;

    exports com.mertblk.eegui;
}
