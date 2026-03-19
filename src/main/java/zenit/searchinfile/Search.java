package zenit.searchinfile;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

import org.fxmisc.richtext.model.StyleSpan;
import org.reactfx.value.Var;

import javafx.application.Platform;
import javafx.scene.control.Label;
import zenit.ui.MainController;
import zenit.util.Tuple;
import zenit.zencodearea.ZenCodeArea;

/**
 * The Search class lets you search for a word then either
 * highlights it yellow or grey, depending on what background you have,
 * or replaces it with another word of your choosing
 *
 * @author Fredrik Eklundh
 *
 */

public class Search {

	private static final long MAX_HIGHLIGHT_BYTES = 500_000; // 500KB

	private ZenCodeArea zenCodeArea;

	private File file;

	private List<Integer> line;
	private List<Integer> wordPos;
	private List<Tuple<Integer, Integer>> absolutePos;

	private int numberOfTimes = 0;
	private int numberOfLines = -1;
	private int lineLenght = 0;
	private int i = 0;

	private String searchWord = "";

	private boolean isDarkMode;
	private boolean caseSensetive = false;

	private java.util.function.IntConsumer onSearchComplete;

	/**
	 * Opens a TextInputDialog and let's you type in a word to search for
	 *
	 * @throws FileNotFoundException
	 */
	public Search(ZenCodeArea zenCodeArea, File file, boolean isDarkMode, MainController mainController) {

		new SearchInFileController(this, mainController);

		this.zenCodeArea = zenCodeArea;
		this.file = file;
		this.isDarkMode = isDarkMode;
	}

