package client;

import java.io.*;
import java.net.Socket;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Klasa client.BattleshipClient obsługuje logikę klienta gry w statki oraz interfejs graficzny.
 */
public class BattleshipClient {
    private JFrame frame;
    private JButton[][] playerButtons; //przyciski graczy
    private JButton[][] opponentButtons;
    private JTextArea messageArea;
    private Socket socket;
    private PrintWriter out; //strumien do wysylania danych
    private BufferedReader in;
    private String playerName;
    private boolean placingShips = true; //flaga czy rozmieszcza statki
    private int shipsPlaced = 0; //licznik statkow
    private boolean[][] playerShips = new boolean[5][5]; //przechowuje pozycje statkow

    /**
     * Konstruktor inicjuje połączenie z serwerem i ustawia GUI.
     */
    public BattleshipClient(String serverAddress, String playerName) {
        this.playerName = playerName;

        try {
            socket = new Socket(serverAddress, 12345);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        frame = new JFrame("Statki - " + playerName);
        playerButtons = new JButton[5][5];
        opponentButtons = new JButton[5][5];
        messageArea = new JTextArea(5, 20);
        messageArea.setEditable(false);
        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(true);
        frame.setLayout(new BorderLayout());

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS)); //układa komponenty w kolumnie

        JPanel playerPanel = new JPanel(new GridLayout(5, 5));
        JPanel opponentPanel = new JPanel(new GridLayout(5, 5));
        JPanel playerPanelContainer = new JPanel(new BorderLayout());
        JPanel opponentPanelContainer = new JPanel(new BorderLayout());

        JLabel playerLabel = new JLabel("Twoje statki", JLabel.CENTER);
        JLabel opponentLabel = new JLabel("Statki przeciwnika", JLabel.CENTER);

