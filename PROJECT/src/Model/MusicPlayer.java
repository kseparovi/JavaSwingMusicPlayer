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
    private static final Object playSignal = new Object(); // Object used for synchronization
    private MainFrame mainFrame;
    private Song currentSong;  // The currently playing song
    private ArrayList<Song> playlist;
    private int currentPlaylistIndex;
    private AdvancedPlayer advancedPlayer;
    private boolean isPaused;
    private boolean songFinished;
    private boolean pressedNext, pressedPrev;
    private int currentTimeInMilliseconds;
    private Thread playbackSliderThread;
    private Thread playerThread;

    //postavili smo dva Threada koja ce se koristiti za azuriranje slajdera za reprodukciju i za pokretanje pjesme

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

    public int getPlaybackPositionInSeconds() {
        return currentTimeInMilliseconds / 1000;
    }

    public void setCurrentTimeInMilliseconds(int timeInMilliseconds) {
        this.currentTimeInMilliseconds = timeInMilliseconds;
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



    //method overrides from PlaybackListener

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
            currentTimeInMilliseconds += evt.getFrame(); //ako je pauzirano vrijeme se povecava za vrijeme frejma
        }
    }




    //*****************************************************************************************************************

    // Methods

    public void loadSong(Song song) {
        stopSong();
        currentSong = song;
        if (currentSong != null) {
            currentTimeInMilliseconds = 0;
            mainFrame.getToolBar().setPlaybackSliderValue(0); // Update the slider value in the toolbar
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

                FileInputStream fileInputStream = new FileInputStream(currentSong.getFile()); //Serialize the file to bytes

                long skipBytes = (long) ((currentTimeInMilliseconds / (double) currentSong.getDurationInSeconds()) * currentSong.getFrameLength());
                //trenutno vrijeme u milisekundama podijeljeno sa ukupnim trajanjem pjesme u sekundama pomnozeno sa ukupnim brojem frejmova - dobijemo trenutni broj frejmova koji treba preskociti
                //treba ih preskociti jer je pjesma vec bila pokrenuta i zelimo da se nastavi od trenutka gdje je stala

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

                advancedPlayer = new AdvancedPlayer(fileInputStream); //Create a new AdvancedPlayer object which will be used to play the song
                advancedPlayer.setPlayBackListener(this);
                songFinished = false;

                playerThread = new Thread(() -> {
                    try {
                        startPlaybackSliderThread();
                        advancedPlayer.play();
                    } catch (Exception e) {
                        System.out.println("Error playing song");
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

        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(playlistFile))) { //used for reading lines of text from a file
            String songPath;
            while ((songPath = bufferedReader.readLine()) != null) {
                Song song = new Song(songPath);
                playlist.add(song);
            }
        } catch (IOException e) { //IOException is thrown when an input or output operation is failed or interpreted
            JOptionPane.showMessageDialog(mainFrame, "Error reading playlist file", "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }

        if (!playlist.isEmpty()) {
            currentPlaylistIndex = 0;
            loadSong(playlist.get(currentPlaylistIndex));  // Load and play the first song
            mainFrame.getViewPanel().updateSongTitleAndArtist(currentSong);
        }
    }

    /**
     * Ova metode sluzi za pokretanje niti koja ce azurirati vrijeme pjesme u sekundama.
     * Metoda se koristi za azuriranje slajdera za reprodukciju na korisnickom sucelju.
     * Metoda se koristi za azuriranje trenutnog vremena u milisekundama.
     * Thread je pokrenut dok se pjesma ne pauzira, dok se ne zavrsi pjesma, dok se ne pritisne sljedeca ili prethodna pjesma.
     * Thread se takodjer koristi za azuriranje slajdera za reprodukciju.
     *
     */

    private void startPlaybackSliderThread() {
        if (playbackSliderThread != null && playbackSliderThread.isAlive()) {
            playbackSliderThread.interrupt();
        }

        playbackSliderThread = new Thread(() -> { //nit koja se koristi za azuriranje slajdera za reprodukciju
            while (!isPaused && !songFinished && !pressedNext && !pressedPrev) { //ako nije pauzirano, ako pjesma nije zavrsena, ako nije pritisnuta sljedeca ili prethodna pjesma
                synchronized (playSignal) { //sinkronizacija
                    try {
                        // Update the current time in milliseconds
                        currentTimeInMilliseconds += 1000;

                        // Update the slider value on the UI thread
                        SwingUtilities.invokeLater(() -> mainFrame.getToolBar().setPlaybackSliderValue(getPlaybackPositionInSeconds()));

                        // Sleep for 1 second
                        playSignal.wait(1000);
                    } catch (InterruptedException e) {
                        System.out.println("Thread interrupted");
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


    /**
     * This method is used to seek to a specific time in the song.
     * It stops the current song, sets the new time in milliseconds and plays the song from the new time.
     * The new time is calculated in milliseconds and is within the duration of the song.
     *
     * @param seconds
     */


    public void seekTo(int seconds) {
        try {
            if (currentSong != null) {
                stopSong();

                int newTimeInMilliseconds = Math.min(seconds * 1000, currentSong.getDurationInSeconds() * 1000);
                // Convert seconds to milliseconds and ensure it is within the song duration
                //min is used to ensure that the new time in milliseconds is not greater than the duration of the song

                setCurrentTimeInMilliseconds(newTimeInMilliseconds);

                playCurrentSong();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}