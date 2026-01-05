package com.the_pathfinders;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

public class BlogContentLoader {
    private static final String BLOGS_DIR = "/data/blogs";

    public static String loadContentForCategory(String category, String fallback) {
        if (category == null) return fallback == null ? "" : fallback;
        try {
            String slug = slugify(category);
            String resourcePath = BLOGS_DIR + "/" + slug + ".txt";
            
            // Try to load from classpath (works in both development and packaged app)
            InputStream is = BlogContentLoader.class.getResourceAsStream(resourcePath);
            if (is != null) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                    return reader.lines().collect(Collectors.joining(System.lineSeparator()));
                }
            }
        } catch (IOException ignored) {}
        return fallback == null ? "" : fallback;
    }

    private static String slugify(String s) {
        return s.toLowerCase().replaceAll("[^a-z0-9]+", "-").replaceAll("(^-|-$)", "");
    }
}
