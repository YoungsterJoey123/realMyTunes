package dk.easv.mrs.BE;

public class Song {

    private int id;
    private String title;
    private String artist;
    private String genre;
    private int playtime;


    public Song(int id, String title, String artist, String genre, int playtime) {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.genre = genre;
        this.playtime = playtime;

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
}
