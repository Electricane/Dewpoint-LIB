# Dewpoint-LIB

This is a collection of utilities that I use in my Raspberry-PI or some Android projects.


## Some Examples

### File Watching
```java
DirectoryWatcher.create((path, event) -> System.out.println(event + " on " + path))
    .recursive(true)
    .debounce(300)
    .start(Paths.get("./src"));
```


### Specific File Watching
```java
new FileWatcher(Paths.get("config.json"), (file, event) -> reloadConfig())
    .startAsync();
```


### Recursive Delete
```java
DewIO.deleteRecursive(Paths.get("temp_folder"));
```


### Moving Files
```java
DewIO.move(Paths.get("old.txt"), Paths.get("new/folder/target.txt"));
```


### Write with Parents
```java
DewIO.writeString(Paths.get("logs/2026/app.log"), "Starting session...");
```


### String Read
```java
String config = DewIO.readStringSafe(Paths.get("settings.json"), "{}");
```


### Directory Size
```java
long totalBytes = DewIO.getDirectorySize(Paths.get("build"));
```


### Recursive Copy
```java
DewIO.copyRecursive(Paths.get("src"), Paths.get("backup/src"));
```


### Find Free Port
```java
int port = DewNet.findFreePort();
```


### Reachability Check
```java
boolean isOnline = DewNet.isReachable("localhost", 8080, 500);
```


### Read URL Content
```java
String html = DewNet.readUrl("https://iamsurethis.doesntexist");
```


### Get Hostname
```java
String nodeName = DewNet.getHostname();
```


### Environment Strings
```java
String apiKey = DewSystem.getEnv("API_KEY", "DEMO_KEY");
```


### Environment Integers
```java
int maxThreads = DewSystem.getEnvInt("MAX_THREADS", 4);
```


### Environment Booleans
```java
boolean isDebug = DewSystem.getEnvBool("DEBUG_MODE", false);
```


### OS Check
```java
if (DewSystem.isWindows()) {
    System.out.println("Running on Windows!");
}
```


### Get Process ID
```java
long pid = DewSystem.getPid();
```


### Uptime
```java
String uptime = DewSystem.getUptimeString(); // Returns your Uptime in a format like this "1h 20m 5s"
```


### Readable File Size
```java
String readable = DewText.formatSize(2500000000L); // Returns "2.3 GB"
```


### Get File Extension
```java
String ext = DewIO.getExtension(Paths.get("data.tar.gz")); // Returns "gz"
```


### Touch File
```java
DewIO.touch(Paths.get("heartbeat.lock"));
```