	/**
	 * Searches for a word in the file. For large files (>500KB), runs entirely
	 * on a background thread to avoid freezing the UI. Returns 0 immediately
	 * for large files — real count comes via setOnSearchComplete callback.
	 * For small files, behaves synchronously as before.
	 */
	public int searchInFile(String word) {
		line = new ArrayList<>();
		wordPos = new ArrayList<>();
		absolutePos = new ArrayList<>();

		numberOfTimes = 0;
		numberOfLines = -1;
		lineLenght = 0;
		i = 0;

		if (word.length() < 1) {
			return 0;
		}

		searchWord = word;

		boolean largeFile = file.length() > MAX_HIGHLIGHT_BYTES;

		if (largeFile) {
			new Thread(() -> {
				scanFileInternal(word);

				Platform.runLater(() -> {
					if (onSearchComplete != null) onSearchComplete.accept(numberOfTimes);
				});
			}, "search-thread").start();

			return 0;

		} else {
			// Liten fil: originalbeteende synkront
			try {
				Scanner txtscan = new Scanner(file);
				if (caseSensetive == false) {
					notCaseSensetive(txtscan);
				} else {
					caseSensetive(txtscan);
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}

			if (numberOfTimes > 0) {
				for (int i = 0; i < numberOfTimes; i++) {
					int start = zenCodeArea.getAbsolutePosition(line.get(i), wordPos.get(i));
					int end = start + searchWord.length();

					// Bounds check
					if (start < 0 || end > zenCodeArea.getLength() || start > end) continue;

					absolutePos.add(new Tuple<>(start, end));

					// Använd absolutposition istället för rad+kolumn
					zenCodeArea.setStyle(start, end,
							List.of(isDarkMode ? "search-dark-mode" : "search-light-mode"));
				}
				if (!absolutePos.isEmpty()) {
					zenCodeArea.moveTo(absolutePos.get(0).fst());
					zenCodeArea.requestFollowCaret();
				}
			}
			return numberOfTimes;
		}
	}

	public void setOnSearchComplete(java.util.function.IntConsumer callback) {
		this.onSearchComplete = callback;
	}

	public boolean isLargeFile() {
		return file != null && file.length() > MAX_HIGHLIGHT_BYTES;
	}

	/**
	 * Clears the "found match"-styling.
	 * @author Fredrik Eklundh, Pontus Laos
	 */
	public void clearZen() {
		if (absolutePos != null) {
			for (int i = 0; i < absolutePos.size(); i++) {
				zenCodeArea.clearStyle(absolutePos.get(i).fst(), absolutePos.get(i).snd());
				zenCodeArea.update();
			}
		}
	}

	/**
	 * When you close the search panel the highlight disappears
	 */
	public void cleanZen() {
		int carPos = zenCodeArea.getCaretPosition();
		zenCodeArea.appendText(" ");
		zenCodeArea.deletePreviousChar();
		zenCodeArea.moveTo(carPos);
	}

	/**
	 * Replaces every occurrence of a certain word with another word.
	 *
	 * @param wordBefore
	 * @param wordAfter
	 * @param absolutePos
	 */
	public void replaceAll(String wordAfter) {
		if (absolutePos == null || absolutePos.isEmpty()) return;

		int lengthDiff = wordAfter.length() - searchWord.length();
		int offset = 0;

		for (int j = 0; j < absolutePos.size(); j++) {
			int start = absolutePos.get(j).fst() + offset;
			int end = absolutePos.get(j).snd() + offset;

			// Bounds check
			if (start < 0 || end > zenCodeArea.getLength() || start > end) continue;

			zenCodeArea.replaceText(start, end, wordAfter);
			offset += lengthDiff;
		}
		absolutePos.clear();
	}

	/**
	 * Replaces a single word with another word.
	 *
	 * @param wordBefore
	 * @param wordAfter
	 * @param absolutePos
	 */
	public void replaceOne(String wordAfter) {
		if (absolutePos == null || absolutePos.isEmpty()) return;
		int start = absolutePos.get(i).fst();
		int end = absolutePos.get(i).snd();
		if (start < 0 || end > zenCodeArea.getLength() || start > end) return;
		zenCodeArea.replaceText(start, end, wordAfter);
	}

	/**
	 * Jumps down/to the next occurrence of the highlighted word
	 */
	public int jumpDown() {
		if (absolutePos == null || absolutePos.isEmpty()) return 0;
		if (i < absolutePos.size() - 1) {
			i++;
		} else {
			i = 0;
		}

		zenCodeArea.moveTo(absolutePos.get(i).fst());
		if (!isLargeFile()) zenCodeArea.requestFollowCaret();
		return i;
	}

	/**
	 * Jumps up/to the previous occurrence of the highlighted word
	 */
	public int jumpUp() {
		if (absolutePos == null || absolutePos.isEmpty()) return 0;
		if (i > 0) {
			i--;
		} else {
			i = absolutePos.size() - 1;
		}

		zenCodeArea.moveTo(absolutePos.get(i).fst());
		if (!isLargeFile()) zenCodeArea.requestFollowCaret();
		return i;
	}


	/**
	 * Runs file scan and populates line/wordPos lists.
	 */
	private void scanFileInternal(String word) {
		try {
			Scanner txtscan = new Scanner(file);
			if (!caseSensetive) {
				notCaseSensetive(txtscan);
			} else {
				caseSensetive(txtscan);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Builds absolute (start, end) positions from a text snapshot.
	 */
	private List<Tuple<Integer, Integer>> buildPositionsFromText(String text) {
		List<Tuple<Integer, Integer>> result = new ArrayList<>();

		List<Integer> lineStarts = new ArrayList<>();
		lineStarts.add(0);
		for (int j = 0; j < text.length(); j++) {
			if (text.charAt(j) == '\n') lineStarts.add(j + 1);
		}

		for (int j = 0; j < line.size(); j++) {
			int lineIdx = line.get(j);
			if (lineIdx < lineStarts.size()) {
				int start = lineStarts.get(lineIdx) + wordPos.get(j);
				result.add(new Tuple<>(start, start + searchWord.length()));
			}
		}
		return result;
	}

	/**
	 * Making the search ignore if it's capital letters or lowercase
	 */
	private void notCaseSensetive(Scanner txtscan) {

		while (txtscan.hasNextLine()) {
			String str = txtscan.nextLine().toLowerCase();
			numberOfLines++;
			lineLenght = 0;
			while (str.indexOf(searchWord.toLowerCase()) != -1) {
				numberOfTimes++;

				line.add(numberOfLines);

				wordPos.add(str.indexOf(searchWord.toLowerCase()) + lineLenght);

				lineLenght += str.length() - str.substring(str.indexOf(searchWord.toLowerCase()) + searchWord.length()).length();

				str = str.substring(str.indexOf(searchWord.toLowerCase()) + searchWord.length());
			}
		}
	}

	/**
	 * This search makes a different if it's capital letters or lowercase
	 */
	private void caseSensetive(Scanner txtscan) {

		while (txtscan.hasNextLine()) {
			String str = txtscan.nextLine();
			numberOfLines++;
			lineLenght = 0;
			while (str.indexOf(searchWord) != -1) {
				numberOfTimes++;

				line.add(numberOfLines);

				wordPos.add(str.indexOf(searchWord) + lineLenght);

				lineLenght += str.length() - str.substring(str.indexOf(searchWord) + searchWord.length()).length();

				str = str.substring(str.indexOf(searchWord) + searchWord.length());
			}
		}
	}
}