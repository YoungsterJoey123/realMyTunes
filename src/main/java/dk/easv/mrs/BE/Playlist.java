package dk.easv.mrs.BE;

/**
 * A playlist as shown in the playlist table. The songs themselves are loaded
 * separately. songCount and playlistTime (seconds) are totals for the whole playlist.
 */
public class Playlist {

    private int id;
    private String name;
    private int songCount;
    private int playlistTime;

    public Playlist(int id, String name, int songCount, int playlistTime) {
        this.id = id;
        this.name = name;
        this.songCount = songCount;
        this.playlistTime = playlistTime;
    }

    public int getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    public int getSongCount() {
        return songCount;
    }
    public int getPlaylistTime() {
        return playlistTime;
    }


    public void setName(String name) {
        this.name = name;
    }
    public void setSongCount(int songCount) {
        this.songCount = songCount;
    }
    public void setPlaylistTime(int playlistTime) {
        this.playlistTime = playlistTime;
    }

}

