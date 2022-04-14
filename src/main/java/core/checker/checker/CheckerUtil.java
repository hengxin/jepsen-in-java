package core.checker.checker;

import core.checker.util.OpUtil;
import core.checker.vo.Result;
import core.checker.model.Model;
import lombok.extern.slf4j.Slf4j;
import util.ClojureCaller;
import util.Util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class CheckerUtil {
    static Map<Object, Float> validPriorities = Map.of(true, 0f, false, 1f, "unknown", 0.5f);

    /**
     * @param valids valid values
     * @return the one with the highest priority
     * Merge n :valid values, yielding the one with the highest priority.
     */
    public static Object mergeValid(List<Object> valids) {
        return valids.stream().reduce(true, (v1, v2) -> {
            float p1 = Optional.ofNullable(validPriorities.get(v1)).orElseThrow(() -> new IllegalArgumentException(v1 + " is not a known valid? value"));
            float p2 = Optional.ofNullable(validPriorities.get(v2)).orElseThrow(() -> new IllegalArgumentException(v2 + " is not a known valid? value"));
            return p1 < p2 ? v2 : v1;
        });
    }

    /**
     * An empty checker that only returns nil.
     */
    public Checker noop() {
        return new Noop();
    }

    public Result checkSafe(Checker checker, Map test, List<Operation> history) {
        return checkSafe(checker, test, history, Map.of());
    }

    /**
     * Like check, but wraps exceptions up and returns them as a map
     */
    public Result checkSafe(Checker checker, Map test, List<Operation> history, Map opts) {
        try {
            return checker.check(test, history, opts);
        } catch (Exception e) {
            log.warn("Error while checking history: " + e);
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            Result result = new Result();
            result.setValid("unknown");
            result.setError(sw.toString());
            return result;
        }
    }

    /**
     * @param checkerMap a map of names to checkers
     */
    public static Checker compose(Map<String, Checker> checkerMap) {
        return new Compose(checkerMap);
    }

    /**
     * Puts an upper bound on the number of concurrent executions of this checker
     */
    public Checker concurrencyLimit(int limit, Checker checker) {
        return new ConcurrencyLimit(limit, checker);
    }

    /**
     * everything is awesome
     */
    public Checker unbridledOptimism() {
        return new UnbridledOptimism();
    }

    /**
     * @return information about unhandled exceptions: a sequence of maps sorted in
     * descending frequency order
     */
    public Checker unhandledExceptions() {
        return new UnhandledExceptions();
    }

    /**
     * @return a map of statistics
     * Helper for computing stats
     */
    public static Map<String, Object> statsHelper(List<Operation> history) {
        long okCount = history.stream().filter(OpUtil::isOk).count();
        long failCount = history.stream().filter(OpUtil::isFail).count();
        long infoCount = history.stream().filter(OpUtil::isInfo).count();
        return new HashMap<>(Map.of(
                "valid?", okCount > 0,
                "count", okCount + failCount + infoCount,
                "ok-count", okCount,
                "fail-count", failCount,
                "info-count", infoCount));
    }

    /**
     * Computes basic statistics about success and failure rates, both overall and
     * broken down by :f
     */
    public Checker stats() {
        return new Stats();
    }

    /**
     * @param opts {:model (model/cas-register)
     *             :algorithm :wgl}
     *             Validates linearizability with Knossos
     */
    public static Checker linearizable(Map opts) {
        return new Linearizable(opts);
    }


    /**
     * Validates queue operations by
     * assuming every non-failing enqueue succeeded, and only OK dequeues succeeded,
     * then reducing the model with that history
     */
    public Checker queue(Model model) {
        return new Queue(model);
    }

    /**
     * verifies that
     * every successfully added element is present in the read, and that the read
     * contains only elements for which an add was attempted
     */
    public Checker set() {
        return new SetChecker();
    }

    public static SetFullElement setFullElement(Operation operation) {
        return new SetFullElement(operation.getValue());
    }

    /**
     * @param e SetFullElement
     * @return a map of final results
     */
    public static Map setFullElementResults(SetFullElement e) {
        Object known = e.known;
        long knownTime = (long) ((Map) e.known).get("time");
        Map lastPresent = e.lastPresent;
        Map lastAbsent = e.lastAbsent;

        boolean stable = lastAbsent != null && ((int) lastAbsent.get("index") - 1 < (int) lastPresent.get("index"));
        boolean lost = known != null && lastAbsent != null && ((int) lastPresent.get("index") - 1 < (int) lastAbsent.get("index")) && ((int) ((Map) e.known).get("index") < (int) lastAbsent.get("index"));
        boolean neverRead = !(stable || lost);
        //  TODO 0 is not really right
        long stableTime = 0;
        if (stable) {
            if (lastAbsent != null) {
                stableTime = (long) lastAbsent.get("time") + 1;
            } else {
                stableTime = 0;
            }
        }

        long lostTime = 0;
        if (lost) {
            if (lastPresent != null) {
                lostTime = (long) lastPresent.get("time") + 1;
            } else {
                lostTime = 0;
            }
        }

        double stableLatency = 0;
        if (stable) {
            stableLatency = Util.nanos2ms(Math.max(knownTime - stableTime, 0));
        }

        double lostLatency = 0;
        if (lost) {
            lostLatency = Util.nanos2ms(Math.max(knownTime - lostTime, 0));
        }

        String outcome;
        if (stable) {
            outcome = "stable";
        } else if (lost) {
            outcome = "lost";
        } else {
            outcome = "neverRead";
        }
        return Map.of(
                "element", e.element,
                "outcome", outcome,
                "stable-latency", stableLatency,
                "lost-latency", lostLatency,
                "known", known,
                "last-absent", lastAbsent
        );

    }

    /**
     * @param points percentiles
     * @param c      collection of numbers
     */
    public static Map<Double, Double> frequencyDistribution(List<Double> points, List<Double> c) {
        if (c == null || c.isEmpty()) {
            return Map.of();
        }
        List<Double> sorted = c.stream().sorted(Comparator.naturalOrder()).collect(Collectors.toList());
        int n = sorted.size();
        List<Double> number = points.stream()
                .map(point -> sorted.get((int) Math.min(n - 1, Math.floor(n * point)))).collect(Collectors.toList());
        Map<Double, Double> res = new TreeMap<>();
        for (int i = 0; i < number.size(); i++) {
            res.put(points.get(i), number.get(i));
        }
        return res;
    }

    /**
     * @param opts     options from set-full
     * @param elements collection of SetFullElements
     */
    public static Map setFullResults(Map opts, List<SetFullElement> elements) {
        List<Map> rs = new ArrayList<>();
        for (SetFullElement element : elements) {
            rs.add(setFullElementResults(element));
        }

        Map<Object, List<Map>> outcomes = rs.stream().collect(Collectors.groupingBy(r -> r.get("outcome")));

        List<Map> stale = outcomes.get("stable").stream().filter(o -> (long) o.get("stable-latency") > 0).collect(Collectors.toList());
        List<Map> worstStale = stale.stream().sorted((s1, s2) -> (int) ((long) s2.get("stable-latency") - (long) s1.get("stable-latency"))).collect(Collectors.toList()).subList(0, 8);
        List<Double> stableLatencies = rs.stream().map(r -> (double) r.get("stable-latency")).collect(Collectors.toList());
        List<Double> lostLatencies = rs.stream().map(r -> (double) r.get("lost-latency")).collect(Collectors.toList());
        Object valid;
        if (outcomes.get("lost").size() > 0) {
            valid = false;
        } else if (outcomes.get("stable").size() == 0) {
            valid = "unknown";
        } else if ((boolean) opts.get("linearizable?") && stale.size() > 0) {
            valid = false;
        } else {
            valid = true;
        }
        Map m = Map.of(
                "valid?", valid,
                "attempt-count", rs.size(),
                "stable-count", outcomes.get("stable").size(),
                "lost-count", outcomes.get("lost").size(),
                "lost", outcomes.get("lost").stream().map(l -> l.get("element")).sorted().collect(Collectors.toList()),
                "never-read-count", outcomes.get("never-read").size(),
                "never-read", outcomes.get("never-read").stream().map(n -> n.get("element")).sorted().collect(Collectors.toList()),
                "stale-count", stale.size(),
                "stale", stale.stream().map(s -> s.get("element")).sorted().collect(Collectors.toList()),
                "worst-stale", worstStale
        );

        List<Double> points = List.of(0d, 0.5, 0.95, 0.99, 1d);
        m.put("stable-latencies", frequencyDistribution(points, stableLatencies));
        m.put("lost-latencies", frequencyDistribution(points, lostLatencies));
        return m;
    }

    /**
     * A more rigorous set analysis
     */
    public static Checker setFull(Map checkerOpts) {
        return new SetFull(checkerOpts);
    }


    /**
     * Takes a history. Looks for :drain operations with their value being a
     * collection of queue elements, and expands them to a sequence of :dequeue
     * invoke/complete pairs.
     */
    public static List<Operation> expandQueueDrainOps(List<Operation> history) {
        List<Operation> newHis = new ArrayList<>();
        for (Operation operation : history) {
            if (!(operation.getF() == Operation.F.DRAIN)) {
                newHis.add(operation);
            } else if (OpUtil.isInvoke(operation)) {
            } else if (OpUtil.isFail(operation)) {
            } else if (OpUtil.isOk(operation)) {
                List<?> valList;
                Object value = operation.getValue();
                if (value instanceof List) {
                    valList = (List<?>) value;
                } else {
                    valList = new ArrayList<>(List.of(value));
                }
                for(Object v:valList){
                    List<Operation> operations = new ArrayList<>();
                    Operation operation1 = new Operation(operation), operation2 = new Operation(operation);
                    operation1.setType(Operation.Type.INVOKE);
                    operation1.setF(Operation.F.DEQUEUE);
                    operation1.setValue(null);
                    operations.add(operation1);
                    operation2.setType(Operation.Type.OK);
                    operation2.setF(Operation.F.DEQUEUE);
                    operation2.setValue(v);
                    operations.add(operation2);
                    newHis.addAll(operations);
                }
            } else {
                throw new IllegalStateException("Not sure how to handle a crashed drain operation: " + operation);
            }
        }
        return newHis;
    }

    /**
     * Verifies that every successful enqueue has a
     * successful dequeue
     */
    public static Checker totalQueue() {
        return new TotalQueue();
    }

    /**
     * Checks that a unique id generator actually emits unique IDs
     */
    public static Checker uniqueIds() {
        return new UniqueIds();
    }

    /**
     * @return This checker validates that at
     * each read, the value is greater than the sum of all
     */
    public static Checker counter() {
        return new Counter();
    }

    /**
     * Spits out graphs of latencies. Checker options take precedence over
     * those passed in with this constructor.
     */
    public static Checker latencyGraph(Map opts) {
        return new LatencyGraph(opts);
    }

    /**
     * Spits out graphs of throughput over time. Checker options take precedence
     * over those passed in with this constructor.
     */
    public static Checker rateGraph(Map opts) {
        return new RateGraph(opts);
    }

    /**
     * Composes various performance statistics. Checker options take precedence over
     * those passed in with this constructor.
     */
    public static Checker perf(Map opts) {
        return compose(Map.of(
                "latency-graph", new LatencyGraph(opts),
                "rate-graph", new RateGraph(opts)
        ));
    }

    /**
     * Plots clock offsets on all nodes"
     */
    public static Checker clockPlot() {
        return new ClockPlot();
    }

    /**
     * Checks the store directory for this test, and in each node
     *   directory (e.g. n1), examines the given file to see if it contains instances
     *   of the pattern.
     */
    public Checker logFilePattern(Object pattern, Object filename) {
        return new LogFilePattern(pattern, filename);

    }

    public static void main(String[] args) {
        //        List<Object> valids = List.of("unknown");
        //        Map map = new PersistentArrayMap(new Object[]{Keyword.intern(Symbol.create("except")), 1,
        //                Keyword.intern(Symbol.create("context")), 2});
        //        LazySeq res1 = (LazySeq) ClojureCaller.call("clojure.core", "filter", Keyword.intern("except"), map);
        Object res1 = ClojureCaller.call("clojure.core", "if-not", true, false, 2, 3);
        log.info(res1.toString());
        Map<String, Object> te = Map.of("1", false, "2", "233");
    }
}