        JButton exitButton = new JButton("Zakończ grę");
        exitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int choice = JOptionPane.showConfirmDialog(frame, "Czy na pewno chcesz zakończyć grę?", "Zakończ grę", JOptionPane.YES_NO_OPTION);
                if (choice == JOptionPane.YES_OPTION) {
                    out.println("EXIT");
                    System.exit(0);
                }
            }
        });

        playerPanelContainer.add(playerLabel, BorderLayout.NORTH);
        playerPanelContainer.add(playerPanel, BorderLayout.CENTER);
        opponentPanelContainer.add(opponentLabel, BorderLayout.NORTH);
        opponentPanelContainer.add(opponentPanel, BorderLayout.CENTER);

        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                playerButtons[i][j] = new JButton();
                playerButtons[i][j].addActionListener(new PlayerButtonClickListener(i, j));
                playerPanel.add(playerButtons[i][j]);

                opponentButtons[i][j] = new JButton();
                opponentButtons[i][j].setEnabled(false);
                opponentButtons[i][j].addActionListener(new OpponentButtonClickListener(i, j));
                opponentPanel.add(opponentButtons[i][j]);
            }
        }

        mainPanel.add(playerPanelContainer);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10))); // Mała przestrzeń między panelami
        mainPanel.add(opponentPanelContainer);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10))); // Mała przestrzeń przed przyciskiem zakończenia
        mainPanel.add(new JScrollPane(messageArea)); // Dodaj obszar wiadomości
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10))); // Mała przestrzeń przed przyciskiem zakończenia
        mainPanel.add(exitButton);

        frame.add(mainPanel, BorderLayout.CENTER);
        frame.setSize(800, 800);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        new Thread(new ServerListener()).start();
    }

    /**
     * Obsługuje kliknięcie przycisku gracza do rozmieszczania statków.
     */
    private class PlayerButtonClickListener implements ActionListener {
        private int row, col;

        public PlayerButtonClickListener(int row, int col) {
            this.row = row;
            this.col = col;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (placingShips) { //sprawdza czy gracz jest w trybie rozmieszczania statkow
                if (shipsPlaced < 3) { // kazdy gracz rozmieszcza 3 statki
                    playerButtons[row][col].setText("S");
                    playerShips[row][col] = true;
                    out.println("PLACE_SHIP " + row + " " + col);
                    shipsPlaced++;
                    if (shipsPlaced == 3) {
                        out.println("DONE");
                        placingShips = false;
                    }
                }
            }
        }
    }

    /**
     * Obsługuje kliknięcie przycisku przeciwnika do strzelania.
     */
    private class OpponentButtonClickListener implements ActionListener {
        private int row, col;

        public OpponentButtonClickListener(int row, int col) {
            this.row = row;
            this.col = col;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            out.println("FIRE " + row + " " + col);
            disableOpponentButtons();
        }
    }

    /**
     * Wyłącza przyciski planszy przeciwnika.
     */
    private void disableOpponentButtons() {
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                opponentButtons[i][j].setEnabled(false);
            }
        }
    }

    /**
     * Włącza przyciski planszy przeciwnika.
     */
    private void enableOpponentButtons() {
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                if (opponentButtons[i][j].getText().equals("") || opponentButtons[i][j].getText().equals("O")) {
                    opponentButtons[i][j].setEnabled(true);
                }
            }
        }
    }

    /**
     * Resetuje stan gry.
     */
    private void resetGame() {
        placingShips = true;
        shipsPlaced = 0;
        playerShips = new boolean[5][5];
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                playerButtons[i][j].setText("");
                opponentButtons[i][j].setText("");
                opponentButtons[i][j].setEnabled(false);
            }
        }
        messageArea.setText("");
    }

    /**
     * Dodaje wiadomość do panelu tekstowego.
     */
    private void appendMessage(String message) {
        messageArea.append(message + "\n"); //dodaje na koniec tekstu wiadomosc i daje enter
        messageArea.setCaretPosition(messageArea.getDocument().getLength()); //ustawia kursor na koniec dzieki metodzie ktora zwraca pozycje ostatniego znaku
    }

    /**
     * Nasłuchuje wiadomości z serwera i odpowiednio reaguje.
     */
    private class ServerListener implements Runnable {
        @Override
        public void run() {
            try {
                String response;
                while ((response = in.readLine()) != null) { //czyta strumien od serwera
                    if (response.equals("PLACE_SHIPS")) {
                        placingShips = true; //czy rozmieszcza statki
                        appendMessage("Rozmieść swoje statki!");
                    } else if (response.equals("WAIT")) {
                        placingShips = false;
                        appendMessage("Czekaj na atak.");
                    } else if (response.equals("YOUR_TURN")) {
                        enableOpponentButtons();
                        appendMessage("Twoja kolej.");
                    } else if (response.equals("YOU_WERE_HIT")) {
                        appendMessage("Twój statek został trafiony!");
                    } else if (response.startsWith("OPPONENT_MOVE")) {
                        String[] parts = response.split(" ");
                        if (parts.length == 4) {
                            try {
                                int row = Integer.parseInt(parts[1]);
                                int col = Integer.parseInt(parts[2]);
                                String result = parts[3];
                                playerButtons[row][col].setText(result.equals("HIT") ? "X" : "O");
                            } catch (NumberFormatException e) {
                                System.err.println("Otrzymano nieprawidłowe współrzędne z serwera: " + response);
                            }
                        }
                    } else if (response.startsWith("RESULT")) {
                        String[] parts = response.split(" ");
                        if (parts.length == 4) {
                            try {
                                int row = Integer.parseInt(parts[1]);
                                int col = Integer.parseInt(parts[2]);
                                String result = parts[3];
                                opponentButtons[row][col].setText(result.equals("HIT") ? "X" : "O");
                                appendMessage(result.equals("HIT") ? "Trafiłeś statek!" : "Pudło.");
                                if (result.equals("HIT")) {
                                    enableOpponentButtons(); // Ponownie włącz przyciski, jeśli trafiono
                                }
                            } catch (NumberFormatException e) {
                                System.err.println("Otrzymano nieprawidłowe współrzędne z serwera: " + response);
                            }
                        }
                    } else if (response.equals("HIT_AGAIN")) {
                        appendMessage("Trafiłeś statek! Masz dodatkowy ruch.");
                        enableOpponentButtons();
                    } else if (response.equals("START")) {
                        appendMessage("Gra rozpoczęta!");
                    } else if (response.startsWith("GAME_OVER")) {
                        String[] parts = response.split(" ");
                        String result = parts[1];
                        int hits = Integer.parseInt(parts[2]);
                        long durationMillis = Long.parseLong(parts[3]);
                        long seconds = durationMillis / 1000;
                        long minutes = seconds / 60;
                        seconds %= 60;
                        String resultMessage = (result.equals("WIN") ? "Wygrałeś!" : "Przegrałeś.") +
                                "\nTrafienia: " + hits +
                                "\nCzas gry: " + minutes + " minut " + seconds + " sekund";
                        appendMessage(resultMessage);
                        int choice = JOptionPane.showConfirmDialog(frame, "Czy chcesz zagrać ponownie?", "Koniec gry", JOptionPane.YES_NO_OPTION);
                        if (choice == JOptionPane.YES_OPTION) {
                            out.println("RESTART");
                            resetGame();
                        } else {
                            out.println("EXIT");
                            System.exit(0);
                        }
                    } else if (response.equals("EXIT")) {
                        appendMessage("Gra została zakończona.");
                        System.exit(0);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        new StartWindow();
    }
}
