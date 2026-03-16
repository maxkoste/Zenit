package zenit.zencodearea;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import zenit.LSP.LspManager;
import javafx.concurrent.Task;

import java.io.File;
import java.time.Duration;
import java.util.Collection;
import java.util.Optional;
import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;
import org.fxmisc.wellbehaved.event.Nodes;
import org.fxmisc.wellbehaved.event.EventPattern;
import org.fxmisc.wellbehaved.event.InputMap;

public class ZenCodeArea extends CodeArea {
	private ExecutorService executor;
	private LspManager lspManager;
	// private int fontSize;
	// private String font;

	private static final String[] KEYWORDS = new String[] {
		"abstract", "assert", "boolean", "byte",
		"case", "catch", "char", "class", "const",
		"default", "double",
		"enum", "extends", "false", "final", "finally", "float",
		"goto", "implements", "import",
		"instanceof", "int", "interface", "long", "native",
		"new", "null", "package", "private", "protected", "public",
		"record", "return", "sealed", "short", "static", "strictfp", "super",
		"synchronized", "throw", "throws",
		"transient", "true", "try", "var", "void", "volatile",
		"permits", "non-sealed", "yield"
	};

	private static final String[] CONTROL_KEYWORDS = new String[] {
		"if", "else", "for", "while", "do", "switch",
		"break", "continue",
		"this"
	};

	private static final String KEYWORD_PATTERN = "\\b(" + String.join("|", KEYWORDS) + ")\\b";
	private static final String CONTROL_PATTERN = "\\b(" + String.join("|", CONTROL_KEYWORDS) + ")\\b";
	private static final String PAREN_PATTERN = "\\(|\\)";
	private static final String BRACE_PATTERN = "\\{|\\}";
	private static final String BRACKET_PATTERN = "\\[|\\]";
	private static final String SEMICOLON_PATTERN = "\\;";
	private static final String STRING_PATTERN = "\"([^\"\\\\]|\\\\.)*\"";
	private static final String CHAR_PATTERN = "'([^'\\\\]|\\\\.)'";
	private static final String COMMENT_PATTERN = "//[^\n]*" + "|" + "/\\*(.|\\R)*?\\*/";
	private static final String NUMBER_PATTERN = "\\b(\\d+\\.?\\d*[fFdDlL]?|0[xX][0-9a-fA-F]+)\\b";
	private static final String ANNOTATION_PATTERN = "@[\\w]+";
	private static final String CLASS_NAME_PATTERN =
	"(?<=\\bclass\\s{1,10}|\\bnew\\s{1,10}|\\bextends\\s{1,10}|\\bimplements\\s{1,10}|\\brecord\\s{1,10})[A-Z][a-zA-Z0-9]*";
	private static final String OPERATOR_PATTERN = "\\+|-|\\*|/|%|==|!=|<=|>=|<|>|&&|\\|\\||!|=|\\+=|-=|\\*=|/=";
	private static final String METHOD_PATTERN = 
	"(?<!\\.)\\b(?!if|for|while|switch|catch|case|return|new|assert|throw)([a-z][a-zA-Z0-9]*)(?=\\s*\\()";

	private File currentFile;

	private static final Pattern PATTERN = Pattern.compile(
		"(?<COMMENT>" + COMMENT_PATTERN + ")"
		+ "|(?<STRING>" + STRING_PATTERN + ")"
		+ "|(?<CHAR>" + CHAR_PATTERN + ")"
		+ "|(?<ANNOTATION>" + ANNOTATION_PATTERN + ")"
		+ "|(?<CONTROL>" + CONTROL_PATTERN + ")"
		+ "|(?<KEYWORD>" + KEYWORD_PATTERN + ")"
		+ "|(?<CLASSNAME>" + CLASS_NAME_PATTERN + ")"
		+ "|(?<METHOD>" + METHOD_PATTERN + ")"
		+ "|(?<NUMBER>" + NUMBER_PATTERN + ")"
		+ "|(?<OPERATOR>" + OPERATOR_PATTERN + ")"
		+ "|(?<PAREN>" + PAREN_PATTERN + ")"
		+ "|(?<BRACE>" + BRACE_PATTERN + ")"
		+ "|(?<BRACKET>" + BRACKET_PATTERN + ")"
		+ "|(?<SEMICOLON>" + SEMICOLON_PATTERN + ")");

