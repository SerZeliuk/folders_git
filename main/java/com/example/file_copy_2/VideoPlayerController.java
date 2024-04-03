package com.example.file_copy_2;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.util.Duration;

public class VideoPlayerController {

    @FXML
    private MediaView mediaView;
    @FXML
    private Slider seekSlider;
    @FXML
    private Button playPauseButton;

    private MediaPlayer mediaPlayer;
    private boolean atEndOfVideo = false;

    public void initialize() {
        // Example media file
        Media media = new Media("file:///path/to/your/video.mp4"); // Update this path
        mediaPlayer = new MediaPlayer(media);
        mediaView.setMediaPlayer(mediaPlayer);

        mediaPlayer.currentTimeProperty().addListener((Observable ov) -> updateValues());
        mediaPlayer.setOnReady(() -> seekSlider.setMax(mediaPlayer.getTotalDuration().toSeconds()));
        mediaPlayer.setOnEndOfMedia(() -> atEndOfVideo = true);
        mediaPlayer.setOnPlaying(() -> playPauseButton.setText("Pause"));
        mediaPlayer.setOnPaused(() -> playPauseButton.setText("Play"));

        seekSlider.valueProperty().addListener((Observable ov) -> {
            if (seekSlider.isValueChanging()) {
                // multiply duration by percentage calculated by slider position
                mediaPlayer.seek(Duration.seconds(seekSlider.getValue()));
            }
        });
    }

    @FXML
    private void togglePlayPause() {
        if (atEndOfVideo) {
            mediaPlayer.seek(mediaPlayer.getStartTime());
            atEndOfVideo = false;
        }
        MediaPlayer.Status status = mediaPlayer.getStatus();
        if (status == MediaPlayer.Status.PLAYING) {
            mediaPlayer.pause();
        } else {
            mediaPlayer.play();
        }
    }

    protected void updateValues() {
        Platform.runLater(() -> {
            Duration currentTime = mediaPlayer.getCurrentTime();
            seekSlider.setValue(currentTime.toSeconds());

        });
    }
}
