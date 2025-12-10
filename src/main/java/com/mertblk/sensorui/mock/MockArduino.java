package com.mertblk.sensorui.mock;

import com.fazecast.jSerialComm.SerialPort;
import java.io.OutputStream;
import java.util.Random;

public class MockArduino {

    private static final String MOCK_PORT_NAME = "COM3";
    private static final int BAUD_RATE = 9600;

    public static void main(String[] args) {
        SerialPort mockPort = SerialPort.getCommPort(MOCK_PORT_NAME);
        mockPort.setBaudRate(BAUD_RATE);

        if (!mockPort.openPort()) {
            System.err.println("Error: " + MOCK_PORT_NAME + "cannot opened");
            return;
        }

        System.out.println("Mock Arduino was started. " + "Sending data to " + MOCK_PORT_NAME);

        try (OutputStream outputStream = mockPort.getOutputStream()) {
            Random random = new Random();
            while (true) {
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

                outputStream.write(dataLine.getBytes());
                outputStream.flush();

                System.out.print("Data was sent: " + dataLine);
                Thread.sleep(2000);
            }
        } catch (Exception e) {
            System.err.println("There is an error: " + e.getMessage());
        } finally {
            mockPort.closePort();
            System.out.println("Port closed.");
        }
    }
}
