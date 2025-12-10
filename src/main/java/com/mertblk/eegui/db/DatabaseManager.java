package com.mertblk.eegui.db;

import com.mertblk.eegui.model.SensorData;
import com.mertblk.eegui.model.SensorReading;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {

    private static final String DB_URL = "jdbc:sqlite:sensor_data.db";
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static void initializeDatabase() {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {
            String sql = "CREATE TABLE IF NOT EXISTS sensor_readings (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "session_id TEXT NOT NULL," +
                    "timestamp TEXT NOT NULL," +
                    "elapsed_seconds REAL NOT NULL," +
                    "temperature REAL," +
                    "humidity INTEGER," +
                    "light INTEGER," +
                    "fire INTEGER," +
                    "s1 INTEGER," +
                    "s2 INTEGER," +
                    "sa INTEGER" +
                    ")";
            stmt.execute(sql);
        } catch (SQLException e) {
            System.err.println("Database initialization error: " + e.getMessage());
        }
    }

    public static void saveSensorData(String sessionId, double elapsedSeconds, SensorData data) {
        String sql = "INSERT INTO sensor_readings(session_id, timestamp, elapsed_seconds, temperature, humidity, light, fire, s1, s2, sa) " +
                "VALUES(?,?,?,?,?,?,?,?,?,?)";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, sessionId);
            pstmt.setString(2, LocalDateTime.now().format(formatter));
            pstmt.setDouble(3, elapsedSeconds);
            pstmt.setFloat(4, data.getTemperature());
            pstmt.setInt(5, data.getHumidity());
            pstmt.setInt(6, data.getLight());
            pstmt.setInt(7, data.getFire());
            pstmt.setInt(8, data.getS1());
            pstmt.setInt(9, data.getS2());
            pstmt.setInt(10, data.getSa());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error saving sensor data: ".concat(e.getMessage()));
        }
    }

    public static boolean doesSessionIdExist(String sessionId) {
        String sql = "SELECT 1 FROM sensor_readings WHERE session_id = ? LIMIT 1";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, sessionId);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("Error checking session ID: " + e.getMessage());
            return true;
        }
    }

    public static List<SensorReading> getSensorReadings(String filterSql, List<Object> params) {
        List<SensorReading> readings = new ArrayList<>();
        String sql = "SELECT * FROM sensor_readings " + filterSql;

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(i + 1, params.get(i));
            }

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                readings.add(new SensorReading(
                        rs.getInt("id"),
                        rs.getString("session_id"),
                        rs.getString("timestamp"),
                        rs.getDouble("elapsed_seconds"),
                        rs.getFloat("temperature"),
                        rs.getInt("humidity"),
                        rs.getInt("light"),
                        rs.getInt("fire"),
                        rs.getInt("s1"),
                        rs.getInt("s2"),
                        rs.getInt("sa")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error querying sensor data: " + e.getMessage());
        }
        return readings;
    }

    public static List<String> getDistinctSessionIds() {
        List<String> sessionIds = new ArrayList<>();
        String sql = "SELECT DISTINCT session_id FROM sensor_readings ORDER BY timestamp DESC";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                sessionIds.add(rs.getString("session_id"));
            }
        } catch (SQLException e) {
            System.err.println("Error getting session IDs: " + e.getMessage());
        }
        return sessionIds;
    }
}
