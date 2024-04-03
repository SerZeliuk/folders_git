package com.example.file_copy_2;

import java.io.File;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Arrays;

public class PhotosScanner {

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
    public void final_scan() {
        String query = "SELECT DirName FROM Directories WHERE DirID = 3"; // Adjust SQL according to your schema
        try (Connection conn = this.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                String a = rs.getString("DirName");
                //System.out.println("Jazda z kurwami");
                //System.out.println(a);
                PhotosScanner temp = new PhotosScanner();
                temp.scanDirectoryForPhotos(conn, a);//.replace("/", "\\"));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    public void insertPhoto(Connection conn1, String photoName, String path, boolean copied, String photoDate, String photoTime) throws SQLException {
        String sql = "INSERT INTO Photos(PhotoName, DirName, Copied, PhotoDate, PhotoTime) VALUES(?,?,?,?,?);";
        String sql1 = "SELECT COUNT(PhotoName) FROM Photos WHERE PhotoName = ? AND  PhotoTime = ? AND PhotoDate = ?";
        try (PreparedStatement pstmt1 = conn1.prepareStatement(sql1)) {
            pstmt1.setString(1, photoName);
            pstmt1.setString(2, photoTime);
            pstmt1.setString(3, photoDate);

            ResultSet rd = pstmt1.executeQuery();
            if(rd.getInt(1) == 0) {
                PreparedStatement pstmt = conn1.prepareStatement(sql);
                pstmt.setString(1, photoName);
                pstmt.setString(2, path);
                pstmt.setBoolean(3, copied);
                pstmt.setString(4, photoDate);
                pstmt.setString(5, photoTime);
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void scanDirectoryForPhotos(Connection conn, String directoryPath) throws SQLException {
        String[] extensions = {".jpg", ".jpeg", ".png"};
        for(String suff : extensions){
            File directory = new File(directoryPath);
            File[] files = directory.listFiles((dir, name) -> name.toLowerCase().endsWith(suff));

            if (files != null) {
                Arrays.sort(files); // Sort files by name (natural order)

                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");

                for (File file : files) {
                    String photoDate = dateFormat.format(file.lastModified());
                    String photoTime = timeFormat.format(file.lastModified());

                    insertPhoto(conn, file.getName(), directoryPath, false, photoDate, photoTime); // Assuming not copied initially
                }
            }
        }
    }

    public static void main(String[] args) throws SQLException {
        PhotosScanner app = new PhotosScanner();
        //app.scanDirectoryForPhotos("C:\\Users\\serhi\\IdeaProjects\\Pipiska"); // Adjust the directory path
        app.final_scan();
    }
}
