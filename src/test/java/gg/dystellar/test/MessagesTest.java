package gg.dystellar.test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Objects;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.hypixel.hytale.server.core.Message;

import gg.dystellar.core.config.Messages;
import gg.dystellar.core.utils.Triple;

class MessagesTest {

	private Messages messages;

	@BeforeEach
	void setUp() {
		messages = new Messages();
	}

	// -------------------------------------------------------------------------
	// Helpers
	// -------------------------------------------------------------------------

	/** Extracts the text of the nth part (0-indexed). */
	private String textAt(Messages.CompiledMessage msg, int index) {
		return msg.parts.get(index).first;
	}

	/** Extracts the hex color of the nth part (0-indexed). */
	private String colorAt(Messages.CompiledMessage msg, int index) {
		return msg.parts.get(index).second.hex_color;
	}

	private Triple<Integer, Integer, Integer> paramAt(Messages.CompiledMessage msg, int index) {
		return msg.params.get(index);
	}

	private String parse(final Message msg) {
		return msg.getChildren().stream()
			.map(Message::getRawText)
			.filter(Objects::nonNull)
			.collect(Collectors.joining());
	}

	// -------------------------------------------------------------------------
	// No tags
	// -------------------------------------------------------------------------

	@Test
	void noTags_producesOnePartWithDefaultColor() {
		var result = messages.compileMsg("Hello world");

		assertEquals(1, result.parts.size());
		assertEquals("Hello world", textAt(result, 0));
		assertEquals("#FFFFFF", colorAt(result, 0)); // DEFAULT_COLOR
	}

	@Test
	void emptyString_producesNoParts() {
		var result = messages.compileMsg("");

		assertTrue(result.parts.isEmpty());
	}

	// -------------------------------------------------------------------------
	// Unknown / invalid tags → DEFAULT_COLOR fallback
	// -------------------------------------------------------------------------

	@Test
	void unknownTag_textBeforeTagUsesDefaultColor() {
		var result = messages.compileMsg("Hello <Unknown>world");

		// "Hello " before the tag → default color
		assertEquals("#FFFFFF", colorAt(result, 0));
		assertEquals("Hello ", textAt(result, 0));
	}

	@Test
	void unknownTag_textAfterTagAlsoUsesDefaultColor() {
		var result = messages.compileMsg("Hello <Unknown>world");

		assertEquals("#FFFFFF", colorAt(result, 1));
		assertEquals("world", textAt(result, 1));
	}

	// -------------------------------------------------------------------------
	// Known tags → correct ColorDeclaration
	// -------------------------------------------------------------------------

	@Test
	void knownTag_red_appliesCorrectHex() {
		var result = messages.compileMsg("<Red>Error!");

		assertEquals(1, result.parts.size());
		assertEquals("Error!", textAt(result, 0));
		assertEquals("#FF5555", colorAt(result, 0));
	}

	@Test
	void knownTag_green_appliesCorrectHex() {
		var result = messages.compileMsg("<Green>Success");

		assertEquals("#55FF55", colorAt(result, 0));
	}

	@Test
	void knownTag_gold_appliesCorrectHex() {
		var result = messages.compileMsg("<Gold>Gold text");

		assertEquals("#FFAA00", colorAt(result, 0));
	}

	// -------------------------------------------------------------------------
	// Multiple tags
	// -------------------------------------------------------------------------

	@Test
	void multipleTags_correctPartCount() {
		// "<Gray>prefix <Red>error"
		// Expected parts: "prefix " (Gray), "error" (Red)
		var result = messages.compileMsg("<Gray>prefix <Red>error");

		assertEquals(2, result.parts.size());
	}

	@Test
	void multipleTags_correctColorsAndText() {
		var result = messages.compileMsg("<Gray>prefix <Red>error");

		assertEquals("prefix ", textAt(result, 0));
		assertEquals("#AAAAAA", colorAt(result, 0)); // Gray

		assertEquals("error", textAt(result, 1));
		assertEquals("#FF5555", colorAt(result, 1)); // Red
	}

	@Test
	void tagAtVeryStart_noLeadingPart() {
		var result = messages.compileMsg("<Blue>text");

		assertEquals(1, result.parts.size());
		assertEquals("text", textAt(result, 0));
		assertEquals("#5555FF", colorAt(result, 0));
	}

