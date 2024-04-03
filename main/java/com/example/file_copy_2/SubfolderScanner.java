package com.example.file_copy_2;

import java.io.File;
import java.sql.*;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SubfolderScanner {
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

    public void insertSubfolder(Connection conn1, String subfolderName, String folderId, String information, int fileCount) {
        String sql1 = "SELECT COUNT(SubfolderName) FROM Subfolders WHERE SubfolderName = ? AND FolderID = ?";
        try (PreparedStatement pstmt1 = conn1.prepareStatement(sql1)) {
            pstmt1.setString(1, subfolderName);
            pstmt1.setString(2, folderId);
            ResultSet rd = pstmt1.executeQuery();

            if (rd.getInt(1) == 0){
                String sql = "INSERT INTO Subfolders(SubfolderName, FolderID, Information, FileCount) VALUES(?,?,?,?);";
                PreparedStatement pstmt = conn1.prepareStatement(sql);
                pstmt.setString(1, subfolderName);
                pstmt.setString(2, folderId);
                pstmt.setString(3, information);
                pstmt.setInt(4, fileCount);
                pstmt.executeUpdate();
            }else{
                String sql = "UPDATE Subfolders SET Information = ?, FileCount =? WHERE SubfolderName = ?;";
                PreparedStatement pstmt = conn1.prepareStatement(sql);
                pstmt.setString(1, information);
                pstmt.setInt(2, fileCount);
                pstmt.setString(3, subfolderName);

                pstmt.executeUpdate();

            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
    public void final_scan() throws SQLException {
        String query = "SELECT DirName FROM Directories WHERE DirID = 1"; // Adjust SQL according to your schema
        try (Connection conn = this.connect()) {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                String a = rs.getString("DirName");
                //System.out.println("Jazda z kurwami");
                //System.out.println(a);
                SubfolderScanner temp = new SubfolderScanner();
                temp.scanDirectory(conn, a);//.replace("/", "\\"));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    public void scanDirectory(Connection conn, String directoryPath) {
        File directory = new File(directoryPath);
        File[] subfolders = directory.listFiles(File::isDirectory);
        String folderId = directory.getAbsolutePath();

        if (subfolders != null) {
            Arrays.sort(subfolders, new Comparator<File>() {
                private final Pattern pattern = Pattern.compile("\\d+");

                @Override
                public int compare(File o1, File o2) {
                    Matcher m1 = pattern.matcher(o1.getName());
                    Matcher m2 = pattern.matcher(o2.getName());
                    if (!m1.find() || !m2.find()) {
                        return o1.getName().compareTo(o2.getName());
                    }
                    Integer i1 = Integer.parseInt(m1.group());
                    Integer i2 = Integer.parseInt(m2.group());
                    return i1.compareTo(i2);
                }
            });

            try(Statement stmt = conn.createStatement()){

                for (File subfolder : subfolders) {
                    String information = Objects.requireNonNull(subfolder.list()).length > 0 ? "Not Empty" : "Empty";
                    int fileCount = Objects.requireNonNull(subfolder.list()).length;

                    insertSubfolder(conn, subfolder.getName(), folderId, information, fileCount);
                }
                }catch (SQLException e) {
                System.out.println(e.getMessage());
            }

        }
    }

    public static void main(String[] args) throws SQLException {
        SubfolderScanner app = new SubfolderScanner();
        app.final_scan();
        //app.scanDirectory(conn, "C:\\Users\\serhi\\IdeaProjects\\Pipiska"); // Adjust the directory path and folder ID
    }
}
