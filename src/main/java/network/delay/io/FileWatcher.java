package network.delay.io;

import java.io.IOException;
import java.nio.file.Path;
import java.util.function.BiConsumer;

public class FileWatcher implements AutoCloseable {

    private final DirectoryWatcher directoryWatcher;
    private final Path targetFile;

    public FileWatcher(Path file, BiConsumer<Path, FileEvent> callback) throws IOException {
        this.targetFile = file.toAbsolutePath().normalize();
        Path parent = targetFile.getParent();
        
        if (parent == null) {
            throw new IllegalArgumentException("File must have a parent directory: " + file);
        }

        this.directoryWatcher = new DirectoryWatcher((path, event) -> {
            if (path.toAbsolutePath().normalize().equals(targetFile)) {
                callback.accept(path, event);
            }
        });
        
        this.directoryWatcher.watch(parent, false);
    }

    public void setDebounce(long millis) {
        directoryWatcher.setDebounce(millis);
    }

    public void startAsync() {
        directoryWatcher.startAsync();
    }

    public void processEvents() {
        directoryWatcher.processEvents();
    }

    @Override
    public void close() throws IOException {
        directoryWatcher.close();
    }
}