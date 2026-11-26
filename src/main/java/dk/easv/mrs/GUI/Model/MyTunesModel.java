package dk.easv.mrs.GUI.Model;

import dk.easv.mrs.BE.Playlist;
import dk.easv.mrs.BE.Song;
import dk.easv.mrs.BLL.PlaylistManager;
import dk.easv.mrs.BLL.SongManager;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * GUI model. Holds the observable lists the views are bound to and keeps them
 * in sync with the database through the business layer.
 */
public class MyTunesModel
{

  private FilteredList<Song> filteredList;
  private ObservableList<Song> songsToBeViewed;
  private ObservableList<Playlist> playlists;
  private ObservableList<Song> playlistSongs;
  private PlaylistManager playlistManager;

  private SongManager songManager;

  public MyTunesModel() throws Exception
  {
    songManager = new SongManager();
    songsToBeViewed = FXCollections.observableArrayList();
    songsToBeViewed.addAll(songManager.getAllSongs());
    filteredList = new FilteredList<>(songsToBeViewed);
    playlistManager = new PlaylistManager();
    playlists = FXCollections.observableArrayList(
        playlistManager.getAllPlaylists());
    playlistSongs = FXCollections.observableArrayList();
  }

  public FilteredList<Song> getObservableSongs()
  {
    return filteredList;
  }

  public void filterSongs(String query)
  {
    filteredList.setPredicate(song -> songManager.matches(song, query));
  }

  public void clearFilter()
  {
    filteredList.setPredicate(song -> true);
  }

  public void createSong(Song newSong) throws Exception
  {
    Song createdSong = songManager.createSong(newSong);
    songsToBeViewed.add(createdSong);
  }

  public void editSong(Song editedSong) throws Exception
  {
    songManager.editSong(editedSong);

    for (int i = 0; i < songsToBeViewed.size(); i++)
    {
      if (songsToBeViewed.get(i).getId() == editedSong.getId())
      {
        songsToBeViewed.set(i, editedSong);
        break;
      }
    }
    for (int i = 0; i < playlistSongs.size(); i++)
    {
      if (playlistSongs.get(i).getId() == editedSong.getId())
      {
        playlistSongs.set(i, editedSong);
        break;
      }
    }
  }

  public void deleteSong(Song selectedSong) throws Exception
  {
    songManager.deleteSong(selectedSong);
    songsToBeViewed.remove(selectedSong);
    playlistSongs.removeIf(s -> s.getId() == selectedSong.getId());

    // refresh song count and total time of every playlist
    for (Playlist fresh : playlistManager.getAllPlaylists())
    {
      for (Playlist p : playlists)
      {
        if (p.getId() == fresh.getId())
        {
          p.setSongCount(fresh.getSongCount());
          p.setPlaylistTime(fresh.getPlaylistTime());
        }
      }
    }
  }

  public ObservableList<Playlist> getObservablePlaylists()
  {
    return playlists;
  }

  public ObservableList<Song> getObservablePlaylistSongs()
  {
    return playlistSongs;
  }

  public void createPlaylist(String name) throws Exception
  {
    playlists.add(playlistManager.createPlaylist(name));
  }

  public void loadSongsOnPlaylist(Playlist playlist) throws Exception
  {
    playlistSongs.setAll(playlistManager.getSongsOnPlaylist(playlist));
  }

  public void addSongToPlaylist(Playlist playlist, Song song) throws Exception
  {
    playlistManager.addSongToPlaylist(playlist, song);

    // keep the GUI in sync without reloading from the database
    playlist.setSongCount(playlist.getSongCount() + 1);
    playlist.setPlaylistTime(playlist.getPlaylistTime() + song.getPlaytime());
    playlistSongs.add(song);
  }

  public void editPlaylist(Playlist playlist, String newName) throws Exception
  {
    playlistManager.editPlaylist(playlist, newName);
    playlist.setName(newName);
  }

  public void deletePlaylist(Playlist playlist) throws Exception
  {
    playlistManager.deletePlaylist(playlist);
    playlists.remove(playlist);
    playlistSongs.clear();
  }

  public void removeSongFromPlaylist(Playlist playlist, int index)
      throws Exception
  {
    List<Song> newOrder = new ArrayList<>(playlistSongs);
    Song removed = newOrder.remove(index);
    playlistManager.saveSongOrder(playlist, newOrder);   // database first
    playlistSongs.remove(index);                         // then the GUI
    playlist.setSongCount(playlist.getSongCount() - 1);
    playlist.setPlaylistTime(
        playlist.getPlaylistTime() - removed.getPlaytime());
  }

  public void moveSongInPlaylist(Playlist playlist, int index, int offset)
      throws Exception
  {
    int newIndex = index + offset;
    if (newIndex < 0 || newIndex >= playlistSongs.size())
      return;
    List<Song> newOrder = new ArrayList<>(playlistSongs);
    Collections.swap(newOrder, index, newIndex);
    playlistManager.saveSongOrder(playlist, newOrder);
    Collections.swap(playlistSongs, index, newIndex);
  }
}