package View;

import javax.swing.*;

public class CustomToolBar extends JToolBar {
    public CustomToolBar(CustomMenuBar customMenuBar) {
        // Set the toolbar properties
        setFloatable(false);
        add(customMenuBar);
    }
}
