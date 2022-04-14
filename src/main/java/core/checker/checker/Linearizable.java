package core.checker.checker;

import core.checker.vo.Result;
import knossos.model.Model;
import lombok.extern.slf4j.Slf4j;
import util.ClojureCaller;

import java.io.File;
import java.util.List;
import java.util.Map;

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
        Map a;
        switch (algorithm.toString()) {
            case "linear":
                a = (Map) ClojureCaller.call("knossos.linear", "analysis", model, history);
                break;
            case "wgl":
                a = (Map) ClojureCaller.call("knossos.wgl", "analysis", model, history);
                break;
            default:
                a = (Map) ClojureCaller.call("knossos.competition", "analysis", model, history);
        }

        if (!(boolean) a.get("valid?")) {
            try {
                File file = (File) ClojureCaller.call("jepsen.store", "path!", test, opts.get("subdirectory"), "linear.svg");
                String path = file.getCanonicalPath();
                ClojureCaller.call("knossos.linear.report", "render-analysis!", history, a, path);
            } catch (Exception e) {
                log.warn(e + " Error rendering linearizability analysis");
            }
        }

        a.put("final-paths", ((List) a.get("final-paths")).subList(0, 10));
        a.put("configs", ((List) a.get("configs")).subList(0, 10));
        return new Result();//TODO
    }
}
