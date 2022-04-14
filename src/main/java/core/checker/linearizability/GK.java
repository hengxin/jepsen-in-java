package core.checker.linearizability;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class GK {
    private final List<Op> history;
    private final List<Op> ops = new ArrayList<>();
    private List<Op> sorted;
    private List<Op> sortByEnd;
    private List<Op> sortByStart;
    private final List<List<Node>> graph = new ArrayList<>();
    private final List<Double> graphEnd = new ArrayList<>();
    private List<Map<Integer, List<Integer>>> edges;
    private boolean linearizable;

    public GK(List<Op> history) {
        History.index(history);
        this.history = history;
        constructOps();
        sort();

    }

    private void constructOps() {
        Map<Integer, Op> sequences = new HashMap<>();
        for (Op op : history) {
            int process = op.getProcess();
            if (sequences.containsKey(process)) {
                if (op.getType().equals("fail")) {
                    sequences.remove(process);
                } else if (op.getType().equals("invoke")) {
                    Op prev = sequences.get(process);
                    prev.setEnd(op.getIndex());
                    ops.add(prev);
                    sequences.remove(process);
                }
            } else {
                sequences.put(process, op);
                op.setStart(op.getIndex());
            }
        }
    }


    private void sort() {
        List<Op> sortByStart = ops.stream().sorted((o1, o2) -> o1.getStart() - o2.getStart() < 0 ? -1 : 0).collect(Collectors.toList());
        List<Op> sortByEnd = ops.stream().sorted((o1, o2) -> o1.getEnd() - o2.getEnd() < 0 ? -1 : 0).collect(Collectors.toList());
        this.sortByStart = sortByStart;
        this.sortByEnd = sortByEnd;
        List<Op> sorted = new ArrayList<>();
        double start;
        double end;
        int i = 0, j = 0;
        while (i < sortByStart.size() && j < sortByEnd.size()) {
            start = sortByStart.get(i).getStart();
            end = sortByEnd.get(j).getEnd();
            if (start <= end) {
                sorted.add(sortByStart.get(i));
                i++;
            } else {
                sorted.add(sortByEnd.get(j));
                j++;
            }
        }

        if (j < sortByEnd.size()) {
            sorted.addAll(sortByEnd.subList(j, sortByEnd.size()));
        }
        double t0 = sorted.get(0).getStart();
        double t8 = sorted.get(sorted.size() - 1).getEnd();
        Op firstWrite = new Op(0, core.checker.checker.Operation.F.WRITE, Integer.MAX_VALUE, t0 / 3, t0 / 2);
        Op lastWrite = new Op(0, core.checker.checker.Operation.F.WRITE, Integer.MAX_VALUE, t8 + 1, t8 + 2);
        this.sorted = new ArrayList<>();
        this.sorted.add(firstWrite);
        this.sorted.addAll(sorted);
        this.sorted.add(lastWrite);
    }

    private void buildGraph() {
        Op firstWrite = sorted.get(0);
        Op lastWrite = sorted.get(sorted.size() - 1);
        Map<Integer, List<Op>> process = new HashMap<>();
        graph.add(List.of(new Node(new ArrayList<>(), firstWrite)));
        graphEnd.add(firstWrite.getEnd());
        for (Op op : sortByEnd) {
            if (process.containsKey(op.getProcess())) {
                process.get(op.getProcess()).add(op);
            } else {
                List<Op> ops = new ArrayList<>(List.of(op));
                process.put(op.getProcess(), ops);
            }
            double end = op.getEnd();
            List<Op> allChoices = new ArrayList<>();
            for (Op cur : sortByStart) {
                if (cur.getStart() <= end && cur.getEnd() >= end) {
                    allChoices.add(cur);
                }
            }
            List<List<Op>> subsets = getSubsets(allChoices);
            List<Node> level = new ArrayList<>();
            for (List<Op> subset : subsets) {
                for (Map.Entry<Integer, List<Op>> entry : process.entrySet()) {
                    int p = entry.getKey();
                    List<Op> ops = entry.getValue();
                    for (int i = ops.size() - 1; i >= 0; i--) {
                        Op cur = ops.get(i);
                        if (!subset.contains(cur) && cur.getStart() <= end) {
                            Node node = new Node(subset, cur);
                            level.add(node);
                        }
                    }
                }

            }
            graph.add(level);
            graphEnd.add(end);

        }
        graph.add(List.of(new Node(new ArrayList<>(), lastWrite)));
        this.edges = new ArrayList<>();

        for (int i = 0; i < graph.size() - 1; i++) {
            List<Node> curLevel = graph.get(i);
            double t1 = graphEnd.get(i);
            List<Node> nextLevel = graph.get(i + 1);
            double t2 = graphEnd.get(i + 1);
            Map<Integer, List<Integer>> curEdges = new HashMap<>();
            for (int j = 0; j < curLevel.size(); j++) {
                for (int k = 0; k < nextLevel.size(); k++) {
                    boolean hasEdge = true;
                    Node n1 = curLevel.get(j);
                    Node n2 = nextLevel.get(k);
                    List<Op> containT2 = new ArrayList<>();
                    for (Op op : sortByEnd) {
                        if (op.getStart() <= t1 && op.getEnd() >= t1 && op.getStart() <= t2 && op.getEnd() >= t2) {
                            if (!n1.getOps().contains(op)) {
                                if (n2.getOps().contains(op)) {
                                    hasEdge = false;
                                    break;
                                }
                            }
                        }
                        if (op.getStart() <= t2 && op.getEnd() >= t2) {
                            if (op.getStart() > t2 || n1.getOps().contains(op)) {
                                if (!n2.getOps().contains(op)) {
                                    containT2.add(op);
                                }
                            }
                        }
                    }

                    if (!hasEdge) continue;

                    for (int t = containT2.size() - 1; t >= 0; t--) {
                        Op a = containT2.get(t);
                        if (a.getF().equals("read")) {
                            if (a.getValue() != n2.getLastWrite().getValue()) {
                                hasEdge = false;
                                Op latestWrite = null;
                                for (int m = t - 1; m >= 0; m--) {
                                    Op prev = containT2.get(m);
                                    if (prev.getF().equals("write")) {
                                        if (latestWrite == null) {
                                            latestWrite = prev;
                                        }
                                        if (latestWrite.getValue() == a.getValue()) {
                                            hasEdge = true;
                                            break;
                                        }
                                        if (prev.getEnd() < latestWrite.getStart()) {
                                            break;
                                        } else {
                                            if (prev.getValue() == a.getValue()) {
                                                hasEdge = true;
                                                break;
                                            }
                                            if (prev.getStart() > latestWrite.getStart()) {
                                                latestWrite = prev;
                                            }
                                        }

                                    }
                                }
                            }
                        }
                    }
                    if (!containT2.contains(n2.getLastWrite())) {
                        if (containT2.stream().noneMatch(c -> c.getF().equals("write"))) {
                            if (!(n2.getLastWrite() == n1.getLastWrite())) {
                                hasEdge = false;
                            }
                        } else {
                            hasEdge = false;
                        }
                    }
                    if (hasEdge) {
                        if (curEdges.containsKey(j)) {
                            curEdges.get(j).add(k);
                        } else {
                            curEdges.put(j, new ArrayList<>(List.of(k)));
                        }

                    }
                }
            }
            edges.add(curEdges);
        }

    }

    public boolean linearizable() {
        Stack<Node> nodes = new Stack<>();
        Stack<Integer> idx = new Stack<>();
        Map<Integer, List<Integer>> visit = new HashMap<>();
        nodes.push(graph.get(0).get(0));
        idx.push(0);
        boolean linearizable = false;
        int i = 0;
        while (i < graph.size() - 1) {
            Node node = nodes.peek();
            int idx_in_level = idx.peek();
            List<Node> cur = graph.get(i);
            Map<Integer, List<Integer>> edge = edges.get(i);
            if (edge.size() == 0) {
                nodes.pop();
                idx.pop();
                i--;
            } else if (!edge.containsKey(idx_in_level)) {
                nodes.pop();
                idx.pop();
                i--;
            } else {
                List<Integer> is = edge.get(idx_in_level);
                for (Integer next : is) {
                    if (visit.getOrDefault(i, new ArrayList<>()).contains(next)) {
                        break;
                    }
                    nodes.push(graph.get(i + 1).get(next));
                    idx.push(next);
                    i++;
                    if (!visit.containsKey(i)) {
                        visit.put(i, new ArrayList<>(List.of(next)));
                    } else {
                        visit.get(i).add(next);
                    }
                }
            }
            if (idx.peek() == graph.size() - 1) {
                linearizable = true;
            }
            if (idx.peek() == 0) {
                break;
            }
        }
        this.linearizable = linearizable;
        return linearizable;
    }

    public static List<List<Op>> getSubsets(List<Op> ops) {
        int n = ops.size();
        List<List<Op>> res = new ArrayList<>();

        // Run a loop from 0 to 2^n
        for (int i = 0; i < (1 << n); i++) {
            List<Op> cur = new ArrayList<>();
            int m = 1; // m is used to check set bit in binary representation.
            for (int j = 0; j < n; j++) {
                if ((i & m) > 0) {
                    cur.add(ops.get(i));
                }
                m = m << 1;
            }

            res.add(cur);
        }
        return res;
    }

    public static void main(String[] args) {
        try {
            File file = new File("C:/Users/Gabri/Desktop/bad-analysis.json");
            String content = FileUtils.readFileToString(file, "UTF-8");
            JSONArray jsonArray = new JSONArray(content);
            String res = "";
            List<Operation> history = new ArrayList<>();
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                Operation op = new Operation((int) jsonObject.get("process"), (String) jsonObject.get("type"), (String) jsonObject.get("f"), (int) jsonObject.get("value"));
                history.add(op);
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
        }

    }
}
