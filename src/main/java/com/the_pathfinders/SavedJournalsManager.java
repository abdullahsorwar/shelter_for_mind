package com.the_pathfinders;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class SavedJournalsManager {
    private static final String SAVED_JOURNALS_DIR = getAppDataDirectory();
    private static final java.util.List<Runnable> listeners = new java.util.ArrayList<>();

    public SavedJournalsManager() {
        // Create directory if it doesn't exist
        try {
            Files.createDirectories(Paths.get(SAVED_JOURNALS_DIR));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Get the application data directory for saved journals
     */
    private static String getAppDataDirectory() {
        String userHome = System.getProperty("user.home");
        String appDataDir = userHome + File.separator + ".shelter_for_mind" + File.separator + "saved_journals";
        return appDataDir;
    }

    /**
     * Save a journal as starred for the given user
     */
    public void saveJournal(String soulId, Journal journal) {
        try {
            String filePath = getSavedJournalsFilePath(soulId);
            Set<String> savedJournalIds = loadSavedJournalIds(soulId);
            savedJournalIds.add(journal.getId());
            writeSavedJournalIds(filePath, savedJournalIds);
            notifyListeners();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Remove a journal from starred list for the given user
     */
    public void removeSavedJournal(String soulId, String journalId) {
        try {
            String filePath = getSavedJournalsFilePath(soulId);
            Set<String> savedJournalIds = loadSavedJournalIds(soulId);
            savedJournalIds.remove(journalId);
            writeSavedJournalIds(filePath, savedJournalIds);
            notifyListeners();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Load all saved journal IDs for a user
     */
    public Set<String> loadSavedJournalIds(String soulId) {
        try {
            String filePath = getSavedJournalsFilePath(soulId);
            File file = new File(filePath);
            if (!file.exists()) {
                return new HashSet<>();
            }
            Set<String> journalIds = new HashSet<>();
            List<String> lines = Files.readAllLines(Paths.get(filePath));
            for (String line : lines) {
                String id = line.trim();
                if (!id.isEmpty()) {
                    journalIds.add(id);
                }
            }
            return journalIds;
        } catch (IOException e) {
            e.printStackTrace();
            return new HashSet<>();
        }
    }

    /**
     * Check if a journal is saved for a user
     */
    public boolean isJournalSaved(String soulId, String journalId) {
        return loadSavedJournalIds(soulId).contains(journalId);
    }

    /**
     * Load saved journals list as formatted info for display in profile
     */
    public List<String> loadSavedJournalsInfo(String soulId, List<Journal> allJournals) {
        Set<String> savedIds = loadSavedJournalIds(soulId);
        List<String> result = new ArrayList<>();
        
        for (Journal journal : allJournals) {
            if (savedIds.contains(journal.getId())) {
                String preview = journal.getText();
                if (preview != null && preview.length() > 60) {
                    preview = preview.substring(0, 60) + "...";
                }
                result.add(preview + " — by " + journal.getSoulId());
            }
        }
        
        return result;
    }

    private String getSavedJournalsFilePath(String soulId) {
        return SAVED_JOURNALS_DIR + File.separator + soulId + "_saved_journals.txt";
    }

    private void writeSavedJournalIds(String filePath, Set<String> journalIds) throws IOException {
        Files.write(Paths.get(filePath), String.join("\n", journalIds).getBytes());
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
