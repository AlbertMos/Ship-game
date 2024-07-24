package utils;

import client.BattleshipClient;

/**
 * Klasa utils.Launcher uruchamia dwóch klientów gry w statki w osobnych wątkach.
 */
public class Launcher {
    /**
     * Metoda główna uruchamia dwóch klientów gry w osobnych wątkach.
     */
    public static void main(String[] args) {

        // Uruchamia pierwszego klienta w osobnym wątku
        new Thread(() -> {
            BattleshipClient.main(new String[0]);
        }).start();

        // Uruchamia drugiego klienta w osobnym wątku
        new Thread(() -> {
            BattleshipClient.main(new String[0]);
        }).start();
    }
}
