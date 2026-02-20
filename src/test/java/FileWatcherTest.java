import network.delay.io.FileWatcher;

import java.nio.file.Paths;
import java.util.Scanner;

public class FileWatcherTest {
    public static void main(String[] args) {
        System.out.println("Starting Single File Watcher...");

        // Watch only build.gradle
        try (FileWatcher watcher = new FileWatcher(Paths.get("build.gradle"), (path, event) -> {
            System.out.printf("[%s] %s changed!%n", event, path.getFileName());
        })) {
            watcher.setDebounce(300);
            
            watcher.startAsync();

            System.out.println("Now watching build.gradle. Try saving a change to it!");
            System.out.println("Type 'exit' to stop.");

            Scanner scanner = new Scanner(System.in);
            while (scanner.hasNextLine()) {
                if ("exit".equalsIgnoreCase(scanner.nextLine())) break;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
