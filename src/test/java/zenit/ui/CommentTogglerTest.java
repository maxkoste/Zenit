package zenit.ui;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import javafx.stage.Stage;
import zenit.zencodearea.ZenCodeArea;

@ExtendWith(ApplicationExtension.class)
class CommentTogglerTest {

    private ZenCodeArea area;

    @Start
    private void start(Stage stage) {
        area = new ZenCodeArea(null, new File("test.java"));
    }

    private void toggle(String text, int caretPos) {
        area.replaceText(text);
        area.moveTo(caretPos);
        new CommentToggler(area).toggle();
    }

    private void toggleEndCaret(String text, int start, int end) {
        area.replaceText(text);
        area.selectRange(start, end);
        new CommentToggler(area).toggle();
    }

    private void toggleStartCaret(String text, int start, int end) {
        area.replaceText(text);
        area.selectRange(end, start);
        new CommentToggler(area).toggle();
    }

	// Single-line
	@Test @DisplayName("Adds '//' to a plain line")
	void singleLine_addsComment() {
		toggle("someCode", 4);
		assertEquals("//someCode", area.getText());
	}

	@Test @DisplayName("Caret advances by 2 after adding '//'")
	void singleLine_caretAdvancesOnAdd() {
		toggle("someCode", 4);
		assertEquals(6, area.getCaretPosition());
	}

	@Test @DisplayName("Removes '// ' (replaces with two spaces)")
	void singleLine_removesDoubleSlashSpace() {

		toggle("// someCode", 5);
		assertEquals("   someCode", area.getText());
	}

	@Test @DisplayName("Removes '// *' prefix (deletes '// ', keeps '*')")
	void singleLine_removesDoubleSlashStar() {
		toggle("// *someCode", 5);
		assertEquals(" *someCode", area.getText());
	}

	@Test @DisplayName("Removes bare '//' — caret well past prefix (moves -2)")
	void singleLine_removesBareDoubleSlashCaretFar() {

		toggle("//someCode", 4);
		assertEquals("someCode", area.getText());
		assertEquals(2, area.getCaretPosition());
	}

	@Test @DisplayName("Removes bare '//' — caret one past prefix (moves -1)")
	void singleLine_removesBareDoubleSlashCaretOnePast() {
		toggle("//someCode", 1);
		assertEquals("someCode", area.getText());
		assertEquals(0, area.getCaretPosition());
	}

	@Test @DisplayName("Removes bare '//' — caret AT prefix start (no movement)")
	void singleLine_removesBareDoubleSlashCaretAtStart() {
		toggle("//someCode", 0);
		assertEquals("someCode", area.getText());
		assertEquals(0, area.getCaretPosition());
	}

	@Test @DisplayName("Replaces four leading spaces with '//'")
	void singleLine_replacesFourSpaces() {
		toggle("    someCode", 6);
		assertEquals("//  someCode", area.getText());
	}

	@Test @DisplayName("Comments both lines")
	void topDown_commentsBothLines() {
		toggleEndCaret("someCode\nmoreCode", 0, 17);
		for (String line : area.getText().split("\n"))
		assertTrue(line.startsWith("//"), "Line should be commented: " + line);
	}

	@Test @DisplayName("Uncomments both '// ' lines")
	void topDown_uncommentsBothLines() {

		String text = "// someCode\n// moreCode";
		toggleEndCaret(text, 0, text.length());
		for (String line : area.getText().split("\n"))
		assertFalse(line.startsWith("//"), "Line should be uncommented: " + line);
	}

	@Test @DisplayName("Uncomments both '// *' lines")
	void topDown_uncommentsBothStarLines() {
		String text = "// *line0\n// *line1";
		toggleEndCaret(text, 0, text.length());
		for (String line : area.getText().split("\n"))
		assertTrue(line.startsWith(" *"), "Star should remain: " + line);
	}

	@Test @DisplayName("Three lines — all get commented")
	void topDown_threeLines() {
		String text = "a\nb\nc";
		toggleEndCaret(text, 0, text.length());
		for (String line : area.getText().split("\n"))
		assertTrue(line.startsWith("//"), "Every line should be commented: " + line);
	}

	// Bottom-up multi-line (caret == start of selection)
	@Test @DisplayName("Comments both lines")
	void bottomUp_commentsBothLines() {
		toggleStartCaret("someCode\nmoreCode", 0, 17);
		for (String line : area.getText().split("\n"))
		assertTrue(line.startsWith("//"), "Line should be commented: " + line);
	}

	@Test @DisplayName("Uncomments both '// ' lines")
	void bottomUp_uncommentsBothLines() {
		String text = "// someCode\n// moreCode";
		toggleStartCaret(text, 0, text.length());
		for (String line : area.getText().split("\n"))
		assertFalse(line.startsWith("//"), "Line should be uncommented: " + line);
	}

	@Test @DisplayName("Uncomments both bare '//' lines")
	void bottomUp_uncommentsBareDoubleSlash() {
		toggleStartCaret("//someCode\n//moreCode", 0, 21);
		assertEquals("someCode\nmoreCode", area.getText());
	}

	@Test @DisplayName("Bottom line processed first keeps top-line offsets valid")
	void bottomUp_bottomLineProcessedFirst() {
		toggleStartCaret("//alpha\n//beta", 0, 14);
		assertEquals("alpha\nbeta", area.getText());
	}


	private void roundTrip_assertRoundTrip(String original, boolean topDown) {
		// Pass 1: add comments
		if (topDown) toggleEndCaret(original, 0, original.length());
		else         toggleStartCaret(original, 0, original.length());
		String commented = area.getText();

		// Pass 2: remove comments
		if (topDown) toggleEndCaret(commented, 0, commented.length());
		else         toggleStartCaret(commented, 0, commented.length());

		assertEquals(original, area.getText(), "Round-trip failed for: " + original);
	}

	@Test
	void roundTrip_twoLinesTopDown() {
		String original = "line0\nline1";
		toggleEndCaret(original, 0, original.length());
		String commented = area.getText();
		toggleEndCaret(commented, 0, commented.length());
		assertEquals(original, area.getText());
	}

	@Test
	void roundTrip_twoLinesBottomUp() {
		String original = "line0\nline1";
		toggleStartCaret(original, 0, original.length());
		String commented = area.getText();
		toggleStartCaret(commented, 0, commented.length());
		assertEquals(original, area.getText());
	}

	@Test
	void roundTrip_threeLinesTopDown() {
		String original = "a\nb\nc";
		toggleEndCaret(original, 0, original.length());
		String commented = area.getText();
		toggleEndCaret(commented, 0, commented.length());
		assertEquals(original, area.getText());
	}

	// Edge cases
	@Test @DisplayName("Single-line selection on second row — only row 1 changes")
	void secondRowOnly() {
		toggle("row0\nrow1", "row0\n".length() + 2);
		String[] lines = area.getText().split("\n");
		assertFalse(lines[0].startsWith("//"), "Row 0 should be untouched");
		assertTrue(lines[1].startsWith("//"),  "Row 1 should be commented");
	}
}
