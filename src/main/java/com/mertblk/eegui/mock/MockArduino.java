package com.mertblk.eegui.mock;

import com.fazecast.jSerialComm.SerialPort;
import java.io.OutputStream;
import java.util.Random;

public class MockArduino {

    // socat komutunun çıktısındaki İLK port yolunu buraya yazın.
    private static final String MOCK_PORT_NAME = "/dev/pts/3";
    private static final int BAUD_RATE = 9600;

    public static void main(String[] args) {
        // jSerialComm'un socat tarafından oluşturulan sanal portları görmesini sağla
        System.setProperty("jSerialComm.pseudoPorts", "true");

        SerialPort mockPort = SerialPort.getCommPort(MOCK_PORT_NAME);
        mockPort.setBaudRate(BAUD_RATE);

        if (!mockPort.openPort()) {
            System.err.println("HATA: Mock port " + MOCK_PORT_NAME + " açılamadı.");
            System.err.println("Lütfen 'socat' komutunun başka bir terminalde çalıştığından ve port yolunun doğru olduğundan emin olun.");
            System.err.println("Ayrıca, kullanıcınızın 'uucp' grubunda olduğundan emin olun (sudo usermod -a -G uucp $USER).");
            return;
        }

        System.out.println("Mock Arduino başlatıldı. " + MOCK_PORT_NAME + " portuna veri gönderiliyor...");
        System.out.println("SensorUI uygulamasından diğer portu (örn: /dev/pts/4) dinleyin.");

        try (OutputStream outputStream = mockPort.getOutputStream()) {
            Random random = new Random();
            while (true) {
                // ViewModel'in beklediği formatta rastgele veri oluştur
                float temp = 20 + random.nextFloat() * 10;
                int hum = 40 + random.nextInt(21);
                int light = 200 + random.nextInt(401);
                int fire = random.nextInt(10) == 0 ? 1 : 0;
                int s1 = random.nextInt(100);
                int s2 = random.nextInt(100);
                int sa = random.nextInt(15) == 0 ? 1 : 0;

                String dataLine = String.format(
                    "TEMP:%.1f,HUM:%d,LIGHT:%d,FIRE:%d,S1:%d,S2:%d,SA:%d\n",
                    temp, hum, light, fire, s1, s2, sa
                );

                // Veriyi sanal porta yaz
                outputStream.write(dataLine.getBytes());
                outputStream.flush();

                System.out.print("Gönderildi: " + dataLine);

                // 2 saniyede bir veri gönder
                Thread.sleep(2000);
            }
        } catch (Exception e) {
            System.err.println("Mock Arduino'da bir hata oluştu: " + e.getMessage());
        } finally {
            mockPort.closePort();
            System.out.println("Mock port kapatıldı.");
        }
    }
}
