package core.checker.linearizability;

import core.checker.checker.Operation;
import core.checker.model.Inconsistent;
import core.checker.model.Model;
import core.checker.util.OpUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.batik.anim.dom.SVGDOMImplementation;
import org.w3c.dom.CDATASection;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class Report {
    static double minStep = 1.0 / 10.0;
    static double hscale = 150.0;
    static String svgNS = "linearizability";
    static double processHeight = 0.4;
    static double legendHeight = processHeight * 0.6;
    static Map<Operation.Type, String> type2color = Map.of(Operation.Type.OK, "#6DB6FE", Operation.Type.INFO, "#FFAA26", Operation.Type.FAIL, "#FEB5DA");
    static String font = "'Helvetica Neue', Helvetica, sans-serif";
    static double strokeWidth = vscale(0.05);
    static double faded = 0.15;
    static String activateScript = "\n" +
            "function abar(id) {\n" +
            "  var bar = document.getElementById(id);\n" +
            "  bar.setAttribute('opacity', '1.0');\n" +
            "  var model = bar.getElementsByClassName('model')[0];\n" +
            "  if (model != undefined) {\n" +
            "    model.setAttribute('opacity', '1.0');\n" +
            "  }\n" +
            "}\n" +
            "\n" +
            "function dbar(id) {\n" +
            "  var bar = document.getElementById(id);\n" +
            "  bar.setAttribute('opacity', '" + faded + "');\n" +
            "  var model = bar.getElementsByClassName('model')[0];\n" +
            "  if (model != undefined) {\n" +
            "    model.setAttribute('opacity', '0.0');\n" +
            "  }\n" +
            "}\n";

    public static List<Op> ops(Map<String, Object> analysis) {
        HashSet<?> finalPaths = (HashSet<?>) analysis.get("final-paths");
        Iterator<?> iterator = finalPaths.iterator();
        List<Op> res = new ArrayList<>();
        while (iterator.hasNext()) {
            List<?> path = (List<?>) iterator.next();
            List<Op> ops = path.stream().map(p -> new Op((Op) ((Map<?, ?>) p).get("op"))).collect(Collectors.toList());
            res.addAll(ops);
        }
        res = res.stream().distinct().collect(Collectors.toList());
        return res;
    }

    public static List<Model> models(Map<String, Object> analysis) {
        HashSet<?> finalPaths = (HashSet<?>) analysis.get("final-paths");
        Iterator<?> iterator = finalPaths.iterator();
        List<Model> res = new ArrayList<>();
        while (iterator.hasNext()) {
            List<?> path = (List<?>) iterator.next();
            List<Model> models = path.stream().map(p -> (Model) ((Map<?, ?>) p).get("model")).collect(Collectors.toList());
            res.addAll(models);
        }
        res = res.stream().distinct().collect(Collectors.toList());
        return res;
    }

    public static Map<Model, Integer> modelNumbers(List<Model> models) {
        Map<Model, Integer> numbers = new HashMap<>();
        for (int i = 0; i < models.size(); i++) {
            Model model = models.get(i);
            numbers.put(model, i);
        }
        return numbers;
    }

    public static String transitionColor(Map<?, ?> transition) {
        if (transition.getOrDefault("model", null) instanceof Inconsistent) {
            return "#FF1E90";
        } else {
            return "#000000";
        }
    }


    public static Map<Integer, Integer> processCoords(List<Op> ops) {
        List<Integer> process = History.processes(ops);
        History.sortProcesses(process);
        Map<Integer, Integer> processCoords = new HashMap<>();
        for (int i = 0; i < process.size(); i++) {
            Integer p = process.get(i);
            processCoords.put(p, i);
        }
        return processCoords;
    }


    public static List<Integer> timeBounds(Map<Op, Op> pairIndex, Map<String, Object> analysis) {
        Op op = History.invocation(pairIndex, (Op) analysis.get("previous-ok"));
        int min;
        if (op == null) {
            min = 1;
        } else {
            min = op.getIndex();
        }
        min -= 1;
        HashSet<?> finalPaths = (HashSet<?>) analysis.get("final-paths");
        Iterator<?> iterator = finalPaths.iterator();
        List<Integer> indices = new ArrayList<>();
        while (iterator.hasNext()) {
            List<?> path = (List<?>) iterator.next();
            for (Object p : path) {
                Map<?, ?> transition = (Map<?, ?>) p;
                Op c = History.completion(pairIndex, (Op) transition.get("op"));
                if (!OpUtil.isInfo(c)) {
                    indices.add(c.getIndex());
                }
            }
        }

        int max = indices.stream().reduce(0, Math::max) + 1;
        return List.of(min, max);
    }

    public static void condenseTimeCoords(Map<Integer, List<Integer>> coords) {
        List<List<Integer>> vals = new ArrayList<>(coords.values());
        SortedSet<Integer> times = new TreeSet<>();
        for (List<Integer> v : vals) {
            times.addAll(v);
        }
        Map<Integer, Integer> indexed = new HashMap<>();
        Iterator<Integer> iterator = times.iterator();
        for (int i = 0; i < times.size(); i++) {
            indexed.put(iterator.next(), i);
        }
        for (Map.Entry<Integer, List<Integer>> entry : coords.entrySet()) {
            List<Integer> t = entry.getValue();
            int t1 = t.get(0);
            int t2 = t.get(1);
            t.set(0, indexed.get(t1));
            t.set(1, indexed.get(t2));
        }
    }

    public static Map<Integer, List<Integer>> timeCoords(Map<Op, Op> pairIndex, List<Integer> bounds, List<Op> ops) {
        Map<Integer, List<Integer>> timeCoords = new HashMap<>();
        for (Op op : ops) {
            int i = op.getIndex();
            assert i >= 0 : "Expected index but got " + i + " for op " + op;
            Op inv = History.invocation(pairIndex, op);
            assert inv != null : "No invocation for op " + op;
            int t1 = Math.max(bounds.get(0), inv.getIndex());
            Op completion = History.completion(pairIndex, op);
            int t2;
            if (OpUtil.isInfo(completion)) {
                t2 = bounds.get(1);
            } else {
                t2 = completion.getIndex();
            }
            timeCoords.put(i, new ArrayList<>(List.of(t1 - bounds.get(0), t2 - bounds.get(0))));
        }
        condenseTimeCoords(timeCoords);
        return timeCoords;
    }

    public static void pathBounds(Map<Integer, List<Integer>> timeCoords, Map<Integer, Integer> processCoords, Map<Integer, Integer> external2Internal, List<?> path) {
        for (Object p : path) {
            Map<Object, Object> transition = (Map<Object, Object>) p;
            Op op = (Op) transition.get("op");
            History.convertOpIndex(external2Internal, op);
            int index = op.getIndex();
            List<Integer> times = timeCoords.get(index);
            transition.put("y", processCoords.get(op.getProcess()));
            transition.put("min-x", times.get(0));
            transition.put("max-x", times.get(1));
        }
    }

    public static List<List<Map<String, Object>>> paths(Map<String, Object> analysis, Map<Integer, List<Integer>> timeCoords, Map<Integer, Integer> processCoords, Map<Integer, Integer> external2Internal) {
        HashSet<?> finalPaths = (HashSet<?>) analysis.get("final-paths");
        Iterator<?> iterator = finalPaths.iterator();
        List<List<Map<String, Object>>> paths = new ArrayList<>();
        while (iterator.hasNext()) {
            List<Map<String, Object>> path = (List<Map<String, Object>>) iterator.next();
            pathBounds(timeCoords, processCoords, external2Internal, path);
            paths.add(path);
        }
        return paths;

    }

    public static List<Map<String, Object>> path2line(List<Map<String, Object>> path, List<Map<?, ?>> lines, Map<Map<?, ?>, Map<?, ?>> models) {
        Double x0 = Double.NEGATIVE_INFINITY;
        Double y0 = null;
        List<Map<String, Object>> resPath = new ArrayList<>();
        for (Map<String, Object> transition : path) {
            Double y1 = Double.parseDouble(transition.get("y").toString());
            Model model = (Model) transition.get("model");
            double x1;
            x1 = Math.max(x0 + minStep, Double.parseDouble(transition.get("min-x").toString()));
            while (true) {
                Model m = (Model) models.getOrDefault(Map.of("x", x1, "y", y1), new HashMap<>()).getOrDefault("model", null);
                if (m != null && !m.equals(model)) {
                    x1 = x1 + minStep;
                } else {
                    break;
                }
            }

            Map<?, ?> bar = models.getOrDefault(Map.of("x", x1, "y", y1), new HashMap<>(Map.of("model", model, "id", models.size())));
            assert x1 <= Double.parseDouble(transition.get("max-x").toString()) : x1 + " starting at " + x0 + " is outside [" + transition.get("min-x") + ", " + transition.get("max-x") + "]\n" + path + transition;
            if (y0 == null) {
                transition.put("bar-id", bar.get("id"));
                resPath.add(transition);
                models.put(Map.of("x", x1, "y", y1), bar);
                x0 = x1;
                y0 = y1;
            } else {
                transition.put("line-id", lines.size());
                transition.put("bar-id", bar.get("id"));
                resPath.add(transition);
                Map<?, ?> line = new HashMap<>(Map.of(
                        "id", lines.size(),
                        "model", transition.get("model"),
                        "x0", x0,
                        "y0", y0,
                        "x1", x1,
                        "y1", y1
                ));
                lines.add(line);
                models.put(Map.of("x", x1, "y", y1), bar);
                x0 = x1;
                y0 = y1;
            }


        }
        return resPath;
    }

    public static List<?> paths2initialLines(List<List<Map<String, Object>>> paths) {
        Iterator<List<Map<String, Object>>> iterator = paths.iterator();
        List<List<Map<String, Object>>> resPath = new ArrayList<>();
        List<Map<?, ?>> lines = new ArrayList<>();
        Map<Map<?, ?>, Map<?, ?>> models = new HashMap<>();
        while (iterator.hasNext()) {
            List<Map<String, Object>> path = iterator.next();
            path = path2line(path, lines, models);
            resPath.add(path);
        }
        return List.of(resPath, lines, models);
    }

    //    public static void mergeLines(List<Map<?, ?>> lines) {
    //        Map<?, ?> mapping = new HashMap<>();
    //
    //            Map<?,?> line=lines.get(i);
    //            String id0=(String) line.get("id");
    //
    //    }

    public static Map<String, HashSet<String>> reachable(List<List<Map<String, Object>>> paths) {
        Map<String, HashSet<String>> rs = new HashMap<>();
        for (List<Map<String, Object>> path : paths) {
            List<String> ids = new ArrayList<>();
            for (Map<String, Object> transition : path) {
                Object b = transition.get("bar-id");
                Object l = transition.getOrDefault("line-id", null);
                if (l != null) {
                    ids.add("line-" + l);
                    ids.add("bar-" + b);
                } else {
                    ids.add("bar-" + b);
                }
            }
            for (String id : ids) {
                HashSet<String> curIds = rs.getOrDefault(id, new HashSet<>());
                curIds.addAll(ids);
                rs.put(id, curIds);
            }

        }
        return rs;

    }

    public static List<?> paths2lines(List<List<Map<String, Object>>> paths) {
        List<?> res = paths2initialLines(paths);
        List<List<Map<String, Object>>> resPaths = (List<List<Map<String, Object>>>) res.get(0);
        List<Map<?, ?>> lines = (List<Map<?, ?>>) res.get(1);
        Map<Map<?, ?>, Map<?, ?>> models = (Map<Map<?, ?>, Map<?, ?>>) res.get(2);
        return List.of(resPaths, lines, models);
    }


    public static Map<Double, Integer> coordinateDensity(Map<?, ?> bars) {
        Object[] keys = bars.keySet().toArray();
        List<List<Double>> xys = new ArrayList<>();
        for (Object key : keys) {
            Map<?, ?> keyMap = (Map<?, ?>) key;
            double x = Math.floor((double) keyMap.get("x"));
            double y = (double) keyMap.get("y");
            xys.add(List.of(x, y));
        }
        Map<Double, List<List<Double>>> coordinate = xys.stream().collect(Collectors.groupingBy(xy -> xy.get(0)));
        Map<Double, Integer> density = new HashMap<>();
        for (Map.Entry<Double, List<List<Double>>> entry : coordinate.entrySet()) {
            double x = entry.getKey();
            List<List<Double>> ys = entry.getValue();
            Map<List<Double>, Integer> frequencies = new HashMap<>();
            for (List<Double> y : ys) {
                frequencies.put(y, frequencies.getOrDefault(y, 0) + 1);
            }
            List<Integer> vals = new ArrayList<>(frequencies.values());
            int value = vals.stream().reduce(0, Math::max);
            density.put(x, value);
        }
        return new TreeMap<>(density);

    }

    public static Map<?, Map<?, ?>> warpTimeCoordinates(Map<Integer, List<Integer>> timeCoords, Map<?, ?> bars) {
        Map<Double, Integer> density = coordinateDensity(bars);
        int dmax = density.values().stream().reduce(0, Math::max);
        int tmin = timeCoords.values().stream().flatMap(Collection::stream).reduce(Math::min).get();
        int tmax = timeCoords.values().stream().flatMap(Collection::stream).reduce(Math::max).get();
        double offset = 0;
        List<List<?>> pairs = new ArrayList<>();
        for (int i = tmin; i < tmax + 1; i++) {
            double t = Math.floor(i);
            double d = density.getOrDefault(t, 1) / (double) dmax;
            pairs.add(List.of(t, Map.of("offset", offset, "scale", d)));
            offset += d;
        }
        Map<Object, Map<?, ?>> m = new HashMap<>();
        for (List<?> pair : pairs) {
            m.put(pair.get(0), (Map<?, ?>) pair.get(1));
        }
        return m;


    }

    public static double hscale(Map<?, Map<?, ?>> m, double t) {
        Map<?, ?> os = m.get(Math.floor(t));
        double offset = (double) os.get("offset");
        double scale = (double) os.get("scale");
        return (offset + scale * (t % 1)) * hscale;

    }

    public static double vscale(double x) {
        return x * 60.0;
    }

    public static double hscale(double x) {
        return x * hscale;
    }

    public static String opColor(Map<Op, Op> pairIndex, Op op) {
        Op completion = History.completion(pairIndex, op);
        return type2color.get(completion.getType());
    }

    public static Element renderOps(Document document, Map<Integer, List<Integer>> timeCoords, Map<Integer, Integer> processCoords, Map<Op, Op> pairIndex, List<Op> ops, Map<Integer, Integer> internalToExternal, Map<?, Map<?, ?>> m) {
        Element element = document.createElementNS(svgNS, "g");
        for (Op op : ops) {
            List<Integer> ts = timeCoords.get(op.getIndex());
            int p = processCoords.get(op.getProcess());
            double width = hscale(m, ts.get(1)) - hscale(m, ts.get(0));
            int i = internalToExternal.get(History.completion(pairIndex, op).getIndex());
            Element group = document.createElementNS(svgNS, "g");
            Element a = document.createElementNS(svgNS, "a");
            group.appendChild(a);
            a.setAttribute("xlink:href", "timeline.html#i" + i);
            a.setAttribute("href", "timeline.html#i" + i);
            Element rect = document.createElementNS(svgNS, "rect");
            rect.setAttribute("x", String.valueOf(hscale(m, ts.get(0))));
            rect.setAttribute("y", String.valueOf(vscale(p)));
            rect.setAttribute("height", String.valueOf(vscale(processHeight)));
            rect.setAttribute("width", String.valueOf(width));
            rect.setAttribute("rx", String.valueOf(vscale(0.1)));
            rect.setAttribute("ry", String.valueOf(vscale(0.1)));
            rect.setAttribute("fill", opColor(pairIndex, op));
            a.appendChild(rect);
            Element text = document.createElementNS(svgNS, "text");
            text.setTextContent(op.getF() + " " + op.getValue());
            text.setAttribute("x", String.valueOf(hscale(m, ts.get(0)) + width / 2.0));
            text.setAttribute("y", String.valueOf(vscale(p + processHeight / 2.0)));
            String style = "fill: #000000; font-size: " + vscale(processHeight * 0.6) + "; font-family: " + font + "; alignment-baseline: middle; text-anchor: middle";
            text.setAttribute("style", style);
            a.appendChild(text);
            element.appendChild(group);
        }
        return element;
    }

    public static void activeLine(Element element, Map<String, HashSet<String>> reachable) {
        String ids = reachable.get(element.getAttribute("id")).stream().map(id -> "'" + id + "'").collect(Collectors.joining(","));
        element.setAttribute("onmouseover", "[" + ids + "].forEach(abar);");
        element.setAttribute("onmouseout", "[" + ids + "].forEach(dbar);");
    }

    public static Element renderBars(Document document, Map<?, Map<?, ?>> m, Map<?, ?> bars, Map<String, HashSet<String>> reachable) {
        Element element = document.createElementNS(svgNS, "g");
        for (Map.Entry<?, ?> entry : bars.entrySet()) {
            Map<?, ?> keys = (Map<?, ?>) entry.getKey();
            double x = (double) keys.get("x");
            double y = (double) keys.get("y");
            Map<?, ?> bar = (Map<?, ?>) entry.getValue();
            int id = (int) bar.get("id");
            Model model = (Model) bar.get("model");
            Element group = document.createElementNS(svgNS, "g");
            Element line = document.createElementNS(svgNS, "line");
            line.setAttribute("x1", String.valueOf(hscale(m, x)));
            line.setAttribute("y1", String.valueOf(vscale(y)));
            line.setAttribute("x2", String.valueOf(hscale(m, x)));
            line.setAttribute("y2", String.valueOf(vscale(y + processHeight)));
            line.setAttribute("stroke-width", String.valueOf(strokeWidth));
            line.setAttribute("stroke", transitionColor(bar));
            group.appendChild(line);

            Element text = document.createElementNS(svgNS, "text");
            text.setTextContent(model.toString());
            text.setAttribute("class", "model");
            text.setAttribute("opacity", "0.0");
            text.setAttribute("x", String.valueOf(hscale(m, x)));
            text.setAttribute("y", String.valueOf(vscale(y - 0.1)));
            String style = "fill: " + transitionColor(bar) + "; font-size: " + vscale(processHeight * 0.5) + "; font-family: " + font + "; alignment-baseline: baseline; text-anchor: middle";
            text.setAttribute("style", style);
            text.setAttribute("filter", "url(#glow)");
            group.appendChild(text);
            group.setAttribute("id", "bar-" + id);
            group.setAttribute("opacity", String.valueOf(faded));
            activeLine(group, reachable);
            element.appendChild(group);
        }
        return element;
    }

    public static Element renderLines(Document document, Map<?, Map<?, ?>> m, List<Map<?, ?>> lines, Map<String, HashSet<String>> reachable) {
        Element group = document.createElementNS(svgNS, "g");
        for (Map<?, ?> line : lines) {
            int id = (int) line.get("id");
            double x0 = (double) line.get("x0");
            double y0 = (double) line.get("y0");
            double x1 = (double) line.get("x1");
            double y1 = (double) line.get("y1");
            boolean up = y0 < y1;
            if (up) {
                y0 = y0 + processHeight;
            } else {
                y1 = y1 + processHeight;
            }
            Element lineEle = document.createElementNS(svgNS, "line");
            lineEle.setAttribute("x1", String.valueOf(hscale(m, x0)));
            lineEle.setAttribute("y1", String.valueOf(vscale(y0)));
            lineEle.setAttribute("x2", String.valueOf(hscale(m, x1)));
            lineEle.setAttribute("y2", String.valueOf(vscale(y1)));
            lineEle.setAttribute("id", "line-" + id);
            lineEle.setAttribute("stroke-width", String.valueOf(strokeWidth));
            lineEle.setAttribute("stroke", transitionColor(line));
            lineEle.setAttribute("opacity", String.valueOf(faded));
            activeLine(lineEle, reachable);
            group.appendChild(lineEle);


        }
        return group;
    }

    public static void legendStyle(Element legend, String s, double x, double y, String style, Map<?, Map<?, ?>> m) {
        legend.setTextContent(s);
        legend.setAttribute("x", String.valueOf(hscale(x)));
        legend.setAttribute("y", String.valueOf(vscale(y)));
        if (!style.isEmpty()) {
            style += "; ";
        }
        style += "fill: #666; font-size: " + vscale(legendHeight) + "; font-family: " + font;
        legend.setAttribute("style", style);
    }

    public static Element renderLegend(Document document, Map<Integer, Integer> processCoords, Map<?, Map<?, ?>> m, Map<Integer, List<Integer>> timeCoords) {
        double y = processHeight + 0.5 + processCoords.values().stream().reduce(0, Math::max);
        double xmax = hscale(m, timeCoords.values().stream().map(t -> t.get(1)).reduce(0, Math::max)) / hscale;
        Element group = document.createElementNS(svgNS, "g");
        Element process = document.createElementNS(svgNS, "text");
        legendStyle(process, "Process", -0.1, y, "algnment:-baseline: baseline; text-anchor: end", m);
        group.appendChild(process);
        Element time = document.createElementNS(svgNS, "text");
        legendStyle(time, "Time -------->", 0, y, "alignment-baseline: baseline; text-anchor: start", m);
        group.appendChild(time);
        Element legal = document.createElementNS(svgNS, "text");
        legendStyle(legal, "Legal", xmax - 2.2, y, "alignment-baseline: baseline; text-anchor: end", m);
        group.appendChild(legal);
        Element line = document.createElementNS(svgNS, "line");
        line.setAttribute("x1", String.valueOf(hscale(xmax - 2.15)));
        line.setAttribute("y1", String.valueOf(vscale(y + legendHeight * -0.3)));
        line.setAttribute("x2", String.valueOf(hscale(xmax - 2.05)));
        line.setAttribute("y2", String.valueOf(vscale(y + legendHeight * -0.3)));
        line.setAttribute("stroke-width", String.valueOf(strokeWidth));
        line.setAttribute("stroke", transitionColor(new HashMap<>()));
        group.appendChild(line);
        Element illegal = document.createElementNS(svgNS, "text");
        legendStyle(illegal, "Illegal", xmax - 1.65, y, "algnment-baseline: baseline; text-anchor: end", m);
        group.appendChild(illegal);

        line = document.createElementNS(svgNS, "line");
        line.setAttribute("x1", String.valueOf(hscale(xmax - 1.6)));
        line.setAttribute("y1", String.valueOf(vscale(y + legendHeight * -0.3)));
        line.setAttribute("x2", String.valueOf(hscale(xmax - 1.5)));
        line.setAttribute("y2", String.valueOf(vscale(y + legendHeight * -0.3)));
        line.setAttribute("stroke-width", String.valueOf(strokeWidth));
        line.setAttribute("stroke", transitionColor(Map.of("model", new Inconsistent(null))));
        group.appendChild(line);

        Element text = document.createElementNS(svgNS, "text");
        text.setTextContent("Crashed Op");
        legendStyle(text, "Crashed Op", xmax - 0.85, y, "alignment-baseline: baseline; text-anchor: end", m);
        group.appendChild(text);

        Element rect = document.createElementNS(svgNS, "rect");
        rect.setAttribute("x", String.valueOf(hscale(xmax - 0.8)));
        rect.setAttribute("y", String.valueOf(vscale(y - legendHeight + 0.03)));
        rect.setAttribute("height", String.valueOf(vscale(legendHeight)));
        rect.setAttribute("width", String.valueOf(hscale(0.16)));
        rect.setAttribute("rx", String.valueOf(vscale(0.05)));
        rect.setAttribute("ry", String.valueOf(vscale(0.05)));
        rect.setAttribute("fill", "#FFAA26");
        group.appendChild(rect);

        text = document.createElementNS(svgNS, "text");
        text.setTextContent("Ok Op");
        legendStyle(text, "Ok Op", xmax - 0.21, y, "alignment-baseline: baseline; text-anchor: end", m);
        group.appendChild(text);

        rect = document.createElementNS(svgNS, "rect");
        rect.setAttribute("x", String.valueOf(hscale(xmax - 0.16)));
        rect.setAttribute("y", String.valueOf(vscale(y - legendHeight + 0.03)));
        rect.setAttribute("height", String.valueOf(vscale(legendHeight)));
        rect.setAttribute("width", String.valueOf(hscale(0.16)));
        rect.setAttribute("rx", String.valueOf(vscale(0.05)));
        rect.setAttribute("ry", String.valueOf(vscale(0.05)));
        rect.setAttribute("fill", type2color.get(Operation.Type.OK));
        group.appendChild(rect);

        Element g = document.createElementNS(svgNS, "g");
        for (Map.Entry<Integer, Integer> py : processCoords.entrySet()) {
            int p = py.getKey();
            y = py.getValue();
            Element processEle = document.createElementNS(svgNS, "text");
            legendStyle(processEle, String.valueOf(p), -0.1, y + processHeight / 2.0, "alignment-baseline: middle; text-anchor: end", m);
            g.appendChild(processEle);
        }
        group.appendChild(g);
        return group;
    }


    public static void renderAnalysis(List<Op> history, Map<String, Object> analysis, String fileName) {
        assert analysis.get("valid?").equals(false);
        List<Object> historyAndIndex = History.preprocess(history);
        history = (List<Op>) historyAndIndex.get(0);
        Map<Integer, Integer> internalToExternal = (Map<Integer, Integer>) historyAndIndex.get(1);
        Map<Integer, Integer> externalToInternal = internalToExternal.entrySet().stream().collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
        Map<Op, Op> pairIndex = History.pairIndexWithInvokesAndInfos(history);
        List<Op> ops = ops(analysis);
        History.convertOpIndices(externalToInternal, ops);
        List<Model> models = models(analysis);
        Map<Model, Integer> modelNumbers = modelNumbers(models);
        Map<Integer, Integer> processCoords = processCoords(ops);
        List<Integer> timeBounds = timeBounds(pairIndex, analysis);
        Map<Integer, List<Integer>> timeCoords = timeCoords(pairIndex, timeBounds, ops);
        List<List<Map<String, Object>>> paths = paths(analysis, timeCoords, processCoords, externalToInternal);
        List<?> res = paths2lines(paths);
        paths = (List<List<Map<String, Object>>>) res.get(0);
        List<Map<?, ?>> lines = (List<Map<?, ?>>) res.get(1);
        Map<?, ?> bars = (Map<?, ?>) res.get(2);
        Map<String, HashSet<String>> reachable = reachable(paths);
        Map<?, Map<?, ?>> m = warpTimeCoordinates(timeCoords, bars);
        DOMImplementation implementation = SVGDOMImplementation.getDOMImplementation();

        Document document = implementation.createDocument(svgNS, "svg", null);
        Element svgRoot = document.getDocumentElement();
        svgRoot.setAttribute("xmlns:svg", "http://www.w3.org/2000/svg");
        svgRoot.setAttribute("xmlns", "http://www.w3.org/2000/svg");
        svgRoot.setAttribute("xmlns:xlink", "http://www.w3.org/1999/xlink");
        svgRoot.setAttribute("version", "2.0");

        CDATASection cdataSection = document.createCDATASection(activateScript);
        Element script = document.createElementNS(svgNS, "script");
        script.setAttribute("type", "application/ecmascript");
        script.appendChild(cdataSection);

        svgRoot.appendChild(script);
        Element defs = document.createElementNS(svgNS, "defs");
        svgRoot.appendChild(defs);


        Element filter = document.createElementNS(svgNS, "filter");
        filter.setAttribute("x", "0");
        filter.setAttribute("y", "0");
        filter.setAttribute("id", "glow");

        defs.appendChild(filter);

        Element feGaussianBlur = document.createElementNS(svgNS, "feGaussianBur");
        feGaussianBlur.setAttribute("in", "SourceAlpha");
        feGaussianBlur.setAttribute("stdDeviation", "2");
        feGaussianBlur.setAttribute("result", "blurred");

        filter.appendChild(feGaussianBlur);

        Element feFlood = document.createElementNS(svgNS, "feFlood");
        feFlood.setAttribute("flood-color", "#fff");
        filter.appendChild(feFlood);

        Element feComposite = document.createElementNS(svgNS, "feComposite");
        feComposite.setAttribute("operator", "in");
        feComposite.setAttribute("in2", "blurred");
        filter.appendChild(feComposite);

        Element feComponentTransfer = document.createElementNS(svgNS, "feComponentTransfer");
        Element feFunA = document.createElementNS(svgNS, "feFuncA");
        feFunA.setAttribute("type", "linear");
        feFunA.setAttribute("slope", "10");
        feFunA.setAttribute("intercept", "0");
        feComponentTransfer.appendChild(feFunA);
        filter.appendChild(feComponentTransfer);

        Element feMerge = document.createElementNS(svgNS, "feMerge");
        Element feMergeNode = document.createElementNS(svgNS, "feMergeNode");
        feMerge.appendChild(feMergeNode);
        feMergeNode = document.createElementNS(svgNS, "feMergeNode");
        feMergeNode.setAttribute("in", "SourceGraphic");
        feMerge.appendChild(feMergeNode);
        filter.appendChild(feMerge);

        Element group = document.createElementNS(svgNS, "g");
        group.appendChild(renderLines(document, m, lines, reachable));
        group.appendChild(renderOps(document, timeCoords, processCoords, pairIndex, ops, internalToExternal, m));
        group.appendChild(renderBars(document, m, bars, reachable));
        group.appendChild(renderLegend(document, processCoords, m, timeCoords));
        group.setAttribute("transform", "translate(" + vscale(1.4) + "," + vscale(0.4) + ")");
        svgRoot.appendChild(group);


        try {
            Path path=Path.of(fileName);
            File file=path.toFile();
            file.getParentFile().mkdirs();
            FileWriter fileWriter = new FileWriter(file, StandardCharsets.UTF_8);
            StreamResult result = new StreamResult(fileWriter);
            DOMSource source = new DOMSource(document);
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.transform(source, result);

        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
        }


    }
}
