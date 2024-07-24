package client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Klasa client.StartWindow obsługuje początkowe okno, w którym gracz podaje swoje imię.
 */
public class StartWindow extends JFrame {
    private JTextField playerNameField;
    private JButton startButton;

    /**
     * Konstruktor inicjuje okno startowe.
     */
    public StartWindow() {
        setTitle("Statki - Rozpocznij grę");
        setSize(300, 150);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JLabel nameLabel = new JLabel("Wprowadź swoje imię:");
        playerNameField = new JTextField();
        startButton = new JButton("Rozpocznij grę");

        JPanel panel = new JPanel(new GridLayout(2, 1));
        panel.add(nameLabel);
        panel.add(playerNameField);
        add(panel, BorderLayout.CENTER);
        add(startButton, BorderLayout.SOUTH);

        // Dodaje nasłuchiwanie na przycisk startowy
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String playerName = playerNameField.getText();
                if (!playerName.isEmpty()) {
                    new BattleshipClient("localhost", playerName); //localhost informacja, ze bedziemy dzialac na lokalnej maszynie
                    dispose(); //zamyka okno
                } else {
                    JOptionPane.showMessageDialog(StartWindow.this, "Proszę wprowadzić swoje imię.", "Błąd", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        setLocationRelativeTo(null); //ustawia na srodek ekranu
        setVisible(true);
    }

    public static void main(String[] args) {
        new StartWindow();
    }
}
