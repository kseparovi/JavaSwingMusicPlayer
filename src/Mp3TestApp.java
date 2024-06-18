import View.MainFrame;

import javax.swing.*;

public class Mp3TestApp {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable(){
            @Override
            public void run(){
                new MainFrame().setVisible(true);


            }
        });
    }

}
