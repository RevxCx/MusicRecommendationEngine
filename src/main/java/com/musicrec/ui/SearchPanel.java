
package com.musicrec.ui;

import com.musicrec.model.Artist;
import com.musicrec.model.Song;
import com.musicrec.repository.FavoriteRepository;
import com.musicrec.repository.SongRepository;
import com.musicrec.spotify.SpotifyApiClient;
import com.musicrec.spotify.SpotifyToken;
import com.musicrec.spotify.SpotifyTrack;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class SearchPanel extends JPanel {

    private final MainFrame mainFrame;
    private final JTextField searchField = new JTextField();
    private final DefaultListModel<SpotifyTrack> results =
            new DefaultListModel<>();
    private final JList<SpotifyTrack> resultList = new JList<>(results);
    private final JLabel status =
            new JLabel("Connect Spotify to begin searching.");
    private final JButton searchButton = new JButton("Search");
    private final JButton saveButton = new JButton("Save to Library");
    private final JButton favoriteButton = new JButton("Add to Favorites");

    public SearchPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;

        setLayout(new BorderLayout(15, 20));
        setBackground(new Color(18, 18, 18));
        setBorder(new EmptyBorder(30, 35, 30, 35));

        JLabel heading = new JLabel("Search Spotify");
        heading.setFont(new Font("SansSerif", Font.BOLD, 28));
        heading.setForeground(Color.WHITE);

        searchField.setFont(new Font("SansSerif", Font.PLAIN, 16));
        searchField.setToolTipText("Enter a song or artist");

        JPanel searchBar = new JPanel(new BorderLayout(10, 0));
        searchBar.setBackground(getBackground());
        searchBar.add(searchField, BorderLayout.CENTER);
        searchBar.add(searchButton, BorderLayout.EAST);

        JPanel top = new JPanel(new BorderLayout(0, 18));
        top.setBackground(getBackground());
        top.add(heading, BorderLayout.NORTH);
        top.add(searchBar, BorderLayout.CENTER);

        resultList.setBackground(new Color(30, 30, 30));
        resultList.setForeground(Color.WHITE);
        resultList.setFont(new Font("SansSerif", Font.PLAIN, 14));
        resultList.setFixedCellHeight(55);
        resultList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        resultList.setCellRenderer((list, track, index, selected, focus) -> {
            JLabel label = new JLabel(
                    track.title() + "  —  " + track.artist()
                            + "  |  " + track.album()
            );
            label.setOpaque(true);
            label.setBorder(new EmptyBorder(8, 10, 8, 10));
            label.setBackground(selected
                    ? new Color(45, 90, 60) : new Color(30, 30, 30));
            label.setForeground(Color.WHITE);
            return label;
        });

        status.setForeground(Color.LIGHT_GRAY);
        saveButton.setEnabled(false);
        favoriteButton.setEnabled(false);

        resultList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                boolean enabled = resultList.getSelectedValue() != null
                        && mainFrame.getSpotifyToken() != null;

                saveButton.setEnabled(enabled);
                favoriteButton.setEnabled(enabled);
            }
        });

        searchButton.addActionListener(e -> search());
        searchField.addActionListener(e -> search());
        saveButton.addActionListener(e -> saveSelectedTrack(false));
        favoriteButton.addActionListener(e -> saveSelectedTrack(true));

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.setBackground(getBackground());
        actions.add(saveButton);
        actions.add(favoriteButton);

        JPanel bottom = new JPanel(new BorderLayout(10, 8));
        bottom.setBackground(getBackground());
        bottom.add(status, BorderLayout.CENTER);
        bottom.add(actions, BorderLayout.EAST);

        add(top, BorderLayout.NORTH);
        add(new JScrollPane(resultList), BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);
    }

    private void search() {
        String query = searchField.getText().trim();
        SpotifyToken token = mainFrame.getSpotifyToken();

        if (query.isEmpty()) {
            status.setText("Enter a song or artist to search.");
            return;
        }

        if (token == null) {
            status.setText("Connect Spotify first using the sidebar.");
            return;
        }

        results.clear();
        searchButton.setEnabled(false);
        saveButton.setEnabled(false);
        favoriteButton.setEnabled(false);
        status.setText("Searching Spotify...");

        new SwingWorker<List<SpotifyTrack>, Void>() {
            @Override
            protected List<SpotifyTrack> doInBackground() throws Exception {
                return new SpotifyApiClient().searchTracks(query, token);
            }

            @Override
            protected void done() {
                searchButton.setEnabled(true);

                try {
                    List<SpotifyTrack> tracks = get();

                    for (SpotifyTrack track : tracks) {
                        results.addElement(track);
                    }

                    status.setText(tracks.isEmpty()
                            ? "No tracks found."
                            : tracks.size() + " tracks found.");
                } catch (Exception e) {
                    Throwable cause = e.getCause() != null
                            ? e.getCause() : e;
                    status.setText("Search failed: " + cause.getMessage());
                }
            }
        }.execute();
    }

    private void saveSelectedTrack(boolean addFavorite) {
        SpotifyTrack track = resultList.getSelectedValue();

        if (track == null) {
            status.setText("Select a track first.");
            return;
        }

        saveButton.setEnabled(false);
        favoriteButton.setEnabled(false);
        status.setText(addFavorite
                ? "Adding favorite..." : "Saving track...");

        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws SQLException {
                SongRepository songs = new SongRepository();

                Artist artist = songs.saveArtist(new Artist(
                        0, null, track.artist(), null
                ));

                Song song = songs.saveSong(new Song(
                        0,
                        track.id(),
                        track.title(),
                        artist.getId(),
                        track.album(),
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        track.spotifyUrl(),
                        track.artworkUrl()
                ));

                if (addFavorite) {
                    new FavoriteRepository().add(
                            mainFrame.getUserId(), song.getId()
                    );
                }

                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    mainFrame.refreshDashboard();
                    mainFrame.refreshFavorites();

                    status.setText(addFavorite
                            ? "Added to favorites: " + track.title()
                            : "Saved: " + track.title());
                } catch (Exception e) {
                    Throwable cause = e.getCause() != null
                            ? e.getCause() : e;
                    status.setText("Save failed: " + cause.getMessage());
                } finally {
                    boolean enabled =
                            resultList.getSelectedValue() != null
                                    && mainFrame.getSpotifyToken() != null;

                    saveButton.setEnabled(enabled);
                    favoriteButton.setEnabled(enabled);
                }
            }
        }.execute();
    }
}
