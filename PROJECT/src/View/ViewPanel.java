package View;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;



class ViewPanel extends JPanel {

    private JLabel albumArtLabel;
    private JLabel songTitleLabel;
    private JLabel artistLabel;
    private ImageIcon defaultAlbumArt;

    public ViewPanel() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        // Load default album art
        defaultAlbumArt = loadImage("/assets/record1.png");
        defaultAlbumArt = new ImageIcon(defaultAlbumArt.getImage().getScaledInstance(300, 300, Image.SCALE_SMOOTH));


        // Album Art
        albumArtLabel = new JLabel(defaultAlbumArt);
        albumArtLabel.setAlignmentX(CENTER_ALIGNMENT); // Center the image
        albumArtLabel.setPreferredSize(new Dimension(300, 300));
        albumArtLabel.setMaximumSize(new Dimension(300, 300));
        add(Box.createRigidArea(new Dimension(0, 20))); // Spacer
        add(albumArtLabel);

        // Song Title
        songTitleLabel = new JLabel("Song Title");
        songTitleLabel.setAlignmentX(CENTER_ALIGNMENT);
        songTitleLabel.setForeground(Color.WHITE);
        songTitleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        add(Box.createRigidArea(new Dimension(0, 20)));
        add(songTitleLabel);

        // Artist
        artistLabel = new JLabel("Artist");
        artistLabel.setAlignmentX(CENTER_ALIGNMENT);
        artistLabel.setForeground(Color.LIGHT_GRAY);
        artistLabel.setFont(new Font("Arial", Font.PLAIN, 18));
        add(Box.createRigidArea(new Dimension(0, 10)));
        add(artistLabel);
    }

    public void updateSongDetails(String artist, String title, String albumArtPath) {
        songTitleLabel.setText(title != null && !title.isEmpty() ? title : "Unknown Title");
        artistLabel.setText(artist != null && !artist.isEmpty() ? artist : "Unknown Artist");

        // Update album art
        ImageIcon albumArtIcon;
        if (albumArtPath != null && !albumArtPath.isEmpty()) {
            albumArtIcon = new ImageIcon(albumArtPath);
        } else {
            albumArtIcon = defaultAlbumArt;
        }

        if (albumArtIcon != null) {
            // Scale image to fit label
            Image img = albumArtIcon.getImage().getScaledInstance(300, 300, Image.SCALE_SMOOTH);
            albumArtLabel.setIcon(new ImageIcon(img));
        } else {
            albumArtLabel.setIcon(null);
        }
    }

    private ImageIcon loadImage(String imagePath) {
        try {
            // Read the image file from the given path
            BufferedImage image = ImageIO.read(getClass().getResource(imagePath));
            // Return an image icon so that our component can render the image
            return new ImageIcon(image);
        } catch (IOException e) {
            e.printStackTrace();
        }
        // Could not find resource
        return null;
    }
}