package core.checker.util;

import core.checker.checker.Operation;
import lombok.extern.slf4j.Slf4j;
import util.ClojureCaller;
import util.Util;

import java.io.File;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
public class Clock {
    public Map history2datasets(List<Operation> history) {
        double finalTime = Util.nanos2secs((double) history.get(0).getTime());
        List<Operation> clockOffsets = history.stream().filter(h -> h.getClockOffsets() != null).collect(Collectors.toList());
        Map<Object, List> series = new HashMap();
        for (Operation operation : clockOffsets) {
            double t = Util.nanos2secs((double) operation.getTime());
            for (List clockOffset : operation.getClockOffsets()) {
                Object node = clockOffset.get(0);
                Object offset = clockOffset.get(1);
                List s = series.getOrDefault(node, new ArrayList());
                s.addAll(List.of(t, offset));
                series.put(node, s);
            }
        }
        Function<Object, Object> seal = (points) -> {
            List p = (List) points;
            ((List) p.get(p.size() - 1)).set(0, finalTime);
            ((List) p.get(p.size() - 1)).add(points);
            return p;
        };
        return Util.mapVals(seal, series);

    }

    public List<String> shortNodeNames(List<String> nodes) {
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

    public void plot(Map test, List<Operation> history, Map opts) {
        if (!history.isEmpty()) {
            try {
                Map<String, Object> dataset = history2datasets(history);
                List<String> nodes = dataset.keySet().stream().sorted().collect(Collectors.toList());
                List<String> nodeNames = shortNodeNames(nodes);
                File file = (File) ClojureCaller.call("jepsen.store", "path!", test, opts.get("subdirectory"), "clock-skew.png");
                String outputPath = file.getCanonicalPath();
                //TODO
                List<Map<String, Object>> series = new ArrayList<>();
                for (int i = 0; i < nodes.size(); i++) {
                    String node = nodes.get(i);
                    String nodeName = nodes.get(i);
                    series.add(Map.of(
                            "title", nodeName,
                            "with", "steps",
                            "data", dataset.get(node)
                    ));
                }
                Map<String, Object> plot = Map.of(
                        "preamble", ClojureCaller.call("jepsen.checker.perf", "preamble", outputPath),
                        "series", series
                );

                if (Perf.hasData(plot)) {
                    Perf.withoutEmptySeries(plot);
                    Perf.withRange(plot);
                    Perf.withNemeses(plot, history, (List) ((Map) test.get("plot")).get("nemeses"));
                    Perf.plot(plot);
                }
            } catch (Exception e) {
                e.printStackTrace();
                log.error(e.getMessage());
            }
        }
    }
}
