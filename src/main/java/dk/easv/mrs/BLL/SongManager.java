package dk.easv.mrs.BLL;
import dk.easv.mrs.BE.Song;
import dk.easv.mrs.BLL.util.SongSearcher;
import dk.easv.mrs.DAL.IMyTunesDataAccess;
import dk.easv.mrs.DAL.db.MyTunesDAO_DB;

import java.io.IOException;
import java.util.List;

/** Business layer for songs. Handles song operations and filter matching, and uses the data access layer. */
public class SongManager {

    private SongSearcher songSearcher = new SongSearcher();
    private IMyTunesDataAccess myTunesDAO;

    public SongManager() throws IOException {
        myTunesDAO = new MyTunesDAO_DB();
    }

    public List<Song> getAllSongs() throws Exception {
        return myTunesDAO.getAllSongs();
    }

    public boolean matches(Song song, String query) {
        return songSearcher.matches(song, query);
     }

    public Song createSong(Song newSong) throws Exception {
        return myTunesDAO.createSong(newSong);
    }

    public void editSong(Song editedSong) throws Exception {
        myTunesDAO.editSong(editedSong);
    }


    public void deleteSong(Song selectedSong) throws Exception {
        myTunesDAO.deleteSong(selectedSong);
    }
}