	// -------------------------------------------------------------------------
	// Trailing text after last tag
	// -------------------------------------------------------------------------

	@Test
	void trailingTextAfterLastTag_isIncluded() {
		var result = messages.compileMsg("<Red>first<Green>second");

		assertEquals(2, result.parts.size());
		assertEquals("second", textAt(result, 1));
	}

	// -------------------------------------------------------------------------
	// Text-only before first tag
	// -------------------------------------------------------------------------

	@Test
	void textBeforeFirstTag_isIncludedWithDefaultColor() {
		var result = messages.compileMsg("intro <Red>error");

		assertEquals("intro ", textAt(result, 0));
		assertEquals("#FFFFFF", colorAt(result, 0));
	}

	// -------------------------------------------------------------------------
	// Bold / italic / monospace flags (Danger, StrongGreen, StrongRed, Test)
	// -------------------------------------------------------------------------

	@Test
	void dangerTag_isBoldAndUsesCorrectHex() {
		var result = messages.compileMsg("<Danger>Watch out");

		assertEquals(1, result.parts.size());
		assertEquals("#AA0000", colorAt(result, 0));
		assertTrue(result.parts.get(0).second.bold);
	}

	@Test
	void testTag_allFormattingFlagsTrue() {
		var result = messages.compileMsg("<Test>formatted");

		var decl = result.parts.get(0).second;
		assertTrue(decl.bold);
		assertTrue(decl.italic);
		assertTrue(decl.monospace);
		assertTrue(decl.underlined);
	}

	// -------------------------------------------------------------------------
	// Unclosed tag — parser should stop at the unclosed '<'
	// -------------------------------------------------------------------------

	@Test
	void unclosedTag_doesNotThrow() {
		assertDoesNotThrow(() -> messages.compileMsg("Hello <Unclosed"));
	}

	// -------------------------------------------------------------------------
	// Realistic message string
	// -------------------------------------------------------------------------

	@Test
	void realisticMessage_correctPartCount() {
		// "<Gray>-> <MaterialEmerald>Hint<White>: <Blue>Use command <Orange>{command}"
		// Parts: "->" (Gray), "Hint" (MaterialEmerald), ": " (White), "Use command " (Blue), "{command}" (Orange)
		var result = messages.compileMsg("<Gray>-> <MaterialEmerald>Hint<White>: <Blue>Use command <Orange>{command}");

		assertEquals(5, result.parts.size());
	}

	@Test
	void realisticMessage_correctTexts() {
		var result = messages.compileMsg("<Gray>-> <MaterialEmerald>Hint<White>: <Blue>Use command <Orange>{command}");

		assertEquals("-> ", textAt(result, 0));
		assertEquals("Hint", textAt(result, 1));
		assertEquals(": ", textAt(result, 2));
		assertEquals("Use command ", textAt(result, 3));
		assertEquals("{command}", textAt(result, 4));
	}

	// -------------------------------------------------------------------------
	// Params parsing
	// -------------------------------------------------------------------------

	@Test
	void simpleEmptyParam_noProcess() {
		final var result = messages.compileMsg("Hello my name is {}");
		assertEquals("Hello my name is {}", textAt(result, 0));
		assertEquals(0, paramAt(result, 0).first);
		assertEquals(17, paramAt(result, 0).second);
		assertEquals(2, paramAt(result, 0).third);
		assertEquals(1, result.params.size());
	}

	@Test
	void simpleFilledWithSpacesParam_noProcess() {
		final var result = messages.compileMsg("Hello my name is {name xd}");
		assertEquals("Hello my name is {name xd}", textAt(result, 0));
		assertEquals(17, paramAt(result, 0).second);
		assertEquals(9, paramAt(result, 0).third);
		assertEquals(1, result.params.size());
	}

	@Test
	void simpleUnclosedParam_noProcess() {
		final var result = messages.compileMsg("Hello my name is{ {name}{");
		assertEquals("Hello my name is{ {name}{", textAt(result, 0));
		assertEquals(16, paramAt(result, 0).second);
		assertEquals(8, paramAt(result, 0).third);
		assertEquals(1, result.params.size());
	}

