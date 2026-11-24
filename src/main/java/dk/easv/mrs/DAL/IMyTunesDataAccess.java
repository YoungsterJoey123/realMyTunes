package dk.easv.mrs.DAL;
import dk.easv.mrs.BE.Song;

import java.util.List;

public interface IMyTunesDataAccess {

    List<Song> getAllSongs() throws Exception;

    Song createSong(Song newSong) throws Exception;

    void editSong(Song song) throws Exception;

    void deleteSong(Song song) throws Exception;

}
