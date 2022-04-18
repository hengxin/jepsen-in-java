package core.checker.util;

import core.checker.checker.Operation;
import core.checker.vo.Plot;
import core.checker.vo.TestInfo;
import lombok.extern.slf4j.Slf4j;
import util.Store;
import util.Util;

import java.io.File;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class Clock {
    public static Map<String, List<List<Double>>> history2datasets(List<Operation> history) {
        double finalTime = Util.nanos2secs(history.get(0).getTime());
        List<Operation> clockOffsets = history.stream().filter(h -> h.getClockOffsets() != null).collect(Collectors.toList());
        Map<String, List<List<Double>>> series = new HashMap<>();
        for (Operation operation : clockOffsets) {
            double t = Util.nanos2secs(operation.getTime());
            Map<String, Double> clockOffset = operation.getClockOffsets();
            for (Map.Entry<String, Double> entry : clockOffset.entrySet()) {
                String node = entry.getKey();
                Double offset = entry.getValue();
                List<List<Double>> s = series.getOrDefault(node, new ArrayList<>());
                s.add(List.of(t, offset));
                series.put(node, s);
            }
        }
        for (Map.Entry<String, List<List<Double>>> entry : series.entrySet()) {
            List<List<Double>> points = entry.getValue();
            List<Double> last = new ArrayList<>(points.get(points.size() - 1));
            last.set(0, finalTime);
            points.add(last);
        }

        return series;

    }

    public static List<String> shortNodeNames(List<String> nodes) {
        List<List<String>> reversed = nodes.stream().map(n -> n.split("\\.")).map(n -> {
                    List<String> tmp = new ArrayList<>(List.of(n));
                    Collections.reverse(tmp);
                    return tmp;
                }
        ).collect(Collectors.toList());
        List<List<String>> dropped = Util.dropCommonProperPrefix(reversed);
        List<String> res = dropped.stream().peek(Collections::reverse).map(d -> String.join(".", d)
        ).collect(Collectors.toList());

        return res;

    }

    public static void plot(Map test, List<Operation> history, Map opts) {
        if (!history.isEmpty()) {
            try {
                Map<String, List<List<Double>>> dataset = history2datasets(history);
                List<String> nodes = dataset.keySet().stream().sorted().collect(Collectors.toList());
                List<String> nodeNames = shortNodeNames(nodes);
                TestInfo testInfo = new TestInfo((String) test.get("name"), LocalDateTime.ofEpochSecond((int) test.get("start-time") / 1000, 0, ZoneOffset.ofHours(8)));
                String subdirectory = (String) opts.getOrDefault("subdirectory", "");
                String[] args = new String[]{subdirectory, "clock-skew.png"};
                File file = Store.makePathIfNotExists(testInfo, args);
                String outputPath = file.getCanonicalPath();
                //TODO
                List<Map<String, Object>> series = new ArrayList<>();
                for (int i = 0; i < nodes.size(); i++) {
                    String node = nodes.get(i);
                    String nodeName = nodes.get(i);
                    series.add(new HashMap<>(Map.of(
                            "title", nodeName,
                            "with", "steps",
                            "data", dataset.get(node)
                    )));
                }
                Plot clockPlot=Perf.plot(Path.of(outputPath));
                clockPlot.setLine(true);
                Map<String, Object> plot = new HashMap<>(Map.of(
                        "preamble", clockPlot,
                        "series", series
                ));

                if (Perf.hasData(plot)) {
                    Perf.withoutEmptySeries(plot);
                    Perf.withRange(plot);
                    List<?> nemeses=new ArrayList<>();
                    if (opts.containsKey("nemeses")) {
                        nemeses = (List) opts.get("nemeses");
                    } else {
                        if (test.containsKey("plot")) {
                            if (((Map) test.get("plot")).containsKey("nemeses")) {
                                nemeses = (List) ((Map) test.get("plot")).get("nemeses");
                            }
                        }
                    }
                    Perf.withNemeses(plot, history, nemeses);
                    Perf.plot(plot);
                }
            } catch (Exception e) {
                e.printStackTrace();
                log.error(e.getMessage());
            }
        }
    }
}
