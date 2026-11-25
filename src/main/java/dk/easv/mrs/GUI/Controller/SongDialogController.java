package dk.easv.mrs.GUI.Controller;

import dk.easv.mrs.BE.Song;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Controller for the New/Edit Song dialog.
 * Usage: call setSong(null) for a new song or setSong(song) to edit one,
 * show the dialog with showAndWait(), then read getResult().
 * getResult() is null if the user cancelled.
 */
public class SongDialogController {

    private static final String MUSIC_FOLDER = "music";

    @FXML private Label lblHeader, lblError;
    @FXML private TextField txtTitle, txtArtist, txtGenre, txtTime, txtFile;

    private Song editedSong;          // null when creating a new song
    private File chosenFile;          // set only if the user picked a (new) file
    private String filePath;          // path stored in the database
    private Song result;

    /** Prepares the dialog. Pass null for "New", or an existing song for "Edit". */
    public void setSong(Song song) {
        this.editedSong = song;
        if (song != null) {
            lblHeader.setText("Edit Song");
            txtTitle.setText(song.getTitle());
            txtArtist.setText(song.getArtist());
            txtGenre.setText(song.getGenre());
            txtTime.setText(formatTime(song.getPlaytime()));
            filePath = song.getFilePath();
            txtFile.setText(filePath);
        }
    }

    /** The song the user entered, or null if the dialog was cancelled. */
    public Song getResult() {
        return result;
    }

    @FXML
    private void handleChooseFile(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("MP3 or WAV files", "*.mp3", "*.wav"));
        File file = fileChooser.showOpenDialog(txtTitle.getScene().getWindow());
        if (file == null) return;

        chosenFile = file;
        filePath = MUSIC_FOLDER + "/" + file.getName();
        txtFile.setText(filePath);
        detectDuration(file);
    }

    /** Reads the song length from the file and fills the Time field. */
    private void detectDuration(File file) {
        try {
            Media media = new Media(file.toURI().toString());
            MediaPlayer player = new MediaPlayer(media);
            player.setOnReady(() -> {
                txtTime.setText(formatTime((int) media.getDuration().toSeconds()));
                player.dispose();
            });
            player.setOnError(player::dispose);   // user can type the time manually
        } catch (Exception e) {
            // ignore, the user can type the time manually
        }
    }

    @FXML
    private void handleSave(ActionEvent event) {
        String title = txtTitle.getText().trim();
        String artist = txtArtist.getText().trim();
        String genre = txtGenre.getText().trim();

        if (title.isEmpty() || artist.isEmpty() || genre.isEmpty()) {
            lblError.setText("Title, artist and category are required.");
            return;
        }
        int playtime = parseTime(txtTime.getText());
        if (playtime < 0) {
            lblError.setText("Time must look like 3:45 (or just seconds).");
            return;
        }
        if (filePath == null || filePath.isBlank()) {
            lblError.setText("Please choose a song file.");
            return;
        }

        try {
            copyFileToMusicFolder();
        } catch (Exception e) {
            lblError.setText("Could not copy the file: " + e.getMessage());
            return;
        }

        int id = (editedSong == null) ? -1 : editedSong.getId();
        result = new Song(id, title, artist, genre, playtime, filePath);
        close();
    }

    @FXML
    private void handleCancel(ActionEvent event) {
        result = null;
        close();
    }

    /** Copies a newly chosen file into the music folder, unless it is already there. */
    private void copyFileToMusicFolder() throws Exception {
        if (chosenFile == null) return;
        Path folder = Path.of(MUSIC_FOLDER);
        Files.createDirectories(folder);
        Path target = folder.resolve(chosenFile.getName());
        if (!Files.exists(target)) {
            Files.copy(chosenFile.toPath(), target);
        }
    }

    private void close() {
        ((Stage) txtTitle.getScene().getWindow()).close();
    }

    /** Accepts "m:ss" or plain seconds. Returns -1 if invalid. */
    private int parseTime(String text) {
        try {
            text = text.trim();
            if (text.contains(":")) {
                String[] parts = text.split(":");
                int minutes = Integer.parseInt(parts[0].trim());
                int seconds = Integer.parseInt(parts[1].trim());
                if (parts.length != 2 || minutes < 0 || seconds < 0 || seconds > 59) return -1;
                return minutes * 60 + seconds;
            }
            int seconds = Integer.parseInt(text);
            return seconds >= 0 ? seconds : -1;
        } catch (Exception e) {
            return -1;
        }
    }

    private String formatTime(int totalSeconds) {
        return String.format("%d:%02d", totalSeconds / 60, totalSeconds % 60);
    }
}
