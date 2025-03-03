// MainTest.java
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import javafx.scene.Scene;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javax.sound.sampled.*;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(ApplicationExtension.class)
class SynthwavePianoTest {
    private SynthwavePiano piano;
    private Scene scene;

    @Start
    private void start(Stage stage) {
        piano = new SynthwavePiano();
        piano.start(stage);
        scene = stage.getScene();
    }

    @Test
    void testPianoInitialization(FxRobot robot) {
        // Test window properties
        assertNotNull(scene);
        assertEquals(800, scene.getWidth());
        assertEquals(400, scene.getHeight());
        
        // Count white and black keys
        List<Rectangle> whiteKeys = robot.lookup((Rectangle r) -> 
            r.getFill().equals(javafx.scene.paint.Color.WHITE)).queryAll();
        List<Rectangle> blackKeys = robot.lookup((Rectangle r) -> 
            r.getFill().equals(javafx.scene.paint.Color.rgb(25, 25, 25))).queryAll();
        
        assertEquals(14, whiteKeys.size()); // Two octaves
        assertEquals(10, blackKeys.size()); // Two octaves minus E-F and B-C gaps
    }

    @Test
    void testKeyDimensions(FxRobot robot) {
        Rectangle whiteKey = robot.lookup((Rectangle r) -> 
            r.getFill().equals(javafx.scene.paint.Color.WHITE)).query();
        Rectangle blackKey = robot.lookup((Rectangle r) -> 
            r.getFill().equals(javafx.scene.paint.Color.rgb(25, 25, 25))).query();
        
        assertEquals(60, whiteKey.getWidth());
        assertEquals(300, whiteKey.getHeight());
        assertEquals(40, blackKey.getWidth());
        assertEquals(180, blackKey.getHeight());
    }
}

// AudioTest.java
@ExtendWith(ApplicationExtension.class)
class SynthwavePianoAudioTest {
    private SynthwavePiano piano;
    private AudioFormat format;
    private SourceDataLine line;

    @BeforeEach
    void setUp() throws Exception {
        format = new AudioFormat(44100, 16, 1, true, true);
        line = AudioSystem.getSourceDataLine(format);
    }

    @Test
    void testAudioFormat() {
        assertEquals(44100, format.getSampleRate());
        assertEquals(16, format.getSampleSizeInBits());
        assertEquals(1, format.getChannels());
        assertTrue(format.isBigEndian());
    }

    @Test
    void testAudioLine() throws Exception {
        assertTrue(line.isOpen());
        assertEquals(format, line.getFormat());
    }

    @Test
    void testFrequencyCalculation() {
        // Test frequency calculations for specific notes
        Map<Rectangle, Integer> keyFrequencies = piano.getKeyFrequencies();
        Rectangle cKey = // ... get C key
        assertEquals(262, keyFrequencies.get(cKey)); // C4
        
        Rectangle aKey = // ... get A key
        assertEquals(440, keyFrequencies.get(aKey)); // A4
    }
}

// InteractionTest.java
@ExtendWith(ApplicationExtension.class)
class SynthwavePianoInteractionTest {
    private SynthwavePiano piano;

    @Start
    private void start(Stage stage) {
        piano = new SynthwavePiano();
        piano.start(stage);
    }

    @Test
    void testKeyPress(FxRobot robot) {
        Rectangle whiteKey = robot.lookup((Rectangle r) -> 
            r.getFill().equals(javafx.scene.paint.Color.WHITE)).query();
        
        // Test key press visual feedback
        robot.moveTo(whiteKey).press(MouseButton.PRIMARY);
        assertEquals(Color.rgb(200, 200, 255), whiteKey.getFill());
        
        // Test key release visual feedback
        robot.release(MouseButton.PRIMARY);
        assertEquals(Color.WHITE, whiteKey.getFill());
    }

    @Test
    void testMultipleKeyPress(FxRobot robot) {
        List<Rectangle> whiteKeys = robot.lookup((Rectangle r) -> 
            r.getFill().equals(javafx.scene.paint.Color.WHITE)).queryAll();
        
        // Test multiple simultaneous key presses
        robot.moveTo(whiteKeys.get(0)).press(MouseButton.PRIMARY);
        robot.moveTo(whiteKeys.get(2)).press(MouseButton.PRIMARY);
        
        assertEquals(Color.rgb(200, 200, 255), whiteKeys.get(0).getFill());
        assertEquals(Color.rgb(200, 200, 255), whiteKeys.get(2).getFill());
        
        robot.release(MouseButton.PRIMARY);
    }
}

// EdgeCasesTest.java
@ExtendWith(ApplicationExtension.class)
class SynthwavePianoEdgeCasesTest {
    private SynthwavePiano piano;

    @Test
    void testAudioResourceLeaks() throws Exception {
        // Test for audio resource leaks
        Rectangle key = // ... get a key
        piano.startNote(key);
        piano.stopNote(key);
        
        // Verify all resources are properly closed
        assertTrue(piano.getActiveNotes().isEmpty());
    }

    @Test
    void testRapidKeyPresses(FxRobot robot) {
        Rectangle key = robot.lookup((Rectangle r) -> 
            r.getFill().equals(javafx.scene.paint.Color.WHITE)).query();
        
        // Test rapid key presses
        for (int i = 0; i < 100; i++) {
            robot.moveTo(key).press(MouseButton.PRIMARY).release(MouseButton.PRIMARY);
        }
        
        // Verify no resource leaks
        assertTrue(piano.getActiveNotes().isEmpty());
    }

    @Test
    void testWindowResize() {
        Stage stage = // ... get stage
        stage.setWidth(400);
        stage.setHeight(200);
        
        // Verify piano remains responsive after resize
        // Add assertions for key positions and dimensions
    }

    @Test
    void testExceptionHandling() {
        // Test with unavailable audio device
        AudioSystem.getSourceDataLine(null); // Should throw exception
        // Verify application handles the exception gracefully
    }
}

// PerformanceTest.java
@ExtendWith(ApplicationExtension.class)
class SynthwavePianoPerformanceTest {
    private SynthwavePiano piano;
    private long startTime;

    @BeforeEach
    void setUp() {
        startTime = System.nanoTime();
    }

    @Test
    void testAudioLatency() {
        Rectangle key = // ... get a key
        piano.startNote(key);
        long latency = System.nanoTime() - startTime;
        
        // Audio latency should be under 50ms
        assertTrue(latency < 50_000_000);
        
        piano.stopNote(key);
    }

    @Test
    void testMemoryUsage() {
        long initialMemory = Runtime.getRuntime().totalMemory() - 
                           Runtime.getRuntime().freeMemory();
        
        // Simulate heavy usage
        for (int i = 0; i < 1000; i++) {
            Rectangle key = // ... get a key
            piano.startNote(key);
            piano.stopNote(key);
        }
        
        long finalMemory = Runtime.getRuntime().totalMemory() - 
                          Runtime.getRuntime().freeMemory();
        
        // Memory increase should be reasonable
        assertTrue((finalMemory - initialMemory) < 50_000_000); // 50MB limit
    }
}

// TestHelper.java
class TestHelper {
    static void waitForAudio(int milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    static boolean isAudioPlaying(SourceDataLine line) {
        return line != null && line.isActive();
    }
}