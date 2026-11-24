package dk.easv.mrs.GUI.Controller;

// java imports
import dk.easv.mrs.BE.Playlist;
import dk.easv.mrs.BE.Song;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.util.ResourceBundle;

// project imports
import dk.easv.mrs.GUI.Model.MyTunesModel;

public class MyTunesViewController implements Initializable {


    @FXML private TableView<Song> tblSong;
    @FXML private TableColumn<Song, String> colTitle;
    @FXML private TableColumn<Song, String> colArtist;
    @FXML private TableColumn<Song, String> colGenre;
    @FXML private TableColumn<Song, Integer> colPlaytime;

    @FXML private TableView<Playlist> tblPlaylist;
    @FXML private TableColumn<Playlist, String> colPlaylistName;
    @FXML private TableColumn<Playlist, String> colPlaylistSongs;
    @FXML private TableColumn<Playlist, Integer> colPlaylistTime;

    @FXML private TableView<Song> tblPlaylistSongs;
    @FXML private TableColumn<Song, String> colPlaylistSongTitle;
    @FXML private TableColumn<Song, String> colPlaylistSongArtist;

    @FXML private TextField txtSongSearch;
    @FXML private TextField txtSongTitle;
    @FXML private TextField txtSongArtist;
    @FXML private TextField txtSongGenre;
    @FXML private TextField txtSongPlaytime;

    @FXML private Button btnNewSong;
    @FXML private Button btnEdit;
    @FXML private Button btnDelete;
    @FXML private Button btnNewPlaylist;

    private MyTunesModel myTunesModel;

    public MyTunesViewController() {
        try {
            myTunesModel = new MyTunesModel();
        } catch (Exception e) {
            displayError(e);
            e.printStackTrace();
        }
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
                        || song.getArtist().toLowerCase().contains(lowerCaseFilter);
            });
        });

        // fill the text fields when a song is selected
        tblSong.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                txtSongTitle.setText(newValue.getTitle());
                txtSongArtist.setText(newValue.getArtist());
                txtSongGenre.setText(newValue.getGenre());
                txtSongPlaytime.setText(String.valueOf(newValue.getPlaytime()));
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
        if (!txtSongTitle.getText().isEmpty() &&
                !txtSongArtist.getText().isEmpty() &&
                !txtSongGenre.getText().isEmpty() &&
                !txtSongPlaytime.getText().isEmpty()) {

            try {
                String title = txtSongTitle.getText();
                String artist = txtSongArtist.getText();
                String genre = txtSongGenre.getText();
                int playtime = Integer.parseInt(txtSongPlaytime.getText());

                Song newSong = new Song(-1, title, artist, genre, playtime);
                myTunesModel.createSong(newSong);
            } catch (Exception err) {
                displayError(err);
            }
        }
    }

    @FXML
    private void handleUpdateSong(ActionEvent actionEvent) {
        Song selectedSong = tblSong.getSelectionModel().getSelectedItem();

        if (selectedSong != null) {
            try {
                // update selectedSong
                selectedSong.setTitle(txtSongTitle.getText());
                selectedSong.setArtist(txtSongArtist.getText());
                selectedSong.setGenre(txtSongGenre.getText());
                selectedSong.setPlaytime(Integer.parseInt(txtSongPlaytime.getText()));

                myTunesModel.editSong(selectedSong);
                tblSong.refresh();
            } catch (Exception err) {
                displayError(err);
            }
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

    public void handleNewPlaylist(ActionEvent actionEvent) {
    }
}