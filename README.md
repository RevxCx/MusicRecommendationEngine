# Music Recommendation Engine

A Java Swing desktop application that integrates with the Spotify Web API
for music search and combines profile-based preferences with a local,
rules-based recommendation engine.

## Features

- Spotify authentication using OAuth 2.0 Authorization Code with PKCE
- Search for tracks using the Spotify Web API
- Save searched tracks to a local SQLite database
- Manage a personal favorites collection
- Customize music preferences through a profile page
- Generate recommendations from a local demo catalog
- Like and dislike demo recommendations
- Persist recommendation feedback in SQLite
- Display recommendation scores and explanations

## Technology Stack

- **Language:** Java 21
- **UI:** Java Swing
- **Build Tool:** Maven
- **Database:** SQLite
- **JSON Processing:** Jackson
- **Music Catalog Search:** Spotify Web API
- **Authentication:** OAuth 2.0 Authorization Code with PKCE

## Requirements

- JDK 21 or later
- Maven 3.9 or later
- A Spotify Developer application
- Internet access for Spotify authentication and music search

## Project Setup

### 1. Clone the Repository

```bash
git clone <YOUR_REPOSITORY_URL>
cd MusicRecommendationEngine
```

Replace `<YOUR_REPOSITORY_URL>` with your GitHub repository URL.

### 2. Configure Spotify

Create an application in the Spotify Developer Dashboard.

Register the following redirect URI in your Spotify application settings:

`http://127.0.0.1:8888/callback`

Copy the example configuration:

**Windows PowerShell:**

```powershell
Copy-Item config/spotify.properties.example config/spotify.properties
```

Open `config/spotify.properties` and replace `YOUR_CLIENT_ID` with your Spotify application's Client ID.

The configuration should look like this:

```properties
spotify.client.id=YOUR_CLIENT_ID
spotify.redirect.uri=http://127.0.0.1:8888/callback
database.path=data/musicrec.db
```

The local configuration file is excluded from Git. Never commit access tokens, refresh tokens, or other credentials.

This application uses PKCE and does not require a Spotify client secret for this authentication flow.

### 3. Build the Project

Open a terminal in the project root and run:

```bash
mvn compile
```

Maven downloads the required dependencies automatically.

### 4. Run the Application

```bash
mvn exec:java
```

The application will open as a desktop window.

## How It Works

### Spotify Integration

The application uses Spotify OAuth with PKCE to authenticate users and the Spotify Web API to search for tracks.

Users can save search results to their local library and manage a favorites collection.

### Local Database

SQLite stores application data locally, including:

- User profiles and music preferences
- Saved songs and artist records
- Favorites
- Listening history
- Recommendation feedback

The database is created at `data/musicrec.db` and is excluded from Git.

### Recommendation Engine

The current fallback engine uses a small synthetic demo catalog.

Recommendations are ranked using explicitly selected preferences:

- Genre
- Mood
- Language
- Energy

Users can like or dislike demo tracks. Their feedback is stored in SQLite and influences subsequent recommendation scores.

The current implementation is **rules-based, not a trained machine-learning model**. Match scores are heuristic scores, not probabilities.

The demo recommendation catalog is separate from Spotify search results.

## Project Structure

```text
MusicRecommendationEngine/
├── config/
│   └── spotify.properties.example
├── data/
├── src/
│   ├── main/
│   │   └── java/com/musicrec/
│   │       ├── Main.java
│   │       ├── config/
│   │       ├── database/
│   │       ├── model/
│   │       ├── repository/
│   │       ├── spotify/
│   │       └── ui/
│   └── test/
├── .gitignore
├── pom.xml
└── README.md
```

## Current Limitations

- Recommendations use a small fictional demo catalog.
- The recommendation engine is not yet trained using machine learning.
- Recommendation feedback is not currently used to train an ML model.
- Spotify API access is subject to Spotify's developer terms and application restrictions.
- The application currently uses a local SQLite database.

## Future Improvements

- Integrate a properly licensed independent music dataset.
- Train and evaluate a machine-learning model.
- Improve recommendation ranking and evaluation.
- Add richer track cards and album artwork where permitted.
- Improve UI styling, transitions, and responsiveness.
- Expand automated tests and documentation.

## License

No license has been specified yet. Add an appropriate license before redistributing the project.