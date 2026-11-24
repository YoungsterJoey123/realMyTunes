package dk.easv.mrs.GUI.Model;

import dk.easv.mrs.BE.Song;
import dk.easv.mrs.BLL.SongManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;

import java.util.List;

public class MyTunesModel {

    private FilteredList<Song> filteredList;
    private ObservableList<Song> songsToBeViewed;

    private SongManager songManager;

    public MyTunesModel() throws Exception {
        songManager = new SongManager();
        songsToBeViewed = FXCollections.observableArrayList();
        songsToBeViewed.addAll(songManager.getAllSongs());
        filteredList = new FilteredList<>(songsToBeViewed);
    }

    public FilteredList<Song> getObservableSongs() {
        return filteredList;
    }

    public void searchSong(String query) throws Exception {
        List<Song> searchResults = songManager.searchSong(query);   // was searchSongs
        songsToBeViewed.clear();
        songsToBeViewed.addAll(searchResults);
    }

    public void createSong(Song newSong) throws Exception {
        Song createdSong = songManager.createSong(newSong);
        songsToBeViewed.add(createdSong);
    }

    public void editSong(Song editedSong) throws Exception {
        songManager.editSong(editedSong);                          // was updateSong
        songsToBeViewed.set(songsToBeViewed.indexOf(editedSong), editedSong);
    }

    public void deleteSong(Song selectedSong) throws Exception {
        songManager.deleteSong(selectedSong);
        songsToBeViewed.remove(selectedSong);
    }
}