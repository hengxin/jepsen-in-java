package util;

import core.checker.vo.TestInfo;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;

@Slf4j
public class Store {
    private static final String BASE_DIR = "store";

    public static File path(TestInfo test) {
        Path path = Path.of(BASE_DIR, test.getName(), test.getStartTime().format(DateTimeFormatter.BASIC_ISO_DATE));
        return path.toFile();
    }

    public static File path(TestInfo testInfo, String[] args) {
        File test = path(testInfo);
        for (int i = 0; i < args.length; i++) {
            if (args[i] == null) {
                args[i] = "";
            }
        }
        Path fullPath = Path.of(test.getPath(), args);
        return fullPath.toFile();
    }

    public static File makePathIfNotExists(TestInfo testInfo, String[] args) {
        File file = path(testInfo, args);

        try {
            file.getParentFile().mkdirs();
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
        }
        return file;
    }
}
