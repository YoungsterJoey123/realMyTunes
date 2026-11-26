package dk.easv.mrs.BLL.util;

import dk.easv.mrs.BE.Song;

/** Decides whether a song matches a filter query (title or artist, case-insensitive). */
public class SongSearcher {

  public boolean matches(Song song, String query) {
    String q = query.toLowerCase();
    return song.getTitle().toLowerCase().contains(q)
        || song.getArtist().toLowerCase().contains(q);
  }
}