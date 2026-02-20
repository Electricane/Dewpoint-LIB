package network.delay.io;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.URL;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class DewIO {

    /**
     * Deletes a file or a directory and all its contents recursively.
     * Java's Files.delete() fails on non-empty directories which crashes my apps so its added here.
     */
    public static void deleteRecursive(Path path) throws IOException {
        if (Files.notExists(path)) return;
        
        Files.walkFileTree(path, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    /**
     * Moves a file or directory.
     * Unlike Files.move, we are creating missing parent directories and handles
     */
    public static void move(Path source, Path target) throws IOException {
        Path parent = target.getParent();
        if (parent != null && Files.notExists(parent)) {
            Files.createDirectories(parent);
        }
        
        try {
            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    /**
     * Reads all lines from a file using UTF-8. 
     * Shorthand for Files.readAllLines.
     */
    public static List<String> readLines(Path path) throws IOException {
        return Files.readAllLines(path, StandardCharsets.UTF_8);
    }

    /**
     * Writes a string to a file, creating parents if they don't exist.
     */
    public static void writeString(Path path, String content) throws IOException {
        Path parent = path.getParent();
        if (parent != null && Files.notExists(parent)) {
            Files.createDirectories(parent);
        }
        Files.writeString(path, content, StandardCharsets.UTF_8);
    }

    /**
     * Checks if a path is empty (no files in dir, or 0-byte file).
     */
    public static boolean isEmpty(Path path) throws IOException {
        if (Files.isDirectory(path)) {
            try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(path)) {
                return !dirStream.iterator().hasNext();
            }
        }
        return Files.size(path) == 0;
    }

    /**
     * Returns the file extension (e.g., "java") or an empty string.
     */
    public static String getExtension(Path path) {
        String name = path.getFileName().toString();
        int lastDot = name.lastIndexOf('.');
        if (lastDot > 0 && lastDot < name.length() - 1) {
            return name.substring(lastDot + 1);
        }
        return "";
    }

    /**
     * Calculates the total size of a directory in bytes.
     * Java doesn't provide this natively; you have to walk the tree.
     * Thank me later!
     */
    public static long getDirectorySize(Path path) throws IOException {
        AtomicLong size = new AtomicLong(0);
        Files.walkFileTree(path, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                size.addAndGet(attrs.size());
                return FileVisitResult.CONTINUE;
            }
        });
        return size.get();
    }

    /**
     * Executes a system command and returns the output as a string.
     * Handles stream consumption (which can hang the JVM if not done).
     */
    public static String runCommand(String... command) throws IOException, InterruptedException {
        return runCommand(Integer.MAX_VALUE, command);
    }

    /**
     * Executes a system command and returns the output as a string.
     * Kills the process if it doesn't finish in n seconds.
     * Handles stream consumption (which can hang the JVM if not done).
     */
    public static String runCommand(int timeoutSeconds, String... command) throws IOException, InterruptedException {
        Process process = new ProcessBuilder(command)
                .redirectErrorStream(true)
                .start();

        String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        boolean finished = process.waitFor(timeoutSeconds, TimeUnit.SECONDS);

        if (!finished) {
            process.destroyForcibly();
            throw new IOException("Command timed out: " + String.join(" ", command));
        }

        return output.trim();
    }

    /**
     * "Touch" a file. Creates it if it doesn't exist, 
     * or updates the last modified time if it does.
     * Just like `touch` in Unix yay!
     */
    public static void touch(Path path) throws IOException {
        if (Files.exists(path)) {
            Files.setLastModifiedTime(path, FileTime.fromMillis(System.currentTimeMillis()));
        } else {
            writeString(path, "");
        }
    }

    /**
     * Copies a directory and all its contents to a new location.
     * Java's Files.copy does NOT do this recursively by default.
     */
    public static void copyRecursive(Path source, Path target) throws IOException {
        Files.walkFileTree(source, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                Path targetDir = target.resolve(source.relativize(dir));
                if (Files.notExists(targetDir)) {
                    Files.createDirectories(targetDir);
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.copy(file, target.resolve(source.relativize(file)), StandardCopyOption.REPLACE_EXISTING);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    /**
     * "Safely" reads a file's content as a String or returns a default value if it fails.
     */
    public static String readStringSafe(Path path, String defaultValue) {
        try {
            return Files.readString(path, StandardCharsets.UTF_8);
        } catch (IOException e) {
            return defaultValue;
        }
    }
}