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

/** Controller for the main window: song and playlist tables, filter and player controls. */
public class MyTunesViewController implements Initializable
{

  @FXML private TableView<Song> tblSong;
  @FXML private TableColumn<Song, String> colTitle, colGenre, colArtist;
  @FXML private TableColumn<Song, Integer> colPlaytime;

  @FXML private TableView<Playlist> tblPlaylist;
  @FXML private TableColumn<Playlist, String> colPlaylistName;
  @FXML private TableColumn<Playlist, Integer> colPlaylistSongs;
  @FXML private TableColumn<Playlist, Integer> colPlaylistTime;

  @FXML private ListView<Song> tblPlaylistSongs;

  @FXML private TextField txtSongSearch;
  @FXML private Button btnFilter, btnNewSong, btnEditSong, btnDeleteSong, btnNewPlaylist, btnEditPlaylist, btnDeletePlaylist, btnDeleteFromPlaylist, btnAddToPlaylist, btnRewind, btnPause, btnSkip, btnMoveDown, btnMoveUp;

  @FXML private Label lblSongName, lblArtist, lblTimePassed, lblTimeLeft;

  @FXML private Slider sldSongLength, sldVolume;
  @FXML private ImageView imgSongArt;

  private MyTunesModel myTunesModel;
  private MediaPlayer mediaPlayer;
  private Song currentSong;
  private Song selectedSong;
  private boolean filterActive = false;

  public MyTunesViewController()
  {
    try
    {
      myTunesModel = new MyTunesModel();
    }
    catch (Exception e)
    {
      displayError(e);
      e.printStackTrace();
    }
  }
  /*
    formats seconds into h:mm:ss, or m:ss when under an hour
    s / 3600 is the number of whole hours
    (s % 3600) / 60 is the minutes left after removing the hours
    s % 60 is the seconds left after removing the minutes
     */
  private String formatTime(double seconds) {
    int s = (int) Math.max(0, seconds);   // never show a negative time
    int hours = s / 3600;
    int minutes = (s % 3600) / 60;
    int secs = s % 60;

    if (hours > 0) {
      return String.format("%d:%02d:%02d", hours, minutes, secs);
    }
    return String.format("%d:%02d", minutes, secs);
  }

  @FXML private void handleFilterClick(ActionEvent actionEvent)
  {
    if (!filterActive)
    {
      String query = txtSongSearch.getText();
      if (query == null || query.isBlank())
        return;

      myTunesModel.filterSongs(query.trim());
      btnFilter.setText("Clear");
    }
    else
    {
      myTunesModel.clearFilter();
      txtSongSearch.clear();
      btnFilter.setText("Filter");
    }
    filterActive = !filterActive;
  }

  @Override public void initialize(URL url, ResourceBundle resourceBundle)
  {

    // setup columns in tableview
    colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
    colArtist.setCellValueFactory(new PropertyValueFactory<>("artist"));
    colGenre.setCellValueFactory(new PropertyValueFactory<>("genre"));
    colPlaytime.setCellValueFactory(new PropertyValueFactory<>("playtime"));
    colPlaytime.setCellFactory(col -> new TableCell<>() {
      @Override
      protected void updateItem(Integer seconds, boolean empty) {
        super.updateItem(seconds, empty);
        setText(empty || seconds == null ? null : formatTime(seconds));
      }
    });

    // connect tableview to the list (filtered, then sorted)
    SortedList<Song> sortedData = new SortedList<>(
        myTunesModel.getObservableSongs());
    sortedData.comparatorProperty().bind(tblSong.comparatorProperty());
    tblSong.setItems(sortedData);

    // playlists table
    colPlaylistName.setCellValueFactory(new PropertyValueFactory<>("name"));
    colPlaylistSongs.setCellValueFactory(
        new PropertyValueFactory<>("songCount"));
    colPlaylistTime.setCellValueFactory(
        new PropertyValueFactory<>("playlistTime"));
    colPlaylistTime.setCellFactory(col -> new TableCell<>() {
      @Override
      protected void updateItem(Integer seconds, boolean empty) {
        super.updateItem(seconds, empty);
        setText(empty || seconds == null ? null : formatTime(seconds));
      }
    });
    tblPlaylist.setItems(myTunesModel.getObservablePlaylists());

    // songs on the selected playlist
    tblPlaylistSongs.setItems(myTunesModel.getObservablePlaylistSongs());
    txtSongSearch.setOnAction(this::handleFilterClick);

    tblPlaylist.getSelectionModel().selectedItemProperty()
         .addListener((obs, oldValue, newValue) -> {
          if (newValue != null)
          {
            try
            {
              myTunesModel.loadSongsOnPlaylist(newValue);
            }
            catch (Exception e)
            {
              displayError(e);
            }
          }
        });
    // listeners for the song list and the playlist list
    tblSong.getSelectionModel().selectedItemProperty().addListener((obs, old, song) -> {
      if (song != null) {
        selectedSong = song;
        tblPlaylistSongs.getSelectionModel().clearSelection();
      }
      updatePlayButton();
    });

    tblPlaylistSongs.getSelectionModel().selectedItemProperty().addListener((obs, old, song) -> {
      if (song != null) {
        selectedSong = song;
        tblSong.getSelectionModel().clearSelection();
      }
      updatePlayButton();
    });
    // setting values for the volume slider
    sldVolume.setMin(0);
    sldVolume.setMax(1);
    sldVolume.setValue(0.5);
    // adding a listener to the volume slider, if something is playing, set the volume to whatever value the slider is on
    sldVolume.valueProperty().addListener((obs, old, v) -> {
      if (mediaPlayer != null) {
        mediaPlayer.setVolume(v.doubleValue());
      }
    });
    tblSong.getSelectionModel().selectedItemProperty()
            .addListener((obs, oldSong, newSong) -> updatePlayButton());
  }

