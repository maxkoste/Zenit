# Zenit IDE

Zenit är en Java IDE byggd med JavaFX.

## Systemkrav

- Java JDK 21 
- **Internetanslutning** (för att ladda ner beroenden vid första körningen)

---

## Metod 1: Köra med Maven (Rekommenderas)

### Steg 1: Installera Maven

#### Windows

1. Ladda ner Maven från:
   ```
   https://archive.apache.org/dist/maven/maven-3/3.9.6/binaries/apache-maven-3.9.6-bin.zip
   ```

2. Extrahera ZIP-filen till din hemkatalog, t.ex.:
   ```
   C:\Users\<DittAnvändarnamn>\apache-maven-3.9.6
   ```

3. Lägg till Maven i systemets PATH (valfritt men rekommenderas):
   - Öppna **Systeminställningar** > **Avancerade systeminställningar** > **Miljövariabler**
   - Under **Systemvariabler**, hitta `Path` och klicka **Redigera**
   - Lägg till: `C:\Users\<DittAnvändarnamn>\apache-maven-3.9.6\bin`
   - Klicka **OK** och starta om terminalen

4. Verifiera installationen:
   ```bash
   mvn --version
   ```

#### macOS / Linux

```bash
# macOS med Homebrew
brew install maven

# Ubuntu/Debian
sudo apt install maven

# Verifiera
mvn --version
```

### Steg 2: Kompilera och köra projektet

1. Öppna terminalen och navigera till projektmappen:
   ```bash
   cd "sökväg/till/zenit"
   ```

2. Kompilera projektet:
   ```bash
   mvn clean compile
   ```

3. Kör applikationen:
   ```bash
   mvn javafx:run
   ```

**OBS:** Vid första körningen laddas alla beroenden ner automatiskt. Detta kan ta några minuter.

---

## Metod 2: Köra med Eclipse

1. Öppna Eclipse
2. Välj **File** > **Import...** > **General** > **Existing Projects into Workspace**
3. Klicka **Browse...** och välj projektmappen "zenit"
4. Klicka **Finish**

5. Ändra JRE System Library till **Java SE 11** eller senare:
   - Högerklicka på projektet > **Properties** > **Java Build Path** > **Libraries**

6. Konfigurera VM-argument:
   - **Run** > **Run Configurations** > **Java Application** > **TestUI** > **Arguments**
   - Lägg till i **VM arguments**:
   ```
   --module-path lib/javafx-sdk-11.0.2/lib/ --add-modules=javafx.controls,javafx.fxml,javafx.web --add-opens javafx.graphics/javafx.scene.text=ALL-UNNAMED --add-exports javafx.graphics/com.sun.javafx.text=ALL-UNNAMED --add-opens javafx.graphics/com.sun.javafx.text=ALL-UNNAMED --add-exports javafx.graphics/com.sun.javafx.scene.text=ALL-UNNAMED --add-exports javafx.graphics/com.sun.javafx.scene=ALL-UNNAMED --add-exports javafx.graphics/com.sun.javafx.geom=ALL-UNNAMED -Dprism.allowhidpi=true
   ```

7. Avmarkera "Use the -XstartOnFirstThread argument when launching with SWT"

8. Kör `src/main/java/zenit/ui/TestUI.java`

---

## Felsökning

| Problem | Lösning |
|---------|---------|
| `mvn` känns inte igen | Kontrollera att Maven är installerat och finns i PATH |
| Kompileringsfel | Kör `mvn clean compile` för att rensa och kompilera om |
| JavaFX-fel | Kontrollera att du har JDK 11 eller senare |

---

## Projektstruktur

```
zenit/
├── src/main/java/zenit/
│   ├── ui/              # Användargränssnitt
│   ├── console/         # Konsolhantering
│   ├── terminal/        # Inbyggd terminal
│   ├── filesystem/      # Filhantering
│   └── zencodearea/     # Kodredigerare
├── src/main/resources/  # FXML och CSS-filer
└── pom.xml              # Maven-konfiguration
```

---

## GitHub

https://github.com/strazan/zenit
