package com.the_pathfinders;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class SavedBlogsManager {
    private static final String SAVED_BLOGS_DIR = "data/saved_blogs";
    private static final java.util.List<Runnable> listeners = new java.util.ArrayList<>();

    public SavedBlogsManager() {
        // Create directory if it doesn't exist
        try {
            Files.createDirectories(Paths.get(SAVED_BLOGS_DIR));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Save a blog as saved for the given user
     */
    public void saveBlog(String soulId, Blog blog) {
        try {
            String filePath = getSavedBlogsFilePath(soulId);
            Set<String> savedBlogIds = loadSavedBlogIds(soulId);
            savedBlogIds.add(blog.getId());
            writeSavedBlogIds(filePath, savedBlogIds);
            notifyListeners();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Remove a blog from saved list for the given user
     */
    public void removeSavedBlog(String soulId, String blogId) {
        try {
            String filePath = getSavedBlogsFilePath(soulId);
            Set<String> savedBlogIds = loadSavedBlogIds(soulId);
            savedBlogIds.remove(blogId);
            writeSavedBlogIds(filePath, savedBlogIds);
            notifyListeners();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Load all saved blog IDs for a user
     */
    public Set<String> loadSavedBlogIds(String soulId) {
        try {
            String filePath = getSavedBlogsFilePath(soulId);
            File file = new File(filePath);
            if (!file.exists()) {
                return new HashSet<>();
            }
            Set<String> blogIds = new HashSet<>();
            List<String> lines = Files.readAllLines(Paths.get(filePath));
            for (String line : lines) {
                String id = line.trim();
                if (!id.isEmpty()) {
                    blogIds.add(id);
                }
            }
            return blogIds;
        } catch (IOException e) {
            e.printStackTrace();
            return new HashSet<>();
        }
    }

    /**
     * Check if a blog is saved for a user
     */
    public boolean isBlogSaved(String soulId, String blogId) {
        return loadSavedBlogIds(soulId).contains(blogId);
    }

    /**
     * Load saved blogs list as a formatted string for display in profile
     */
    public List<String> loadSavedBlogsInfo(String soulId, List<Blog> allBlogs) {
        Set<String> savedIds = loadSavedBlogIds(soulId);
        List<String> result = new ArrayList<>();
        
        for (Blog blog : allBlogs) {
            if (savedIds.contains(blog.getId())) {
                result.add(blog.getTitle() + " — " + blog.getCategory());
            }
        }
        
        return result;
    }

    private String getSavedBlogsFilePath(String soulId) {
        return SAVED_BLOGS_DIR + File.separator + soulId + "_saved_blogs.txt";
    }

    private void writeSavedBlogIds(String filePath, Set<String> blogIds) throws IOException {
        Files.write(Paths.get(filePath), String.join("\n", blogIds).getBytes());
    }

    public static void addListener(Runnable r) {
        if (r == null) return;
        synchronized (listeners) { listeners.add(r); }
    }

    public static void removeListener(Runnable r) {
        if (r == null) return;
        synchronized (listeners) { listeners.remove(r); }
    }

    private static void notifyListeners() {
        synchronized (listeners) {
            for (Runnable r : new java.util.ArrayList<>(listeners)) {
                try { r.run(); } catch (Exception ignored) {}
            }
        }
    }
}
