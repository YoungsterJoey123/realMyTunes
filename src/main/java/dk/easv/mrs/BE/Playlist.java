package dk.easv.mrs.BE;

public class Playlist {

    private String name;
    private int size;
    private int time;

    public Playlist(String name, int size, int time){
        this.name = name;
        this.size = size;
        this.time = time;
    }

    public String getName() {
        return name;
    }

    public int getSize() {
        return size;
    }

    public int getTime() {
        return time;
    }


}

