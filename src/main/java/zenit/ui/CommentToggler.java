package zenit.ui;

import javafx.scene.control.IndexRange;
import zenit.zencodearea.ZenCodeArea;
import java.util.ArrayList;
import java.util.List;
 
/**
 * Handles toggling line comments (//) in a ZenCodeArea.
 * Extracted from MainController so it can be tested without
 * a JavaFX Application Thread or any UI wiring.
 */
public class CommentToggler {
 

    private final ZenCodeArea area;
 
    public CommentToggler(ZenCodeArea area) {
        this.area = area;
    }
 
    public void toggle() {
 
		int caretPos                = area.getCaretPosition();
		int caretColumn             = area.getCaretColumn();
		int length                  = area.getLength();
		int whereToReplaceFirstLine = caretPos - caretColumn;
		int rowNumber               = area.getCurrentParagraph();
		int paragraphLength         = area.getParagraphLength(rowNumber);
 
        List<Integer> whereToReplaceList = new ArrayList<>();
        IndexRange zen      = area.getSelection();
        int endOfSelection   = zen.getEnd();
        int startOfSelection = zen.getStart();
 
        boolean topDown     = true;
        int n               = 1;
        int whereToReplace  = whereToReplaceFirstLine;
        whereToReplaceList.add(whereToReplaceFirstLine);
 
        // If the selection starts at least one row above the end
        if (caretPos == endOfSelection && whereToReplaceFirstLine > startOfSelection) {
            topDown = true;
            do {
                whereToReplace = whereToReplace - 1 - area.getParagraphLength(rowNumber - n);
                n++;
                whereToReplaceList.add(whereToReplace);
            } while (whereToReplace > startOfSelection);
        }
 
        // If the selection starts at least one row below the end
        if (caretPos == startOfSelection && whereToReplace + paragraphLength < endOfSelection) {
            topDown = false;
            do {
                whereToReplace = whereToReplace + 1 + area.getParagraphLength(rowNumber + n - 1);
                n++;
                whereToReplaceList.add(whereToReplace);
            } while (whereToReplace + area.getParagraphLength(rowNumber + n - 1) < endOfSelection);
        }
 
        boolean[] addComment = new boolean[whereToReplaceList.size()];
 
        if (topDown) {
            int stepsToMove = 0;

			// if (caretPos > length - 3) {
			// 	area.insertText(caretPos, "\t  ");
			// }
 
            for (int i = 0; i < n; i++) {
                whereToReplace = whereToReplaceList.get(i);

                if (safeGetText(whereToReplace, whereToReplace + 3).equals("// ")) {
                    if (area.getText(whereToReplace, whereToReplace + 4).equals("// *")) {
                        area.deleteText(whereToReplace, whereToReplace + 2);
                        stepsToMove -= 2;
                        addComment[i] = false;
                    } else {
                        area.replaceText(whereToReplace, whereToReplace + 2, "  ");
                        addComment[i] = false;
                    }
                } else {
                    if (safeGetText(whereToReplace, whereToReplace + 2).equals("//")) {
                        area.deleteText(whereToReplace, whereToReplace + 2);
                        addComment[i] = false;
                        if (whereToReplace == caretPos) {
                            // no movement
                        } else if (whereToReplace + 1 == caretPos) {
                            stepsToMove--;
                        } else {
                            stepsToMove -= 2;
                        }
                    } else if (safeGetText(whereToReplace, whereToReplace + 4).equals("    ")) {
                        area.replaceText(whereToReplace, whereToReplace + 2, "//");
                        addComment[i] = false;
                    } else {
                        area.insertText(whereToReplace, "//");
                        stepsToMove += 2;
                        addComment[i] = true;
                    }
                }
            }
 
			if (whereToReplaceList.size() < 2) {
				area.moveTo(caretPos + stepsToMove);
			} else if (addComment[0] && addComment[n - 1]) {
				area.selectRange(
					Math.max(0, startOfSelection + 2), Math.max(0, endOfSelection + stepsToMove)
				);
			} else if (addComment[0]) {
				area.selectRange(Math.max(0, startOfSelection + 2), 
					Math.max(0, endOfSelection + stepsToMove)
				);
			} else if (addComment[n - 1]) {
				area.selectRange(Math.max(0,startOfSelection - 2), Math.max(0,endOfSelection + stepsToMove + 2)
				);
			} else {
				area.selectRange(Math.max(0, startOfSelection - 2), Math.max(0, endOfSelection + stepsToMove));
			}
        }
 
        if (!topDown) {
            for (int i = whereToReplaceList.size() - 1; i >= 0; i--) {
                whereToReplace = whereToReplaceList.get(i);
 
                // if (caretPos > length - 3) {
                //     area.insertText(caretPos, "\t  ");
                //     area.moveTo(caretPos);
                // }
 
                if (safeGetText(whereToReplace, whereToReplace + 3).equals("// ")) {
                    if (safeGetText(whereToReplace, whereToReplace + 4).equals("// *")) {
                        area.deleteText(whereToReplace, whereToReplace + 2);
                        addComment[i] = false;
                    } else {
                        area.replaceText(whereToReplace, whereToReplace + 2, "  ");
                        area.moveTo(caretPos);
                        addComment[i] = false;
                    }
                } else {
                    if (safeGetText(whereToReplace, whereToReplace + 2).equals("//")) {
                        area.deleteText(whereToReplace, whereToReplace + 2);
                        addComment[i] = false;
                        if (whereToReplace == caretPos) {
                            area.moveTo(caretPos);
                        } else if (whereToReplace + 1 == caretPos) {
                            area.moveTo(Math.max(0,caretPos - 1));
                        } else {
                            area.moveTo(Math.max(0,caretPos - 2));
                        }
                    } else {
                        area.insertText(whereToReplace, "//");
                        area.moveTo(caretPos + 2);
                        addComment[i] = true;
                    }
                }
            }
			int last = whereToReplaceList.size() - 1;
			if (addComment[0] && addComment[last]) {
				area.selectRange(Math.max(0, rowNumber + last),
					Math.max(0, endOfSelection - whereToReplaceList.get(last) + 2),
					Math.max(0, rowNumber), Math.max(0, caretColumn + 2));
			} else if (addComment[0]) {
				area.selectRange(Math.max(0, rowNumber + last),
					Math.max(0, endOfSelection - whereToReplaceList.get(last) - 2),
					Math.max(0,rowNumber), Math.max(0, caretColumn + 2));
			} else if (addComment[last]) {
				area.selectRange(Math.max(0,rowNumber + last),
					Math.max(0, endOfSelection - whereToReplaceList.get(last) + 2),
					Math.max(0, rowNumber), Math.max(0, caretColumn - 2));
			} else {
				area.selectRange(Math.max(0, rowNumber + last),
					Math.max(0, endOfSelection - whereToReplaceList.get(last) - 2),
					Math.max(0,rowNumber) , Math.max(0, caretColumn - 2));
			}
        }
    }

	private String safeGetText(int start, int end) {
		return area.getText(start, Math.min(end, area.getLength()));
	}
}
