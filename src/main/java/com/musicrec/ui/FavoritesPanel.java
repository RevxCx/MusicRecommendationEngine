package com.musicrec.ui;

import com.musicrec.model.Song;
import com.musicrec.model.User;
import com.musicrec.repository.FavoriteRepository;
import com.musicrec.repository.SongRepository;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class FavoritesPanel extends JPanel {

    private static final Color BG = UiStyle.BG;
    private static final Color PANEL = UiStyle.PANEL;

    private final User user;
    private final DefaultListModel<Song> model = new DefaultListModel<>();
    private final JList<Song> list = new JList<>(model);
    private final JLabel status = new JLabel("Your favorite tracks");
    private final JLabel count = new JLabel("0 tracks");

    public FavoritesPanel(User user) {
        this.user = user;

        setLayout(new BorderLayout(15, 20));
        setBackground(BG);
        setBorder(new EmptyBorder(30, 35, 30, 35));

        JLabel heading = new JLabel("Your Favorites");
        heading.setFont(new Font("SansSerif", Font.BOLD, 28));
        heading.setForeground(Color.WHITE);

        JButton refresh = new JButton("Refresh");
        UiStyle.button(refresh);
        refresh.addActionListener(e -> refreshFavorites());

        JButton remove = new JButton("Remove Favorite");
        UiStyle.button(remove);
        remove.addActionListener(e -> removeSelected());

        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(BG);
        top.add(heading, BorderLayout.WEST);
        top.add(refresh, BorderLayout.EAST);

        list.setBackground(PANEL);
        list.setForeground(Color.WHITE);
        list.setFont(new Font("SansSerif", Font.PLAIN, 14));
        list.setFixedCellHeight(42);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        list.setCellRenderer((items, song, index, selected, focus) -> {
            String album = song.getAlbumName() == null
                    ? "Unknown album" : song.getAlbumName();

            JLabel label = new JLabel(song.getTitle() + "  —  " + album);
            label.setOpaque(true);
            label.setBorder(new EmptyBorder(8, 10, 8, 10));
            label.setBackground(selected
                    ? new Color(45, 90, 60) : PANEL);
            label.setForeground(Color.WHITE);
            return label;
        });

        JPanel bottom = new JPanel(new BorderLayout(10, 0));
        bottom.setBackground(BG);
        status.setForeground(Color.LIGHT_GRAY);
        count.setForeground(Color.LIGHT_GRAY);

        bottom.add(status, BorderLayout.CENTER);
        bottom.add(count, BorderLayout.WEST);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.setBackground(BG);
        actions.add(remove);

        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(BG);
        footer.add(bottom, BorderLayout.CENTER);
        footer.add(actions, BorderLayout.SOUTH);

        add(top, BorderLayout.NORTH);
        add(UiStyle.scrollPane(list), BorderLayout.CENTER);
        add(footer, BorderLayout.SOUTH);

        refreshFavorites();
    }

    public void refreshFavorites() {
        model.clear();

        try {
            FavoriteRepository favorites = new FavoriteRepository();
            SongRepository songs = new SongRepository();

            List<Integer> ids = favorites.findSongIds(user.getId());

            for (int id : ids) {
                songs.findById(id).ifPresent(model::addElement);
            }

            count.setText(model.size()
                    + (model.size() == 1 ? " favorite" : " favorites"));

            status.setText(model.isEmpty()
                    ? "No favorites yet. Add tracks from Search Music."
                    : "Your favorites are saved locally.");

        } catch (SQLException e) {
            status.setText("Unable to load favorites.");
            count.setText("");
        }
    }

    private void removeSelected() {
        Song song = list.getSelectedValue();

        if (song == null) {
            status.setText("Select a favorite to remove.");
            return;
        }

        try {
            new FavoriteRepository().remove(user.getId(), song.getId());
            refreshFavorites();
            status.setText("Removed " + song.getTitle() + " from favorites.");
        } catch (SQLException e) {
            status.setText("Unable to remove favorite.");
        }
    }
}