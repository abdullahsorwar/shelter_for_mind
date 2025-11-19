package com.the_pathfinders;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class BlogContentLoader {
    private static final String BLOGS_DIR = "data/blogs";

    public static String loadContentForCategory(String category, String fallback) {
        if (category == null) return fallback == null ? "" : fallback;
        try {
            String slug = slugify(category);
            Path p = Paths.get(BLOGS_DIR, slug + ".txt");
            if (Files.exists(p)) {
                return String.join(System.lineSeparator(), Files.readAllLines(p, StandardCharsets.UTF_8));
            }
        } catch (IOException ignored) {}
        return fallback == null ? "" : fallback;
    }

    private static String slugify(String s) {
        return s.toLowerCase().replaceAll("[^a-z0-9]+", "-").replaceAll("(^-|-$)", "");
    }
}
