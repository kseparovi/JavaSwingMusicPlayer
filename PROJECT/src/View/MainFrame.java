package View;

import Model.MusicPlayer;
import Model.Song;

import javax.swing.*;
import java.awt.*;
import java.io.File;


public class MainFrame extends JFrame {

    private ViewPanel viewPanel; // This field is used to display the album art, song title, and artist
    private MusicPlayer musicPlayer; // This field is used to play songs and manage the playlist
    private ToolBar toolBar;
    private MenuBar menuBar;

    public MainFrame() {
        // Initialize components
        viewPanel = new ViewPanel();
        musicPlayer = new MusicPlayer(this);
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