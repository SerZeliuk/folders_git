package com.example.file_copy_2;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.util.Duration;
import org.kordamp.ikonli.javafx.FontIcon;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import static java.lang.Thread.sleep;

public class JavaFXCustomInterfaceController implements Initializable {
    public VBox rightBox;
    public StackPane mediaViewContainer;
    public StackPane photosContainer;
    public Button left_photo;
    public Button right_photo;
    public Button kopiuj;
    public Button right_video;
    public Button playPauseButton;
    public Button deletePhoto;
    public Button deleteVideo;
    public Button left_video;
    public boolean jeden_film=false;
    public ImageView photoImageView;
    public Label photoLabel;
    public Label videoLabel;
    public Slider seekSlider;
    public Label kopiuj_label;
    public Label nazwa_folderu;
    public Label duration_label;
    public Label max_duration;
    @FXML private TextField directoryField1;
    @FXML private TextField directoryField2;
    @FXML private TextField directoryField3;
    @FXML private TextField directoryField4;
    @FXML private MediaView mediaView;
    @FXML private ProgressBar myProgressBarPhotos;
    @FXML private ProgressBar myProgressBarVideos;
    @FXML private Label myProgressLabelPhotos;
    @FXML private Label myProgressLabelVideos;
    private DirectoryChooser directoryChooser = new DirectoryChooser();
    private MediaPlayer mediaPlayer;
    private List<String> imagePaths; // Initialize with paths to your images
    private ArrayList<String> sosiska;
    private int currentIndex = 0;
    private int vid_current_index = 0;
    private int kopiuj_mode = 0;
    public String curr_subfolder;
    public String cur_subfolder_name;
    private boolean atEndOfVideo = false;

    @FXML
    private void clearTextFields() {
        directoryField1.clear();
        directoryField2.clear();
        directoryField3.clear();
        directoryField4.clear();
    }
    @FXML
    public void loadImage(String imagePath) {

        File file = new File(imagePath);
        if (file.exists()) {
            Image image = new Image(file.toURI().toString());
            photoImageView.setImage(image);
            int d = imagePaths.size();
            photoLabel.setText(file.getName() + "  " + (currentIndex+1) + " z " + d);
        } else {
            System.err.println("File does not exist: " + imagePath);
        }
    }
    @FXML
    private void show_komplet() throws SQLException {
        set_next_folder_to_dir_2();
        show_photos();
        show_videos();
        kopiuj_mode = 3;
    }
    @FXML
    private void showNextVideo() {
        left_video.setDisable(false);
        if (vid_current_index < sosiska.size() - 1) {
            left_video.setDisable(false);
            vid_current_index++;
            playVideo(sosiska.get(vid_current_index));
        }
        if (vid_current_index == sosiska.size() - 1) {
            right_video.setDisable(true);
        }
    }
    @FXML
    private void showPreviousVideo() {
        right_video.setDisable(false);
        if (vid_current_index > 0) {
            vid_current_index--;
            playVideo(sosiska.get(vid_current_index));
        }
        if (vid_current_index == 0){
            left_video.setDisable(true);
        }
    }
    @FXML
    private void showNextPhoto() {
        left_photo.setDisable(false);
        if (currentIndex < imagePaths.size() - 1) {
            left_photo.setDisable(false);
            currentIndex++;
            System.out.println("Kur ind: " + currentIndex );
            loadImage(imagePaths.get(currentIndex));


        }
        else if (currentIndex == imagePaths.size() - 1) {
            currentIndex = 0;
            System.out.println("Kur ind: " + currentIndex );

            loadImage(imagePaths.get(currentIndex));
        }
    }
    @FXML
    private void showPreviousPhoto() {
        right_photo.setDisable(false);
        if (currentIndex > 0) {
            currentIndex--;
            System.out.println("Kur ind:" + currentIndex);

            loadImage(imagePaths.get(currentIndex));

        }else if (currentIndex == 0){
            currentIndex = imagePaths.size()-1;
            System.out.println("Kur ind:" + currentIndex);

            loadImage(imagePaths.get(currentIndex));
        }
    }
    @FXML
    private void chooseDirectory1() throws SQLException {
        chooseDirectory(directoryField1);
    }
    @FXML
    private void chooseDirectory2() throws SQLException {
        chooseDirectory(directoryField2);
    }
    @FXML
    private void chooseDirectory3() throws SQLException {
        chooseDirectory(directoryField3);
    }
    @FXML
    private void chooseDirectory4() throws SQLException {

        chooseDirectory(directoryField4);
        scan();
    }
    private void chooseDirectory(TextField field) throws SQLException {
        var directory = directoryChooser.showDialog(null);
        if (directory != null) {
            int id = Integer.parseInt(field.getId().substring(field.getId().length() - 1));
            String path = directory.getAbsolutePath();
            FolderDatabaseCreator a = new FolderDatabaseCreator();
            a.set_to_dirs(id, path);
            if (id==1){
                SubfolderScanner tmp1 = new SubfolderScanner();
                tmp1.final_scan();
               }
            field.setText(directory.getAbsolutePath());
        }
    }

