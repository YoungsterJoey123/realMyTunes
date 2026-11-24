package dk.easv.mrs.BLL;
import dk.easv.mrs.BE.Song;
import dk.easv.mrs.BLL.util.SongSearcher;
import dk.easv.mrs.DAL.IMyTunesDataAccess;
import dk.easv.mrs.DAL.db.MyTunesDAO_DB;

import java.io.IOException;
import java.util.List;

public class SongManager {

    private SongSearcher songSearcher = new SongSearcher();
    private IMyTunesDataAccess myTunesDAO;

    public SongManager() throws IOException {
        myTunesDAO = new MyTunesDAO_DB();
    }

    public List<Song> getAllSongs() throws Exception {
        return myTunesDAO.getAllSongs();
    }

    public List<Song> searchSong(String query) throws Exception {
        List<Song> allSongs = getAllSongs();
        return songSearcher.search(allSongs, query);
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