	@Test
	void simpleUnclosedMultipleParam_noProcess() {
		final var result = messages.compileMsg("Hello{ my name is{{ {name}");
		assertEquals("Hello{ my name is{{ {name}", textAt(result, 0));
		assertEquals(5, paramAt(result, 0).second);
		assertEquals(21, paramAt(result, 0).third);
		assertEquals(1, result.params.size());
	}

	@Test
	void simpleClosersParam_noProcess() {
		final var result = messages.compileMsg("Hello} my name is {name}}");
		assertEquals("Hello} my name is {name}}", textAt(result, 0));
		assertEquals(18, paramAt(result, 0).second);
		assertEquals(6, paramAt(result, 0).third);
		assertEquals(1, result.params.size());
	}

	@Test
	void multipleParam_noProcess() {
		final var result = messages.compileMsg("Hello {param1} my name is {name}");
		assertEquals("Hello {param1} my name is {name}", textAt(result, 0));
		assertEquals(2, result.params.size());
		assertEquals(6, paramAt(result, 0).second);
		assertEquals(8, paramAt(result, 0).third);
		assertEquals(26, paramAt(result, 1).second);
		assertEquals(6, paramAt(result, 1).third);
	}

	// -------------------------------------------------------------------------
	// Params processing
	// -------------------------------------------------------------------------

	@Test
	void emptyParam() {
		final var result = messages.compileMsg("Hello my name is {}");
		final var noArgs = result.buildMessage();
		final var textNoArgs = parse(noArgs);
		assertEquals("Hello my name is {}", textNoArgs);
		final var text = parse(result.buildMessage("algorhythmic"));
		assertEquals("Hello my name is algorhythmic", text);
		final var textMultiple = parse(result.buildMessage("algorhythmic", "somethingelse"));
		assertEquals("Hello my name is algorhythmic", textMultiple);
		assertEquals("Hello my name is ", parse(result.buildMessage("")));
	}

	@Test
	void filledParam() {
		final var result = messages.compileMsg("Hello my name is {name}");
		assertEquals("Hello my name is {name}", parse(result.buildMessage()));
		assertEquals("Hello my name is algorhythmic", parse(result.buildMessage("algorhythmic")));
		assertEquals("Hello my name is algorhythmic", parse(result.buildMessage("algorhythmic", "unused")));
		final var spaces = messages.compileMsg("Hello my name is {my name}");
		assertEquals("Hello my name is {my name}", parse(spaces.buildMessage()));
		assertEquals("Hello my name is algorhythmic", parse(spaces.buildMessage("algorhythmic")));
		assertEquals("Hello my name is algorhythmic", parse(spaces.buildMessage("algorhythmic", "unused")));
	}

	@Test
	void unclosedParam() {
		final var result = messages.compileMsg("Hello my name is{ {name}");
		assertEquals("Hello my name is{ {name}", parse(result.buildMessage()));
		assertEquals("Hello my name isbob", parse(result.buildMessage("bob")));
	}

	@Test
	void unclosedMultipleParam() {
		final var result = messages.compileMsg("Hello{ my name is{{ {name}");
		assertEquals("Hellobob", parse(result.buildMessage("bob", "wrong", "wrong2")));
	}

	@Test
	void invalidClosersParam() {
		final var result = messages.compileMsg("Hello} my name is {name}}");
		assertEquals("Hello} my name is bob}", parse(result.buildMessage("bob")));
	}

	@Test
	void multipleParam() {
		final var result = messages.compileMsg("Hello {param1} my name is {name}");
		assertEquals("Hello {param1} my name is {name}", parse(result.buildMessage()));
		assertEquals("Hello jacob my name is {name}", parse(result.buildMessage("jacob")));
		assertEquals("Hello jacob my name is bob", parse(result.buildMessage("jacob", "bob")));
		assertEquals("Hello jacob my name is bob", parse(result.buildMessage("jacob", "bob", "ignored")));
	}

	@Test
	void fakeParam() {
		final var result = messages.compileMsg("Hello {param1} my name is {name}");
		assertEquals("Hello {s} my name is bob", parse(result.buildMessage("{s}", "bob")));
		assertEquals("Hello a{s} my name is bob", parse(result.buildMessage("a{s}", "bob")));
		assertEquals("Hello parammmmm1something{s} my name is bob", parse(result.buildMessage("parammmmm1something{s}", "bob", "failed")));
	}
}
