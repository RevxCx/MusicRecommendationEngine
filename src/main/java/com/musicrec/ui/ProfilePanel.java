package com.musicrec.ui;

import com.musicrec.model.User;
import com.musicrec.repository.UserRepository;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.SQLException;

public class ProfilePanel extends JPanel {

    private static final Color BG = new Color(18, 18, 18);
    private static final Color CARD = new Color(35, 35, 35);
    private static final Color GREEN = new Color(30, 215, 96);
    private static final Color TEXT = new Color(245, 245, 245);
    private static final Color MUTED = new Color(175, 175, 175);

    private final User user;

    private final JComboBox<String> genre = new JComboBox<>(
            new String[]{"Pop", "Indie", "Hip-Hop", "Electronic",
                    "Bollywood", "Ambient", "Jazz"}
    );

    private final JComboBox<String> mood = new JComboBox<>(
            new String[]{"Happy", "Calm", "Relaxed", "Sad",
                    "Energetic", "Romantic"}
    );

    private final JComboBox<String> language = new JComboBox<>(
            new String[]{"English", "Hindi"}
    );

    private final JComboBox<String> energy = new JComboBox<>(
            new String[]{"Low", "Medium", "High"}
    );

    private final JLabel status = new JLabel(" ");

    public ProfilePanel(User user) {
        this.user = user;

        setLayout(new BorderLayout(20, 20));
        setBackground(BG);
        setBorder(new EmptyBorder(30, 35, 30, 35));

        JLabel heading = new JLabel("Your profile");
        heading.setForeground(TEXT);
        heading.setFont(new Font("SansSerif", Font.BOLD, 28));

        JLabel subtitle = new JLabel(
                "Customize your music taste and recommendations");
        subtitle.setForeground(MUTED);
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 13));

        JPanel titles = new JPanel();
        titles.setOpaque(false);
        titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));
        titles.add(heading);
        titles.add(Box.createVerticalStrut(7));
        titles.add(subtitle);

        JPanel form = new JPanel(new GridLayout(0, 2, 18, 20));
        form.setBackground(CARD);
        form.setBorder(new EmptyBorder(25, 25, 25, 25));

        addField(form, "Name", createNameField());
        addField(form, "Preferred genre", genre);
        addField(form, "Preferred mood", mood);
        addField(form, "Preferred language", language);
        addField(form, "Preferred energy", energy);

        select(genre, user.getPreferredGenre());
        select(mood, user.getPreferredMood());
        select(language, user.getPreferredLanguage());
        select(energy, user.getPreferredEnergy());

        styleCombo(genre);
        styleCombo(mood);
        styleCombo(language);
        styleCombo(energy);

        JButton save = new JButton("Save preferences");
        save.setFont(new Font("SansSerif", Font.BOLD, 14));
        save.setForeground(Color.BLACK);
        save.setBackground(GREEN);
        save.setFocusPainted(false);
        save.setBorder(new EmptyBorder(12, 20, 12, 20));
        save.setCursor(Cursor.getPredefinedCursor(
                Cursor.HAND_CURSOR));
        save.addActionListener(e -> saveProfile());

        status.setFont(new Font("SansSerif", Font.PLAIN, 12));
        status.setForeground(MUTED);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 8));
        bottom.setOpaque(false);
        bottom.add(save);
        bottom.add(Box.createHorizontalStrut(15));
        bottom.add(status);

        JPanel center = new JPanel(new BorderLayout(0, 20));
        center.setOpaque(false);
        center.add(titles, BorderLayout.NORTH);
        center.add(form, BorderLayout.CENTER);

        add(center, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);
    }

    private JTextField createNameField() {
        JTextField field = new JTextField(user.getName());
        field.setEditable(false);
        field.setToolTipText("Your profile name");
        return field;
    }

    private void addField(
            JPanel panel, String label, JComponent input) {

        JLabel text = new JLabel(label);
        text.setForeground(TEXT);
        text.setFont(new Font("SansSerif", Font.PLAIN, 14));

        panel.add(text);
        panel.add(input);
    }

    private void styleCombo(JComboBox<String> box) {
        box.setFont(new Font("SansSerif", Font.PLAIN, 13));
        box.setBackground(new Color(48, 48, 48));
        box.setForeground(TEXT);
        box.setFocusable(false);
    }

    private void select(JComboBox<String> box, String value) {
        if (value == null || value.isBlank()) {
            return;
        }

        for (int i = 0; i < box.getItemCount(); i++) {
            if (box.getItemAt(i).equalsIgnoreCase(value)) {
                box.setSelectedIndex(i);
                return;
            }
        }

        // Preserve older saved preferences that aren't in the new list.
        box.addItem(value);
        box.setSelectedItem(value);
    }

    private void saveProfile() {
        user.setPreferredGenre((String) genre.getSelectedItem());
        user.setPreferredMood((String) mood.getSelectedItem());
        user.setPreferredLanguage((String) language.getSelectedItem());
        user.setPreferredEnergy((String) energy.getSelectedItem());

        try {
            new UserRepository().save(user);

            status.setForeground(GREEN);
            status.setText("Preferences saved successfully!");

            JOptionPane.showMessageDialog(
                    this,
                    "Your preferences have been saved.\n"
                            + "Open Recommendations and click "
                            + "\"Discover More\" to view the updated ranking.",
                    "Profile Updated",
                    JOptionPane.INFORMATION_MESSAGE
            );

        } catch (SQLException e) {
            status.setForeground(new Color(255, 100, 100));
            status.setText("Unable to save preferences.");

            JOptionPane.showMessageDialog(
                    this,
                    "Unable to save preferences: " + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }
}