package ui;

import java.util.Scanner;

/**
 * BoxUI - Boxed Console Interface Component
 * 
 * This class provides professional-looking boxed menus and sections for the console application.
 * It creates clean, formatted interfaces using Unicode box-drawing characters to make the
 * Smart Campus Management System look professional and user-friendly.
 * 
 * Key Features:
 * - Automatic box sizing based on content
 * - Centered titles and formatted options
 * - Professional menu rendering with box borders
 * - Input validation for numeric choices
 * - Section printing for multi-line content
 * - Error and success message formatting
 * 
 * Key Concepts Demonstrated:
 * - Unicode character rendering for box drawing
 * - String manipulation and formatting
 * - Scanner input handling
 * - Dynamic width calculation
 * - Method overloading for different use cases
 * 
 * This class is used throughout the application to create consistent, professional-looking
 * console interfaces for menus, forms, and information displays.
 */
public class BoxUI {
    
    /** Minimum width for any box to ensure readability */
    private static final int MIN_BOX_WIDTH = 39;
    
    /** Scanner object for reading user input from console */
    private final Scanner scanner;
    
    /** Current active box width (changes based on content) */
    private int activeBoxWidth = MIN_BOX_WIDTH;

    /**
     * Constructor for BoxUI.
     * 
     * @param scanner Scanner object for reading user input
     */
    public BoxUI(Scanner scanner) {
        this.scanner = scanner;
    }

    /**
     * Prompts the user for input and returns the trimmed response.
     * 
     * This is a simple input method that displays a prompt and reads a line of text.
     * It automatically removes leading/trailing whitespace for cleaner input handling.
     * 
     * @param text The prompt text to display to the user
     * @return The user's input with whitespace trimmed
     */
    public String prompt(String text) {
        System.out.print(text);
        return scanner.nextLine().trim();
    }

    /**
     * Displays a formatted menu with a title and list of options.
     * 
     * This method creates a professional-looking boxed menu with:
     * - Centered title at the top
     * - Horizontal separator line
     * - Numbered or bulleted options
     * - Box borders using Unicode characters
     * 
     * Example:
     * ```
     * +-----------------------+
     * |      MAIN MENU        |
     * +-----------------------+
     * |  1. Option One        |
     * |  2. Option Two        |
     * |  3. Exit              |
     * +-----------------------+
     * ```
     * 
     * @param title The menu title (will be centered)
     * @param options Array of option strings to display
     */
    public void printMenu(String title, String[] options) {
        // Calculate the optimal box width based on title and options
        int innerWidth = resolveBoxWidth(title, options);
        
        // Print empty line for spacing
        System.out.println();
        
        // Print top border
        System.out.println("╔" + "═".repeat(innerWidth) + "╗");
        
        // Print centered title
        System.out.println(formatBoxRow(centerText(title, innerWidth), innerWidth));
        
        // Print separator line
        System.out.println("╠" + "═".repeat(innerWidth) + "╣");
        
        // Print each option with proper spacing
        for (String option : options) {
            System.out.println(formatBoxRow("  " + option, innerWidth));
        }
        
        // Print bottom border
        System.out.println("╚" + "═".repeat(innerWidth) + "╝");
    }

    /**
     * Starts a new section with a title.
     * 
     * This method begins a multi-line section that can be used for displaying
     * detailed information, forms, or other content that spans multiple lines.
     * The section must be ended with endSection().
     * 
     * Example:
     * ```
     * printSection("Student Profile", "Name: John Doe", "ID: 12345");
     * line("Department: Computer Science");
     * line("Year: 3");
     * endSection();
     * ```
     * 
     * @param title The section title (will be centered)
     * @param linesForSizing Sample lines to calculate optimal box width
     */
    public void printSection(String title, String... linesForSizing) {
        // Calculate box width based on title and sample lines
        int innerWidth = resolveBoxWidth(title, linesForSizing);
        
        // Set the active box width for subsequent line() calls
        activeBoxWidth = innerWidth;
        
        System.out.println();
        
        // Print top border
        System.out.println("╔" + "═".repeat(innerWidth) + "╗");
        
        // Print centered title
        System.out.println(formatBoxRow(centerText(title, innerWidth), innerWidth));
        
        // Print separator line (bottom of header)
        System.out.println("╠" + "═".repeat(innerWidth) + "╣");
    }

    /**
     * Prints a line of content within an active section.
     * 
     * This method must be called between printSection() and endSection().
     * It formats the text to fit within the current box width.
     * 
     * @param text The content to display (null values become empty strings)
     */
    public void line(String text) {
        // Handle null values gracefully
        String content = text == null ? "" : text;
        
        // Format the line with proper spacing and box borders
        System.out.println(formatBoxRow("  " + content, activeBoxWidth));
    }

