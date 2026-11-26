package dk.easv.mrs.BLL;

import dk.easv.mrs.BE.Playlist;
import dk.easv.mrs.BE.Song;
import dk.easv.mrs.DAL.IMyTunesDataAccess;
import dk.easv.mrs.DAL.db.MyTunesDAO_DB;

import java.io.IOException;
import java.util.List;

public class PlaylistManager {

    private IMyTunesDataAccess myTunesDAO;

    public PlaylistManager() throws IOException {
        myTunesDAO = new MyTunesDAO_DB();
    }

    public List<Playlist> getAllPlaylists() throws Exception {
        return myTunesDAO.getAllPlaylists();
    }

    public Playlist createPlaylist(String name) throws Exception {
        return myTunesDAO.createPlaylist(name);
    }

    public List<Song> getSongsOnPlaylist(Playlist playlist) throws Exception {
        return myTunesDAO.getSongsOnPlaylist(playlist.getId());
    }

    public void addSongToPlaylist(Playlist playlist, Song song) throws Exception {
        myTunesDAO.addSongToPlaylist(playlist.getId(), song.getId());
    }

    public void editPlaylist(Playlist playlist, String newName) throws Exception {
        myTunesDAO.updatePlaylist(playlist.getId(), newName);
    }

    public void deletePlaylist(Playlist playlist) throws Exception {
        myTunesDAO.deletePlaylist(playlist.getId());
    }

    public void saveSongOrder(Playlist playlist, List<Song> songs) throws Exception {
        myTunesDAO.saveSongOrder(playlist.getId(), songs);
    }
}