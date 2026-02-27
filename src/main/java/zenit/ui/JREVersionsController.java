package zenit.ui;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import zenit.filesystem.jreversions.JDKVerifier;
import zenit.filesystem.jreversions.JREVersions;

public class JREVersionsController extends AnchorPane {
	
	private Stage stage;
	private boolean darkmode;
	private List<File> JVMs;
	
	@FXML
	private ListView<String> JDKList;

	@FXML
	private Label defaultJDKLabel = new Label();

	@FXML
	private Label statusLabel;
	
	public JREVersionsController(boolean darkmode) {
		this.darkmode = darkmode;
	}
	
	public void start() {
		try {
			//setup scene
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(getClass().getResource("/zenit/ui/JREVersions.fxml"));
			loader.setController(this);
			AnchorPane root = (AnchorPane) loader.load();
			Scene scene = new Scene(root);

			//set up stage
			stage = new Stage();
			stage.setResizable(false);
			stage.setScene(scene);
			
			initialize();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void initialize() {	
		ifDarkModeChanged(darkmode);
		
		updateList();
		
		stage.show();
	}
	
	private void updateList() {
		JVMs = JREVersions.read();
		ArrayList<String> JVMsString = new ArrayList<String>();

		File defaultJDK = JREVersions.getDefaultJDKFile();
		Optional<File> javaHome = JREVersions.getJavaHomeFromEnv();

		// Check if JAVA_HOME is in the manually added JVMs list
		boolean javaHomeIsManuallyAdded = false;
		if (javaHome.isPresent()) {
			for (File JVM : JVMs) {
				if (JVM.getPath().equals(javaHome.get().getPath())) {
					javaHomeIsManuallyAdded = true;
					break;
				}
			}
		}

		for (File JVM : JVMs) {
			String displayName = JVM.getName();
			if(defaultJDK != null && JVM.getPath().equals(defaultJDK.getPath())){
				displayName += " [default]";
			}
			if(javaHome.isPresent() && JVM.getPath().equals(javaHome.get().getPath())){
				displayName += " [JAVA_HOME - system]";
			}

			JVMsString.add(displayName);
		}

		if(javaHome.isPresent() && !javaHomeIsManuallyAdded){
			String javaHomeName = javaHome.get().getName();

			if(defaultJDK != null && javaHome.get().getPath().equals(defaultJDK.getPath())){
				javaHomeName += " [JAVA_HOME - system] [default]";
			}
			else {
				javaHomeName += " [JAVA_HOME - system]";
			}

			JVMsString.add(javaHomeName);
		}

		
		JDKList.getItems().clear();
		JDKList.getItems().addAll(JVMsString);

		JDKList.getItems().sort((o1, o2) -> {
			return o1.compareTo(o2);
		});

		//Update label
		if (defaultJDK != null) {
			String labelText = "New projects will use: " + defaultJDK.getName();
			if(javaHome.isPresent() && javaHome.get().getPath().equals(defaultJDK.getPath())){
				labelText += " (from JAVA_HOME)";
			}
			defaultJDKLabel.setText(labelText);
		}
		else {
			defaultJDKLabel.setText("No default JDK set - projects may fail to compile");
		}

		updateStatusLabel();
	}

	private void updateStatusLabel() {
		if (statusLabel == null) {
			return;
		}

		File defaultJDK = JREVersions.getDefaultJDKFile();
		Optional<File> javaHome = JREVersions.getJavaHomeFromEnv();

		if (defaultJDK != null && defaultJDK.exists() && JDKVerifier.validJDK(defaultJDK)) {
			//User has set a default - priority over JAVA_HOME
			statusLabel.setText("Default JDK: " + defaultJDK.getName() + " (overrides JAVA_HOME)");
			statusLabel.setStyle("-fx-text-fill: #4CAF50;"); //Green
		} else if (javaHome.isPresent() && JDKVerifier.validJDK(javaHome.get())) {
			// No default set, using JAVA_HOME
			statusLabel.setText("Using JAVA_HOME: " + javaHome.get().getName() + " (system default)");
			statusLabel.setStyle("-fx-text-fill: #2196F3;"); //Blue
		} else {
			// No JDK at all
			statusLabel.setText("No JDK configured - please add one");
			statusLabel.setStyle("-fx-text-fill: #f44336;"); //Red
		}
	}
	
	@FXML
	private void addJRE() {
		DirectoryChooser dc = new DirectoryChooser();
		dc.setInitialDirectory(JREVersions.getJVMDirectory());
		dc.setTitle("Select JDK to add");
		
		File selected = dc.showDialog(stage);
		
		if (selected != null) {
			boolean success = JREVersions.append(selected);
			if (success) {
				updateList();
			} else {
				DialogBoxes.errorDialog("JDK doesn't contain java or javac", "", "The selected JDK doesn't"
						+ "contain the needed java or javac executables");
			}
		}
		
	}
	
	@FXML
	private void removeJRE() {
		String selected = JDKList.getSelectionModel().getSelectedItem();
		File selectedFile = null;
		
		if (selected != null && selected.endsWith(" [default]")) {
			DialogBoxes.errorDialog("Can't remove default JDK", "", "Can't remove the default"
					+ "JDK, choose another default JDK to remove this one");
			return;
		}
		
		if (selected != null) {
			for (File JVM : JVMs) {
				if (JVM.getPath().endsWith(selected)) {
					selectedFile = JVM;
					break;
				}
			}
			if (selectedFile != null) { 
				boolean success = JREVersions.remove(selectedFile);
				if (success) {
					DialogBoxes.informationDialog("JDK removed from Zenit", "The JDK " + selected
							+ " has been removed from Zenit");
					updateList();
				} else {
					DialogBoxes.errorDialog("Couldn't remove JDK", "", "The JDK " + selected +
							" couldn't be removed from Zenit");
				}
			}
		} else {
			DialogBoxes.errorDialog("No JDK selected", "", "Select a JDK to remove from Zenit");
		}
	}
	
	@FXML
	private void selectDefaultJRE() {
		String selected = JDKList.getSelectionModel().getSelectedItem();
		File selectedFile = null;
		
		if (selected != null && selected.endsWith(" [default]")) {
			return;
		}
		
		if (selected != null) {
			for (File JVM : JVMs) {
				if (JVM.getPath().endsWith(selected)) {
					selectedFile = JVM;
					break;
				}
			}
		}

		//Check if it's the JAVA_HOME option
		if (selected.endsWith(" [JAVA_HOME - system]")) {
			Optional<File> javaHome = JREVersions.getJavaHomeFromEnv();
			if (javaHome.isPresent()) {
				selectedFile = javaHome.get();
			} else {
				DialogBoxes.errorDialog("JAVA_HOME not found", "",
						"JAVA_HOME environment variable is not set or invalid.");
				return;
			}
		} else {
			//It's a manually added JDK
			for (File JVM : JVMs) {
				if (JVM.getPath().endsWith(selected)) {
					selectedFile = JVM;
					break;
				}
			}
		}

		if (selectedFile != null) {
			JREVersions.setDefaultJDKFile(selectedFile);
			updateList();

			DialogBoxes.informationDialog("Default JDK updated",
					"All projects will now use "+ selectedFile.getName()+" by default.\n\n" +
					"This overrides the JAVA_HOME envionment variable");
		}
		else{
			DialogBoxes.errorDialog("No JDK selected", "", "Select a JDK from the list to set as default.");
		}
	}
	
	/**
	 * Changes css style depending on set light mode.
	 * @param isDarkMode true if dark mode is enabled
	 */
	public void ifDarkModeChanged(boolean isDarkMode) {
		var stylesheets = stage.getScene().getStylesheets();
		var darkMode = getClass().getResource("/zenit/ui/projectinfo/mainStyle.css").toExternalForm();
		var lightMode = getClass().getResource("/zenit/ui/projectinfo/mainStyle-lm.css").toExternalForm();
		
		if (isDarkMode) {
			if (stylesheets.contains(lightMode)) {
				stylesheets.remove(lightMode);
			}
			
			stylesheets.add(darkMode);
		} else {
			if (stylesheets.contains(darkMode)) {
				stylesheets.remove(darkMode);
			}
			stylesheets.add(lightMode);
		}	
	}
}
