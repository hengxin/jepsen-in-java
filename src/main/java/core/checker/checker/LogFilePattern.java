package core.checker.checker;

import core.checker.vo.Result;
import core.checker.vo.TestInfo;
import lombok.extern.slf4j.Slf4j;
import util.Store;

import java.io.File;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Takes a PCRE regular expression pattern (as a Pattern or string) and a
 * filename. Checks the store directory for this test, and in each node
 * directory (e.g. n1), examines the given file to see if it contains instances
 * of the pattern. Returns :valid? true if no instances are found, and :valid?
 * false otherwise, along with a :count of the number of matches, and a :matches
 * list of maps, each with the node and matching string from the file.
 */
@Slf4j
public class LogFilePattern implements Checker {
    Object pattern;
    Object filename;

    public LogFilePattern(Object pattern, Object filename) {
        this.pattern = pattern;
        this.filename = filename;
    }

    @Override
    public Result check(Map test, List<Operation> history, Map opts) {
        List matches = ((List<Map>) test.get("nodes")).stream().map(node -> {
                    //TODO shell execute
                    int exit = (int) node.get("exit");
                    String out = (String) node.get("out");
                    Object err = node.get("err");
                    TestInfo testInfo = new TestInfo((String) test.getOrDefault("name", ""), (LocalDateTime) test.getOrDefault("start-time", 0));
                    String[] args = new String[]{node.toString(), filename.toString()};
                    File file = Store.path(testInfo, args);

                    try {
                        file.getCanonicalPath();
                    } catch (Exception e) {
                        log.error(e.getMessage());
                    }
                    switch (exit) {
                        case 0:
                            String[] lines = out.split("\n");
                            return Arrays.stream(lines).map(line -> Map.of(
                                    "node", node,
                                    "line", line));
                        case 1:
                            break;
                        case 2:
                            if (err.equals("No such file")) {
                                log.error(err.toString());
                            }
                            break;
                    }

                    return null;
                }
        ).collect(Collectors.toList());
        Result result = new Result();
        result.setValid(matches.isEmpty());
        result.setCount(matches.size());
        result.setMatches(matches);
        return result;
    }
}
