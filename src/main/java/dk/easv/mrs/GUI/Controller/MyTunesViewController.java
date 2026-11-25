package dk.easv.mrs.GUI.Controller;

// java imports
import dk.easv.mrs.BE.Playlist;
import dk.easv.mrs.BE.Song;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.Modality;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

// project imports
import dk.easv.mrs.GUI.Model.MyTunesModel;
import javafx.stage.FileChooser;

public class MyTunesViewController implements Initializable {

    @FXML private TableView<Song> tblSong;
    @FXML private TableColumn<Song, String> colTitle, colGenre, colArtist;
    @FXML private TableColumn<Song, Integer> colPlaytime;

    @FXML private TableView<Playlist> tblPlaylist;
    @FXML private TableColumn<Playlist, String> colPlaylistName, colPlaylistSongs;
    @FXML private TableColumn<Playlist, Integer> colPlaylistTime;

    //@FXML private TableView<Song> tblPlaylistSongs;
    //@FXML private TableColumn<Song, String> colPlaylistSongTitle, colPlaylistSongArtist;
    @FXML private ListView<Song> tblPlaylistSongs;

    @FXML private TextField txtSongSearch;
    @FXML private Button btnNewSong, btnEditSong, btnDeleteSong, btnNewPlaylist, btnEditPlaylist, btnDeletePlaylist, btnDeleteFromPlaylist, btnAddToPlaylist, btnRewind, btnPause, btnSkip;
    @FXML private Label lblSongName, lblArtist, lblTimePassed, lblTimeLeft;

    @FXML private Slider sldSongLength, sldVolume;
    @FXML private ImageView imgSongArt;


    private MyTunesModel myTunesModel;
    private MediaPlayer mediaPlayer;

    public MyTunesViewController() {
        try {
            myTunesModel = new MyTunesModel();
        } catch (Exception e) {
            displayError(e);
            e.printStackTrace();
        }
    }
    private void playSong(Song song) {
        if (song == null || song.getFilePath() == null) return;

        // stop and release the previous song
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.dispose();
        }

        File file = new File(song.getFilePath());
        if (!file.exists()) {
            System.out.println("File not found: " + file.getAbsolutePath());
            return;
        }

        Media media = new Media(file.toURI().toString());
        mediaPlayer = new MediaPlayer(media);

        lblSongName.setText(song.getTitle());
        lblArtist.setText(song.getArtist());
        mediaPlayer.setVolume(sldVolume.getValue());

        mediaPlayer.setOnReady(() ->
                sldSongLength.setMax(media.getDuration().toSeconds()));

