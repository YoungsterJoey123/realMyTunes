package dk.easv.mrs.DAL;
import dk.easv.mrs.BE.Playlist;
import dk.easv.mrs.BE.Song;

import java.util.List;

public interface IMyTunesDataAccess {

    List<Song> getAllSongs() throws Exception;

    Song createSong(Song newSong) throws Exception;

    void editSong(Song song) throws Exception;

    void deleteSong(Song song) throws Exception;

    List<Playlist> getAllPlaylists() throws Exception;

    Playlist createPlaylist(String name) throws Exception;

    List<Song> getSongsOnPlaylist(int playlistId) throws Exception;

    void addSongToPlaylist(int playlistId, int songId) throws Exception;

}
