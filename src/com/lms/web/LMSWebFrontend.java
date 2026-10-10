package com.lms.web;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Serves the web interface from its editable HTML source.
 * This keeps the frontend maintainable and avoids Java's 64 KB string-literal
 * limit that applied when the entire page was embedded in this class.
 */
public final class LMSWebFrontend {

    private LMSWebFrontend() {
    }

    public static final String HTML_PAGE = loadPage();

    private static String loadPage() {
        try {
            return Files.readString(Path.of("web", "index.html"));
        } catch (IOException exception) {
            return "<!doctype html><html lang=\"en\"><head><title>NextGen LMS</title>"
                    + "</head><body><h1>NextGen LMS</h1>"
                    + "<p>Unable to load the web interface. Start the server from the project folder.</p>"
                    + "</body></html>";
        }
    }
}
