package it.unibo.mvc;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

/**
 */
public final class DrawNumberApp implements DrawNumberViewObserver {

    private final DrawNumber model;
    private final List<DrawNumberView> views;

    /**
     * @param views
     *            the views to attach
     */
    public DrawNumberApp(final String config, final DrawNumberView... views) {
        /*
         * Side-effect proof
         */
        this.views = Arrays.asList(Arrays.copyOf(views, views.length));
        for (final DrawNumberView view: views) {
            view.setObserver(this);
            view.start();
        }

        final Configuration.Builder builder = new Configuration.Builder();

        try (InputStream input = DrawNumberApp.class.getResourceAsStream("/config.yml");
             BufferedReader reader = new BufferedReader(new InputStreamReader(input))) {

            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim(); // Rimuove spazi bianchi iniziali e finali
                if (line.startsWith("minimum:")) {
                    builder.setMin(Integer.parseInt(line.split(":")[1].trim()));
                    System.out.println("MIN: " + Integer.parseInt(line.split(":")[1].trim()));
                } else if (line.startsWith("maximum:")) {
                    builder.setMax(Integer.parseInt(line.split(":")[1].trim()));
                    System.out.println("MAX: " + Integer.parseInt(line.split(":")[1].trim()));
                } else if (line.startsWith("attempts:")) {
                    builder.setAttempts(Integer.parseInt(line.split(":")[1].trim()));
                    System.out.println("ATTEMPS: " + Integer.parseInt(line.split(":")[1].trim()));
                }
            }
        } catch (IOException e) {
            displayError(e.getMessage());
        }
        final Configuration configuration = builder.build();
        if (configuration.isConsistent()) {
            this.model = new DrawNumberImpl(configuration);
        } else {
            displayError("Inconsistent configuration: " + "min: " + configuration.getMin() + ", " + "max: " + configuration.getMax() + ", " + "attempts: " + configuration.getAttempts() + ". Using defaults instead.");
            this.model = new DrawNumberImpl(new Configuration.Builder().build());
        }

    }

    private void displayError(final String error) {
        for (final DrawNumberView view: views) {
            view.displayError(error);
        }
    }

    @Override
    public void newAttempt(final int n) {
        try {
            final DrawResult result = model.attempt(n);
            for (final DrawNumberView view: views) {
                view.result(result);
            }
        } catch (IllegalArgumentException e) {
            for (final DrawNumberView view: views) {
                view.numberIncorrect();
            }
        }
    }

    @Override
    public void resetGame() {
        this.model.reset();
    }

    @Override
    public void quit() {
        /*
         * A bit harsh. A good application should configure the graphics to exit by
         * natural termination when closing is hit. To do things more cleanly, attention
         * should be paid to alive threads, as the application would continue to persist
         * until the last thread terminates.
         */
        System.exit(0);
    }

    /**
     * @param args
     *            ignored
     * @throws FileNotFoundException 
     */
    public static void main(final String... args) throws FileNotFoundException {
        String configPath = "config.yml"; 
        DrawNumberView view1 = new DrawNumberViewImpl();
        DrawNumberView view2 = new DrawNumberViewImpl();
        DrawNumberView view3 = new PrintStreamView(System.out);
        DrawNumberView view4 = new PrintStreamView("output.log");

        DrawNumberApp app = new DrawNumberApp(configPath, view1, view2, view3, view4);
    }
}


