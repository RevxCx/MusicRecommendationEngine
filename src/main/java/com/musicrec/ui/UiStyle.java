package com.musicrec.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicArrowButton;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;

final class UiStyle {

    static final Color BG = new Color(12, 14, 21);
    static final Color SIDEBAR = new Color(18, 20, 30);
    static final Color PANEL = new Color(27, 30, 43);
    static final Color PANEL_HOVER = new Color(39, 43, 60);
    static final Color ACCENT = new Color(157, 111, 255);
    static final Color ACCENT_DARK = new Color(108, 75, 201);
    static final Color ACCENT_HOVER = new Color(181, 147, 255);
    static final Color TEXT = new Color(245, 246, 252);
    static final Color MUTED = new Color(165, 172, 191);
    static final Color BORDER = new Color(49, 54, 73);
    static final Color RED = new Color(255, 112, 135);
    static final Color GREEN = new Color(92, 220, 166);

    private UiStyle() {}

    static void button(JButton button) {
        styleButton(button, PANEL_HOVER);
    }

    static void primaryButton(JButton button) {
        styleButton(button, ACCENT);
    }

    private static void styleButton(JButton button, Color base) {
        button.setFont(new Font("SansSerif", Font.BOLD, 13));
        button.setForeground(TEXT);
        button.setBackground(base);
        button.setOpaque(true);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setBorder(new EmptyBorder(10, 15, 10, 15));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        button.addChangeListener(e -> {
            ButtonModel model = button.getModel();

            if (!button.isEnabled()) {
                button.setForeground(MUTED);
                button.setBackground(PANEL);
            } else if (model.isPressed()) {
                button.setBackground(ACCENT_DARK);
            } else if (model.isRollover()) {
                button.setBackground(
                        base.equals(ACCENT) ? ACCENT_HOVER : BORDER
                );
            } else {
                button.setForeground(TEXT);
                button.setBackground(base);
            }
        });
    }

    static void textField(JTextField field) {
        field.setBackground(PANEL);
        field.setForeground(TEXT);
        field.setCaretColor(ACCENT);
        field.setSelectionColor(ACCENT_DARK);
        field.setSelectedTextColor(Color.WHITE);
        field.setFont(new Font("SansSerif", Font.PLAIN, 14));
        field.setOpaque(true);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(10, 12, 10, 12)
        ));
    }

    static void comboBox(JComboBox<String> box) {
        box.setFont(new Font("SansSerif", Font.PLAIN, 14));
        box.setForeground(TEXT);
        box.setBackground(PANEL);
        box.setOpaque(true);
        box.setFocusable(false);
        box.setBorder(BorderFactory.createLineBorder(BORDER));

        box.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    JList<?> list, Object value, int index,
                    boolean selected, boolean focus) {

                JLabel label = (JLabel) super.getListCellRendererComponent(
                        list, value, index, selected, focus
                );

                label.setOpaque(true);
                label.setFont(new Font("SansSerif", Font.PLAIN, 14));
                label.setBorder(new EmptyBorder(9, 12, 9, 12));
                label.setBackground(selected ? ACCENT_DARK : PANEL);
                label.setForeground(TEXT);

                return label;
            }
        });

        box.setUI(new BasicComboBoxUI() {
            @Override
            protected JButton createArrowButton() {
                JButton arrow = new BasicArrowButton(
                        BasicArrowButton.SOUTH,
                        PANEL_HOVER,
                        BORDER,
                        TEXT,
                        ACCENT
                );
                arrow.setOpaque(true);
                arrow.setBorder(BorderFactory.createEmptyBorder());
                return arrow;
            }

            @Override
            public void paintCurrentValueBackground(
                    Graphics g, Rectangle bounds, boolean hasFocus) {
                g.setColor(PANEL);
                g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
            }

            @Override
            public void paintCurrentValue(
                    Graphics g, Rectangle bounds, boolean hasFocus) {
                ListCellRenderer<Object> renderer =
                        (ListCellRenderer<Object>) comboBox.getRenderer();

                Component component = renderer.getListCellRendererComponent(
                        listBox,
                        comboBox.getSelectedItem(),
                        -1,
                        false,
                        false
                );

                component.setBackground(PANEL);
                component.setForeground(TEXT);

                currentValuePane.paintComponent(
                        g, component, comboBox,
                        bounds.x + 2, bounds.y + 1,
                        bounds.width - 4, bounds.height - 2,
                        true
                );
            }
        });

        box.setBackground(PANEL);
        box.setForeground(TEXT);
        box.setMaximumRowCount(8);
        box.revalidate();
        box.repaint();
    }

    static JPanel card() {
        JPanel panel = new JPanel();
        panel.setBackground(PANEL);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(16, 16, 16, 16)
        ));
        return panel;
    }

    static JScrollPane scrollPane(Component view) {
        JScrollPane pane = new JScrollPane(view);
        pane.setBorder(BorderFactory.createEmptyBorder());
        pane.getViewport().setBackground(BG);
        pane.getVerticalScrollBar().setUnitIncrement(18);
        pane.setHorizontalScrollBarPolicy(
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
        );

        pane.getVerticalScrollBar().setUI(new BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                thumbColor = new Color(76, 65, 111);
                trackColor = BG;
            }

            @Override
            protected JButton createDecreaseButton(int orientation) {
                return invisibleButton();
            }

            @Override
            protected JButton createIncreaseButton(int orientation) {
                return invisibleButton();
            }

            private JButton invisibleButton() {
                JButton button = new JButton();
                button.setPreferredSize(new Dimension(0, 0));
                button.setMinimumSize(new Dimension(0, 0));
                button.setMaximumSize(new Dimension(0, 0));
                return button;
            }

            @Override
            protected void paintThumb(
                    Graphics g, JComponent c, Rectangle bounds) {
                if (bounds.isEmpty() || !scrollbar.isEnabled()) return;

                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(
                        RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON
                );
                g2.setColor(thumbColor);
                g2.fillRoundRect(
                        bounds.x + 3, bounds.y + 2,
                        bounds.width - 6, bounds.height - 4,
                        10, 10
                );
                g2.dispose();
            }
        });

        return pane;
    }

    static JLabel heading(String text, int size) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.BOLD, size));
        label.setForeground(TEXT);
        return label;
    }

    static JLabel muted(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.PLAIN, 13));
        label.setForeground(MUTED);
        return label;
    }
}