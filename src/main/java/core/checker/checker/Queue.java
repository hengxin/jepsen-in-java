package core.checker.checker;

import core.checker.model.Inconsistent;
import core.checker.model.Model;
import core.checker.model.ModelUtil;
import core.checker.util.OpUtil;
import core.checker.vo.Result;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Every dequeue must come from somewhere. Validates queue operations by
 *   assuming every non-failing enqueue succeeded, and only OK dequeues succeeded,
 *   then reducing the model with that history. Every subhistory of every queue
 *   should obey this property. Should probably be used with an unordered queue
 *   model, because we don't look for alternate orderings. O(n).
 */
public class Queue implements Checker {
    private final Model model;

    public Queue(Model model) {
        this.model = model;
    }


    @Override
    public Result check(Map test, List<Operation> history, Map opts) {
        List<Operation> finalHis = history.stream().filter(op -> {
            Operation.F f = op.getF();
            if (f == Operation.F.ENQUEUE) {
                return OpUtil.isInvoke(op);
            } else if (f == Operation.F.DEQUEUE) {
                return OpUtil.isOk(op);
            } else {
                return false;
            }
        }).collect(Collectors.toList());

        Model finalModel = null;
        for (Operation r : finalHis) {
            finalModel = model.step(r);
        }
        Result result = new Result();
        if (ModelUtil.isInconsistent(finalModel)) {
            result.setValid(false);
            Inconsistent inconsistent = (Inconsistent) finalModel;
            result.setError(inconsistent.getMsg());
        } else {
            result.setValid(true);
            result.setFinalQueue(finalModel);
        }
        return result;

    }
}
