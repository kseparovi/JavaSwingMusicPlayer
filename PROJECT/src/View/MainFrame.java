package View;

import Model.MusicPlayer;
import Model.Song;
import javax.swing.*;
import java.awt.*;
import java.io.File;

/*
    * MainFrame je glavna klasa u aplikaciji koja predstavlja prozor aplikacije.
    * U nju su smjestene sve komponente koje se koriste u aplikaciji.
    * U njoj se nalazi ViewPanel, MusicPlayer, ToolBar i MenuBar.
    * Koristi metode updateSongTitleAndArtist, updatePlaybackSlider, enablePauseButtonDisablePlayButton, setPlaybackSliderValue, loadPlaylist.
    * Metoda updateSongTitleAndArtist se koristi za azuriranje naslova pjesme i izvodjaca.
    * Metoda updatePlaybackSlider se koristi za azuriranje trake za pustanje pjesme.
    * Metoda enablePauseButtonDisablePlayButton se koristi za omogucavanje dugmeta za pauziranje i onemogucavanje dugmeta za pustanje.
    * Metoda setPlaybackSliderValue se koristi za postavljanje vrijednosti trake za pustanje pjesme.
    * Metoda loadPlaylist se koristi za ucitavanje plejliste iz fajla.
    * Takodjer imamo i gettere za ToolBar.
    *
 */

public class MainFrame extends JFrame {

    private ViewPanel viewPanel; // This field is used to display the album art, song title, and artist
    private MusicPlayer musicPlayer; // This field is used to play songs and manage the playlist
    private ToolBar toolBar;
    private MenuBar menuBar;

    public MainFrame() {
        // Initialize components
        musicPlayer = new MusicPlayer(this);
        viewPanel = new ViewPanel();
        toolBar = new ToolBar(this, musicPlayer); // Toolbar instance
        menuBar = new MenuBar(this, musicPlayer, new JFileChooser());

        // Set up the frame
        setTitle("Music Player");
        setSize(400, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout()); // Use BorderLayout
        setResizable(false);

        // Set MenuBar
        setJMenuBar(menuBar);

        // Configure and add ViewPanel (for album art, song title, artist)
        viewPanel.setBackground(new Color(0, 0, 25)); // Match the background
        add(viewPanel, BorderLayout.CENTER);

        // Create a panel to hold the toolbar and playback slider
        JPanel bottomPanel = new JPanel(new BorderLayout());

        // Add ToolBar to the bottom panel
        bottomPanel.add(toolBar, BorderLayout.NORTH);

        // Add bottom panel to the SOUTH of the main frame
        add(bottomPanel, BorderLayout.SOUTH);

        // Make the frame visible
        setVisible(true);
    }

    //Methods

    public void updateSongTitleAndArtist(Song song) {
        viewPanel.updateSongDetails(song.getSongArtist(), song.getSongTitle(), song.getAlbumArtPath());
    }

    public void updatePlaybackSlider(Song song) {
        // Update the playback slider in the toolbar
        toolBar.updatePlaybackSlider(song);
    }

    public void enablePauseButtonDisablePlayButton() {
        toolBar.enablePauseButtonDisablePlayButton();
    }

    public void setPlaybackSliderValue(int seconds) {
        toolBar.setPlaybackSliderValue(seconds);
    }

    public void loadPlaylist(File selectedFile) {
        musicPlayer.loadPlaylist(selectedFile);
        updateSongTitleAndArtist(musicPlayer.getCurrentSong());
        updatePlaybackSlider(musicPlayer.getCurrentSong());
        musicPlayer.playSong();
    }

    public ToolBar getToolBar() {
        return toolBar;
    }

    public MainFrame getViewPanel() {
        return this;
    }
}