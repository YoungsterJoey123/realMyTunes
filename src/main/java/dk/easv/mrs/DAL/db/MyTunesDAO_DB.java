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

/**
 * Data access layer. Implements IMyTunesDataAccess against SQL Server using JDBC.
 * Multi-step changes (deleting a song or playlist, saving song order) run in a transaction.
 */
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
                String filePath = rs.getString("FilePath");

                songs.add(new Song(id, title, artist, genre, playtime, filePath));
            }
        }

        return songs;
    }

    @Override
    public Song createSong(Song song) throws Exception {
        String sql = "INSERT INTO Songs (Title, Artist, Genre, Playtime, FilePath) VALUES (?, ?, ?, ?, ?);";

        try (Connection conn = dbConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            // Bind parameters
            stmt.setString(1, song.getTitle());
            stmt.setString(2, song.getArtist());
            stmt.setString(3, song.getGenre());
            stmt.setInt(4, song.getPlaytime());
            stmt.setString(5, song.getFilePath());

            // Run the specified SQL statement
            stmt.executeUpdate();

            // Get the generated ID from the DB
            ResultSet rs = stmt.getGeneratedKeys();
            int id = 0;

            if (rs.next()) {
                id = rs.getInt(1);
            }

            // Create song object and send up the layers
            return new Song(id, song.getTitle(), song.getArtist(), song.getGenre(), song.getPlaytime(), song.getFilePath());
        }
    }

    @Override
    public void editSong(Song song) throws Exception {
        String sql = "UPDATE Songs SET Title = ?, Artist = ?, Genre = ?, Playtime = ?, FilePath = ? WHERE Id = ?";

        try (Connection conn = dbConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            // Bind parameters
            stmt.setString(1, song.getTitle());
            stmt.setString(2, song.getArtist());
            stmt.setString(3, song.getGenre());
            stmt.setInt(4, song.getPlaytime());
            stmt.setString(5, song.getFilePath());
            stmt.setInt(6, song.getId());

            // Run the specified SQL statement
            stmt.executeUpdate();
        }
    }

   @Override
   public void deleteSong(Song song) throws Exception {
     String deleteLinks = "DELETE FROM PlaylistSong WHERE SongId = ?";
     String deleteSong  = "DELETE FROM Songs WHERE Id = ?";

     try (Connection conn = dbConnector.getConnection()) {
       conn.setAutoCommit(false);
       try (PreparedStatement links = conn.prepareStatement(deleteLinks);
           PreparedStatement songs = conn.prepareStatement(deleteSong)) {

         links.setInt(1, song.getId());
         links.executeUpdate();          // remove the song from all playlists

         songs.setInt(1, song.getId());
         songs.executeUpdate();          // then delete the song itself

         conn.commit();
       } catch (Exception e) {
         conn.rollback();
         throw e;
       }
     }
   }

    @Override
    public List<Playlist> getAllPlaylists() throws Exception {
        List<Playlist> playlists = new ArrayList<>();

        String sql = "SELECT p.Id, p.Name, COUNT(ps.SongId) AS SongCount, " +
                "ISNULL(SUM(s.Playtime), 0) AS PlaylistTime " +
                "FROM Playlist p " +
                "LEFT JOIN PlaylistSong ps ON ps.PlaylistId = p.Id " +
                "LEFT JOIN Songs s ON s.Id = ps.SongId " +
                "GROUP BY p.Id, p.Name";

        try (Connection conn = dbConnector.getConnection();
             Statement stmt = conn.createStatement()) {

            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                playlists.add(new Playlist(
                        rs.getInt("Id"),
                        rs.getString("Name"),
                        rs.getInt("SongCount"),
                        rs.getInt("PlaylistTime")));
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

        String sql = "SELECT s.* FROM Songs s " +
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
                        rs.getInt("Playtime"),
                        rs.getString("FilePath")));
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

    @Override
    public void updatePlaylist(int playlistId, String name) throws Exception {
        String sql = "UPDATE Playlist SET Name = ? WHERE Id = ?";
        try (Connection conn = dbConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, name);
            stmt.setInt(2, playlistId);
            stmt.executeUpdate();
        }
    }

    @Override
    public void deletePlaylist(int playlistId) throws Exception {
        try (Connection conn = dbConnector.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement links = conn.prepareStatement("DELETE FROM PlaylistSong WHERE PlaylistId = ?");
                 PreparedStatement list = conn.prepareStatement("DELETE FROM Playlist WHERE Id = ?")) {
                links.setInt(1, playlistId);
                links.executeUpdate();          // only the links, the songs stay
                list.setInt(1, playlistId);
                list.executeUpdate();
                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
        }
    }

    /** Replaces the playlist's content with the given songs, in the given order. */
    @Override
    public void saveSongOrder(int playlistId, List<Song> songs) throws Exception {
        try (Connection conn = dbConnector.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement del = conn.prepareStatement("DELETE FROM PlaylistSong WHERE PlaylistId = ?");
                 PreparedStatement ins = conn.prepareStatement("INSERT INTO PlaylistSong (PlaylistId, SongId) VALUES (?, ?)")) {
                del.setInt(1, playlistId);
                del.executeUpdate();
                for (Song song : songs) {
                    ins.setInt(1, playlistId);
                    ins.setInt(2, song.getId());
                    ins.executeUpdate();
                }
                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
        }
    }
}