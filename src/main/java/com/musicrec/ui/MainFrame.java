package com.musicrec.ui;

import com.musicrec.model.User;
import com.musicrec.repository.UserRepository;
import com.musicrec.spotify.SpotifyOAuthService;
import com.musicrec.spotify.SpotifyToken;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;

public class MainFrame extends JFrame {

    private static final Color BG = UiStyle.BG;
    private static final Color SIDEBAR = UiStyle.SIDEBAR;
    private static final Color GREEN = UiStyle.ACCENT;

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel content = new JPanel(cardLayout);
    private final JLabel connectionStatus =
            new JLabel("Spotify: Not connected");
    private final JButton connectButton = new JButton("Connect Spotify");

    private final User user;
    private final DashboardPanel dashboardPanel;
    private final FavoritesPanel favoritesPanel;

    private SpotifyToken spotifyToken;

    public MainFrame(User user) {
        this.user = user;

        setTitle("Music Recommendation Engine");
        setSize(1280, 820);
        setMinimumSize(new Dimension(980, 650));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(BG);

        JPanel sidebar = new JPanel();
        sidebar.setPreferredSize(new Dimension(235, 0));
        sidebar.setBackground(SIDEBAR);
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBorder(BorderFactory.createEmptyBorder(28, 17, 22, 17));

        JLabel logo = new JLabel("♫  MUSICREC");
        logo.setFont(new Font("SansSerif", Font.BOLD, 23));
        logo.setForeground(GREEN);
        logo.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(logo);
        sidebar.add(Box.createVerticalStrut(42));

        addNavigation(sidebar, "Home", "home");
        addNavigation(sidebar, "Search Music", "search");
        addNavigation(sidebar, "Favorites", "favorites");
        addNavigation(sidebar, "Recommendations", "recommendations");
        addNavigation(sidebar, "My Profile", "profile");

        sidebar.add(Box.createVerticalStrut(25));

        connectionStatus.setForeground(Color.LIGHT_GRAY);
        connectionStatus.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(connectionStatus);
        sidebar.add(Box.createVerticalStrut(10));

        connectButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        UiStyle.primaryButton(connectButton);
        connectButton.setCursor(
                Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        );
        connectButton.addActionListener(e -> connectSpotify());
        sidebar.add(connectButton);

        sidebar.add(Box.createVerticalGlue());

        JLabel footer = new JLabel("Powered by your taste");
        footer.setForeground(Color.GRAY);
        footer.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(footer);

        dashboardPanel = new DashboardPanel(user);
        favoritesPanel = new FavoritesPanel(user);

        content.setBackground(BG);
        content.add(dashboardPanel, "home");
        content.add(new SearchPanel(this), "search");
        content.add(favoritesPanel, "favorites");
        content.add(new RecommendationPanel(user), "recommendations");
        content.add(new ProfilePanel(user), "profile");

        add(sidebar, BorderLayout.WEST);
        add(content, BorderLayout.CENTER);
        cardLayout.show(content, "home");
    }

    private void addNavigation(
            JPanel sidebar, String label, String page
    ) {
        JButton button = new JButton(label);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setForeground(Color.WHITE);
        button.setBackground(SIDEBAR);
        button.setOpaque(true);
        button.setFont(new Font("SansSerif", Font.BOLD, 14));
        button.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 8));
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(
                Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        );

        button.addActionListener(e -> {
            if ("home".equals(page)) refreshDashboard();
            if ("favorites".equals(page)) refreshFavorites();
            cardLayout.show(content, page);
        });

        sidebar.add(button);
        sidebar.add(Box.createVerticalStrut(6));
    }

    private void connectSpotify() {
        connectButton.setEnabled(false);
        connectButton.setText("Connecting...");
        connectionStatus.setText("Spotify: Waiting for login...");

        new SpotifyOAuthService().connect().whenComplete((token, error) ->
                SwingUtilities.invokeLater(() -> {
                    connectButton.setEnabled(true);

                    if (error != null) {
                        spotifyToken = null;
                        connectButton.setText("Connect Spotify");
                        connectionStatus.setText("Spotify: Not connected");

                        Throwable cause = error.getCause() != null
                                ? error.getCause() : error;

                        JOptionPane.showMessageDialog(
                                this, cause.getMessage(),
                                "Spotify Connection Failed",
                                JOptionPane.ERROR_MESSAGE
                        );
                    } else {
                        spotifyToken = token;
                        connectionStatus.setText("Spotify: Connected");
                        connectButton.setText("Reconnect Spotify");

                        JOptionPane.showMessageDialog(
                                this, "Spotify connected successfully!",
                                "Connected", JOptionPane.INFORMATION_MESSAGE
                        );
                    }
                })
        );
    }

    public SpotifyToken getSpotifyToken() {
        return spotifyToken;
    }

    public int getUserId() {
        return user.getId();
    }

    public void refreshDashboard() {
        dashboardPanel.refreshLibrary();
    }

    public void refreshFavorites() {
        favoritesPanel.refreshFavorites();
    }

    public static void launch() throws SQLException {
        UserRepository repository = new UserRepository();
        User user = repository.findById(1).orElse(null);

        if (user == null) {
            user = new User(
                    0, "Music Lover", "Pop", "Happy", "English", "Medium"
            );
            user = repository.save(user);
        }

        User finalUser = user;

        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame(finalUser);
            frame.setVisible(true);
        });
    }
}