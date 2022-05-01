package core.checker.checker;

import core.checker.linearizability.Op;
import core.checker.linearizability.Report;
import core.checker.linearizability.Wgl;
import core.checker.model.Model;
import core.checker.vo.Result;
import core.checker.vo.TestInfo;
import lombok.extern.slf4j.Slf4j;
import util.Store;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Validates linearizability with Knossos
 */
@Slf4j
public class Linearizable implements Checker {
    private Object algorithm;
    private Model model;

    public Linearizable(Map opts) {
        this.algorithm = opts.get("algorithm");
        Object model = opts.get("model");
        assert model instanceof Model : "The linearizable checker requires a model. It received: " + model + " instead.";
        this.model = (Model) model;
    }

    @Override
    public Result check(Map test, List<Operation> history, Map opts) {
        Map<String, Object> a;
        switch (algorithm.toString()) {
            case "linear":
                a = Wgl.analysis(model, history.stream().map(Op::new).collect(Collectors.toList()));
                break;
            case "wgl":
                a = Wgl.analysis(model, history.stream().map(Op::new).collect(Collectors.toList()));
                break;
            default:
                a = Wgl.analysis(model, history.stream().map(Op::new).collect(Collectors.toList()));
        }

        if (!(boolean) a.get("valid?")) {
            try {
                TestInfo testInfo = new TestInfo((String) test.getOrDefault("name", ""), (LocalDateTime) test.get("start-time"));
                String[] args = new String[]{(String) opts.getOrDefault("subdirectory", ""), "linear.svg"};
                File file = Store.makePathIfNotExists(testInfo, args);
                String path = file.getPath();
                Report.renderAnalysis(history.stream().map(Op::new).collect(Collectors.toList()), a, path);
            } catch (Exception e) {
                log.warn(e + " Error rendering linearizability analysis");
            }
        }

        int size=((HashSet<?>) a.get("final-paths")).size();
        a.put("final-paths", (new ArrayList<>((HashSet<?>) a.get("final-paths"))).subList(0, Math.min(10,size)));
        size=((List) a.getOrDefault("configs",new ArrayList<>())).size();
        a.put("configs", ((List) a.getOrDefault("configs",new ArrayList<>())).subList(0, Math.min(10,size)));
        Result result = new Result(a.get("valid?"));
        result.setRes(a);
        return result;

    }
}
