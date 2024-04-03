package com.example.file_copy_2;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import static java.lang.Thread.sleep;

public class FileCopier {
    ProgressBar progressBarPhotos; // Assume this is initialized elsewhere in your UI code
    private Label progressLabelPhotos;
    ProgressBar progressBarVideos; // Assume this is initialized elsewhere in your UI code
    private Label progressLabelVideos;
    public String current_subfolder;
    ArrayList<String> alibaba;


    public FileCopier(ProgressBar progressBarPhotos, ProgressBar progressBarVideos, Label progressLabelPhotos, Label progressLabelVideos) {
        this.progressBarPhotos = progressBarPhotos;
        this.progressBarVideos = progressBarVideos;
        this.progressLabelPhotos = progressLabelPhotos;
        this.progressLabelVideos = progressLabelVideos;

    }

    private Connection connect() throws SQLException {
        String dbUrl = "jdbc:sqlite:baza_twoja_stara.db";
        return DriverManager.getConnection(dbUrl);
    }

    public String get_paths_from_database(int a) {

        String insertSQL = "SELECT DirName From Directories WHERE DirID = ?";
        String insertSQL2 = "SELECT COUNT(*) From Directories WHERE DirID = ?";
        StringBuilder result = new StringBuilder(new String());
        int rowCount = 0;
        try (Connection conn = this.connect();
             PreparedStatement countStmt = conn.prepareStatement(insertSQL2)) {
            countStmt.setInt(1, a);
            ResultSet countRs = countStmt.executeQuery();
            if (countRs.next()) {
                rowCount = countRs.getInt(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        try (Connection conn = this.connect();
             PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {
            pstmt.setInt(1, a);

            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {

                result.append(rs.getString("DirName"));
                if (rowCount > 1) {
                    result.append("  ||  ");
                }
                rowCount--;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        //DateTimeFormatter timeFormatter =
        return result.toString();
    }
    public ArrayList<ArrayList<String>> get_the_photos(Connection conn) {
        String query = "SELECT PhotoName, DirName FROM Photos WHERE Copied = 0"; // Adjust SQL according to your schema
        ArrayList<String> photos = new ArrayList<String>();
        ArrayList<String> photodirs = new ArrayList<String>();
        ArrayList<ArrayList<String>> result = new ArrayList<>();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                photos.add(rs.getString("PhotoName"));
                photodirs.add(rs.getString("DirName"));

            }
            result.add(photos);
            result.add(photodirs);
            return result;
        } catch (SQLException e) {
            System.err.println("Database connection failed.");
            e.printStackTrace();
        }
        return new ArrayList<>();
    }
    public ArrayList<ArrayList<String>> get_the_videos(Connection conn) {
        String query = "SELECT VideoName, DirName FROM Videos WHERE Copied = 0"; // Adjust SQL according to your schema
        ArrayList<String> video = new ArrayList<String>();
        ArrayList<String> videodirs = new ArrayList<String>();
        ArrayList<ArrayList<String>> result = new ArrayList<>();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                video.add(rs.getString("VideoName"));
                videodirs.add(rs.getString("DirName"));

            }
            result.add(video);
            result.add(videodirs);
            return result;
        } catch (SQLException e) {
            System.err.println("Database connection failed.");
            e.printStackTrace();
        }
        return new ArrayList<>();
    }
    public void deleteFromDatabase(boolean photos, String name){
        File file = new File(name);
        String directory = file.getParent();
        String fileName = file.getName();

        System.out.println("Directory: " + directory);
        System.out.println("File Name: " + fileName);
        String sql = null;
        if(photos) {sql = "UPDATE Photos SET Copied = 2, CopiedTo = 'deleted' WHERE DirName = ? AND PhotoName = ?";}
        else{sql = "UPDATE Videos SET Copied = 2, CopiedTo = 'deleted' WHERE DirName = ? AND VideoName = ?";}
        try(Connection connec = this.connect();
            PreparedStatement pstmt = connec.prepareStatement(sql)) {
            pstmt.setString(1, directory);
            pstmt.setString(2, fileName);
            pstmt.executeUpdate();
            System.out.println("Zajebiście");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    public ArrayList<String> previewPhotos() throws SQLException {
        try (Connection coon = this.connect()) {
            ArrayList<ArrayList<String>> a = get_the_photos(coon);
            ArrayList<String> photos = a.get(0);
            ArrayList<String> dirs = a.get(1);
            ArrayList<String> result = new ArrayList<>();
            for (int i = 0; i < photos.size(); i++) {
                String sourcePath = dirs.get(i);
                String photoName = photos.get(i);
                result.add(sourcePath+"\\"+photoName);
            }

            return result;
        }
    }
    public ArrayList<String> previewVideos() throws SQLException {
        try (Connection coon = this.connect()) {
            ArrayList<ArrayList<String>> a = get_the_videos(coon);
            ArrayList<String> photos = a.get(0);
            ArrayList<String> dirs = a.get(1);
            ArrayList<String> result = new ArrayList<>();
            for (int i = 0; i < photos.size(); i++) {
                String sourcePath = dirs.get(i);
                String photoName = photos.get(i);
                result.add(sourcePath+"\\"+photoName);
            }

            return result;
        }
    }
    public Task<Void> createCopyVideosTask() {
        return new Task<>() {
            @Override
            protected Void call() throws Exception {
                try (Connection conn = connect()) {
                    String destinationFolder = current_subfolder;
                    ArrayList<ArrayList<String>> videoData = get_the_videos(conn);
                    ArrayList<String> videos = videoData.get(0);
                    ArrayList<String> dirs = videoData.get(1);
                    //System.out.println(current_subfolder);
                    int cur = 1;
                    int size = videos.size();
                    for (int i = 0; i < videos.size(); i++) {
                        if (isCancelled()) {
                            updateMessage("Cancelled");
                            break;
                        }

                        String sourcePath = dirs.get(i);
                        String videoName = videos.get(i);
                        Path sourceFilePath = Paths.get(sourcePath, videoName);
                        Path destinationFilePath = Paths.get(destinationFolder, videoName);

                        long totalBytes = Files.size(sourceFilePath);
                        try (InputStream in = Files.newInputStream(sourceFilePath);
                             OutputStream out = Files.newOutputStream(destinationFilePath)) {
                            byte[] buffer = new byte[65536];
                            int bytesRead;
                            long bytesCopied = 0;

                            while ((bytesRead = in.read(buffer)) > 0) {
                                out.write(buffer, 0, bytesRead);
                                bytesCopied += bytesRead;
                                double progress = (double) bytesCopied / totalBytes;
                                updateProgress(progress, 1.0);
                                updateMessage(String.format("Kopiuję %d z %d  %s (%.2f%%)", cur, size, videoName, progress * 100));
                            }

                            insertCopyInfo(false, conn, videoName, destinationFolder);
                        } catch (IOException | SQLException e) {
                            updateMessage("Failed to copy: " + videoName);
                            e.printStackTrace();
                        }
                    }
                    updateMessage("Copy completed");
                    updateProgress(0, 1.0);

                    return null;

                }
            }

            private Connection connect() throws SQLException {
                String dbUrl = "jdbc:sqlite:baza_twoja_stara.db";
                return DriverManager.getConnection(dbUrl);
            }
        };
    }
    public Task<Void> createCopyPhotosTask() {
        return new Task<>() {
            @Override
            protected Void call() throws Exception {
                try(Connection conn = this.connect()){
                    String destinationFolder = current_subfolder;
                    //System.out.println(current_subfolder);
                    ArrayList<ArrayList<String>> photoData = get_the_photos(conn);
                    ArrayList<String> photos = photoData.get(0);
                    ArrayList<String> dirs = photoData.get(1);
                    //System.out.println(photos.size());
                    int succes_ = 0;
                    int fail = 0;
                    for (int i = 0; i < photos.size(); i++) {
                        //System.out.println("kurwwaaaaa");

                        String sourcePath = dirs.get(i);
                        String photoName = photos.get(i);
                        Path sourceFilePath = Paths.get(sourcePath, photoName);
                        //alibaba.add(sourcePath);
                        Path destinationFilePath = Paths.get(destinationFolder, photoName);

                        try {

                            Files.copy(sourceFilePath, destinationFilePath, StandardCopyOption.REPLACE_EXISTING);
                            System.out.println("Copied: " + photoName);
                            succes_++;
                            //System.out.println("kurwwaaaaa");

                            insertCopyInfo(true, conn, photoName, destinationFolder);
                        } catch (IOException | SQLException e) {
                            //System.out.println("kurwwaaaaa");
                            fail++;
                            System.err.println("Failed to copy: " + photoName);
                            e.printStackTrace();
                        }
                        //System.out.println("kurwwaaaaa");

                        // Update progress
                        updateMessage(String.format("Kopiuję... %d z %d", succes_, photos.size()));
                        updateProgress((i + 1), photos.size());
                        final int currentFileIndex = i + 1;
                        Platform.runLater(() -> {
                            progressLabelPhotos.setText(String.format("Kopiuję... %d z %d", currentFileIndex, photos.size()));
                        });
                        sleep(150);
                    }
                    updateMessage("Skopiowane : " + succes_ +" z "+photos.size());
                    updateProgress(0, 1.0);

                    //progressLabelPhotos.setText("Copied: " + photos.size() + " files");
                }

                return null;
            }

            private Connection connect() throws SQLException {
                String dbUrl = "jdbc:sqlite:baza_twoja_stara.db";
                return DriverManager.getConnection(dbUrl);
            }
        };
    }
        private void insertCopyInfo(boolean photo_true, Connection conn, String fileName, String destinationPath) throws SQLException {
            String insertSQL;
            if(photo_true){
                insertSQL = "UPDATE Photos SET PhotoName = ?, Copied=1, CopiedTo=?, CopyTimestamp=? WHERE PhotoName = ?"; // Adjust SQL according to your schema
            }else{
                insertSQL = "UPDATE Videos SET VideoName = ?, Copied=1, CopiedTo=?, CopyTimestamp=? WHERE VideoName = ?"; // Adjust SQL according to your schema

            }

            try (PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {
                //DateTimeFormatter timeFormatter =
                pstmt.setString(1, fileName);
                pstmt.setString(2, destinationPath);
                DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
                LocalTime now = LocalTime.now();
                String formattedTime = now.format(timeFormatter);
                pstmt.setString(3, formattedTime);
                pstmt.setString(4, fileName);

                pstmt.executeUpdate();
                System.out.println("Database updated for: " + fileName);
            }
        }
        public static void main (String[]args){
            //FileCopier copier = new FileCopier();
            //copier.copyFiles();
        }
    }

