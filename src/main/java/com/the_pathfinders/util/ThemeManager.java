package com.the_pathfinders.util;

import javafx.scene.Parent;

/**
 * Global theme manager to maintain consistent light/dark mode across all pages.
 * Uses volatile variable to ensure thread-safe access.
 */
public class ThemeManager {
    
    private static volatile boolean isLightMode = false;
    
    /**
     * Check if light mode is currently enabled
     */
    public static boolean isLightMode() {
        return isLightMode;
    }
    
    /**
     * Set the theme mode
     */
    public static void setLightMode(boolean lightMode) {
        isLightMode = lightMode;
    }
    
    /**
     * Toggle between light and dark mode
     */
    public static void toggleTheme() {
        isLightMode = !isLightMode;
    }
    
    /**
     * Apply the current theme to a root node
     */
    public static void applyTheme(Parent root) {
        if (root == null) return;
        
        if (isLightMode) {
            root.getStyleClass().remove("dark-mode");
            if (!root.getStyleClass().contains("light-mode")) {
                root.getStyleClass().add("light-mode");
            }
        } else {
            root.getStyleClass().remove("light-mode");
            if (!root.getStyleClass().contains("dark-mode")) {
                root.getStyleClass().add("dark-mode");
            }
        }
    }
}
