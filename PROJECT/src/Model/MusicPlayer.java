package Model;

import View.MainFrame;
import javazoom.jl.player.advanced.AdvancedPlayer;
import javazoom.jl.player.advanced.PlaybackEvent;
import javazoom.jl.player.advanced.PlaybackListener;

import javax.swing.*;
import java.io.*;
import java.util.ArrayList;

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
    private Thread playerThread;  // Added field to manage the playback thread

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
            playCurrentSong();
        }
    }

    public void playCurrentSong() {
        try {
            if (currentSong != null) {
                // Log to ensure the method is not called multiple times
                System.out.println("Attempting to play song: " + currentSong.getFile());

                FileInputStream fileInputStream = new FileInputStream(currentSong.getFile());

                long skipBytes = (long) ((currentTimeInMilliseconds / (double) currentSong.getDurationInSeconds()) * currentSong.getFrameLength());

                // Ensure skipBytes is within valid range
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

                // Ensure only one thread is started
                if (playerThread != null && playerThread.isAlive()) {
                    playerThread.interrupt();
                }

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

        // Ensure the player thread is stopped
        if (playerThread != null && playerThread.isAlive()) {
            playerThread.interrupt();
        }
    }

    @Override
    public void nextSong() {
        pressedNext = true;
        if (playlist != null && !playlist.isEmpty()) {
            currentPlaylistIndex = (currentPlaylistIndex + 1) % playlist.size();
            loadSong(playlist.get(currentPlaylistIndex));
        }
        pressedNext = false;
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
        playbackSliderThread = new Thread(() -> {
            while (!isPaused && !songFinished && !pressedNext && !pressedPrev) {
                synchronized (playSignal) {
                    try {
                        // Update the current time in milliseconds
                        currentTimeInMilliseconds += 1000;

                        // Update the slider value on the UI thread
                        SwingUtilities.invokeLater(() -> mainFrame.getViewPanel().setPlaybackSliderValue(getPlaybackPositionInSeconds()));

                        // Sleep for 1 second
                        playSignal.wait(1000);
                    } catch (InterruptedException e) {
                        // Handle thread interruption, likely from stopSong()
                        return;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        playbackSliderThread.start();
    }

    @Override
    public void playbackStarted(PlaybackEvent evt) {
        System.out.println("Playback Started - " + evt.getFrame());
        songFinished = false;
    }

    @Override
    public void playbackFinished(PlaybackEvent evt) {
        System.out.println("Playback Finished - " + evt.getFrame());

        if (isPaused) {
            currentTimeInMilliseconds += evt.getFrame();  // Update time with the last frame played
        } else if (!pressedNext && !pressedPrev && !songFinished) {
            nextSong();
        }
    }

    public void seekTo(int seconds) {
        try {
            if (currentSong != null) {
                stopSong();  // Stop current playback

                // Calculate the new time position in milliseconds
                int newTimeInMilliseconds = Math.min(seconds * 1000, currentSong.getDurationInSeconds() * 1000);

                // Update the current time
                setCurrentTimeInMilliseconds(newTimeInMilliseconds);

                // Restart the song from the new position
                playCurrentSong();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
