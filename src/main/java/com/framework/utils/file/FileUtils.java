package com.framework.utils.file;

import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.awaitility.Awaitility;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * File system utility for download verification, upload paths, and file assertions.
 *
 * FAANG usage patterns:
 *  - Verify a file was downloaded after clicking "Export" in the UI
 *  - Resolve upload file paths relative to test-data directory
 *  - Clean up downloaded files after test execution
 *
 * Usage:
 *   FileUtils.waitForDownload("target/downloads", "report", "csv", 30);
 *   String path = FileUtils.getTestDataPath("test-data/excel/users.xlsx");
 *   FileUtils.cleanDownloads("target/downloads");
 */
public final class FileUtils {

    private static final Logger log = LogManager.getLogger(FileUtils.class);
    private static final String PROJECT_ROOT = System.getProperty("user.dir");

    private FileUtils() {}

    // ── Download Verification ────────────────────────────────────────────

    /**
     * Wait until a file matching the given name fragment and extension
     * appears in the download directory. Polls every 500ms.
     *
     * Returns the matched File or throws TimeoutException.
     */
    public static File waitForDownload(String downloadDir, String fileNameContains,
                                       String extension, int timeoutSeconds) {
        log.info("Waiting for download: [{}*.{}] in [{}]", fileNameContains, extension, downloadDir);

        final File[] result = new File[1];

        Awaitility.await()
            .alias("File download: " + fileNameContains + "." + extension)
            .atMost(timeoutSeconds, TimeUnit.SECONDS)
            .pollInterval(500, TimeUnit.MILLISECONDS)
            .until(() -> {
                Optional<File> file = findFile(downloadDir, fileNameContains, extension);
                file.ifPresent(f -> result[0] = f);
                return file.isPresent();
            });

        log.info("Download confirmed: {}", result[0].getAbsolutePath());
        return result[0];
    }

    /**
     * Wait for any .crdownload / .tmp (in-progress) files to complete.
     */
    public static void waitForDownloadToComplete(String downloadDir, int timeoutSeconds) {
        Awaitility.await()
            .atMost(timeoutSeconds, TimeUnit.SECONDS)
            .pollInterval(500, TimeUnit.MILLISECONDS)
            .until(() -> {
                File dir = new File(downloadDir);
                if (!dir.exists()) return true;
                File[] inProgress = dir.listFiles(f ->
                    f.getName().endsWith(".crdownload") || f.getName().endsWith(".tmp")
                );
                return inProgress == null || inProgress.length == 0;
            });
        log.info("All downloads completed in: {}", downloadDir);
    }

    // ── File Search ──────────────────────────────────────────────────────

    public static Optional<File> findFile(String directory, String nameContains, String extension) {
        File dir = new File(directory);
        if (!dir.exists() || !dir.isDirectory()) return Optional.empty();

        File[] files = dir.listFiles(f ->
            !f.isDirectory()
            && f.getName().toLowerCase().contains(nameContains.toLowerCase())
            && FilenameUtils.getExtension(f.getName()).equalsIgnoreCase(extension)
            && !f.getName().endsWith(".crdownload")
        );

        return (files == null || files.length == 0)
            ? Optional.empty()
            : Optional.of(files[0]);
    }

    public static List<File> listFiles(String directory, String extension) {
        File dir = new File(directory);
        if (!dir.exists()) return Collections.emptyList();

        File[] files = dir.listFiles(f ->
            !f.isDirectory()
            && FilenameUtils.getExtension(f.getName()).equalsIgnoreCase(extension)
        );

        return files == null ? Collections.emptyList() : Arrays.asList(files);
    }

    // ── Path Helpers ─────────────────────────────────────────────────────

    /**
     * Resolve a path relative to the project root (user.dir).
     * Usage: FileUtils.getTestDataPath("test-data/json/user.json")
     */
    public static String getTestDataPath(String relativePath) {
        return Paths.get(PROJECT_ROOT, relativePath).toString();
    }

    /**
     * Get absolute path of a classpath resource.
     */
    public static String getClasspathResourcePath(String resourcePath) {
        try {
            return Objects.requireNonNull(
                FileUtils.class.getClassLoader().getResource(resourcePath),
                "Resource not found: " + resourcePath
            ).getPath();
        } catch (NullPointerException e) {
            throw new RuntimeException("Classpath resource not found: " + resourcePath, e);
        }
    }

    // ── File Assertions ──────────────────────────────────────────────────

    public static boolean fileExists(String filePath) {
        return new File(filePath).exists();
    }

    public static long getFileSizeBytes(String filePath) {
        return new File(filePath).length();
    }

    public static boolean isFileNonEmpty(String filePath) {
        File file = new File(filePath);
        return file.exists() && file.length() > 0;
    }

    public static String readFileAsString(String filePath) {
        try {
            return Files.readString(Paths.get(filePath));
        } catch (IOException e) {
            throw new RuntimeException("Failed to read file: " + filePath, e);
        }
    }

    // ── Cleanup ──────────────────────────────────────────────────────────

    public static void cleanDirectory(String directory) {
        File dir = new File(directory);
        if (!dir.exists()) return;
        File[] files = dir.listFiles();
        if (files == null) return;
        int count = 0;
        for (File file : files) {
            if (file.delete()) count++;
        }
        log.info("Cleaned {} file(s) from: {}", count, directory);
    }

    public static void createDirectoryIfNotExists(String directory) {
        File dir = new File(directory);
        if (!dir.exists() && dir.mkdirs()) {
            log.debug("Created directory: {}", directory);
        }
    }

    public static void copyFile(String sourcePath, String destPath) {
        try {
            Files.copy(Paths.get(sourcePath), Paths.get(destPath), StandardCopyOption.REPLACE_EXISTING);
            log.info("Copied file: {} → {}", sourcePath, destPath);
        } catch (IOException e) {
            throw new RuntimeException("Failed to copy file: " + sourcePath + " → " + destPath, e);
        }
    }
}
