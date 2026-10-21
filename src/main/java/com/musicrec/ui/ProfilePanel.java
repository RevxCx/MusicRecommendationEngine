package com.musicrec.ui;

import com.musicrec.model.User;
import com.musicrec.repository.UserRepository;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.SQLException;

public class ProfilePanel extends JPanel {

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

        setLayout(new BorderLayout(22, 22));
        setBackground(UiStyle.BG);
        setBorder(new EmptyBorder(30, 35, 30, 35));

        JLabel heading = UiStyle.heading("Your listening profile", 30);
        JLabel subtitle = UiStyle.muted(
                "Tune your preferences to personalize your discoveries."
        );

        JPanel titlePanel = new JPanel();
        titlePanel.setOpaque(false);
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.add(heading);
        titlePanel.add(Box.createVerticalStrut(8));
        titlePanel.add(subtitle);

        JPanel intro = UiStyle.card();
        intro.setLayout(new BorderLayout(12, 5));

        JLabel icon = new JLabel("♫");
        icon.setFont(new Font("SansSerif", Font.BOLD, 34));
        icon.setForeground(UiStyle.ACCENT);

        JPanel introText = new JPanel();
        introText.setOpaque(false);
        introText.setLayout(new BoxLayout(introText, BoxLayout.Y_AXIS));
        introText.add(UiStyle.heading(user.getName(), 18));
        introText.add(Box.createVerticalStrut(5));
        introText.add(UiStyle.muted(
                "Your taste. Your soundtrack. Your discovery."
        ));

        intro.add(icon, BorderLayout.WEST);
        intro.add(introText, BorderLayout.CENTER);

        JPanel form = UiStyle.card();
        form.setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(12, 8, 12, 8);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        c.gridx = 0;
        c.gridwidth = 2;

        JLabel formHeading = UiStyle.heading("Taste preferences", 19);
        form.add(formHeading, c);

        c.gridwidth = 1;
        c.weightx = 0.35;

        addField(form, c, 1, "Preferred genre", genre);
        addField(form, c, 2, "Preferred mood", mood);
        addField(form, c, 3, "Preferred language", language);
        addField(form, c, 4, "Preferred energy", energy);

        select(genre, user.getPreferredGenre());
        select(mood, user.getPreferredMood());
        select(language, user.getPreferredLanguage());
        select(energy, user.getPreferredEnergy());

        UiStyle.comboBox(genre);
        UiStyle.comboBox(mood);
        UiStyle.comboBox(language);
        UiStyle.comboBox(energy);

        JButton save = new JButton("Save preferences  →");
        UiStyle.primaryButton(save);
        save.addActionListener(e -> saveProfile());

        status.setForeground(UiStyle.MUTED);
        status.setFont(new Font("SansSerif", Font.PLAIN, 13));

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 5));
        bottom.setOpaque(false);
        bottom.add(save);
        bottom.add(Box.createHorizontalStrut(15));
        bottom.add(status);

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.add(titlePanel);
        content.add(Box.createVerticalStrut(22));
        content.add(intro);
        content.add(Box.createVerticalStrut(18));
        content.add(form);

        add(UiStyle.scrollPane(content), BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);
    }

    private void addField(
            JPanel panel, GridBagConstraints c,
            int row, String label, JComponent input) {

        c.gridy = row;
        c.gridx = 0;
        c.weightx = 0.35;

        JLabel name = new JLabel(label);
        name.setForeground(UiStyle.TEXT);
        name.setFont(new Font("SansSerif", Font.BOLD, 13));
        panel.add(name, c);

        c.gridx = 1;
        c.weightx = 0.65;
        panel.add(input, c);
    }

    private void select(JComboBox<String> box, String value) {
        if (value == null || value.isBlank()) return;

        for (int i = 0; i < box.getItemCount(); i++) {
            if (box.getItemAt(i).equalsIgnoreCase(value)) {
                box.setSelectedIndex(i);
                return;
            }
        }

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

            status.setForeground(UiStyle.ACCENT);
            status.setText("✓ Preferences saved");

        } catch (SQLException e) {
            status.setForeground(UiStyle.RED);
            status.setText("Could not save preferences.");

            JOptionPane.showMessageDialog(
                    this,
                    "Unable to save preferences:\n" + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }
}