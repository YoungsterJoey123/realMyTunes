package dk.easv.mrs.DAL.db;

import dk.easv.mrs.BE.Song;
import dk.easv.mrs.DAL.IMyTunesDataAccess;

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
}