  private void displayError(Throwable t)
  {
    Alert alert = new Alert(Alert.AlertType.ERROR);
    alert.setTitle("Something went wrong");
    alert.setHeaderText(t.getMessage());
    alert.showAndWait();
  }

  @FXML private void handleCreateNewSong(ActionEvent actionEvent)
  {
    Song newSong = showSongDialog(null);
    if (newSong == null)
      return;
    try
    {
      myTunesModel.createSong(newSong);
    }
    catch (Exception err)
    {
      displayError(err);
    }
  }

  @FXML private void handleUpdateSong(ActionEvent actionEvent)
  {
    Song selectedSong = tblSong.getSelectionModel().getSelectedItem();
    if (selectedSong == null)
      return;

    Song edited = showSongDialog(
        selectedSong);   // a new object, original is untouched
    if (edited == null)
      return;
    try
    {
      myTunesModel.editSong(edited);
    }
    catch (Exception err)
    {
      displayError(err);
    }
  }

  /** Opens the New/Edit dialog. Returns the entered song, or null if cancelled. */
  private Song showSongDialog(Song song)
  {
    try
    {
      FXMLLoader loader = new FXMLLoader(
          getClass().getResource("/views/SongDialogView.fxml"));
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
    }
    catch (Exception e)
    {
      displayError(e);
      return null;
    }
  }

  @FXML private void handleDeleteSong(ActionEvent actionEvent)
  {
    Song selectedSong = tblSong.getSelectionModel().getSelectedItem();
    if (selectedSong == null)
      return;

    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
        "Delete \"" + selectedSong
            + "\"? It will also be removed from all playlists.", ButtonType.YES,
        ButtonType.NO);
    confirm.setHeaderText("Delete song");
    if (confirm.showAndWait().orElse(ButtonType.NO) != ButtonType.YES)
      return;

