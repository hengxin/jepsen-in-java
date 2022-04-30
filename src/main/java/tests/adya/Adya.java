package tests.adya;

import core.checker.checker.Checker;
import core.checker.checker.Operation;
import core.checker.vo.Result;
import tests.Invoke;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

public class Adya {
   public static List<Invoke> g2Gen() {
      AtomicInteger ids = new AtomicInteger(0);
      return List.of(new Invoke(Operation.Type.INVOKE, Operation.F.INSERT, List.of(Optional.empty(), ids.incrementAndGet())), new Invoke(Operation.Type.INVOKE, Operation.F.INSERT, List.of(ids.incrementAndGet(), Optional.empty())));
   }

   class G2Checker implements Checker {

      @Override
      public Result check(Map test, List<Operation> history, Map opts) {
         Map<Object, Integer> m = new HashMap<>();
         for (Operation op : history) {
            if (op.getF() == Operation.F.INSERT) {
               Object k = ((Map<?, ?>) op.getValue()).keySet();
               if (op.getType() == Operation.Type.OK) {
                  m.put(k, m.getOrDefault(k, 0) + 1);
               } else {
                  m.put(k, m.getOrDefault(k, 0));
               }
            }
         }

         long insertCount = m.entrySet().stream().filter(entry -> {
            int cnt = entry.getValue();
            return cnt > 0;
         }).count();

         Map<Object, Integer> illegal = new HashMap<>();
         for (Map.Entry<Object, Integer> entry : m.entrySet()) {
            int cnt = entry.getValue();
            if (1 < cnt) {
               illegal.put(entry.getKey(), cnt);
            }
         }

         Map<String, Object> res = new HashMap<>(Map.of(
                 "valid?", illegal.isEmpty(),
                 "key-count", m.size(),
                 "legal-count", insertCount - illegal.size(),
                 "illegal-count", illegal.size(),
                 "illegal", illegal
         ));

         Result result = new Result();
         result.setValid(res.get("valid?"));
         result.setRes(res);
         return result;
      }
   }


}
