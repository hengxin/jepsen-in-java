package tests.bank;

import core.checker.checker.Checker;
import core.checker.checker.Operation;
import core.checker.util.Perf;
import core.checker.vo.Plot;
import core.checker.vo.Result;
import core.checker.vo.TestInfo;
import lombok.extern.slf4j.Slf4j;
import util.Store;

import java.io.File;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
class BankPlotter implements Checker {

    @Override
    public Result check(Map test, List<Operation> history, Map opts) {
        List<Operation> reads = Bank.okReads(history);
        Map<?, List<Operation>> byNode = Bank.byNode(test, reads);
        Map<Object, List<List<Double>>> totals = new HashMap<>();
        for (Map.Entry<?, List<Operation>> entry : byNode.entrySet()) {
            totals.put(entry.getKey(), Bank.points(entry.getValue()));
        }

        Map<String, String> colors = Perf.ns2Colors(totals.keySet().stream().map(k -> (String) k).collect(Collectors.toList()));
        TestInfo testInfo = new TestInfo((String) test.get("name"), (LocalDateTime) test.get("start-time"));
        String[] args = new String[]{(String) opts.get("subdirectory"), "bank.png"};
        File file = Store.makePathIfNotExists(testInfo, args);
        Path path = file.toPath();
        try {
            path = Path.of(file.getCanonicalPath());

        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
        }

        Plot plot = Perf.plot(path);
        plot.setTitle(test.get("name") + " bank");
        plot.setYLabel("Total of all accounts");

        List<Map<String, Object>> series = new ArrayList<>();
        for (Map.Entry<Object, List<List<Double>>> entry : totals.entrySet()) {
            series.add(new HashMap<>(Map.of(
                    "title", entry.getValue(),
                    "with", "points",
                    "pointtype", 2,
                    "linetype", colors.get(entry.getKey()),
                    "data", entry.getValue()
            )));
        }

        Map<String, Object> res = new HashMap<>(Map.of(
                "preamble", plot,
                "series", series
        ));
        try {
            Perf.withRange(res);
            Perf.withNemeses(res, history, (List) ((Map) test.getOrDefault("plot", new HashMap<>())).getOrDefault("nemeses", new ArrayList<>()));
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
        }

        Perf.plot(res);
        return new Result(true);
    }
}