    try
    {
      myTunesModel.deleteSong(selectedSong);
      tblPlaylist.refresh();   // redraws the Songs and Time columns
    }
    catch (Exception err)
    {
      displayError(err);
    }
  }

  @FXML private void handleNewPlaylist(ActionEvent actionEvent)
  {
    TextInputDialog dialog = new TextInputDialog();
    dialog.setTitle("New Playlist");
    dialog.setHeaderText("Name your playlist");
    dialog.setContentText("Name:");

    dialog.showAndWait().ifPresent(name -> {
      if (!name.isBlank())
      {
        try
        {
          myTunesModel.createPlaylist(name.trim());
        }
        catch (Exception e)
        {
          displayError(e);
        }
      }
    });
  }

  @FXML private void handleAddClick(ActionEvent actionEvent)
  {
    Playlist selectedPlaylist = tblPlaylist.getSelectionModel()
        .getSelectedItem();
    Song selectedSong = tblSong.getSelectionModel().getSelectedItem();

    if (selectedPlaylist != null && selectedSong != null)
    {
      try
      {
        myTunesModel.addSongToPlaylist(selectedPlaylist, selectedSong);
        tblPlaylist.refresh();   // updates the Songs and Time columns
      }
      catch (Exception e)
      {
        displayError(e);
      }
    }
  }

  @FXML private void handleUpdatePlaylist(ActionEvent e)
  {
    Playlist selected = tblPlaylist.getSelectionModel().getSelectedItem();
    if (selected == null)
      return;

    TextInputDialog dialog = new TextInputDialog(selected.getName());
    dialog.setTitle("Edit Playlist");
    dialog.setHeaderText("Rename your playlist");
    dialog.setContentText("Name:");
    dialog.showAndWait().ifPresent(name -> {
      if (name.isBlank())
        return;
      try
      {
        myTunesModel.editPlaylist(selected, name.trim());
        tblPlaylist.refresh();
      }
      catch (Exception ex)
      {
        displayError(ex);
      }
    });
  }

  @FXML private void handleDeletePlaylist(ActionEvent e)
  {
    Playlist selected = tblPlaylist.getSelectionModel().getSelectedItem();
    if (selected == null)
      return;

    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
        "Delete playlist \"" + selected.getName()
            + "\"? The songs themselves are not deleted.", ButtonType.OK,
        ButtonType.CANCEL);
    confirm.setHeaderText("Are you sure?");
    if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK)
      return;
    try
    {
      myTunesModel.deletePlaylist(selected);
    }
    catch (Exception ex)
    {
      displayError(ex);
    }
  }

  @FXML private void handleDeleteFromPlaylist(ActionEvent e)
  {
    Playlist playlist = tblPlaylist.getSelectionModel().getSelectedItem();
    int index = tblPlaylistSongs.getSelectionModel().getSelectedIndex();
    if (playlist == null || index < 0)
      return;
    try
    {
      myTunesModel.removeSongFromPlaylist(playlist, index);
      tblPlaylist.refresh();
    }
    catch (Exception ex)
    {
      displayError(ex);
    }
  }

  @FXML private void handleMoveUp(ActionEvent e)
  {
    moveSelectedSong(-1);
  }

  @FXML private void handleMoveDown(ActionEvent e)
  {
    moveSelectedSong(1);
  }

  private void moveSelectedSong(int offset) {
    Playlist playlist = tblPlaylist.getSelectionModel().getSelectedItem();
    int index = tblPlaylistSongs.getSelectionModel().getSelectedIndex();
    if (playlist == null || index < 0) return;

    int newIndex = index + offset;
    if (newIndex < 0 || newIndex >= tblPlaylistSongs.getItems().size()) return;   // already first or last

    try {
      myTunesModel.moveSongInPlaylist(playlist, index, offset);
      tblPlaylistSongs.getSelectionModel().select(newIndex);  // keep the song selected
    } catch (Exception ex) { displayError(ex); }
  }
  private void playSong(Song song)
  {
    if (song == null || song.getFilePath() == null)
      return;

    File file = new File(song.getFilePath());
    if (!file.exists())
    {
      System.out.println("File not found: " + file.getAbsolutePath());
      return;                      // old song keeps playing, nothing is disposed
    }

    // stop and release the previous song
    if (mediaPlayer != null)
    {
      mediaPlayer.stop();
      mediaPlayer.dispose();
      mediaPlayer = null;
    }

    Media media = new Media(file.toURI().toString());
    mediaPlayer = new MediaPlayer(media);
    currentSong = song;

    lblSongName.setText(song.getTitle());
    lblArtist.setText(song.getArtist());
    mediaPlayer.setVolume(sldVolume.getValue());

    mediaPlayer.setOnReady(
            () -> sldSongLength.setMax(media.getDuration().toSeconds()));

    mediaPlayer.currentTimeProperty().addListener((obs, old, now) -> {
      if (!sldSongLength.isValueChanging())
      {
        sldSongLength.setValue(now.toSeconds());
      }
      lblTimePassed.setText(formatTime(now.toSeconds()));
      lblTimeLeft.setText(
              formatTime(media.getDuration().toSeconds() - now.toSeconds()));
    });

    // keep the play/pause button in sync
    mediaPlayer.statusProperty().addListener((obs, old, status) -> updatePlayButton());
    mediaPlayer.setOnEndOfMedia(() -> {
      mediaPlayer.stop();
      updatePlayButton();
    });

    media.setOnError(() -> System.out.println("Media error: " + media.getError()));
    mediaPlayer.setOnError(() -> System.out.println("Player error: " + mediaPlayer.getError()));

    mediaPlayer.play();
  }

  public void handleRewindClick(ActionEvent actionEvent)
  {
  }

  public void handlePauseClick(ActionEvent actionEvent)
  {
    Song selected = selectedSong;

    boolean isNewSong = selected != null
            && (currentSong == null || selected.getId() != currentSong.getId());

    if (isNewSong) {
      playSong(selected);
      return;
    }
    if (mediaPlayer == null) return;

    if (mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
      mediaPlayer.pause();
    } else {
      mediaPlayer.play();
    }
  }
  // updates the symbol of the play/pause button depending on which song is selected and/or already playing
  private void updatePlayButton() {
    Song selected = selectedSong;

    boolean sameSong = selected == null || (currentSong != null && selected.getId() == currentSong.getId());
    boolean playing = mediaPlayer != null && mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING;

    btnPause.setText(playing && sameSong ? "⏸" : "▶");
  }

  public void handleSkipClick(ActionEvent actionEvent)
  {
  }

}