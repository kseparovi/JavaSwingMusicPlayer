package Model;

import com.mpatric.mp3agic.Mp3File;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.file.Files;
import java.util.List;


/**
 * Klasa koja predstavlja pjesmu.
 * Pjesma ima svoj naziv, izvodjaca, duzinu, putanju do fajla, mp3 fajl, frame rate po milisekundi, putanju do album arta i trajanje.
 * Klasa sadrzi konstruktor koji prima putanju do fajla i na osnovu nje popunjava sve atribute pjesme.
 * Koristimo listu za cuvanje podataka o album artu.
 *
 */

public class Song {

    private String songTitle;
    private String songArtist;
    private String songLength;
    private String filePath;
    private Mp3File mp3File;
    private double frameRatePerMilliseconds;
    private String albumArtPath;
    private int duration;

    public Song(String filePath) {
        this.filePath = filePath;
        try {
            mp3File = new Mp3File(filePath);
            frameRatePerMilliseconds = (double) mp3File.getFrameCount() / mp3File.getLengthInMilliseconds();
            songLength = convertToSongLengthFormat();
            duration = (int) mp3File.getLengthInSeconds();

            // Use the jaudiotagger library to read the mp3 file's metadata
            AudioFile audioFile = AudioFileIO.read(new File(filePath));
            Tag tag = audioFile.getTag();

            if (tag != null) {
                songTitle = tag.getFirst(FieldKey.TITLE);
                songArtist = tag.getFirst(FieldKey.ARTIST);
                albumArtPath = extractAlbumArt(tag);
            } else {
                songTitle = "N/A";
                songArtist = "N/A";
                albumArtPath = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error reading file: " + filePath);
        }
    }

    // Getters
    public String getSongTitle() {
        return songTitle;
    }

    public String getSongArtist() {
        return songArtist;
    }


    public String getAlbumArtPath() {
        return albumArtPath;
    }


    public String getFile() {
        return filePath;
    }


    public int getDurationInSeconds() {
        return (int) mp3File.getLengthInSeconds();
    }

    public int getFrameLength() {
        return mp3File.getFrameCount();
    }


    private String convertToSongLengthFormat() {
        long minutes = mp3File.getLengthInSeconds() / 60;
        long seconds = mp3File.getLengthInSeconds() % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }


    private String extractAlbumArt(Tag tag) {
        try {
            List<?> albumArtData = tag.getArtworkList();
            if (!albumArtData.isEmpty()) {
                byte[] imageData = tag.getFirstArtwork().getBinaryData();
                BufferedImage img = ImageIO.read(new ByteArrayInputStream(imageData));

                File tempFile = Files.createTempFile("albumArt", ".png").toFile();
                ImageIO.write(img, "png", tempFile);

                return tempFile.getAbsolutePath();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


}