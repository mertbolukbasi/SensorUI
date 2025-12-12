package com.mertblk.sensorui.mock;

import com.fazecast.jSerialComm.SerialPort;
import java.io.OutputStream;
import java.util.Locale;
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
            Thread.sleep(2000);

            Random random = new Random();
            while (true) {
                String dummy = "DUMMY";
                float temp = 20 + random.nextFloat() * 10;
                int fire = random.nextInt(2);
                float hum = random.nextFloat() * 100;
                int light = random.nextInt(2);
                int s1 = random.nextInt(2);
                int s2 = random.nextInt(2);
                int sa = random.nextInt(2);

                String dataLine = String.format(
                        Locale.US,
                        "%s,%.1f,%d,%.1f,%d,%d,%d,%d\n",
                        dummy, temp, fire, hum, light, s1, s2, sa
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