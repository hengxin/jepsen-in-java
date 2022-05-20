package core.checker.linearizability;

import core.checker.checker.Operation;
import lombok.extern.slf4j.Slf4j;

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
    private final Map<Integer, Set<Op>> contain = new HashMap<>();
    private List<Map<Integer, List<Integer>>> edges;
    private boolean linearizable;
    private final Map<Integer, Integer> mapping = new HashMap<>();

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
                //remove failed ops
                if (op.getType() == Operation.Type.FAIL) {
                    sequences.remove(process);
                } else if (op.getType() == Operation.Type.OK) {
                    Op prev = sequences.get(process);
                    prev.setEnd(op.getIndex());
                    prev.setValue(op.getValue());
                    ops.add(prev);
                    sequences.remove(process);
                    mapping.put(prev.getIndex(), op.getIndex());
                    mapping.put(op.getIndex(), prev.getIndex());
                }
            } else if (op.getType() == Operation.Type.INVOKE) {
                sequences.put(process, op);
                op.setStart(op.getIndex());

                for (Map.Entry<Integer, Op> entry : sequences.entrySet()) {
                    Op write = entry.getValue();
                    if (write.getF() == Operation.F.WRITE) {
                        if (contain.containsKey(op.getIndex())) {
                            contain.get(op.getIndex()).add(write);
                        } else {
                            contain.put(op.getIndex(), new HashSet<>(List.of(write)));
                        }
                    }
                }

                if (op.getF() == Operation.F.WRITE) {
                    for (Map.Entry<Integer, Op> entry : sequences.entrySet()) {
                        Op prev = entry.getValue();
                        if (contain.containsKey(prev.getIndex())) {
                            contain.get(prev.getIndex()).add(op);
                        } else {
                            contain.put(prev.getIndex(), new HashSet<>(List.of(op)));

                        }
                    }
                }
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
            sorted.addAll(new ArrayList<>(sortByEnd.subList(j, sortByEnd.size())));
        }
        double t0 = sorted.get(0).getStart();
        double t8 = sorted.get(sorted.size() - 1).getEnd();
        Op firstWrite = new Op(0, core.checker.checker.Operation.F.WRITE, Integer.MAX_VALUE, t0 - 2, t0 - 1);
        Op lastWrite = new Op(0, core.checker.checker.Operation.F.WRITE, Integer.MAX_VALUE, t8 + 1, t8 + 2);
        this.sorted = new ArrayList<>();
        this.sorted.add(firstWrite);
        this.sorted.addAll(sorted);
        this.sorted.add(lastWrite);
        this.sortByStart.add(0, firstWrite);
        this.sortByStart.add(lastWrite);
        this.sortByEnd.add(0, firstWrite);
        this.sortByEnd.add(lastWrite);
    }

    private void buildGraph() {
        Op firstWrite = sorted.get(0);
        Op lastWrite = sorted.get(sorted.size() - 1);
        Map<Integer, List<Op>> process = new HashMap<>();
        //first node
        graph.add(List.of(new Node(new ArrayList<>(), firstWrite)));
        graphEnd.add(firstWrite.getEnd());
        for (Op op : sortByEnd.subList(1, sortByEnd.size() - 1)) {
            if (process.containsKey(op.getProcess())) {
                process.get(op.getProcess()).add(op);
            } else {
                List<Op> ops = new ArrayList<>(List.of(op));
                process.put(op.getProcess(), ops);
            }
            double end = op.getEnd();
            List<Op> allChoices = new ArrayList<>();
            int opIdx = sortByEnd.indexOf(op);
            for (int i = opIdx; i < sortByEnd.size(); i++) {
                Op cur = sortByEnd.get(i);
                if (cur.getStart() <= end && cur.getEnd() > end) {
                    allChoices.add(cur);
                }
            }
            List<List<Op>> subsets = getSubsets(allChoices);
            List<Node> level = new ArrayList<>();

            //search last write
            for (List<Op> subset : subsets) {
                boolean hasLastWrite = false;
                Op knownLastWrite = null;
                //ends before cur op starts
                int idx = this.sortByEnd.indexOf(op);
                for (int i = idx; i >= 0; i--) {
                    Op cur = this.sortByEnd.get(i);
                    if (cur.getF() != Operation.F.WRITE) continue;
                    if (cur.getEnd() > op.getStart()) continue;
                    if (knownLastWrite == null) {
                        knownLastWrite = cur;
                    } else {
                        if (cur.getStart() > knownLastWrite.getStart()) {
                            knownLastWrite = cur;
                        } else if (cur.getEnd() < knownLastWrite.getStart()) break;
                    }
                    hasLastWrite = true;
                    Node node = new Node(subset, cur);
                    level.add(node);

                }

                Set<Op> ws = contain.getOrDefault(op.getIndex(), new HashSet<>());
                for (Op write : ws) {
                    Node node = new Node(subset, write);
                    level.add(node);
                }
                if (ws.size() > 0) hasLastWrite = true;

                if (!hasLastWrite) {
                    Node node = new Node(subset, firstWrite);
                    level.add(node);
                }

            }
            graph.add(level);
            graphEnd.add(end);

        }
        graph.add(List.of(new Node(new ArrayList<>(), lastWrite)));
        graphEnd.add(lastWrite.getEnd());
        this.edges = new ArrayList<>();

        for (int i = 0; i < graph.size() - 2; i++) {
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
                            if (op.getStart() > t1 || n1.getOps().contains(op)) {
                                if (!n2.getOps().contains(op)) {
                                    containT2.add(op);
                                }
                            }
                        }
                    }

                    if (!hasEdge) continue;

                    for (int t = containT2.size() - 1; t >= 0; t--) {
                        Op a = containT2.get(t);
                        if (a.getF() == Operation.F.READ) {
                            if (n1.getLastWrite() != null && a.getValue() != n1.getLastWrite().getValue()) {
                                hasEdge = false;
                                for (Op prev : containT2) {
                                    if (prev.getF() == Operation.F.WRITE) {
                                        if (prev.getValue() == a.getValue()) {
                                            hasEdge = true;
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                        if (!hasEdge) {
                            break;
                        }
                    }
                    if (!containT2.contains(n2.getLastWrite())) {
                        if (containT2.stream().noneMatch(c -> c.getF() == Operation.F.WRITE)) {
                            if (!n2.getLastWrite().equals(n1.getLastWrite())) {
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
        int len = graph.get(graph.size() - 2).size();
        Map<Integer, List<Integer>> lastEdge = new HashMap<>();
        for (int i = 0; i < len; i++) {
            lastEdge.put(i, List.of(0));
        }
        edges.add(lastEdge);
    }

    public boolean linearizable() {
        buildGraph();
        Stack<Node> nodes = new Stack<>();
        Stack<Integer> idx = new Stack<>();
        Map<Integer, Map<Integer, List<Integer>>> visit = new HashMap<>();
        nodes.push(graph.get(0).get(0));
        idx.push(0);
        int max_size = 0;
        boolean linearizable = false;
        int i = 0; //level
        while (i < graph.size() - 1) {
            Node node = nodes.peek();
            int idx_in_level = idx.peek();
            List<Node> cur = graph.get(i);
            Map<Integer, List<Integer>> edge = edges.get(i);
            //no edge to next level
            if (!edge.containsKey(idx_in_level)) {
                nodes.pop();
                idx.pop();
                i--;
            } else {
                List<Integer> is = edge.get(idx_in_level);
                boolean findNext = false;
                if (!visit.containsKey(i)) {
                    visit.put(i, new HashMap<>());
                }
                Map<Integer, List<Integer>> currentVisit = visit.getOrDefault(i, new HashMap<>());
                for (Integer next : is) {
                    if (currentVisit.getOrDefault(idx_in_level, new ArrayList<>()).contains(next)) {  //already visited
                        continue;
                    }
                    // visit next node on next level

                    nodes.push(graph.get(i + 1).get(next));
                    idx.push(next);
                    if (!currentVisit.containsKey(idx_in_level)) {
                        currentVisit.put(idx_in_level, new ArrayList<>(List.of(next)));
                    } else {
                        currentVisit.get(idx_in_level).add(next);
                    }
                    i++;
                    findNext = true;
                    break;
                }

                if (!findNext) {
                    nodes.pop();
                    idx.pop();
                    i--;
                }
            }
            if (idx.size() == graph.size()) {
                linearizable = true;
                break;
            }
            if (idx.size() == 0) {
                break;
            }
            max_size = Math.max(idx.size(), max_size);
        }
        System.out.println("max size " + max_size);
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
                    cur.add(ops.get(j));
                }
                m = m << 1;
            }

            res.add(cur);
        }
        return res;
    }

    public static List<List<Op>> getSubsetsByEnd(List<Op> ops, double end) {
        int n = ops.size();
        Set<List<Op>> res = new HashSet<>();

        // Run a loop from 0 to 2^n
        for (int i = 0; i < (1 << n); i++) {
            List<Op> cur = new ArrayList<>();
            int m = 1; // m is used to check set bit in binary representation.
            for (int j = 0; j < n; j++) {
                if ((i & m) > 0) {
                    Op op = ops.get(j);
                    if (op.getEnd() > end) {
                        cur.add(ops.get(j));
                    }

                }
                m = m << 1;
            }

            res.add(cur);
        }
        return new ArrayList<>(res);
    }

    //    public static void main(String[] args) {
    //        try {
    //            File file = new File("C:/Users/Gabri/Desktop/bad-analysis.json");
    //            String content = FileUtils.readFileToString(file, "UTF-8");
    //            JSONArray jsonArray = new JSONArray(content);
    //            String res = "";
    //            List<GKOp> history = new ArrayList<>();
    //            for (int i = 0; i < jsonArray.length(); i++) {
    //                JSONObject jsonObject = jsonArray.getJSONObject(i);
    //                Operation op = new Operation((int) jsonObject.get("process"), jsonObject.get("type"), (String) jsonObject.get("f"), (int) jsonObject.get("value"));
    //                history.add(op);
    //            }
    //            GK gk=new GK(history);
    //
    //
    //        } catch (Exception e) {
    //            e.printStackTrace();
    //            log.error(e.getMessage());
    //        }

    //    }
}
