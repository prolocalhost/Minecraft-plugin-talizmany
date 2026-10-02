# 🔮 localhost-talizmany

![Java](https://img.shields.io/badge/Java-17-orange.svg)
![Minecraft](https://img.shields.io/badge/Minecraft-1.20.4-brightgreen.svg)
![Engine](https://img.shields.io/badge/Server-Paper%20%7C%20Spigot%20%7C%20Purpur-blue.svg)
![Build](https://img.shields.io/badge/Build-Maven-red.svg)
![Author](https://img.shields.io/badge/Author-Localhost-purple.svg)

Kompletny, zaawansowany i wysoce zoptymalizowany plugin dodający na serwer **Magiczne Talizmany** z unikalnymi zdolnościami pasywnymi oraz aktywnymi, efektami cząsteczkowymi (particles), dźwiękami, dynamicznym paskiem **ActionBar** oraz interaktywnym menu **GUI**.

Plugin stworzony z myślą o serwerach Survival, Anarchia, PvP, Gildie, BoxPvP oraz MediumHC.

---

## ✨ Funkcje i Możliwości

- **5 Unikalnych Talizmanów**:
  - 🩸 **Talizman Wampiryzmu** (*Mityczny*) – Szansa na kradzież życia (lifesteal) od przeciwnika podczas ciosu, z efektami cząsteczkowymi krwi/obrażeń.
  - 🕊️ **Talizman Feniksa** (*Legendarny*) – Chroni gracza przed śmiertelnym ciosem, odnawia życie, nadaje regenerację, absorpcję i odporność na ogień wraz z efektem totemu.
  - ⚡ **Talizman Gromu** (*Rzadki*) – Szansa na porażenie przeciwnika potężnym piorunem i zadanie dodatkowych obrażeń przy uderzeniu.
  - 💨 **Talizman Wiatru** (*Epicki*) – **Zdolność aktywna (PPM)**: potężny odskok powietrzny w stronę patrzenia gracza z ochroną przed obrażeniami od upadku (fall damage).
  - 🛡️ **Talizman Tytana** (*Epicki*) – Pancerz awaryjny: automatycznie aktywuje odporność (Resistance II), gdy zdrowie gracza spadnie poniżej 30%.
- **Nowoczesna technologia PersistentDataContainer (PDC)**:
  - Każdy talizman posiada unikalny identyfikator `NamespacedKey` – gracze nie mogą sfałszować talizmanu przez zmianę nazwy w kowadle.
- **Wsparcie CustomModelData**:
  - Pełna integracja z serwerowymi paczkami zasobów (Resource Pack, ItemsAdder, Oraxen).
- **Dynamiczny ActionBar**:
  - Pasek stanu na dole ekranu pokazujący w czasie rzeczywistym czas odnawiania talizmanu trzymanego w dłoni.
- **Katalog GUI (`/talizman`)**:
  - Estetyczne menu podglądu wszystkich dostępnych talizmanów na serwerze.
  - Funkcja **Shift + LPM** dla Administratorów umożliwiająca natychmiastowe pobranie talizmanu bezpośrednio z GUI.
- **Elastyczna konfiguracja (`config.yml`)**:
  - 100% edytowalne komunikaty z obsługą kolorów (`&`), dźwięków, cząsteczek, szans procentowych i czasów cooldownu.
  - Możliwość wyboru trybu aktywacji: `INVENTORY` (działa z dowolnego slotu ekwipunku) lub `HANDS` (działa tylko w ręce).

---

## 🗡️ Lista Talizmanów

| Talizman | Typ działania | Domyślny materiał | CMD | Opis mechaniki |
| :--- | :---: | :---: | :---: | :--- |
| **Wampiryzm** | Pasywny | `REDSTONE` | 101 | 35% szansy na wyssanie 25% zadanych obrażeń i uleczenie gracza |
| **Feniks** | Pasywny (Ocalenie) | `TOTEM_OF_UNDYING` | 102 | Ratuje przed śmiercią, czyści negatywne efekty, nadaje buffy (CD: 180s) |
| **Grom** | Pasywny | `AMETHYST_SHARD` | 103 | 20% szansy na uderzenie piorunem i dodatkowe obrażenia |
| **Wiatr** | **Aktywny (PPM)** | `FEATHER` | 104 | Katapultuje gracza w przód/górę z negacją obrażeń od upadku (CD: 15s) |
| **Tytan** | Pasywny (Awaryjny) | `IRON_INGOT` | 105 | Przy HP poniżej 30% nadaje Odporność II na 6 sekund (CD: 60s) |

---

## 💻 Komendy i Uprawnienia

### Komendy:
Główna komenda: `/talizman` (aliasy: `/talizmany`, `/talisman`)

- `/talizman` – Otwiera graficzne menu (GUI) z katalogiem talizmanów.
- `/talizman lista` – Wyświetla listę wszystkich zarejestrowanych talizmanów na czacie.
- `/talizman daj <gracz> <typ> [ilość]` – Nadaje graczowi określoną liczbę talizmanów (*np. `/talizman daj Steve vampirism 1`*).
- `/talizman reload` – Przeładowuje plik konfiguracyjny `config.yml`.

### Uprawnienia (Permissions):

| Uprawnienie | Domyślnie | Opis |
| :--- | :---: | :--- |
| `talizmany.use` | Wszyscy (`true`) | Dostęp do otwierania katalogu GUI `/talizman` oraz używania talizmanów |
| `talizmany.admin` | Administratorzy (`op`) | Dostęp do komendy `/talizman daj`, `/talizman reload` oraz pobierania z GUI |

---

## 📂 Struktura Projektu

```text
localhost-talizmany/
├── pom.xml
├── README.md
└── src/
    └── main/
        ├── java/
        │   └── pl/localhost/talizmany/
        │       ├── Main.java                        # Główna klasa pluginu (Main)
        │       ├── command/
        │       │   └── TalismanCommand.java         # Komendy oraz auto-uzupełnianie (TabComplete)
        │       ├── gui/
        │       │   └── TalismanGUI.java             # Menu ekwipunku GUI
        │       ├── listener/
        │       │   └── TalismanListener.java        # Obsługa obrażeń, zgonów, PPM i efektów
        │       ├── manager/
        │       │   ├── ConfigManager.java           # Zarządzanie konfiguracją i wiadomościami
        │       │   ├── CooldownManager.java         # Pomiary cooldownów oraz pętla ActionBar
        │       │   └── TalismanManager.java         # Generowanie i identyfikacja przedmiotów (PDC)
        │       └── model/
        │           └── TalismanType.java            # Enum typów talizmanów
        └── resources/
            ├── config.yml                           # Główny plik konfiguracyjny
            └── plugin.yml                           # Metadane pluginu
```

---

## 🛠️ Jak Zbudować (Kompilacja)

### Wymagania:
- **Java 17** lub nowsza
- **Maven 3.8+**

### Krok po kroku:
1. Sklonuj repozytorium lub przejdź do folderu pluginu:
   ```bash
   cd localhost-talizmany
   ```
2. Zbuduj paczkę za pomocą Mavena:
   ```bash
   mvn clean package
   ```
3. Gotowy plik `.jar` pojawi się w katalogu `target/`:
   ```text
   target/localhost-talizmany-1.0-SNAPSHOT.jar
   ```
4. Wrzuć plik do folderu `plugins/` swojego serwera Paper/Spigot 1.20+ i zrestartuj serwer!

---

## ⚙️ Wymagania i Zależności

- **Zależności obowiązkowe:** Brak! Plugin jest w pełni niezależny (*standalone*) i działa natywnie na silnikach **Paper**, **Purpur** oraz **Spigot 1.20+**.
- **Kompatybilność:** Java 17+, Paper 1.20 - 1.20.4+ (oraz nowsze 1.20.x).

---

## 👤 Autor

- **Autor:** Localhost
- **Discord:** `localhost_127001`
- **Projekt:** Seria pluginów serwerowych `localhost-*`
