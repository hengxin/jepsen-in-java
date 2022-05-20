package core.checker.util;

import core.checker.checker.Operation;
import core.checker.linearizability.History;
import util.Util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TimeLine {
    public final static int OP_LIMIT = 10000;
    public final static  Double TIME_SCALE = 1e6;
    public final static Double COL_WIDTH = 100d;
    public final static Double GUTTER_WIDTH = 106d;
    public final static  Double HEIGHT = 16d;

    public final static String STYLE_SHEET =
            ".ops        { position: absolute; }\n" +
                    ".op         { position: absolute; padding: 2px; border-radius: 2px; box-shadow: 0 1px 3px rgba(0,0,0,0.12), 0 1px 2px rgba(0,0,0,0.24); transition: all 0.3s cubic-bezier(.25,.8,.25,1); overflow: hidden; }\n" +
                    ".op.invoke  { background: #eeeeee; }\n" +
                    ".op.ok      { background: #6DB6FE; }\n" +
                    ".op.info    { background: #FFAA26; }\n" +
                    ".op.fail    { background: #FEB5DA; }\n" +
                    ".op:target  { box-shadow: 0 14px 28px rgba(0,0,0,0.25), 0 10px 10px rgba(0,0,0,0.22); }\n";


    public static String style(Map m) {
        List<String> names = new ArrayList<>();
        for (Object key : m.keySet()) {
            String name = key.toString() + ":" + m.get(key).toString();
            names.add(name);
        }
        return String.join(";", names);
    }

    public static List pairs(List<Operation> history) {
        return pairs(Map.of(), history);
    }

    public static List pairs(Map invocations, List<Operation> history) {
        List res = new ArrayList();
        for (Operation operation : history) {
            String type = operation.getType().toString();
            if (type.equals("info")) {
                if (invocations.containsKey(operation.getProcess())) {
                    Object invoke = invocations.get(operation.getProcess());
                    List first = List.of(invoke, operation);
                    invocations.remove(operation.getProcess());
                    List rest = pairs(invocations, history);
                    res = new ArrayList(List.of(first));
                    res.addAll(rest);

                } else {
                    res = new ArrayList(List.of(operation));
                    res.addAll(pairs(invocations, history));
                }
            } else if (type.equals("invoke")) {
                assert !invocations.containsKey(operation.getProcess());
                invocations.put(operation.getProcess(), operation);
                res = pairs(invocations, history);
            } else if (type.equals("ok") || type.equals("fail")) {
                assert invocations.containsKey(operation.getProcess());
                List first = List.of(invocations.get(operation.getProcess()), operation);
                invocations.remove(operation.getProcess());
                List rest = pairs(invocations, history);
                res = new ArrayList(List.of(first));
                res.addAll(rest);
            }

        }
        return res;
    }

    public static boolean isNemesis(Map op) {
        return op.get("process").equals("nemesis");
    }

    public static String renderOpExtraKeys(Map op) {
        op.remove("process");
        op.remove("type");
        op.remove("f");
        op.remove("index");
        op.remove("sub-index");
        op.remove("value");
        op.remove("time");
        List<String> extras = new ArrayList<>();
        for (Object key : op.keySet()) {
            String extra = "\n " + key + " " + op.get(key);
            extras.add(extra);
        }
        return String.join("", extras);
    }

    public static String renderOp(Map op) {
        return "Op:\n" +
                "{:process " + op.get("process") +
                "\n :type " + op.get("type") +
                "\n :f " + op.get("f") +
                "\n :index " + op.get("index") +
                renderOpExtraKeys(op) +
                "\n :value " + op.get("value") + "}";
    }

    public static String renderMsg(Map op) {
        return "Msg: " + op.get("value");
    }

    public static String renderError(Map op) {
        return "Err: " + op.get("error");
    }

    public static String renderDuration(Map start, Map stop) {
        double startTime = (double) start.get("time");
        double stopTime = (double) stop.get("time");
        double duration = (long) Util.nanos2ms(stopTime - startTime);
        return "Dur: " + duration + " ms";
    }

    public static String renderWallTime(Map test, Map op) {
        long start = (long) test.get("start-time");
        long opTime = (long) Util.nanos2ms((long) op.get("time"));
        long w = start + opTime;
        return "Wall-clock Time: " + w;
    }

    public static String title(Map test, Map op, Map start, Map stop) {
        String res = "";
        if (isNemesis(op)) {
            res += renderMsg(start);
        }
        if (stop != null) {
            res += renderDuration(start, stop);
        }
        res += "\n" + renderError(op) + "\n" + renderWallTime(test, op) + "\n" + "\n" + renderOp(op);
        return res;
    }

    public static String body(Map op, Map start, Map stop) {
        boolean samePairValues = start.get("value") == stop.get("value");
        String res = op.get("process") + " " + op.get("f") + " ";
        if (!isNemesis(op)) {
            res += start.get("value");
        }
        if (!samePairValues) {
            res += "<br />" + stop.get("value");
        }
        return res;
    }

    public static String pair2div(List<Operation> history, Map test, Map processIndex, Map start, Map stop) {
        Object p = start.get("process");
        Map op;
        if (stop != null) {
            op = stop;
        } else {
            op = start;
        }
        Map<String, Double> s = Map.of(
                "width", COL_WIDTH,
                "left", GUTTER_WIDTH * (double) processIndex.get(p),
                "top", HEIGHT * (double) start.get("sub-index")
        );
        String style = "";
        if (stop.get("type").equals("info")) {
            s.put("height", HEIGHT * (history.size() + 1 - (int) start.get("sub-index")));
        } else if (stop != null) {
            s.put("height", HEIGHT * ((int) stop.get("sub-index") - (int) start.get("sub-index")));
        } else {
            s.put("height", HEIGHT);
        }
        String res = "<a href=" + "\"#i" + op.get("index") + "\">" + "<div class=" + "\"op " + op.get("type") + "\" " +
                "id=\"i" + op.get("index") + "\" " + "style=\"" + style(s) + "\" " + "title=\"" + title(test, op, start, stop) +
                ">" + body(op, start, stop) + "</div></a>";
        return res;

    }

    public static String linkifyTime(String time) {
        return time.replaceAll("-|:", "");
    }

    public static String breadCrumbs(Map test, String historyKey) {
        String filesName = "/files/" + (String) test.get("name");
        String startTime = linkifyTime(test.get("time").toString());
        String indep = "independent";
        String key = historyKey;
        return "<div>" +
                "<a href=\"/\">jepsen</a>" +
                "<a href=\"" + filesName + "\">" + (String) test.get("name") + "</a>" +
                "<a href=\"" + filesName + "/" + startTime + "\">" + startTime + "</a>" +
                "<a href=\"" + filesName + "/" + startTime + "/\">" + indep + "</a>" +
                "<a href=\"" + filesName + "/" + startTime + "/" + indep + "/" + key + "\">" + key + "</a>";

    }

    public static Map<Object, Integer> processIndex(List<Operation> history) {
        List processes = History.processes(history);
        History.sortProcesses(processes);
        Map<Object, Integer> m = new HashMap();
        for (Object p : processes) {
            m.put(p, m.size());
        }
        return m;
    }

    public static void subIndex(List<Operation> history) {
        for (int i = 0; i < history.size(); i++) {
            Operation operation = history.get(i);
            operation.setSubIndex(i);
        }
    }

}
