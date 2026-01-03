package com.the_pathfinders;

/**
 * Launcher class to properly start JavaFX application when packaged.
 * This avoids issues with module system and JavaFX initialization.
 */
public class Launcher {
    public static void main(String[] args) {
        // Launch the JavaFX application
        App.main(args);
    }
}
