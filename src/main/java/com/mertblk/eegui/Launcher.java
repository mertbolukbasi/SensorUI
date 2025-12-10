package com.mertblk.eegui;

import com.mertblk.eegui.view.MainView;
import javafx.application.Application;

public class Launcher {
    public static void main(String[] args) {
        // jSerialComm'un socat tarafından oluşturulan sanal portları görmesini sağla
        System.setProperty("jSerialComm.pseudoPorts", "true");
        
        Application.launch(MainView.class, args);
    }
}
