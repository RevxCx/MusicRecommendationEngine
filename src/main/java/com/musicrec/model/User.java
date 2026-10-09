
package com.musicrec.model;

public class User {
    private int id;
    private final String name;
    private String preferredGenre;
    private String preferredMood;
    private String preferredLanguage;
    private String preferredEnergy;

    public User(int id, String name, String genre, String mood,
                String language, String energy) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name cannot be empty.");
        }
        this.id = id;
        this.name = name.trim();
        this.preferredGenre = genre;
        this.preferredMood = mood;
        this.preferredLanguage = language;
        this.preferredEnergy = energy;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public String getPreferredGenre() { return preferredGenre; }
    public String getPreferredMood() { return preferredMood; }
    public String getPreferredLanguage() { return preferredLanguage; }
    public String getPreferredEnergy() { return preferredEnergy; }

    public void setPreferredGenre(String value) { preferredGenre = value; }
    public void setPreferredMood(String value) { preferredMood = value; }
    public void setPreferredLanguage(String value) { preferredLanguage = value; }
    public void setPreferredEnergy(String value) { preferredEnergy = value; }
}
