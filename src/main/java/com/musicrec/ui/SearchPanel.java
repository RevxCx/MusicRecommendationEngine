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
    private final JLabel status = new JLabel("Connect Spotify to search.");
    private final JButton searchButton = new JButton("Search");
    private final JButton saveButton = new JButton("Save to Library");
    private final JButton favoriteButton = new JButton("Add to Favorites");

    public SearchPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;

        setLayout(new BorderLayout(18, 20));
        setBackground(UiStyle.BG);
        setBorder(new EmptyBorder(30, 35, 30, 35));

        JLabel heading = new JLabel("Find your next favorite");
        heading.setFont(new Font("SansSerif", Font.BOLD, 30));
        heading.setForeground(UiStyle.TEXT);

        JLabel subtitle = new JLabel(
                "Search Spotify by track, artist, or album."
        );
        subtitle.setForeground(UiStyle.MUTED);

        UiStyle.textField(searchField);
        searchField.setToolTipText("Enter a song or artist");

        UiStyle.primaryButton(searchButton);
        UiStyle.button(saveButton);
        UiStyle.button(favoriteButton);

        JPanel searchBar = new JPanel(new BorderLayout(10, 0));
        searchBar.setOpaque(false);
        searchBar.add(searchField, BorderLayout.CENTER);
        searchBar.add(searchButton, BorderLayout.EAST);

        JPanel top = new JPanel();
        top.setOpaque(false);
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.add(heading);
        top.add(Box.createVerticalStrut(7));
        top.add(subtitle);
        top.add(Box.createVerticalStrut(20));
        top.add(searchBar);

        resultList.setBackground(UiStyle.PANEL);
        resultList.setForeground(UiStyle.TEXT);
        resultList.setFont(new Font("SansSerif", Font.PLAIN, 14));
        resultList.setFixedCellHeight(58);
        resultList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        resultList.setCellRenderer((list, track, index, selected, focus) -> {
            JLabel label = new JLabel(
                    "♫   " + track.title() + "   —   "
                            + track.artist() + "   ·   " + track.album()
            );
            label.setOpaque(true);
            label.setBorder(new EmptyBorder(10, 14, 10, 14));
            label.setBackground(selected
                    ? UiStyle.ACCENT_DARK : UiStyle.PANEL);
            label.setForeground(UiStyle.TEXT);
            return label;
        });

        status.setForeground(UiStyle.MUTED);
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
        actions.setOpaque(false);
        actions.add(saveButton);
        actions.add(favoriteButton);

        JPanel bottom = new JPanel(new BorderLayout(10, 8));
        bottom.setOpaque(false);
        bottom.add(status, BorderLayout.CENTER);
        bottom.add(actions, BorderLayout.SOUTH);

        add(top, BorderLayout.NORTH);
        add(UiStyle.scrollPane(resultList), BorderLayout.CENTER);
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
            status.setText("Connect Spotify using the sidebar first.");
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
                            ? "No tracks found. Try another search."
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
                        0, track.id(), track.title(), artist.getId(),
                        track.album(), null, null, null, null, null,
                        null, null, null, track.spotifyUrl(),
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
                    boolean enabled = resultList.getSelectedValue() != null
                            && mainFrame.getSpotifyToken() != null;
                    saveButton.setEnabled(enabled);
                    favoriteButton.setEnabled(enabled);
                }
            }
        }.execute();
    }
}