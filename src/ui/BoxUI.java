package ui;

import java.util.Scanner;

/**
 * Shared boxed console rendering + input helpers.
 */
public class BoxUI {
    private static final int MIN_BOX_WIDTH = 39;
    private final Scanner scanner;
    private int activeBoxWidth = MIN_BOX_WIDTH;

    public BoxUI(Scanner scanner) {
        this.scanner = scanner;
    }

    public String prompt(String text) {
        System.out.print(text);
        return scanner.nextLine().trim();
    }

    public void printMenu(String title, String[] options) {
        int innerWidth = resolveBoxWidth(title, options);
        System.out.println();
        System.out.println("╔" + "═".repeat(innerWidth) + "╗");
        System.out.println(formatBoxRow(centerText(title, innerWidth), innerWidth));
        System.out.println("╠" + "═".repeat(innerWidth) + "╣");
        for (String option : options) {
            System.out.println(formatBoxRow("  " + option, innerWidth));
        }
        System.out.println("╚" + "═".repeat(innerWidth) + "╝");
    }

    public void printSection(String title, String... linesForSizing) {
        int innerWidth = resolveBoxWidth(title, linesForSizing);
        activeBoxWidth = innerWidth;
        System.out.println();
        System.out.println("╔" + "═".repeat(innerWidth) + "╗");
        System.out.println(formatBoxRow(centerText(title, innerWidth), innerWidth));
        System.out.println("╠" + "═".repeat(innerWidth) + "╣");
    }

    public void line(String text) {
        String content = text == null ? "" : text;
        System.out.println(formatBoxRow("  " + content, activeBoxWidth));
    }

    public void endSection() {
        System.out.println("╚" + "═".repeat(activeBoxWidth) + "╝");
        activeBoxWidth = MIN_BOX_WIDTH;
    }

    public void success(String message) {
        printSection("SUCCESS", message);
        line(message);
        endSection();
    }

    public void error(String message) {
        printSection("ERROR", message);
        line(message);
        endSection();
    }

    public void info(String message) {
        printSection("INFO", message);
        line(message);
        endSection();
    }

    public int readInt(String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            try {
                int v = Integer.parseInt(line);
                if (v >= min && v <= max) {
                    return v;
                }
            } catch (NumberFormatException ignored) {
                // retry
            }
            error("Enter a number between " + min + " and " + max + ".");
        }
    }

    private int resolveBoxWidth(String title, String... lines) {
        int longest = title == null ? 0 : title.length();
        if (lines != null) {
            for (String line : lines) {
                if (line != null && line.length() > longest) {
                    longest = line.length();
                }
            }
        }
        return Math.max(MIN_BOX_WIDTH, longest + 4);
    }

    private String formatBoxRow(String content, int width) {
        String text = content == null ? "" : content;
        return "║" + String.format("%-" + width + "s", text) + "║";
    }

    private String centerText(String text, int width) {
        String value = text == null ? "" : text;
        if (value.length() >= width) {
            return value.substring(0, width);
        }
        int leftPad = (width - value.length()) / 2;
        int rightPad = width - value.length() - leftPad;
        return " ".repeat(leftPad) + value + " ".repeat(rightPad);
    }
}