        mediaPlayer.currentTimeProperty().addListener((obs, old, now) -> {
            if (!sldSongLength.isValueChanging()) {
                sldSongLength.setValue(now.toSeconds());
            }
            lblTimePassed.setText(formatTime(now.toSeconds()));
            lblTimeLeft.setText(formatTime(media.getDuration().toSeconds() - now.toSeconds()));
        });
        mediaPlayer.play();
    }

    /*
    formats seconds into minutes:seconds
    s / 60 returns how many times s can be divided into minutes
    s % 60 returns how many seconds are left after dividing into minutes
     */
    private String formatTime(double seconds) {
        int s = (int) seconds;
        return String.format("%d:%02d", s / 60, s % 60);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        // setup columns in tableview
        colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colArtist.setCellValueFactory(new PropertyValueFactory<>("artist"));
        colGenre.setCellValueFactory(new PropertyValueFactory<>("genre"));
        colPlaytime.setCellValueFactory(new PropertyValueFactory<>("playtime"));

        // connect tableview to the list (filtered, then sorted)
        SortedList<Song> sortedData = new SortedList<>(myTunesModel.getObservableSongs());
        sortedData.comparatorProperty().bind(tblSong.comparatorProperty());
        tblSong.setItems(sortedData);

        // search filter
        txtSongSearch.textProperty().addListener((observableValue, oldValue, newValue) -> {
            myTunesModel.getObservableSongs().setPredicate(song -> {

                // If filter text is empty, display all songs.
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }

                String lowerCaseFilter = newValue.toLowerCase();

                return song.getTitle().toLowerCase().contains(lowerCaseFilter)
                        || song.getArtist().toLowerCase().contains(lowerCaseFilter)
                        || song.getGenre().toLowerCase().contains(lowerCaseFilter);
            });
        });

        // playlists table
        colPlaylistName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colPlaylistSongs.setCellValueFactory(new PropertyValueFactory<>("songCount"));
        colPlaylistTime.setCellValueFactory(new PropertyValueFactory<>("playlistTime"));
        tblPlaylist.setItems(myTunesModel.getObservablePlaylists());

        // songs on the selected playlist
        //colPlaylistSongTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        //colPlaylistSongArtist.setCellValueFactory(new PropertyValueFactory<>("artist"));
        tblPlaylistSongs.setItems(myTunesModel.getObservablePlaylistSongs());

        tblPlaylist.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue != null) {
                try {
                    myTunesModel.loadSongsOnPlaylist(newValue);
                } catch (Exception e) {
                    displayError(e);
                }
            }
        });
    }

    private void displayError(Throwable t) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Something went wrong");
        alert.setHeaderText(t.getMessage());
        alert.showAndWait();
    }

    @FXML
    private void handleCreateNewSong(ActionEvent actionEvent) {
    Song newSong = showSongDialog(null);
    if (newSong == null) return;
    try {
      myTunesModel.createSong(newSong);
    } catch (Exception err) {
      displayError(err);
    }
  }

    @FXML
    private void handleUpdateSong(ActionEvent actionEvent) {
    Song selectedSong = tblSong.getSelectionModel().getSelectedItem();
    if (selectedSong == null) return;

    Song edited = showSongDialog(selectedSong);   // a new object, original is untouched
    if (edited == null) return;
    try {
      myTunesModel.editSong(edited);
    } catch (Exception err) {
      displayError(err);
    }
  }

  /** Opens the New/Edit dialog. Returns the entered song, or null if cancelled. */
  private Song showSongDialog(Song song) {
    try {
      FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/SongDialogView.fxml"));
      Parent root = loader.load();
      SongDialogController dialog = loader.getController();
      dialog.setSong(song);

      Stage stage = new Stage();
      stage.setTitle(song == null ? "New Song" : "Edit Song");
      stage.initModality(Modality.APPLICATION_MODAL);
      stage.initOwner(tblSong.getScene().getWindow());
      stage.setScene(new Scene(root));
      stage.showAndWait();

      return dialog.getResult();
    } catch (Exception e) {
      displayError(e);
      return null;
    }
  }

    @FXML
    private void handleDeleteSong(ActionEvent actionEvent) {
        Song selectedSong = tblSong.getSelectionModel().getSelectedItem();

        if (selectedSong != null) {
            try {
                myTunesModel.deleteSong(selectedSong);
            } catch (Exception err) {
                displayError(err);
            }
        }
    }

    @FXML
    private void handleNewPlaylist(ActionEvent actionEvent) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("New Playlist");
        dialog.setHeaderText("Name your playlist");
        dialog.setContentText("Name:");

        dialog.showAndWait().ifPresent(name -> {
            if (!name.isBlank()) {
                try {
                    myTunesModel.createPlaylist(name.trim());
                } catch (Exception e) {
                    displayError(e);
                }
            }
        });
    }

    @FXML
    private void handleAddClick(ActionEvent actionEvent) {
        Playlist selectedPlaylist = tblPlaylist.getSelectionModel().getSelectedItem();
        Song selectedSong = tblSong.getSelectionModel().getSelectedItem();

        if (selectedPlaylist != null && selectedSong != null) {
            try {
                myTunesModel.addSongToPlaylist(selectedPlaylist, selectedSong);
                tblPlaylist.refresh();   // updates the Songs and Time columns
            } catch (Exception e) {
                displayError(e);
            }
        }
    }

    public void handleRewindClick(ActionEvent actionEvent) {
    }

    public void handlePauseClick(ActionEvent actionEvent) {
    }

    public void handleSkipClick(ActionEvent actionEvent) {
    }

    public void handleUpdatePlaylist(ActionEvent actionEvent) {
    }

    public void handleDeletePlaylist(ActionEvent actionEvent) {
    }

    public void handleDeleteFromPlaylist(ActionEvent actionEvent) {
    }

}