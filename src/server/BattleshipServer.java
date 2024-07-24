package server;

import utils.GameResultWriter;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Klasa server.BattleshipServer obsługuje logikę serwera gry w statki.
 */
public class BattleshipServer {
    private static Socket client1, client2;
    private static PrintWriter out1, out2; //do wysylania danych do klientow
    private static BufferedReader in1, in2;
    private static boolean[][] client1Ships = new boolean[5][5]; //przechowuje pozycje statkow
    private static boolean[][] client2Ships = new boolean[5][5];
    private static int client1Hits = 0, client2Hits = 0; //licznik trafien
    private static Instant startTime; //przechowuje czas rozpoczecia gry. Dzieki temu mozemy sledzic potem czas gry. z pakietu java time

    /**
     * Metoda główna uruchamiająca serwer i oczekująca na połączenia klientów.
     */
    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(12345);
        System.out.println("Serwer uruchomiony. Oczekiwanie na klientów...");

        client1 = serverSocket.accept();
        System.out.println("Klient 1 połączony.");
        out1 = new PrintWriter(client1.getOutputStream(), true);
        in1 = new BufferedReader(new InputStreamReader(client1.getInputStream()));

        client2 = serverSocket.accept();
        System.out.println("Klient 2 połączony.");
        out2 = new PrintWriter(client2.getOutputStream(), true);
        in2 = new BufferedReader(new InputStreamReader(client2.getInputStream()));

        startGame();
    }

    /**
     * Rozpoczyna grę, zarządza fazami rozmieszczania statków i wykonywania ruchów.
     */
    private static void startGame() throws IOException {
        resetGame();

        // Faza 1: Klient 1 rozmieszcza statki
        out1.println("PLACE_SHIPS"); //klient pierwszy otrzymuje polecenie rozstawienia, a drugi czekania
        out2.println("WAIT");
        waitForShips(in1, client1Ships);

        // Faza 2: Klient 2 rozmieszcza statki
        out1.println("WAIT");
        out2.println("PLACE_SHIPS");
        waitForShips(in2, client2Ships);

        // Rozpocznij grę
        out1.println("START");
        out2.println("START");

        startTime = Instant.now(); // Rozpocznij timer

        boolean client1Turn = true;
        while (true) {
            if (client1Turn) {
                out1.println("YOUR_TURN");
                out2.println("WAIT");
                boolean hit = processMove(in1, out1, out2, client2Ships, true); //hit sprawdza, czy trafilismy w statek
                if (!hit) {
                    client1Turn = false;
                }
            } else {
                out2.println("YOUR_TURN");
                out1.println("WAIT");
                boolean hit = processMove(in2, out2, out1, client1Ships, false);
                if (!hit) {
                    client1Turn = true;
                }
            }
            if (client1Hits == 3) {
                endGame("WIN", "LOSE", client1Hits, client2Hits);
                break;
            } else if (client2Hits == 3) {
                endGame("LOSE", "WIN", client1Hits, client2Hits);
                break;
            }
        }
    }

    /**
     * Kończy grę, przesyła wyniki do klientów i zarządza ponownym uruchomieniem gry lub wyjściem.
     */
    private static void endGame(String result1, String result2, int client1Hits, int client2Hits) throws IOException {
        Duration gameDuration = Duration.between(startTime, Instant.now()); // Oblicz czas gry
        long seconds = gameDuration.getSeconds();
        long minutes = seconds / 60;
        seconds %= 60;

        //Wybieramy bierzacy czas do zapisu i formatujemy kod
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedDateTime = now.format(formatter);

        String gameResult = "Data i godzina gry: " + formattedDateTime + "\n" +
                "Gracz 1: " + (result1.equals("WIN") ? "Wygrał" : "Przegrał") +
                "\nTrafienia: " + client1Hits +
                "\nGracz 2: " + (result2.equals("WIN") ? "Wygrał" : "Przegrał") +
                "\nTrafienia: " + client2Hits +
                "\nCzas gry: " + minutes + " minut " + seconds + " sekund\n";

        out1.println("GAME_OVER " + result1 + " " + client1Hits + " " + gameDuration.toMillis());
        out2.println("GAME_OVER " + result2 + " " + client2Hits + " " + gameDuration.toMillis());

        GameResultWriter gameResultWriter = new GameResultWriter();
        gameResultWriter.savetoFile(gameResult, "wyniki_gry.txt");

        String response1 = in1.readLine();
        String response2 = in2.readLine();

        if (response1.equals("RESTART") && response2.equals("RESTART")) {
            startGame();
        } else {
            out1.println("EXIT");
            out2.println("EXIT");
            client1.close();
            client2.close();
        }
    }

    /**
     * Przetwarza ruch wykonany przez gracza, sprawdza trafienia i zarządza kolejnością ruchów.
     */
    private static boolean processMove(BufferedReader in, PrintWriter out, PrintWriter opponentOut, boolean[][] opponentShips, boolean isClient1) throws IOException {
        String move = in.readLine();
        if (move.startsWith("FIRE")) {
            String[] parts = move.split(" ");
            int row = Integer.parseInt(parts[1]);
            int col = Integer.parseInt(parts[2]);
            boolean hit = opponentShips[row][col];
            opponentOut.println("OPPONENT_MOVE " + row + " " + col + " " + (hit ? "HIT" : "MISS"));
            out.println("RESULT " + row + " " + col + " " + (hit ? "HIT" : "MISS"));
            if (hit) {
                opponentOut.println("YOU_WERE_HIT"); // Poinformuj przeciwnika, że został trafiony
                if (isClient1) {
                    client1Hits++;
                } else {
                    client2Hits++;
                }
                out.println("HIT_AGAIN");
            }
            return hit;
        }
        return false;
    }

    /**
     * Oczekuje na rozmieszczenie statków przez gracza.
     */
    private static void waitForShips(BufferedReader in, boolean[][] ships) throws IOException {
        String line;
        while (!(line = in.readLine()).equals("DONE")) {
            if (line.startsWith("PLACE_SHIP")) {
                String[] parts = line.split(" "); //dzieli na 3 czesci: placeShip, 1, 2
                int row = Integer.parseInt(parts[1]);
                int col = Integer.parseInt(parts[2]);
                ships[row][col] = true;
            }
        }
    }

    /**
     * Resetuje stan gry do początkowego.
     */
    private static void resetGame() {
        client1Ships = new boolean[5][5]; //tablice statkow sa resetowane
        client2Ships = new boolean[5][5];
        client1Hits = 0; //ilosc trafien jest resetowana
        client2Hits = 0;
    }
}
