package com.lms.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Function;

/**
 * Demonstrates Generics & File Output.
 * Exports domain model collections into CSV or structured text reports.
 */
public class ReportExporter {

    /**
     * Generic CSV export.
     *
     * @param items      List of domain items
     * @param headers    CSV column headers
     * @param extractors Functions mapping item to string values
     * @param targetFile File to write to
     */
    public static <T> void exportToCSV(List<T> items, String[] headers, List<Function<T, String>> extractors, File targetFile) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(targetFile, StandardCharsets.UTF_8))) {
            // Write headers
            writer.write(String.join(",", headers));
            writer.newLine();

            // Write rows
            for (T item : items) {
                StringBuilder row = new StringBuilder();
                for (int i = 0; i < extractors.size(); i++) {
                    String val = extractors.get(i).apply(item);
                    if (val == null) val = "";
                    // Escape CSV
                    if (val.contains(",") || val.contains("\"") || val.contains("\n")) {
                        val = "\"" + val.replace("\"", "\"\"") + "\"";
                    }
                    row.append(val);
                    if (i < extractors.size() - 1) row.append(",");
                }
                writer.write(row.toString());
                writer.newLine();
            }
        }
    }

    /**
     * Exports a formatted text report with headers and summary.
     */
    public static void exportTextSummary(String title, List<String> sections, File targetFile) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(targetFile, StandardCharsets.UTF_8))) {
            writer.write("================================================================================");
            writer.newLine();
            writer.write(" " + title.toUpperCase());
            writer.newLine();
            writer.write(" Generated on: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            writer.newLine();
            writer.write("================================================================================");
            writer.newLine();
            writer.newLine();

            for (String section : sections) {
                writer.write(section);
                writer.newLine();
                writer.newLine();
            }

            writer.write("--------------------------------------------------------------------------------");
            writer.newLine();
            writer.write(" End of Report - Online Learning Management System (LMS)");
            writer.newLine();
        }
    }
}
