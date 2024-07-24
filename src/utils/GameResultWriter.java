package utils;

import java.io.FileWriter;
import java.io.IOException;

/**
 * Klasa utils.Writer służy do zapisywania wyników gry do pliku.
 */
public class GameResultWriter {

    /**
     * Metoda zapisuje wynik gry do podanego pliku.
     *
     * @param result   Wynik gry jako ciąg znaków.
     * @param fileName Nazwa pliku, do którego zostanie zapisany wynik gry.
     */
    public void savetoFile(String result, String fileName) {
        try (FileWriter writer = new FileWriter(fileName, true)) {
            writer.write(result + "\n");
            System.out.println("Wynik gry został zapisany do pliku.");
        } catch (IOException e) {
            System.err.println("Wystąpił błąd zapisu: " + e.getMessage());
        }
    }
}
