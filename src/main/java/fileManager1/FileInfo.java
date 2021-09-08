package fileManager1;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileInfo {
    public static final String UP_TOKEN = "[..]";
    private final String filename;
    private final long length;

    public String getFilename() {
        return filename;
    }

    public long getLength() {
        return length;
    }

    public boolean isDirectory() {
        return length == -1L;
    }
    public boolean isUpElement() {
        return length == -2L;
    }

    public FileInfo(String filename, long length) {
        this.filename = filename;
        this.length = length;
    }

    public FileInfo(Path path) {
        try {
            this.filename = path.getFileName().toString();
            if (Files.isDirectory(path)) {
                this.length = -1L;
            } else {

                this.length = Files.size(path);
            }
        } catch (IOException e) {
            throw new RuntimeException("Something wrong with file: " + path.toAbsolutePath());
        }

    }
}
