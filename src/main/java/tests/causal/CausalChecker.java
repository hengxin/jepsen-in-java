package tests.causal;

import core.checker.checker.Checker;
import core.checker.checker.Operation;
import core.checker.model.Inconsistent;
import core.checker.model.Model;
import core.checker.util.OpUtil;
import core.checker.vo.Result;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

class CausalChecker implements Checker {
    private Model model;

    CausalChecker(Model model) {
        this.model = model;
    }


    @Override
    public Result check(Map test, List<Operation> history, Map opts) {
        List<Operation> completed = history.stream().filter(OpUtil::isOk).collect(Collectors.toList());
        Model s = model;
        history = completed;
        for (Operation op : history) {
            Model newS = s.step(op);
            if (newS instanceof Inconsistent) {
                Map<String, Object> res = new HashMap<>(Map.of(
                        "valid?", false,
                        "error", ((Inconsistent) newS).getMsg()
                ));
                Result result = new Result(false);
                result.setRes(res);
            }
            s = newS;
        }
        Map<String, Object> res = new HashMap<>(Map.of(
                "valid?", true,
                "model", s
        ));
        Result result = new Result(false);
        result.setRes(res);
        return result;
    }
}