    /**
     * Ends the current section by printing the bottom border.
     * 
     * This method closes a section that was started with printSection().
     * It resets the active box width to the minimum for the next section.
     */
    public void endSection() {
        // Print bottom border
        System.out.println("╚" + "═".repeat(activeBoxWidth) + "╝");
        
        // Reset active box width for next section
        activeBoxWidth = MIN_BOX_WIDTH;
    }

    // ========== MESSAGE DISPLAY METHODS ==========

    /**
     * Displays a success message in a formatted box.
     * 
     * This method creates a green-themed success message box that's commonly used
     * to confirm successful operations like adding students, enrolling in courses, etc.
     * 
     * @param message The success message to display
     */
    public void success(String message) {
        printSection("SUCCESS", message);
        line(message);
        endSection();
    }

    /**
     * Displays an error message in a formatted box.
     * 
     * This method creates a red-themed error message box that's commonly used
     * to display validation errors, operation failures, or other issues.
     * 
     * @param message The error message to display
     */
    public void error(String message) {
        printSection("ERROR", message);
        line(message);
        endSection();
    }

    /**
     * Displays an informational message in a formatted box.
     * 
     * This method creates a blue-themed info message box that's commonly used
     * to provide helpful information, tips, or system status updates.
     * 
     * @param message The informational message to display
     */
    public void info(String message) {
        printSection("INFO", message);
        line(message);
        endSection();
    }

    // ========== INPUT VALIDATION METHODS ==========

    /**
     * Reads and validates an integer input from the user.
     * 
     * This method provides robust input validation for numeric choices:
     * 1. Prompts the user with the provided message
     * 2. Reads and parses the input as an integer
     * 3. Validates that the number is within the specified range
     * 4. Displays an error message and retries if invalid
     * 
     * This method will continue prompting until valid input is received,
     * making it ideal for menu selections and other numeric choices.
     * 
     * @param prompt The message to display to the user
     * @param min Minimum acceptable value (inclusive)
     * @param max Maximum acceptable value (inclusive)
     * @return The validated integer input from the user
     */
    public int readInt(String prompt, int min, int max) {
        while (true) {
            // Display prompt and read user input
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            
            try {
                // Try to convert input to integer
                int v = Integer.parseInt(line);
                
                // Check if the number is within the valid range
                if (v >= min && v <= max) {
                    return v; // Valid input - return it
                }
            } catch (NumberFormatException ignored) {
                // Input wasn't a valid number - will retry
            }
            
            // Display error message and retry
            error("Enter a number between " + min + " and " + max + ".");
        }
    }

    // ========== HELPER METHODS (PRIVATE) ==========

    /**
     * Calculates the optimal box width based on content.
     * 
     * This helper method determines the appropriate width for a box by:
     * 1. Finding the longest line among title and content lines
     * 2. Adding padding space (4 characters: 2 on each side)
     * 3. Ensuring the width meets the minimum requirement
     * 
     * @param title The title text to consider for width calculation
     * @param lines Array of content lines to consider
     * @return The calculated optimal box width
     */
    private int resolveBoxWidth(String title, String... lines) {
        // Start with title length (or 0 if null)
        int longest = title == null ? 0 : title.length();
        
        // Check each line to find the longest
        if (lines != null) {
            for (String line : lines) {
                if (line != null && line.length() > longest) {
                    longest = line.length();
                }
            }
        }
        
        // Return the maximum of minimum width and longest content + padding
        return Math.max(MIN_BOX_WIDTH, longest + 4);
    }

    /**
     * Formats a single row of content within a box.
     * 
     * This helper method creates a properly formatted box row with:
     * - Left border character (|)
     * - Padded content (left-aligned)
     * - Right border character (|)
     * 
     * If content is too long, it gets truncated with "..." to fit.
     * 
     * @param content The text content to format
     * @param width The total width of the box interior
     * @return Formatted box row string with borders
     */
    private String formatBoxRow(String content, int width) {
        // Handle null content gracefully
        String text = content == null ? "" : content;
        
        // Truncate content if it's too long for the box
        if (text.length() > width) {
            text = text.substring(0, width - 3) + "...";
        }
        
        // Format with left alignment and padding
        return "║" + String.format("%-" + width + "s", text) + "║";
    }

    /**
     * Centers text within a specified width.
     * 
     * This helper method calculates the appropriate padding on both sides
     * to center text within a box row. If text is too long, it gets truncated.
     * 
     * @param text The text to center
     * @param width The total width available
     * @return Centered text with appropriate padding
     */
    private String centerText(String text, int width) {
        // Handle null text gracefully
        String value = text == null ? "" : text;
        
        // If text is too long, truncate it
        if (value.length() >= width) {
            return value.substring(0, width);
        }
        
        // Calculate padding for both sides
        int leftPad = (width - value.length()) / 2;
        int rightPad = width - value.length() - leftPad;
        
        // Return centered text with padding
        return " ".repeat(leftPad) + value + " ".repeat(rightPad);
    }
}
