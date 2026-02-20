package network.delay.io;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;
import java.util.function.BiConsumer;

import static java.nio.file.StandardWatchEventKinds.*;

public class DirectoryWatcher implements AutoCloseable {

    private final WatchService watchService;
    private final Map<WatchKey, Path> keys;
    private final BiConsumer<Path, FileEvent> callback;
    private final Map<Path, ScheduledFuture<?>> pendingEvents = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "dewpoint-scheduler");
        t.setDaemon(true);
        return t;
    });

    private long debounceMillis = 0;
    private boolean recursive;
    private ExecutorService executor;

    public static DirectoryWatcher create(BiConsumer<Path, FileEvent> callback) throws IOException {
        return new DirectoryWatcher(callback);
    }

    public DirectoryWatcher recursive(boolean recursive) {
        this.recursive = recursive;
        return this;
    }

    public DirectoryWatcher debounce(long millis) {
        this.debounceMillis = millis;
        return this;
    }

    public void start(Path path) throws IOException {
        watch(path, this.recursive);
        startAsync();
    }

    public DirectoryWatcher(BiConsumer<Path, FileEvent> callback) throws IOException {
        this.watchService = FileSystems.getDefault().newWatchService();
        this.keys = new HashMap<>();
        this.callback = callback;
    }

    /**
     * Starts the watch loop in a background daemon thread.
     */
    public void startAsync() {
        if (executor != null && !executor.isShutdown()) {
            return;
        }
        executor = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "dewpoint-directory-watcher");
            t.setDaemon(true);
            return t;
        });
        executor.submit(this::processEvents);
    }

    public void watch(Path dir, boolean recursive) throws IOException {
        this.recursive = recursive;
        if (recursive) {
            registerAll(dir);
        } else {
            register(dir);
        }
    }

    private void register(Path dir) throws IOException {
        WatchKey key = dir.register(watchService, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
        keys.put(key, dir);
    }

    private void registerAll(final Path start) throws IOException {
        Files.walkFileTree(start, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                register(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    /**
     * Starts the watch loop in a blocking manner.
     */
    public void processEvents() {
        try {
            while (true) {
                WatchKey key = watchService.take();
                Path dir = keys.get(key);

                if (dir == null) continue;

                for (WatchEvent<?> event : key.pollEvents()) {
                    WatchEvent.Kind<?> kind = event.kind();

                    if (kind == OVERFLOW) continue;

                    @SuppressWarnings("unchecked")
                    WatchEvent<Path> ev = (WatchEvent<Path>) event;
                    Path name = ev.context();
                    Path child = dir.resolve(name);

                    FileEvent fileEvent = null;
                    if (kind == ENTRY_CREATE) fileEvent = FileEvent.CREATE;
                    else if (kind == ENTRY_MODIFY) fileEvent = FileEvent.MODIFY;
                    else if (kind == ENTRY_DELETE) fileEvent = FileEvent.DELETE;

                    if (fileEvent != null) {
                        handleEvent(child, fileEvent);
                    }

                    if (recursive && kind == ENTRY_CREATE) {
                        try {
                            if (Files.isDirectory(child, LinkOption.NOFOLLOW_LINKS)) {
                                registerAll(child);
                            }
                        } catch (IOException x) {
                            x.printStackTrace();
                        }
                    }
                }

                boolean valid = key.reset();
                if (!valid) {
                    keys.remove(key);
                    if (keys.isEmpty()) break;
                }
            }
        } catch (InterruptedException x) {
            Thread.currentThread().interrupt();
        }
    }

    private void handleEvent(Path path, FileEvent event) {
        if (debounceMillis <= 0) {
            callback.accept(path, event);
            return;
        }

        ScheduledFuture<?> existing = pendingEvents.remove(path);
        if (existing != null) {
            existing.cancel(false);
        }

        ScheduledFuture<?> future = scheduler.schedule(() -> {
            pendingEvents.remove(path);
            callback.accept(path, event);
        }, debounceMillis, TimeUnit.MILLISECONDS);

        pendingEvents.put(path, future);
    }

    @Override
    public void close() throws IOException {
        scheduler.shutdownNow();
        if (executor != null) {
            executor.shutdownNow();
        }
        watchService.close();
    }

    public void setDebounce(long millis) {
        debounceMillis = millis;
    }
}
