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

    private void addDialogComponents() {
        JPanel songContainer = new JPanel();
        songContainer.setLayout(new BoxLayout(songContainer, BoxLayout.Y_AXIS));
        songContainer.setBounds((int) (getWidth() * 0.025), 10, (int) (getWidth() * 0.90), (int) (getHeight() * 0.75));
        add(songContainer);

        JButton addSongButton = new JButton("Add");
        addSongButton.setBounds(60, (int) (getHeight() * 0.80), 100, 25);
        addSongButton.setFont(new Font("Dialog", Font.BOLD, 14));
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
                            filePathLabel.setFont(new Font("Dialog", Font.BOLD, 12));
                            filePathLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK));

                            songPaths.add(filePath);
                            songContainer.add(filePathLabel);
                            songContainer.revalidate();
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
                try {
                    JFileChooser jFileChooser = new JFileChooser();
                    jFileChooser.setCurrentDirectory(new File("PROJECT/src/assets"));
                    int result = jFileChooser.showSaveDialog(MusicPlaylistDialog.this);

                    if (result == JFileChooser.APPROVE_OPTION) {
                        File selectedFile = jFileChooser.getSelectedFile();

                        if (selectedFile != null) {
                            if (!selectedFile.getName().toLowerCase().endsWith(".txt")) {
                                selectedFile = new File(selectedFile.getAbsoluteFile() + ".txt");
                            }

                            selectedFile.createNewFile();

                            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(selectedFile));
                            for (String songPath : songPaths) {
                                bufferedWriter.write(songPath + "\n");
                            }
                            bufferedWriter.close();

                            JOptionPane.showMessageDialog(MusicPlaylistDialog.this, "Successfully Created Playlist!");

                            mainFrame.loadPlaylist(selectedFile); // Load the created playlist in MainFrame

                            MusicPlaylistDialog.this.dispose();
                        }
                    }
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
            }
        });
        add(savePlaylistButton);
    }
}