	public ZenCodeArea(LspManager lspManager, File file) {
		this(14, "Times new Roman", lspManager, file);

	}

	public ZenCodeArea(int textSize, String font, LspManager lspManager, File file) {
		this.currentFile= file;
		System.out.println("[DEBUG] ZenCodeArea: current file " + this.currentFile.getAbsolutePath());
		setParagraphGraphicFactory(LineNumberFactory.get(this));

		multiPlainChanges().successionEnds(
			Duration.ofMillis(300)).subscribe( //changeing the ms here determines how fast the lsp server recieves msgs
				ignore ->{
					setStyleSpans(0, computeHighlighting(getText()));
					try {
						if (file != null || this.lspManager != null) {
							System.out.println("[DEBUG] Sending didChange from ZenCodeArea");
							lspManager.sendDidChange(file.getAbsolutePath(), getText());
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				});

		executor = Executors.newSingleThreadExecutor();
		setParagraphGraphicFactory(LineNumberFactory.get(this));
		multiPlainChanges().successionEnds(Duration.ofMillis(500)).supplyTask(
			this::computeHighlightingAsync).awaitLatest(multiPlainChanges()).filterMap(t -> {
				if (t.isSuccess()) {
					return Optional.of(t.get());
				} else {
					t.getFailure().printStackTrace();
					return Optional.empty();
				}
			}).subscribe(this::applyHighlighting);
		computeHighlightingAsync();


		// fontSize = textSize;
		// this.font = font;
		setStyle("-fx-font-size: " + textSize + ";-fx-font-family: " + font);
	}

	public void update() {
		var highlighting = computeHighlighting(getText());
		applyHighlighting(highlighting);
	}

	private Task<StyleSpans<Collection<String>>> computeHighlightingAsync() {
		String text = getText();
		Task<StyleSpans<Collection<String>>> task = new Task<StyleSpans<Collection<String>>>() {
			@Override
			protected StyleSpans<Collection<String>> call() throws Exception {
				return computeHighlighting(text);
			}
		};
		executor.execute(task);
		return task;
	}

	private void applyHighlighting(StyleSpans<Collection<String>> highlighting) {
		setStyleSpans(0, highlighting);

		InputMap<KeyEvent> im = InputMap.consume(
			EventPattern.keyPressed(KeyCode.TAB),
			e -> this.replaceSelection("    "));
		Nodes.addInputMap(this, im);
	}

	private static StyleSpans<Collection<String>> computeHighlighting(String text) {
		Matcher matcher = PATTERN.matcher(text);
		int lastKwEnd = 0;
		StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();
		while (matcher.find()) {
			String styleClass =
			matcher.group("COMMENT")    != null ? "comment"    :
			matcher.group("STRING")     != null ? "string"     :
			matcher.group("CHAR")       != null ? "string"     :
			matcher.group("ANNOTATION") != null ? "annotation" :
			matcher.group("CONTROL")    != null ? "control"    :
			matcher.group("KEYWORD")    != null ? "keyword"    :
			matcher.group("CLASSNAME")  != null ? "classname"  :
			matcher.group("METHOD")     != null ? "method"     :
			matcher.group("NUMBER")     != null ? "number"     :
			matcher.group("OPERATOR")   != null ? "operator"   :
			matcher.group("PAREN")      != null ? "paren"      :
			matcher.group("BRACE")      != null ? "brace"      :
			matcher.group("BRACKET")    != null ? "bracket"    :
			matcher.group("SEMICOLON")  != null ? "semicolon"  :
			null;
			assert styleClass != null;
			spansBuilder.add(Collections.emptyList(), matcher.start() - lastKwEnd);
			spansBuilder.add(Collections.singleton(styleClass), matcher.end() - matcher.start());
			lastKwEnd = matcher.end();
		}
		spansBuilder.add(Collections.emptyList(), text.length() - lastKwEnd);
		return spansBuilder.create();
	}

	public void setFontSize(int newFontSize) {
		// fontSize = newFontSize;
		setStyle("-fx-font-size: " + newFontSize);
	}

	public void updateAppearance(String fontFamily, int size) {
		// font = fontFamily;
		setStyle("-fx-font-family: " + fontFamily + ";" +
			"-fx-font-size: " + size + ";");
	}
}
