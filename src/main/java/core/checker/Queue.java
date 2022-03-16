package core.checker;

import knossos.model.Model;
import util.ClojureCaller;

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
    private Model model;

    public Queue(Model model) {
        this.model = model;
    }


    @Override
    public Map check(Map test, List<Map> history, Map opts) {
        List<Map> finalHis = history.stream().filter(op -> {
            Object f = op.get("f");
            if (f.equals("enqueue")) {
                return Op.isInvoke(op);
            } else if (f.equals("dequeue")) {
                return Op.isOk(op);
            } else {
                return false;
            }
        }).collect(Collectors.toList());

        Object finalModel = null;
        for (Map r : finalHis) {
            finalModel = model.step(r);
        }
        if ((boolean) ClojureCaller.call("knossos.model", "inconsistent?", finalModel)) {
            return Map.of(
                    "valid?", false,
                    "error", ((Map) finalModel).get("msg"));
        } else {
            return Map.of(
                    "valid?", true,
                    "final-queue", finalModel
            );
        }

    }
}