    public void set_next_folder_to_dir_2(){
        FileCopier tmp = new FileCopier(myProgressBarPhotos, myProgressBarVideos, myProgressLabelPhotos, myProgressLabelVideos); // Assuming you have a label for status
        String ab = tmp.current_subfolder;
    }
    @FXML
    private void stopVideo(){
        if (mediaPlayer != null) {
            mediaPlayer.stop(); // Stop the currently playing video
        }
    }
    @FXML
    private void playVideo(String path) {
        stopVideo();
        seekSlider.setVisible(true);
        playPauseButton.setVisible(true);
        mediaViewContainer.setVisible(true);
        File file = new File(path);
        String name = file.getName();
        URI uri = file.toURI();
        Media media = new Media(uri.toString()); // Adjust path
        mediaPlayer = new MediaPlayer(media);
        mediaPlayer.currentTimeProperty().addListener(ov -> updateValues());
        mediaPlayer.setOnReady(() -> seekSlider.setMax(mediaPlayer.getTotalDuration().toSeconds()));

        mediaPlayer.setOnEndOfMedia(() -> atEndOfVideo = true);
        mediaPlayer.setOnPlaying(() -> playPauseButton.setText("Pause"));
        mediaPlayer.setOnPaused(() -> playPauseButton.setText("Play"));

        mediaPlayer.statusProperty().addListener((obs, oldStatus, newStatus) -> {
            if (newStatus == MediaPlayer.Status.READY) {
                Duration totalDuration = mediaPlayer.getMedia().getDuration();
                String formattedDuration = formatDuration(totalDuration);
                max_duration.setText(formattedDuration);
            }
        });

        seekSlider.valueProperty().addListener(ov -> {
            if (seekSlider.isValueChanging()) {
                mediaPlayer.seek(Duration.seconds(seekSlider.getValue()));

            }
        });
        mediaView.setMediaPlayer(mediaPlayer);
        mediaView.setPreserveRatio(false);
        videoLabel.setText(name + "   " + (vid_current_index+1) + " z " + sosiska.size());

        mediaView.setFitWidth(350);
        mediaView.setFitHeight(200);
        mediaPlayer.play();
    }
    protected void updateValues() {
        Platform.runLater(() -> {
            Duration currentTime = mediaPlayer.getCurrentTime();
            String formatted_time = formatDuration(currentTime);
            seekSlider.setValue(currentTime.toSeconds());
            duration_label.setText(formatted_time);
        });
    }
    private String formatDuration(Duration duration) {
        int minutes = (int) duration.toMinutes();
        int seconds = (int) duration.toSeconds() % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }
    @FXML
    public void show_photos() throws SQLException {
        currentIndex = 0;
        scan();
        scan_from_db();
        if(imagePaths == null || imagePaths.isEmpty()) {
            deletePhoto.setDisable(true);
            Stage window = new Stage();
            window.initModality(Modality.APPLICATION_MODAL);
            window.setTitle("WYDUPCAJ");

            Label messageLabel = new Label("Ni mo kurwa zdjęć");

            Button okButton = new Button("Ok");
            okButton.setOnAction(e -> {
                jeden_film = false;
                window.close();
            });

            okButton.setOnAction(e -> {
                jeden_film = true;
                window.close();
            });

            HBox buttonLayout = new HBox(10); // Spacing between buttons
            buttonLayout.getChildren().addAll(okButton);
            buttonLayout.setAlignment(Pos.CENTER); // Center buttons horizontally

            BorderPane layout = new BorderPane();
            layout.setCenter(messageLabel); // Adds the message label to the center
            layout.setBottom(buttonLayout); // Adds the button layout to the bottom
            BorderPane.setAlignment(buttonLayout, Pos.CENTER); // Centers the button layout horizontally
            BorderPane.setMargin(buttonLayout, new Insets(10)); // Adds some margin around the button layout

            Scene scene = new Scene(layout, 400, 200); // Adjust the size as needed
            window.setScene(scene);

            window.showAndWait();

        }
        else {
            deletePhoto.setVisible(true);

            deletePhoto.setDisable(false);
            set_next_folder_to_dir_2();
            left_photo.setVisible(true);
            right_photo.setVisible(true);
            //left_photo.setDisable(true);
            System.out.println(imagePaths.get(0));
            loadImage(imagePaths.get(0));

            left_photo.setDisable(true);
            kopiuj.setDisable(false);
            if(kopiuj_mode == 0){kopiuj_mode = 1;}else{kopiuj_mode=3;}

        }
    }
    private static void scan() throws SQLException {
        SubfolderScanner app = new SubfolderScanner();
        app.final_scan();
        PhotosScanner app1 = new PhotosScanner();
        app1.final_scan();
        VideoScanner app2 = new VideoScanner();
        app2.final_scan();
    }
    private void scan_from_db() throws SQLException {
        imagePaths = null;
        sosiska = null;
        FileCopier tmp = new FileCopier(myProgressBarPhotos, myProgressBarVideos, myProgressLabelPhotos, myProgressLabelVideos); // Assuming you have a label for status
        imagePaths = tmp.previewPhotos();
        sosiska = tmp.previewVideos();
    }
    public void photos_get() throws SQLException, InterruptedException {
        scan(); // Assuming this method is correctly setting up for the task
        FileCopier tmp = new FileCopier(myProgressBarPhotos, myProgressBarVideos, myProgressLabelPhotos, myProgressLabelVideos);
        //if(kopiuj_mode !=2){get_next_folder();}
        Task<Void> copyPhotosTask = tmp.createCopyPhotosTask();
        tmp.current_subfolder = curr_subfolder;
        System.out.println("Copying tooooo: " + tmp.current_subfolder);

        myProgressBarPhotos.progressProperty().bind(copyPhotosTask.progressProperty());

        // Bind the label text to reflect the task's progress updates
        copyPhotosTask.messageProperty().addListener((observable, oldValue, newValue) -> {
            Platform.runLater(() -> myProgressLabelPhotos.setText(newValue));
        });

        Thread thread = new Thread(copyPhotosTask);
        thread.setDaemon(true);
        thread.start();
        //thread.join();
        sleep(500);
    }
    public void videos_get() throws SQLException {
        scan(); // Assuming this method is correctly setting up for the task
        FileCopier tmp = new FileCopier(myProgressBarPhotos, myProgressBarVideos, myProgressLabelPhotos, myProgressLabelVideos);
        //if (kopiuj_mode==2){get_next_folder();}
        tmp.current_subfolder = curr_subfolder;
        System.out.println("Copying tooooo: " +tmp.current_subfolder);


        Task<Void> copyVideosTask = tmp.createCopyVideosTask(); // Assuming 'conn' is your connection

        myProgressBarVideos.progressProperty().bind(copyVideosTask.progressProperty());
        myProgressLabelVideos.textProperty().bind(copyVideosTask.messageProperty());

        Thread thread = new Thread(copyVideosTask);
        thread.setDaemon(true);
        thread.setDaemon(true);
        thread.start();
        //myProgressBarVideos.setProgress(0);
    }
    @FXML
    public void show_videos() throws SQLException {
        scan();
        scan_from_db();
        FileCopier tmp = new FileCopier(myProgressBarPhotos, myProgressBarVideos, myProgressLabelPhotos, myProgressLabelVideos);
        sosiska = tmp.previewVideos();
        //System.out.println(sosiska.get(0));
        if(sosiska.size()==1){
            deleteVideo.setVisible(true);

            playVideo(sosiska.get(0));
            kopiuj.setDisable(false);
            deleteVideo.setDisable(true);
        }
        else if(sosiska.isEmpty()) {
            Stage window = new Stage();
            window.initModality(Modality.APPLICATION_MODAL);
            window.setTitle("WYDUPCAJ");

            Label messageLabel = new Label("Ni mo kurwa filmów");

            Button okButton = new Button("Ok");
            okButton.setOnAction(e -> {
                jeden_film = false;
                window.close();
            });

            okButton.setOnAction(e -> {
                jeden_film = true;
                window.close();
            });

            HBox buttonLayout = new HBox(10); // Spacing between buttons
            buttonLayout.getChildren().addAll(okButton);
            buttonLayout.setAlignment(Pos.CENTER); // Center buttons horizontally

            BorderPane layout = new BorderPane();
            layout.setCenter(messageLabel); // Adds the message label to the center
            layout.setBottom(buttonLayout); // Adds the button layout to the bottom
            BorderPane.setAlignment(buttonLayout, Pos.CENTER); // Centers the button layout horizontally
            BorderPane.setMargin(buttonLayout, new Insets(10)); // Adds some margin around the button layout

            Scene scene = new Scene(layout, 400, 200); // Adjust the size as needed
            window.setScene(scene);

            window.showAndWait();

        }
        else{

            Stage window = new Stage();
            window.initModality(Modality.APPLICATION_MODAL);
            window.setTitle("WYDUPCAJ");

            Label messageLabel = new Label("Jest więcej niż jeden film. Chcesz wrzucić więcej??");

            Button yesButton = new Button("Tak");
            Button noButton = new Button("Nie");


            yesButton.setOnAction(e -> {
                jeden_film = false;
                window.close();
                deleteVideo.setDisable(false);
                deleteVideo.setVisible(true);

            });

            noButton.setOnAction(e -> {
                deleteVideo.setVisible(true);

                deleteVideo.setDisable(true);
                jeden_film = true;
                window.close();
            });

            HBox buttonLayout = new HBox(10); // Spacing between buttons
            buttonLayout.getChildren().addAll(yesButton, noButton);
            buttonLayout.setAlignment(Pos.CENTER); // Center buttons horizontally

            BorderPane layout = new BorderPane();
            layout.setCenter(messageLabel); // Adds the message label to the center
            layout.setBottom(buttonLayout); // Adds the button layout to the bottom
            BorderPane.setAlignment(buttonLayout, Pos.CENTER); // Centers the button layout horizontally
            BorderPane.setMargin(buttonLayout, new Insets(10)); // Adds some margin around the button layout

            Scene scene = new Scene(layout, 400, 200); // Adjust the size as needed
            window.setScene(scene);

            window.showAndWait();
            kopiuj.setDisable(false);

        }
        if(jeden_film){
            String sosiska_temp = sosiska.getFirst();
            sosiska.clear();
            sosiska.add(sosiska_temp);
            playVideo(sosiska.get(0));
        }
        else{
            left_video.setVisible(true);
            right_video.setVisible(true);
            playVideo(sosiska.get(vid_current_index));
        }
        String ab = tmp.current_subfolder;
        if(kopiuj_mode==0){kopiuj_mode = 2;}else{kopiuj_mode=3;}
;
    }
    @FXML
    public void delete_current_photo() throws SQLException {

        scan_from_db();
        FileCopier tmp = new FileCopier(myProgressBarPhotos, myProgressBarVideos, myProgressLabelPhotos, myProgressLabelVideos); // Assuming you have a label for status
        tmp.deleteFromDatabase(true, imagePaths.get(currentIndex));
        if(currentIndex!=imagePaths.size()-1){showNextPhoto();}
        else{showPreviousPhoto();}
        imagePaths.remove(currentIndex);
        show_photos();
    }
    @FXML
    public void delete_current_video() throws SQLException {
        scan_from_db();
        FileCopier tmp = new FileCopier(myProgressBarPhotos, myProgressBarVideos, myProgressLabelPhotos, myProgressLabelVideos); // Assuming you have a label for status
        tmp.deleteFromDatabase(false, sosiska.get(vid_current_index));
        if(vid_current_index!=sosiska.size()-1){showNextVideo();}
        else{showPreviousVideo();}
        sosiska.remove(vid_current_index);
        show_photos();
    }
    public void init__arrows(){
        seekSlider.setVisible(false);
        playPauseButton.setVisible(false);
        directoryField1.setEditable(false);
        directoryField1.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (isNowFocused) Platform.runLater(() -> photoImageView.requestFocus());
        });
        directoryField2.setEditable(false);
        directoryField2.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (isNowFocused) Platform.runLater(() -> photoImageView.requestFocus());
        });
        directoryField3.setEditable(false);
        directoryField3.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (isNowFocused) Platform.runLater(() -> photoImageView.requestFocus());
        });
        directoryField4.setEditable(false);
        directoryField4.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (isNowFocused) Platform.runLater(() -> photoImageView.requestFocus());
        });
        FontIcon leftArrowIcon = new FontIcon("fas-arrow-left");
        left_photo.setGraphic(leftArrowIcon);
        FontIcon rightArrowIcon = new FontIcon("fas-arrow-right");
        right_photo.setGraphic(rightArrowIcon);
        //left_photo.setDefaultButton(Right);
        left_photo.setVisible(false);
        right_photo.setVisible(false);


        left_video.setGraphic(leftArrowIcon);
        right_video.setGraphic(rightArrowIcon);
        //left_photo.setDefaultButton(Right);
        left_video.setVisible(false);
        right_video.setVisible(false);

        left_photo.setOnKeyPressed(keyEvent -> {
            if(keyEvent.getCode().equals(KeyCode.LEFT)){
                //System.out.println("w pytę");
                left_photo.fire();
                keyEvent.consume();
            }
        });
        right_photo.setOnKeyPressed(keyEvent -> {
            if(keyEvent.getCode().equals(KeyCode.RIGHT)){
                System.out.println("noice");
                right_photo.fire();
                keyEvent.consume();
            }
        });
    }
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        init__arrows();
        get_next_folder();
        try {
            scan();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        if(curr_subfolder!=null) {
            File a = new File(curr_subfolder);
            String papapa = a.getName();
            cur_subfolder_name = papapa;
            nazwa_folderu.setText(cur_subfolder_name);
        }
        deletePhoto.setVisible(false);
        deleteVideo.setVisible(false);
        FileCopier tmp = new FileCopier(myProgressBarPhotos, myProgressBarVideos, myProgressLabelPhotos, myProgressLabelVideos); // Assuming you have a label for status
        kopiuj.setDisable(true);
        myProgressLabelPhotos.setText("Zdjęcia");
        myProgressLabelVideos.setText("Film");
        String f1 = tmp.get_paths_from_database(1);
        directoryField1.setText(f1);
        String f3 = tmp.get_paths_from_database(3);
        directoryField3.setText(f3);
        String f4 = tmp.get_paths_from_database(4);
        directoryField4.setText(f4);

        Platform.runLater(() -> {
            Scene scene = kopiuj.getScene(); // 'kopiuj' is a known element in the scene, so we use it to get the Scene
            if (scene != null) {
                scene.setOnKeyPressed(event -> {
                    if (event.getCode() == KeyCode.SPACE) {
                        togglePlayPause();
                    }
                });
            }
        });
    }
    @FXML
    private void togglePlayPause() {
        if (mediaPlayer != null) {
            if (mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
                mediaPlayer.pause();
            } else if (mediaPlayer.getStatus() == MediaPlayer.Status.PAUSED || mediaPlayer.getStatus() == MediaPlayer.Status.STOPPED) {
                mediaPlayer.play();
            }
        }
    }
    @FXML
    public void copy_the_files() throws SQLException, InterruptedException {
        stopVideo();
        System.out.println("Kopiuję w trybie: " + kopiuj_mode);
        switch (kopiuj_mode){
            case 1:
                photos_get();
                get_next_folder();
                imagePaths=null;
                break;
            case 2:
                videos_get();
                get_next_folder();
                sosiska=null;
                break;
            case 3:
                photos_get();
                sleep(100);
                videos_get();
                get_next_folder();
                kopiuj_mode = 0;
                imagePaths=null;
                sosiska=null;
                break;
        }
        kopiuj.setDisable(true);
    }
    private Connection connect() throws SQLException {
        String dbUrl = "jdbc:sqlite:baza_twoja_stara.db";
        return DriverManager.getConnection(dbUrl);
    }
    @FXML
    public void get_next_folder() {
        String query = "SELECT SubfolderName,FolderID FROM Subfolders WHERE FileCount = 0 LIMIT 1"; // Adjust SQL according to your schema

        try (Connection conn = this.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            if(rs.getString("SubfolderName")!=null){curr_subfolder = rs.getString("FolderID") + "\\" + rs.getString("SubfolderName");}
            directoryField2.setText(curr_subfolder);
            if(curr_subfolder!=null){
                File a = new File(curr_subfolder);
                String papapa = a.getName();
                cur_subfolder_name = papapa;
                nazwa_folderu.setText(cur_subfolder_name);
            }
        } catch (SQLException e) {
            System.err.println("Database connection failed.");
            e.printStackTrace();
        }

    }
    @FXML
    public void get_previous_folder() {
        String query = "SELECT SubfolderName,FolderID FROM Subfolders WHERE SubfolderID = (SELECT CAST(SubfolderID as INTEGER)-1 From Subfolders WHERE SubfolderName = ?) LIMIT 1";

        File a = new File(curr_subfolder);
        String papapa = a.getName();
        try (Connection conn = this.connect();
             PreparedStatement stmt = conn.prepareStatement(query))
             {
                 stmt.setString(1, papapa);
                 ResultSet rs = stmt.executeQuery();
                 if(rs.getString("SubfolderName")!=null){curr_subfolder = rs.getString("FolderID") + "\\" + rs.getString("SubfolderName");}
                 directoryField2.setText(curr_subfolder);
                 File ab = new File(curr_subfolder);
                 String papapa1 = ab.getName();
                 cur_subfolder_name = papapa1;
                 nazwa_folderu.setText(cur_subfolder_name);
        } catch (SQLException e) {
            System.err.println("Database connection failed.");
            e.printStackTrace();
        }
    }
    @FXML
    public void get_next_folder1() {
        String query = "SELECT SubfolderName,FolderID FROM Subfolders WHERE SubfolderID = (SELECT CAST(SubfolderID as INTEGER)+1 From Subfolders WHERE SubfolderName = ?) LIMIT 1";

        File a = new File(curr_subfolder);
        String papapa = a.getName();

        try (Connection conn = this.connect();
             PreparedStatement stmt = conn.prepareStatement(query))
        {
            stmt.setString(1, papapa);
            ResultSet rs = stmt.executeQuery();
            if(rs.getString("SubfolderName")!=null){curr_subfolder = rs.getString("FolderID") + "\\" + rs.getString("SubfolderName");}
            directoryField2.setText(curr_subfolder);
            File ab = new File(curr_subfolder);
            String papapa1 = ab.getName();
            cur_subfolder_name = papapa1;
            nazwa_folderu.setText(cur_subfolder_name);
        } catch (SQLException e) {
            System.err.println("Database connection failed.");
            e.printStackTrace();
        }
    }
}
