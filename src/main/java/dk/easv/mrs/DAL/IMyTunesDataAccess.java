package dk.easv.mrs.DAL;
import dk.easv.mrs.BE.Playlist;
import dk.easv.mrs.BE.Song;

import java.util.List;

/** Data access contract. The business layer only depends on this interface, not on the database. */
public interface IMyTunesDataAccess {

  /** Returns all songs in the library. */
    List<Song> getAllSongs() throws Exception;

  /** Saves a new song and returns it with its generated id. */
  Song createSong(Song newSong) throws Exception;

  /** Updates title, artist, category, playtime and file of an existing song. */
  void editSong(Song song) throws Exception;

  /** Deletes a song and removes it from all playlists. */
  void deleteSong(Song song) throws Exception;

  /** Returns all playlists with their song count and total playtime. */
  List<Playlist> getAllPlaylists() throws Exception;

  /** Saves a new empty playlist and returns it with its generated id. */
  Playlist createPlaylist(String name) throws Exception;

  /** Returns the songs on a playlist in playlist order. */
  List<Song> getSongsOnPlaylist(int playlistId) throws Exception;

  /** Adds a song to the end of a playlist. */
  void addSongToPlaylist(int playlistId, int songId) throws Exception;

  /** Renames a playlist. */
  void updatePlaylist(int playlistId, String name) throws Exception;

  /** Deletes a playlist but not the songs on it. */
  void deletePlaylist(int playlistId) throws Exception;

  /** Replaces the content of a playlist with the given songs, in the given order. */
  void saveSongOrder(int playlistId, List<Song> songs) throws Exception;

}
