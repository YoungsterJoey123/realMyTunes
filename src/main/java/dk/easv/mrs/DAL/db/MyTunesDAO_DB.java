package dk.easv.mrs.DAL.db;

import dk.easv.mrs.BE.Song;
import dk.easv.mrs.DAL.IMyTunesDataAccess;
import dk.easv.mrs.BE.Playlist;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class MyTunesDAO_DB implements IMyTunesDataAccess {

    private DBConnector dbConnector = new DBConnector();

    public MyTunesDAO_DB() throws IOException {
    }

    @Override
    public List<Song> getAllSongs() throws Exception {

        List<Song> songs = new ArrayList<>();

        // get data from db
        // try-with-resources
        try (Connection conn = dbConnector.getConnection();
             Statement stmt = conn.createStatement()) {

            String sql = "SELECT * FROM Songs";

            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {

                // transfer rows to arraylist
                int id = rs.getInt("Id");
                String title = rs.getString("Title");
                String artist = rs.getString("Artist");
                String genre = rs.getString("Genre");
                int playtime = rs.getInt("Playtime");

                songs.add(new Song(id, title, artist, genre, playtime));
            }
        }

        return songs;
    }

    @Override
    public Song createSong(Song song) throws Exception {
        String sql = "INSERT INTO Songs (Title, Artist, Genre, Playtime) VALUES (?, ?, ?, ?);";

        try (Connection conn = dbConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            // Bind parameters
            stmt.setString(1, song.getTitle());
            stmt.setString(2, song.getArtist());
            stmt.setString(3, song.getGenre());
            stmt.setInt(4, song.getPlaytime());

            // Run the specified SQL statement
            stmt.executeUpdate();

            // Get the generated ID from the DB
            ResultSet rs = stmt.getGeneratedKeys();
            int id = 0;

            if (rs.next()) {
                id = rs.getInt(1);
            }

            // Create song object and send up the layers
            return new Song(id, song.getTitle(), song.getArtist(), song.getGenre(), song.getPlaytime());
        }
    }

    @Override
    public void editSong(Song song) throws Exception {
        String sql = "UPDATE Songs SET Title = ?, Artist = ?, Genre = ?, Playtime = ? WHERE Id = ?";

        try (Connection conn = dbConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            // Bind parameters
            stmt.setString(1, song.getTitle());
            stmt.setString(2, song.getArtist());
            stmt.setString(3, song.getGenre());
            stmt.setInt(4, song.getPlaytime());
            stmt.setInt(5, song.getId());

            // Run the specified SQL statement
            stmt.executeUpdate();
        }
    }

    @Override
    public void deleteSong(Song song) throws Exception {
        String sql = "DELETE FROM Songs WHERE Id = ?";

        try (Connection conn = dbConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, song.getId());

            stmt.executeUpdate();
        }
    }

    @Override
    public List<Playlist> getAllPlaylists() throws Exception {
        List<Playlist> playlists = new ArrayList<>();

        String sql = "SELECT p.Id, p.Name, COUNT(ps.SongId) AS SongCount, " +
                "ISNULL(SUM(s.[Time]), 0) AS TotalTime " +
                "FROM Playlist p " +
                "LEFT JOIN PlaylistSong ps ON ps.PlaylistId = p.Id " +
                "LEFT JOIN Song s ON s.Id = ps.SongId " +
                "GROUP BY p.Id, p.Name";

        try (Connection conn = dbConnector.getConnection();
             Statement stmt = conn.createStatement()) {

            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                playlists.add(new Playlist(
                        rs.getInt("Id"),
                        rs.getString("Name"),
                        rs.getInt("SongCount"),
                        rs.getInt("TotalTime")));
            }
        }
        return playlists;
    }

    @Override
    public Playlist createPlaylist(String name) throws Exception {
        String sql = "INSERT INTO Playlist (Name) VALUES (?);";

        try (Connection conn = dbConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, name);
            stmt.executeUpdate();

            ResultSet rs = stmt.getGeneratedKeys();
            int id = 0;
            if (rs.next()) {
                id = rs.getInt(1);
            }
            return new Playlist(id, name, 0, 0);
        }
    }

    @Override
    public List<Song> getSongsOnPlaylist(int playlistId) throws Exception {
        List<Song> songs = new ArrayList<>();

        String sql = "SELECT s.* FROM Song s " +
                "JOIN PlaylistSong ps ON ps.SongId = s.Id " +
                "WHERE ps.PlaylistId = ? " +
                "ORDER BY ps.Id";

        try (Connection conn = dbConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, playlistId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                songs.add(new Song(
                        rs.getInt("Id"),
                        rs.getString("Title"),
                        rs.getString("Artist"),
                        rs.getString("Genre"),
                        rs.getInt("Time")));
            }
        }
        return songs;
    }

    @Override
    public void addSongToPlaylist(int playlistId, int songId) throws Exception {
        String sql = "INSERT INTO PlaylistSong (PlaylistId, SongId) VALUES (?, ?);";

        try (Connection conn = dbConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, playlistId);
            stmt.setInt(2, songId);
            stmt.executeUpdate();
        }
    }
}