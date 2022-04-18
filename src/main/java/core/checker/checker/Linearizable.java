package core.checker.checker;

import core.checker.linearizability.Op;
import core.checker.linearizability.Wgl;
import core.checker.model.Model;
import core.checker.vo.Result;
import core.checker.vo.TestInfo;
import lombok.extern.slf4j.Slf4j;
import util.Store;

import java.io.File;
import java.time.LocalDateTime;
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
                String path = file.getCanonicalPath();
                //                ClojureCaller.call("knossos.linear.report", "render-analysis!", history, a, path);
            } catch (Exception e) {
                log.warn(e + " Error rendering linearizability analysis");
            }
        }

        a.put("final-paths", ((List) a.get("final-paths")).subList(0, 10));
        a.put("configs", ((List) a.get("configs")).subList(0, 10));
        return new Result();//TODO
    }
}
