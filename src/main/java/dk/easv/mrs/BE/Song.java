package dk.easv.mrs.BE;

/**
 * A song in the library. Playtime is in seconds, and filePath is relative to
 * the application folder (for example "music/song.mp3").
 */
public class Song {

    private int id;
    private String title;
    private String artist;
    private String genre;
    private int playtime;
    private String filePath;


    public Song(int id, String title, String artist, String genre, int playtime, String filePath) {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.genre = genre;
        this.playtime = playtime;
        this.filePath = filePath;

    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    public String getGenre() {
        return genre;
    }

    public int getPlaytime() {
        return playtime;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public void setPlaytime(int playtime) {
        this.playtime = playtime;
    }


    @Override
    public String toString() {
        return title + " - " + artist;
    }

    public String getFilePath() {
        return filePath;
    }
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
}
