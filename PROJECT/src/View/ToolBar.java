package View;

import Model.MusicPlayer;
import Model.Song;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Hashtable;

public class ToolBar extends JToolBar {

    private MusicPlayer musicPlayer;
    private JButton playButton;
    private JButton pauseButton;
    private JButton nextButton;
    private JButton previousButton;
    private JSlider playbackSlider;
    private JLabel songTitle;
    private JLabel songArtist;
    private Timer playbackTimer;

    private final Color TEXT_COLOR = Color.WHITE;
    private boolean isDragging = false;

    // Constructor
    public ToolBar(MainFrame mainFrame, MusicPlayer musicPlayer) {
        this.musicPlayer = musicPlayer;

        // Set up the toolbar
        setFloatable(false);
        setBackground(new Color(0, 0, 25));
        setLayout(new BorderLayout());

        // Initialize song title and artist labels
        songTitle = new JLabel();
        songArtist = new JLabel();
        songTitle.setForeground(TEXT_COLOR);
        songArtist.setForeground(TEXT_COLOR);
        songTitle.setFont(new Font("Dialog", Font.BOLD, 18));
        songArtist.setFont(new Font("Dialog", Font.PLAIN, 16));

        // Create a panel for song title and artist
        JPanel titlePanel = new JPanel(new GridLayout(2, 1));
        titlePanel.add(songTitle);
        titlePanel.add(songArtist);

        // Add title panel to the top of the toolbar
        add(titlePanel, BorderLayout.NORTH);

        // Create buttons with icons
        playButton = createButton("PROJECT/src/assets/play.png");
        pauseButton = createButton("PROJECT/src/assets/pause.png");
        nextButton = createButton("PROJECT/src/assets/next.png");
        previousButton = createButton("PROJECT/src/assets/previous.png");

        // Set actions
        playButton.addActionListener(e -> {
            musicPlayer.playSong();
            enablePauseButtonDisablePlayButton();
            updatePlaybackSlider(musicPlayer.getCurrentSong());
        });

        pauseButton.addActionListener(e -> {
            musicPlayer.pauseSong();
            enablePlayButtonDisablePauseButton();
            stopPlaybackTimer();
        });

        nextButton.addActionListener(e -> {
            musicPlayer.nextSong();
            mainFrame.updateSongTitleAndArtist(musicPlayer.getCurrentSong());
            updatePlaybackSlider(musicPlayer.getCurrentSong());
        });

        previousButton.addActionListener(e -> {
            musicPlayer.prevSong();
            mainFrame.updateSongTitleAndArtist(musicPlayer.getCurrentSong());
            updatePlaybackSlider(musicPlayer.getCurrentSong());
        });

        // Create a panel to hold the buttons and center them
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setBackground(new Color(0, 0, 25));
        buttonPanel.add(previousButton);
        buttonPanel.add(playButton);
        buttonPanel.add(pauseButton);
        buttonPanel.add(nextButton);

        // Add button panel to the bottom of the toolbar
        add(buttonPanel, BorderLayout.SOUTH);

        // Add the playback slider to the toolbar (above the buttons)
        addPlaybackSlider();

        enablePlayButtonDisablePauseButton();
    }

    private JButton createButton(String iconPath) {
        JButton button = new JButton(new ImageIcon(iconPath));
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        return button;
    }

    public void enablePauseButtonDisablePlayButton() {
        playButton.setEnabled(false);
        pauseButton.setEnabled(true);
    }

    public void enablePlayButtonDisablePauseButton() {
        playButton.setEnabled(true);
        pauseButton.setEnabled(false);
    }

    private void addPlaybackSlider() {
        playbackSlider = new JSlider(JSlider.HORIZONTAL, 0, 100, 0);
        playbackSlider.setBackground(null);

        playbackSlider.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                musicPlayer.pauseSong();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                JSlider source = (JSlider) e.getSource();
                int seconds = source.getValue();
                musicPlayer.seekTo(seconds);
                enablePauseButtonDisablePlayButton();
                updatePlaybackSlider(musicPlayer.getCurrentSong());
            }
        });

        add(playbackSlider, BorderLayout.CENTER);
    }

    public void setPlaybackSliderValue(int seconds) {
        playbackSlider.setValue(seconds);
    }

    public void updateSongTitleAndArtist(Song song) {
        songTitle.setText(song.getSongTitle());
        songArtist.setText(song.getSongArtist());
    }

    public void updatePlaybackSlider(Song song) {
        // Reset the slider and timer
        if (playbackTimer != null) {
            playbackTimer.stop();
        }
        playbackSlider.setValue(0);

        // Set maximum value of the slider
        playbackSlider.setMaximum(song.getDurationInSeconds());

        // Update slider based on current position in the song every 1000 ms (1 second)
        playbackTimer = new Timer(1000, e -> {
            int currentSeconds = musicPlayer.getPlaybackPositionInSeconds();
            playbackSlider.setValue(currentSeconds);
        });

        playbackTimer.start(); // Start the timer when the song is played

        // Create labels for slider
        Hashtable<Integer, JLabel> labelTable = new Hashtable<>();
        JLabel labelBeginning = new JLabel("00:00");
        labelBeginning.setFont(new Font("Dialog", Font.BOLD, 18));
        labelBeginning.setForeground(TEXT_COLOR);

        JLabel labelEnd = new JLabel(formatTime(song.getDurationInSeconds()));
        labelEnd.setFont(new Font("Dialog", Font.BOLD, 18));
        labelEnd.setForeground(TEXT_COLOR);

        labelTable.put(0, labelBeginning);
        labelTable.put(song.getDurationInSeconds(), labelEnd);

        playbackSlider.setLabelTable(labelTable);
        playbackSlider.setPaintLabels(true);
        playbackSlider.setMajorTickSpacing(song.getDurationInSeconds() / 10);
        playbackSlider.setPaintTicks(true);
    }

    private String formatTime(int totalSeconds) {
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    public void stopPlaybackTimer() {
        if (playbackTimer != null) {
            playbackTimer.stop();
        }
    }

    public void resetPlaybackSlider() {
        playbackSlider.setValue(0);
    }
}