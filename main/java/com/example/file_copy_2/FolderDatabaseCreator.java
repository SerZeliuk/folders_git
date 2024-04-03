package com.example.file_copy_2;

import java.sql.*;
import java.util.ArrayList;

public class FolderDatabaseCreator {
    public static Connection connect(String filename) {
        Connection conn = null;
        try {
            // db parameters
            String url = "jdbc:sqlite:" + filename;
            // create a connection to the database
            try{
                conn = DriverManager.getConnection(url);
            }catch(Exception e){
                CreateDatabase.createNewDatabase(filename);
            }
            // tworzenie tabeli
            String sql =     """
                    CREATE TABLE IF NOT EXISTS Subfolders (
                        SubfolderID INTEGER PRIMARY KEY AUTOINCREMENT,
                        SubfolderName VARCHAR(255),
                        FolderID VARCHAR(255),
                        Information VARCHAR(255),
                        FileCount INT
                        )
                        
                    
                    """;
            String ex_vid = """
                    CREATE TABLE IF NOT EXISTS Videos (
                        VideoID INTEGER PRIMARY KEY,
                        VideoName VARCHAR(255),
                        DirName VARCHAR(255),
                        Copied VARCHAR(3),
                        CopiedTo VARCHAR(255),
                        CopyTimestamp TIME,
                        VideoDate DATE,
                        VideoTime TIME
                    );
                    """;
            String ex_photos = """
                    CREATE TABLE IF NOT EXISTS Photos (
                        PhotoID INTEGER PRIMARY KEY,
                        PhotoName VARCHAR(255),
                        DirName VARCHAR(255),
                        Copied VARCHAR(3),
                        CopiedTo VARCHAR(255),
                        CopyTimestamp TIME,
                        PhotoDate DATE,
                        PhotoTime TIME
                                                 
                    );
                    """;
            String ex_directories = """
                    CREATE TABLE IF NOT EXISTS Directories (
                        DirID INTEGER,
                        DirName VARCHAR(255)
                    );
                    """;
            // uruchomienie zapytania
            assert conn != null;
            Statement stmt = conn.createStatement();
            stmt.execute(sql);
            stmt.execute(ex_vid);
            stmt.execute(ex_photos);
            stmt.execute(ex_directories);


        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }


        return conn;
    }

    private Connection connect1() {
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

    public boolean exists(int id, String name) throws SQLException {
        String query = "SELECT COUNT(*) FROM Directories WHERE DirName = ? AND DirID = ?";
        try (Connection conn = connect1();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, name);
            pstmt.setInt(2, id);

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return false;
    }

    public void insertUser(int id, String name) throws SQLException {
        if (!exists(id, name)) {
            String insert = "INSERT INTO Directories (DirID, DirName) VALUES (?, ?)";
            try (Connection conn = connect1();
                 PreparedStatement pstmt = conn.prepareStatement(insert)) {
                pstmt.setInt(1, id);
                pstmt.setString(2, name);
                pstmt.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        } else {
            System.out.println("User with this email already exists.");
        }
    }


    public void set_to_dirs(int a, String name) throws SQLException {
        insertUser(a, name);
    }
    public ArrayList<String> get_from_dirs(int a){
        ArrayList<String> resultList = new ArrayList<>();

        String sql = "SELECT DirName FROM Directories WHERE DirID = ?;";
        //String sql1 = "SELECT COUNT(PhotoName) FROM Photos WHERE PhotoName = ? AND PhotoTime = ?";
        try (Connection conn1 = connect1();
             PreparedStatement pstmt1 = conn1.prepareStatement(sql)) {
                     pstmt1.setInt(1, a);
             ResultSet rd = pstmt1.executeQuery();
            while (rd.next()) {
                // Assuming 'your_column' is of type STRING
                String value = rd.getString("DirName");
                resultList.add(value);
            }

        }catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return resultList;
    }

    public static void main(String[] args) {
        connect("baza_twoja_stara.db");
    }
}