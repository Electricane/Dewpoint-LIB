import java.nio.file.Paths;
import java.util.Scanner;

public class DirectoryWatcher {
    public static void main(String[] args) {
        System.out.println("Starting Async Dir-Watcher...");

        try (network.delay.io.DirectoryWatcher watcher = new network.delay.io.DirectoryWatcher((path, event) -> {
            System.out.printf("[%s] %s%n", event, path.getFileName());
        })) {
            watcher.watch(Paths.get("."), true);
            watcher.startAsync();

            System.out.println("Watcher started. Type 'exit' to stop.");
            Scanner scanner = new Scanner(System.in);
            while (scanner.hasNextLine()) {
                if ("exit".equalsIgnoreCase(scanner.nextLine())) break;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
