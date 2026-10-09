
package com.musicrec.spotify;

public record SpotifyTrack(
        String id,
        String title,
        String artist,
        String album,
        String spotifyUrl,
        String artworkUrl
) {
}
