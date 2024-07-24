# Projekt: Gra w Statki
**Autor:** Albert Mossakowski

## 1. Wymagania oraz zasoby

### Wymagania systemowe:
- System operacyjny: Windows
- Zainstalowana Java: w wersji 8 lub wyższej
- Biblioteki: standardowe Java SE

### Wymagania uruchomieniowe:
- Serwer: Uruchomienie serwera przed uruchomieniem klientów
- Klienci: Klienci łączą się do serwera przez adres IP i port 12345

## 2. Krótki opis działania programu
Gra w statki jest dwuosobową grą, w której każdy z graczy ma za zadanie zatopić wszystkie statki przeciwnika. Gra składa się z dwóch etapów:
1. Rozmieszczenie statków na swojej planszy.
2. Strzelanie do statków przeciwnika.
Każdy gracz umieszcza na swojej planszy 3 statki, a następnie wykonuje na przemian ruchy, starając się trafić w statki przeciwnika. Jeśli gracz trafi w statek, otrzymuje dodatkowy ruch. Gra kończy się, gdy jeden z graczy zatopi wszystkie statki przeciwnika.

## 3. Opis funkcjonalności
- **Okno startowe**: Pozwala graczom na podanie swoich nazw graczy.
- **Okna gry**: Zawierają plansze - jedną dla każdego gracza. Każdy gracz umieszcza na swojej planszy swoje statki, a potem próbuje trafić w statki przeciwnika, klikając na komórki planszy.
- **Komunikaty**: Informują gracza o aktualnym stanie gry (tura gracza, trafienie, pudło, wygrana, przegrana).
- **Przyciski**: Aktywne przyciski pozwalają na wykonanie ruchu, a nieaktywne informują o oczekiwaniu na ruch przeciwnika oraz zakończenie programu.

## 4. Implementacja
### Struktura projektu
Projekt składa się z trzech głównych pakietów:
1. `server`
   - `BattleshipServer`: Odpowiada za logikę serwera gry, zarządza komunikacją między dwoma klientami oraz przechowuje stan gry.
2. `client`
   - `BattleshipClient`: Odpowiada za logikę klienta gry, w tym interfejs graficzny oraz komunikację z serwerem.
   - `StartWindow`: Odpowiada za początkowe okno, w którym gracz podaje swoje imię.
3. `utils`
   - `GameResultWriter`: Zapisuje wyniki gry do pliku tekstowego.
   - `Launcher`: Uruchamia dwóch klientów gry w statki w osobnych wątkach.

### Opis poszczególnych klas
- **BattleshipServer**
  - `main`: Uruchamia serwer, akceptuje połączenia od dwóch klientów i rozpoczyna grę.
  - `startGame`: Rozpoczyna grę, zarządza fazami rozmieszczania statków i wykonywania ruchów.
  - `endGame`: Kończy grę, przesyła wyniki do klientów i zarządza ponownym uruchomieniem gry lub wyjściem.
  - `processMove`: Przetwarza ruch wykonany przez gracza, sprawdza trafienia i zarządza kolejnością ruchów.
  - `waitForShips`: Oczekuje na rozmieszczenie statków przez gracza.
  - `resetGame`: Resetuje stan gry do początkowego.

- **BattleshipClient**
  - `BattleshipClient`: Konstruktor inicjuje połączenie z serwerem i ustawia GUI.
  - `PlayerButtonClickListener`: Obsługuje kliknięcie przycisku gracza do rozmieszczania statków.
  - `OpponentButtonClickListener`: Obsługuje kliknięcie przycisku przeciwnika do strzelania.
  - `disableOpponentButtons`: Wyłącza przyciski planszy przeciwnika.
  - `enableOpponentButtons`: Włącza przyciski planszy przeciwnika.
  - `resetGame`: Resetuje stan gry.
  - `appendMessage`: Dodaje wiadomość do panelu tekstowego.
  - `ServerListener`: Nasłuchuje wiadomości z serwera i odpowiednio reaguje.

- **StartWindow**
  - `StartWindow`: Konstruktor inicjuje okno startowe, w którym gracz podaje swoje imię.
  - `main`: Uruchamia okno startowe.

- **GameResultWriter**
  - `zapiszDoPliku`: Zapisuje wynik gry do podanego pliku.

### Mechanizmy języka, funkcje, dodatkowe biblioteki
- **Swing**: Do tworzenia interfejsu graficznego.
- **Socket**: Do komunikacji sieciowej między serwerem a klientami.
- **PrintWriter** i **BufferedReader**: Do obsługi strumieni wejścia i wyjścia.

## 5. Problemy i rozwiązania
### Największe problemy
- **Zarządzanie ruchem graczy**: Problemy z prawidłowym przekazywaniem tury między graczami oraz zarządzaniem dodatkowymi ruchami po trafieniu.
- **Komunikacja sieciowa**: Problemy z synchronizacją ruchów i stanem gry między klientami a serwerem.

### Rozwiązania
- Poprawne zarządzanie aktywacją i dezaktywacją przycisków planszy przeciwnika.
- Dokładne przetestowanie i debugowanie komunikacji sieciowej.

### Powód do dumy
- Udało się stworzyć w pełni funkcjonalną grę sieciową, w której dwaj gracze mogą grać w statki w czasie rzeczywistym.
- Interfejs graficzny jest przejrzysty i łatwy w obsłudze, a komunikaty dla graczy są jasne i zrozumiałe.

## 6. Dodatkowe funkcjonalności
- Możliwość ponownego uruchomienia gry po zakończeniu.
- Wyświetlanie wyników gry, takich jak liczba trafionych statków i czas gry.
