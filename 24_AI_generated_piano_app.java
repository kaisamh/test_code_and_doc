import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javax.sound.sampled.*;
import java.util.HashMap;
import java.util.Map;

public class SynthwavePiano extends Application {
    private static final int SAMPLE_RATE = 44100;
    private Map<Rectangle, Integer> keyFrequencies = new HashMap<>();
    private Map<Rectangle, SourceDataLine> activeNotes = new HashMap<>();

    @Override
    public void start(Stage primaryStage) {
        Pane root = new Pane();
        root.setStyle("-fx-background-color: #120458;"); // Dark synthwave background

        // Create piano keys
        createPianoKeys(root);

        Scene scene = new Scene(root, 800, 400);
        primaryStage.setTitle("Synthwave Piano");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void createPianoKeys(Pane root) {
        int whiteKeyWidth = 60;
        int whiteKeyHeight = 300;
        int blackKeyWidth = 40;
        int blackKeyHeight = 180;
        
        String[] notes = {"C", "D", "E", "F", "G", "A", "B"};
        int[] frequencies = {262, 294, 330, 349, 392, 440, 494}; // Base frequencies for octave 4
        
        for (int i = 0; i < 14; i++) { // Two octaves
            int octaveOffset = i / 7;
            int noteIndex = i % 7;
            
            Rectangle whiteKey = createWhiteKey(i * whiteKeyWidth, whiteKeyWidth, whiteKeyHeight);
            keyFrequencies.put(whiteKey, frequencies[noteIndex] * (1 + octaveOffset));
            
            // Add event handlers
            whiteKey.setOnMousePressed(e -> startNote(whiteKey));
            whiteKey.setOnMouseReleased(e -> stopNote(whiteKey));
            
            root.getChildren().add(whiteKey);
            
            // Add black keys (except between E-F and B-C)
            if (noteIndex != 2 && noteIndex != 6) {
                Rectangle blackKey = createBlackKey(i * whiteKeyWidth + whiteKeyWidth * 2/3, 
                                                  blackKeyWidth, blackKeyHeight);
                keyFrequencies.put(blackKey, (int)(frequencies[noteIndex] * 1.059463094359 * (1 + octaveOffset)));
                
                blackKey.setOnMousePressed(e -> startNote(blackKey));
                blackKey.setOnMouseReleased(e -> stopNote(blackKey));
                
                root.getChildren().add(blackKey);
            }
        }
    }

    private Rectangle createWhiteKey(int x, int width, int height) {
        Rectangle key = new Rectangle(x, 50, width, height);
        key.setFill(Color.WHITE);
        key.setStroke(Color.rgb(138, 43, 226)); // Purple stroke for synthwave effect
        key.setStrokeWidth(2);
        key.setArcWidth(10);
        key.setArcHeight(10);
        return key;
    }

    private Rectangle createBlackKey(int x, int width, int height) {
        Rectangle key = new Rectangle(x, 50, width, height);
        key.setFill(Color.rgb(25, 25, 25));
        key.setStroke(Color.rgb(255, 50, 255)); // Pink stroke for synthwave effect
        key.setStrokeWidth(2);
        key.setArcWidth(5);
        key.setArcHeight(5);
        return key;
    }

    private void startNote(Rectangle key) {
        try {
            AudioFormat format = new AudioFormat(SAMPLE_RATE, 16, 1, true, true);
            SourceDataLine line = AudioSystem.getSourceDataLine(format);
            line.open(format);
            line.start();
            
            int frequency = keyFrequencies.get(key);
            activeNotes.put(key, line);
            
            // Start audio synthesis in separate thread
            new Thread(() -> {
                try {
                    byte[] buffer = new byte[4096];
                    double angle = 0;
                    double angleDelta = 2.0 * Math.PI * frequency / SAMPLE_RATE;
                    
                    while (activeNotes.containsKey(key)) {
                        for (int i = 0; i < buffer.length; i += 2) {
                            double sample = Math.sin(angle) * 0.2; // Sine wave with reduced amplitude
                            sample += Math.sin(angle * 2) * 0.1; // Add harmonics for richer sound
                            
                            short s = (short) (sample * Short.MAX_VALUE);
                            buffer[i] = (byte) (s >> 8);
                            buffer[i + 1] = (byte) (s & 0xFF);
                            
                            angle += angleDelta;
                            if (angle > 2.0 * Math.PI) {
                                angle -= 2.0 * Math.PI;
                            }
                        }
                        line.write(buffer, 0, buffer.length);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
            
            // Visual feedback
            key.setFill(key.getFill() == Color.WHITE ? 
                       Color.rgb(200, 200, 255) : Color.rgb(50, 50, 75));
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void stopNote(Rectangle key) {
        SourceDataLine line = activeNotes.remove(key);
        if (line != null) {
            line.drain();
            line.stop();
            line.close();
        }
        
        // Reset key color
        key.setFill(key.getStroke() == Color.rgb(138, 43, 226) ? 
                    Color.WHITE : Color.rgb(25, 25, 25));
    }

    public static void main(String[] args) {
        launch(args);
    }
}