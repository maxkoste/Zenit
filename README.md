# Zenit IDE

Zenit is a Java IDE built with JavaFX, designed for a streamlined development experience.

![CI](https://github.com/maxkoste/Zenit/actions/workflows/ci.yml/badge.svg)

---

## System Requirements

- Java JDK 21+
- **Internet connection** (required for downloading dependencies on first run)

---

## Method 1: Running with Maven (Recommended)

### Step 1: Install Maven

#### Windows

1. Download Maven from:
```
   https://archive.apache.org/dist/maven/maven-3/3.9.6/binaries/apache-maven-3.9.6-bin.zip
```
2. Extract the ZIP to a directory of your choice, e.g.:
```
   C:\Users\<YourUsername>\apache-maven-3.9.6
```
3. Add Maven to your system PATH:
   - Open **System Properties** → **Advanced** → **Environment Variables**
   - Under **System Variables**, find `Path` and click **Edit**
   - Add: `C:\Users\<YourUsername>\apache-maven-3.9.6\bin`
   - Click **OK** and restart your terminal
4. Verify the installation:
```bash
   mvn --version
```

#### macOS / Linux
```bash
# macOS (Homebrew)
brew install maven

# Ubuntu/Debian
sudo apt install maven

# Verify
mvn --version
```

### Step 2: Build and Run

1. Open a terminal and navigate to the project directory:
```bash
   cd path/to/zenit
```
2. Compile the project:
```bash
   mvn clean compile
```
3. Run the application:
```bash
   mvn javafx:run
```

> **Note:** The first run will automatically download all required dependencies. This may take a few minutes.

---

## Method 2: Running with Eclipse

1. Open Eclipse
2. Go to **File** → **Import...** → **General** → **Existing Projects into Workspace**
3. Click **Browse...** and select the `zenit` project folder
4. Click **Finish**
5. Set the JRE System Library to **Java SE 21**:
   - Right-click the project → **Properties** → **Java Build Path** → **Libraries**
6. Configure VM arguments:
   - Go to **Run** → **Run Configurations** → **Java Application** → **TestUI** → **Arguments**
   - Add the following under **VM arguments**:
```
     --module-path lib/javafx-sdk-11.0.2/lib/
     --add-modules=javafx.controls,javafx.fxml,javafx.web
     --add-opens javafx.graphics/javafx.scene.text=ALL-UNNAMED
     --add-exports javafx.graphics/com.sun.javafx.text=ALL-UNNAMED
     --add-opens javafx.graphics/com.sun.javafx.text=ALL-UNNAMED
     --add-exports javafx.graphics/com.sun.javafx.scene.text=ALL-UNNAMED
     --add-exports javafx.graphics/com.sun.javafx.scene=ALL-UNNAMED
     --add-exports javafx.graphics/com.sun.javafx.geom=ALL-UNNAMED
     -Dprism.allowhidpi=true
```
7. Uncheck **"Use the -XstartOnFirstThread argument when launching with SWT"**
8. Run `src/main/java/zenit/ui/TestUI.java`

---

## Troubleshooting

| Issue | Solution |
|-------|----------|
| `mvn` command not found | Verify Maven is installed and added to your PATH |
| Compilation errors | Run `mvn clean compile` to do a full clean rebuild |
| JavaFX errors | Ensure you are using JDK 21 or later |
| `pty4j` dependency missing | See the section below |

### Missing pty4j Dependency

If the project fails to compile due to a missing `pty4j` dependency, install it manually:

1. Download the JAR:
```
   https://repo1.maven.org/maven2/org/jetbrains/pty4j/pty4j/0.13.12/pty4j-0.13.12.jar
```
2. Install it into your local Maven repository (update the path to match where you saved the JAR):
```bash
   mvn install:install-file \
     -Dfile=/path/to/pty4j-0.13.12.jar \
     -DgroupId=org.jetbrains.pty4j \
     -DartifactId=pty4j \
     -Dversion=0.13.12 \
     -Dpackaging=jar
```
3. Then run a clean compile as normal:
```bash
   mvn clean compile
```

---

## Project Structure
```
zenit/
├── src/
│   ├── main/
│   │   ├── java/zenit/
│   │   │   ├── ui/              # User interface
│   │   │   ├── console/         # Console management
│   │   │   ├── terminal/        # Integrated terminal
│   │   │   ├── filesystem/      # File management
│   │   │   └── zencodearea/     # Code editor
│   │   └── resources/           # FXML and CSS files
│   └── test/
│       └── java/zenit/          # Unit tests
└── pom.xml                      # Maven configuration
```
