package com.example.file_copy_2;

import java.io.File;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Arrays;

public class VideoScanner {
    public void final_scan() {
        String query = "SELECT DirName FROM Directories WHERE DirID = 4"; // Adjust SQL according to your schema
        try (Connection conn = this.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                String a = rs.getString("DirName");
                //System.out.println("Jazda z kurwami");
                //System.out.println(a);
                VideoScanner temp = new VideoScanner();
                temp.scanDirectoryForVideo(conn, a);//.replace("/", "\\"));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    private Connection connect() {
        // SQLite connection string
        String url = "jdbc:sqlite:baza_twoja_stara.db"; // Adjust for your database
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(url);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return conn;
    }

    public void insertvideo(Connection conn1, String videoName, String path, boolean copied, String videoDate, String videoTime) throws SQLException {
        String sql = "INSERT INTO Videos(VideoName, DirName, Copied, VideoDate, VideoTime) VALUES(?,?,?,?,?);";
        String sql1 = "SELECT COUNT(VideoName) FROM videos WHERE VideoName = ? AND VideoTime = ?";
        try (PreparedStatement pstmt1 = conn1.prepareStatement(sql1)) {
            pstmt1.setString(1, videoName);
            pstmt1.setString(2, videoTime);
            ResultSet rd = pstmt1.executeQuery();
            if(rd.getInt(1) == 0) {
                PreparedStatement pstmt = conn1.prepareStatement(sql);
                pstmt.setString(1, videoName);
                pstmt.setString(2, path);

                pstmt.setBoolean(3, copied);
                pstmt.setString(4, videoDate);
                pstmt.setString(5, videoTime);
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void scanDirectoryForVideo(Connection conn, String directoryPath) throws SQLException {
        File directory = new File(directoryPath);
        File[] files = directory.listFiles((dir, name) -> name.toLowerCase().endsWith(".mp4"));

        if (files != null) {
            Arrays.sort(files); // Sort files by name (natural order)

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");

            for (File file : files) {
                String videoDate = dateFormat.format(file.lastModified());
                String videoTime = timeFormat.format(file.lastModified());

                insertvideo(conn, file.getName(), directoryPath, false, videoDate, videoTime); // Assuming not copied initially
            }
        }
    }

    public static void main(String[] args) throws SQLException {
        VideoScanner app = new VideoScanner();
        //app.scanDirectoryForVideo("C:\\Users\\serhi\\IdeaProjects\\Pipiska"); // Adjust the directory path
        app.final_scan();
    }
}
