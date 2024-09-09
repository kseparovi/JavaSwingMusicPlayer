package Model;

import View.MainFrame;
import javazoom.jl.player.advanced.AdvancedPlayer;
import javazoom.jl.player.advanced.PlaybackEvent;
import javazoom.jl.player.advanced.PlaybackListener;

import javax.swing.*;
import java.io.*;
import java.util.ArrayList;

/**
 * This class handles the playback of songs using the JLAYER library.
 * It extends PlaybackListener to listen to playback events.
 * Implements the Functions interface for basic music control functions.
 */
public class MusicPlayer extends PlaybackListener implements Functions {

    // Fields
    private static final Object playSignal = new Object();
    private MainFrame mainFrame;
    private Song currentSong;
    private ArrayList<Song> playlist;
    private int currentPlaylistIndex;
    private AdvancedPlayer advancedPlayer;
    private boolean isPaused;
    private boolean songFinished;
    private boolean pressedNext, pressedPrev;
    private int currentTimeInMilliseconds;
    private Thread playbackSliderThread;
    private Thread playerThread;

    // Constructor
    public MusicPlayer(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.playlist = new ArrayList<>();
        this.currentPlaylistIndex = 0;
    }

    // Getters and Setters
    public Song getCurrentSong() {
        return currentSong;
    }

    public void setCurrentTimeInMilliseconds(int timeInMilliseconds) {
        this.currentTimeInMilliseconds = timeInMilliseconds;
    }

    public int getPlaybackPositionInSeconds() {
        return currentTimeInMilliseconds / 1000;
    }

    // Methods
    public void loadSong(Song song) {
        stopSong();
        currentSong = song;
        if (currentSong != null) {
            currentTimeInMilliseconds = 0;
            mainFrame.getViewPanel().setPlaybackSliderValue(0);
            mainFrame.getViewPanel().updateSongTitleAndArtist(currentSong); // Update song title and artist
            playCurrentSong();
        }
    }

    public void playCurrentSong() {
        try {
            if (currentSong != null) {
                // Guard to check if the player is already playing
                if (playerThread != null && playerThread.isAlive()) {
                    playerThread.interrupt();  // Interrupt the current player thread
                }

                FileInputStream fileInputStream = new FileInputStream(currentSong.getFile());

                long skipBytes = (long) ((currentTimeInMilliseconds / (double) currentSong.getDurationInSeconds()) * currentSong.getFrameLength());

                // Ensure skipBytes is within a valid range
                if (skipBytes < 0) {
                    skipBytes = 0;
                } else if (skipBytes > fileInputStream.available()) {
                    skipBytes = fileInputStream.available();
                }

                fileInputStream.skip(skipBytes);

                // Ensure previous player is stopped before creating a new one
                if (advancedPlayer != null) {
                    advancedPlayer.close();
                }

                advancedPlayer = new AdvancedPlayer(fileInputStream);
                advancedPlayer.setPlayBackListener(this);
                songFinished = false;

                playerThread = new Thread(() -> {
                    try {
                        startPlaybackSliderThread();
                        advancedPlayer.play();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
                playerThread.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loadPlaylist(File playlistFile) {
        stopSong();  // Ensure any currently playing song is stopped
        playlist.clear();

        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(playlistFile))) {
            String songPath;
            while ((songPath = bufferedReader.readLine()) != null) {
                Song song = new Song(songPath);
                playlist.add(song);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (!playlist.isEmpty()) {
            currentPlaylistIndex = 0;
            loadSong(playlist.get(currentPlaylistIndex));  // Load and play the first song
            mainFrame.getViewPanel().updateSongTitleAndArtist(currentSong);
        }
    }

    @Override
    public void playSong() {
        if (isPaused && advancedPlayer != null) {
            resumeSong();
        } else {
            playCurrentSong();
        }
    }

    @Override
    public void pauseSong() {
        if (advancedPlayer != null) {
            isPaused = true;
            advancedPlayer.close();
        }
    }

    @Override
    public void resumeSong() {
        if (isPaused) {
            isPaused = false;
            playCurrentSong();  // This should now start from the last known position
        }
    }

    @Override
    public void stopSong() {
        if (advancedPlayer != null) {
            advancedPlayer.close();
            advancedPlayer = null;
        }
        songFinished = true;
        isPaused = false;
        currentTimeInMilliseconds = 0;

        // Stop the slider thread
        if (playbackSliderThread != null && playbackSliderThread.isAlive()) {
            playbackSliderThread.interrupt();
        }

        // Stop the player thread
        if (playerThread != null && playerThread.isAlive()) {
            playerThread.interrupt();
        }
    }

    @Override
    public void nextSong() {  // Move to the next song in the playlist
        if (playlist != null && !playlist.isEmpty()) {
            currentPlaylistIndex = (currentPlaylistIndex + 1) % playlist.size();
            loadSong(playlist.get(currentPlaylistIndex));
            mainFrame.getViewPanel().updateSongTitleAndArtist(currentSong); // Update song title and artist
            playCurrentSong();  // Ensure the next song starts playing automatically
        }
    }

    @Override
    public void prevSong() {
        pressedPrev = true;
        if (playlist != null && !playlist.isEmpty()) {
            currentPlaylistIndex = (currentPlaylistIndex - 1 + playlist.size()) % playlist.size();
            loadSong(playlist.get(currentPlaylistIndex));
        }
        pressedPrev = false;
    }

    private void startPlaybackSliderThread() {
        if (playbackSliderThread != null && playbackSliderThread.isAlive()) {
            playbackSliderThread.interrupt();
        }

        playbackSliderThread = new Thread(() -> {
            while (!isPaused && !songFinished && !pressedNext && !pressedPrev) {
                synchronized (playSignal) {
                    try {
                        // Update the current time in milliseconds
                        currentTimeInMilliseconds += 1000;

                        // Update the slider value on the UI thread
                        SwingUtilities.invokeLater(() -> mainFrame.getToolBar().setPlaybackSliderValue(getPlaybackPositionInSeconds()));

                        // Sleep for 1 second
                        playSignal.wait(1000);
                    } catch (InterruptedException e) {
                        return;
                    } catch (Exception e) {
                        e.printStackTrace();
                        System.out.println("Error updating playback slider");
                    }
                }
            }
        });
        playbackSliderThread.start();
    }

    @Override
    public void playbackStarted(PlaybackEvent evt) {
        System.out.println("Playback Started");
        songFinished = false;
    }

    @Override
    public void playbackFinished(PlaybackEvent evt) {
        if (!isPaused && !pressedNext && !pressedPrev) {
            // Automatically play the next song if not paused or if next/prev buttons are not pressed
            nextSong();
        } else if (isPaused) {
            currentTimeInMilliseconds += evt.getFrame();
        }
    }

    public void seekTo(int seconds) {
        try {
            if (currentSong != null) {
                stopSong();

                int newTimeInMilliseconds = Math.min(seconds * 1000, currentSong.getDurationInSeconds() * 1000);

                setCurrentTimeInMilliseconds(newTimeInMilliseconds);

                playCurrentSong();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}