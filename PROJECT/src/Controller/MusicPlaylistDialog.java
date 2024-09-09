package Controller;

import View.MainFrame;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;

/**
 * Ova klasa predstavlja dijalog za kreiranje plejliste.
 * Korisnik preko ovog dijaloga moze dodavati pjesme u plejlistu i sacuvati je u .txt fajl.
 * Klasa je bitna jer omogucava korisniku da kreira plejlistu i da je sacuva.
 */
public class MusicPlaylistDialog extends JDialog {

    private MainFrame mainFrame;
    private ArrayList<String> songPaths; // Store all of the paths to be written to a txt file (when we load a playlist)

    // Constructor
    public MusicPlaylistDialog(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        songPaths = new ArrayList<>();

        // Configure dialog
        setTitle("The greatest playlist: ");
        setSize(400, 400);
        setResizable(false);
        getContentPane().setBackground(Color.DARK_GRAY);
        setLayout(null);
        setModal(true);
        setLocationRelativeTo(mainFrame);
        addDialogComponents();
    }

    /**
     * Metoda koja dodaje komponente u dijalog.
     * Dodaje dugme za dodavanje pjesme u plejlistu i dugme za cuvanje plejliste.
     * Dugme za dodavanje pjesme otvara file chooser dijalog i omogucava korisniku da odabere pjesmu.
     * Dugme za cuvanje plejliste otvara file chooser dijalog i omogucava korisniku da sacuva plejlistu u .txt fajl.
     * Ukoliko korisnik odabere fajl i sacuva plejlistu, dijalog se zatvara i plejlista se ucitava u glavni prozor.
     * Ukoliko korisnik odabere fajl i ne sacuva plejlistu, dijalog se zatvara.
     *
     */

    private void addDialogComponents() {
        JPanel songContainer = new JPanel();
        songContainer.setLayout(new BoxLayout(songContainer, BoxLayout.Y_AXIS));
        songContainer.setBounds((int) (getWidth() * 0.025), 10, (int) (getWidth() * 0.90), (int) (getHeight() * 0.75));
        add(songContainer);

        JButton addSongButton = new JButton("Add");
        addSongButton.setBounds(60, (int) (getHeight() * 0.80), 100, 25);
        addSongButton.setFont(new Font("Ariel", Font.BOLD, 14));
        addSongButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser jFileChooser = new JFileChooser();
                jFileChooser.setFileFilter(new FileNameExtensionFilter("MP3", "mp3"));
                jFileChooser.setCurrentDirectory(new File("PROJECT/src/assets"));
                int result = jFileChooser.showOpenDialog(MusicPlaylistDialog.this);

                if (result == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = jFileChooser.getSelectedFile();
                    if (selectedFile != null) {
                        String filePath = selectedFile.getPath();
                        if (!songPaths.contains(filePath)) {  // Check for duplicates
                            JLabel filePathLabel = new JLabel(filePath);
                            filePathLabel.setFont(new Font("Arial", Font.BOLD, 12));
                            filePathLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK));

                            songPaths.add(filePath);
                            songContainer.add(filePathLabel);
                            songContainer.revalidate(); //this method is used to notify the layout manager to recalculate the layout of the components
                            songContainer.repaint();  // Repaint to update UI
                        }
                    }
                }
            }
        });
        add(addSongButton);

        JButton savePlaylistButton = new JButton("Save");
        savePlaylistButton.setBounds(215, (int) (getHeight() * 0.80), 100, 25);
        savePlaylistButton.setFont(new Font("Dialog", Font.BOLD, 14));
        savePlaylistButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                savePlaylist();
            }
        });
        add(savePlaylistButton);
    }

    private void savePlaylist() {
        try {
            if (songPaths.isEmpty()) {
                JOptionPane.showMessageDialog(this, "First add songs to playlist");
                return;
            }

            String playlistName = JOptionPane.showInputDialog(this, "Enter playlist name:");
            if (playlistName == null || playlistName.trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Playlist name cannot be empty");
                return;
            }

            File selectedFile = new File("PROJECT/src/assets/" + playlistName + ".txt");

            if (selectedFile != null) {
                selectedFile.createNewFile();

                BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(selectedFile));
                for (String songPath : songPaths) {
                    bufferedWriter.write(songPath + "\n");
                }
                bufferedWriter.close();

                JOptionPane.showMessageDialog(this, "Successfully Created Playlist!");

                mainFrame.loadPlaylist(selectedFile); // Load the created playlist in MainFrame

                this.dispose();
            }
        } catch (Exception exception) { // Catch all exceptions if something goes wrong
            exception.printStackTrace();
        }
    }
}