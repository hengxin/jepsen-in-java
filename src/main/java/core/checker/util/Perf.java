package core.checker.util;

import com.panayotis.gnuplot.JavaPlot;
import com.panayotis.gnuplot.dataset.DataSet;
import core.checker.checker.Operation;
import core.checker.exception.NoPointsException;
import core.checker.linearizability.Op;
import core.checker.vo.Plot;
import core.checker.vo.TestInfo;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.units.qual.C;
import org.icepear.echarts.Bar;
import org.icepear.echarts.Option;
import org.icepear.echarts.render.Engine;
import org.icepear.echarts.serializer.EChartsSerializer;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.Axis;
import org.jfree.chart.axis.LogAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.FastScatterPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.chart.ui.HorizontalAlignment;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.chart.ui.VerticalAlignment;
import org.jfree.data.Range;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.xy.DefaultXYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import util.Store;
import util.Util;

import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class Perf {
    final static String DEFAULT_NEMESIS_COLOR = "#cccccc";
    final static Operation.Type[] TYPES = new Operation.Type[]{Operation.Type.OK, Operation.Type.INFO, Operation.Type.FAIL};
    final static Map<Operation.Type, String> TYPE2COLOR = Map.of(Operation.Type.OK, "#81BFFC", Operation.Type.INFO, "#FFA400", Operation.Type.FAIL, "#FF1E90");
    final static double NEMESIS_ALPHA = 0.6;

    public static double bucketScale(double dt, double b) {
        return dt / 2 + dt * Double.valueOf(b).longValue();
    }

    public static double bucketTime(double dt, double t) {
        return bucketScale(dt, t / dt);
    }

    public static List<Double> buckets(double dt, long tMax) {
        long i = 0;
        double scale = bucketScale(dt, i);
        ;
        List<Double> res = new ArrayList<>();
        while (tMax >= scale) {
            res.add(scale);
            i++;
            scale = bucketScale(dt, i);
        }
        return res;
    }

    public static Map<Double, List<List<?>>> bucketPoints(long dt, List<List<?>> points) {
        Map<Double, List<List<?>>> res = new TreeMap<>();
        for (List<?> point : points) {
            double time = bucketTime(dt,Double.parseDouble(point.get(0).toString()));
            if (res.containsKey(time)) {
                res.get(time).add(point);
            } else {
                res.put(time, new ArrayList<>(List.of(point)));
            }
        }
        return res;
    }

    public static Map<Double, Double> quantiles(List<Double> qs, List<Double> points) {
        List<Double> sorted = points.stream().sorted().collect(Collectors.toList());
        Map<Double, Double> res = new HashMap<>();
        if (!sorted.isEmpty()) {
            int n = sorted.size();
            for (Double q : qs) {
                int idx = (int) Math.min(n - 1, Math.floor(n * q));
                res.put(q, sorted.get(idx));
            }
        }
        return res;
    }

    public static Map<Double, String> qs2Colors(List<Double> qs) {
        qs.sort(Comparator.reverseOrder());
        Map<Double, String> res = new HashMap<>();
        List<String> colors = List.of("red", "orange", "purple", "blue", "green", "grey");
        int i = 0;
        for (double q : qs) {
            i = i % colors.size();
            res.put(q, colors.get(i));
            i++;
        }
        return res;
    }

    public static Map<String, String> ns2Colors(List<String> qs) {
        qs.sort(Comparator.reverseOrder());
        Map<String, String> res = new HashMap<>();
        List<String> colors = List.of("red", "orange", "purple", "blue", "green", "grey");
        int i = 0;
        for (String q : qs) {
            i = i % colors.size();
            res.put(q, colors.get(i));
            i++;
        }
        return res;
    }



    public static Map<Double, List<List<?>>> latencies2quantiles(long dt, List<Double> qs, List<List<?>> points) {
        for (double q : qs) {
            assert 0 <= q && q <= 1;
        }
        Map<Double, List<List<?>>> bps = bucketPoints(dt, points);
        List<List<?>> buckets = new ArrayList<>();
        for (Map.Entry<Double, List<List<?>>> bp : bps.entrySet()) {
            Double bucketTime = bp.getKey();
            List<List<?>> ps = bp.getValue();
            List<Double> latencies = ps.stream().map(p -> Double.parseDouble(p.get(1).toString())).collect(Collectors.toList());
            buckets.add(List.of(bucketTime, quantiles(qs, latencies)));
        }

        Map<Double, List<List<?>>> res = new HashMap<>();
        for (Double q : qs) {
            List<List<?>> tmp = new ArrayList<>();
            for (List<?> b : buckets) {
                double t = (double) b.get(0);
                Map<Double, Double> qsBuckets = (Map) b.get(1);
                tmp.add(List.of(t, qsBuckets.get(q)));
            }
            res.put(q, tmp);
        }
        return res;

    }

    public static Plot plot(Path outputPath) {
        Plot plot = new Plot();
        plot.setOutput(outputPath);
        plot.setSize(new int[]{900, 400});
        plot.setXLabel("Time (s)");
        return plot;
    }

    public static Plot latencyPlot(Map test, Path outputPath) {
        Plot plot = plot(outputPath);
        plot.setTitle(test.get("name") + " latency");
        plot.setYLabel("Latency (ms)");
        plot.setLogScale("y");
        return plot;
    }

    public static List<Double> latencyPoint(Operation op) {
        return List.of(
                Util.nanos2secs(op.getTime()),
                Util.nanos2ms(op.getLatency())
        );
    }

    public static Map<Operation.Type, Object> invokeByType(List<Operation> operations) {
        Map<Operation.Type, Object> res = new HashMap<>();
        res.put(Operation.Type.OK, operations.stream().filter(op -> (op.getCompletion().getType() == Operation.Type.OK)).collect(Collectors.toList()));
        res.put(Operation.Type.FAIL, operations.stream().filter(op -> (op.getCompletion().getType() == Operation.Type.FAIL)).collect(Collectors.toList()));
        res.put(Operation.Type.INFO, operations.stream().filter(op -> (op.getCompletion().getType() == Operation.Type.INFO)).collect(Collectors.toList()));
        return res;
    }

    public static Map invokesByFType(List<Operation> history) {
        Map<Operation.F, List<Operation>> invokes = history.stream().filter(OpUtil::isInvoke).collect(Collectors.groupingBy(Operation::getF));
        Map res = new HashMap();
        for (Map.Entry<Operation.F, List<Operation>> entry : invokes.entrySet()) {
            res.put(entry.getKey(), invokeByType(entry.getValue()));
        }

        return res;
    }

    public static Map<Operation.F,List<Operation>> invokesByF(List<Operation> history) {
        return history.stream().filter(OpUtil::isInvoke).collect(Collectors.groupingBy(Operation::getF));
    }


    public static boolean hasData(Map<String, Object> plot) {
        List<Map> series = (List<Map>) plot.get("series");
        for (Map m : series) {
            if (!((List) m.get("data")).isEmpty()) {
                return true;
            }
        }
        return false;
    }

    public static List<Map> nemesisSeries(Map<String, Object> plot, List<Map> nemeses) {

        List<Map> series = nemeses.stream().map(
                n -> {
                    String linecolor;
                    if (n.containsKey("fill-color")) {
                        linecolor = (String) n.get("fil-color");
                    } else if (n.containsKey("color")) {
                        linecolor = (String) n.get("color");
                    } else {
                        linecolor = DEFAULT_NEMESIS_COLOR;
                    }
                    return Map.of(
                            "title", n.get("name"),
                            "with", "lines",
                            "linecolor", linecolor,
                            "linewidth", 6,
                            "data", List.of(List.of(-1, -1))
                    );
                }
        ).collect(Collectors.toList());

        return series;
    }

    public static void withoutEmptySeries(Map<String, Object> plot) {
        List<Map> series = (List<Map>) plot.get("series");
        List<Map> res = series.stream().filter(s -> {
            return !((List) s.get("data")).isEmpty();
        }).collect(Collectors.toList());
        plot.put("series", res);
    }

    public static double mod(double num, double div) {
        return num - Math.floor(num / div) * div;
    }

    /**
     * @param a lower
     * @param b upper
     * @return Given a [lower upper] range for a plot, returns [lower' upper'], which
     * covers the original range, but slightly expanded, to fall nicely on integral
     * boundaries.
     */
    public static List<Double> broadenRange(double a, double b) {
        if (a == b) {
            return List.of(a - 1, a + 1);
        }

        double size = Math.abs(b - a);
        double grid = size / 10;
        double scale = Math.pow(10, Math.round(Math.log10(grid)));
        double a_new = a - mod(a, scale);
        double b_new;
        double m = mod(b, scale);
        if (m / scale < 0.001) {
            b_new = b;
        } else {
            b_new = scale + (b - mod(b, scale));
        }

        a_new = Math.min(a, a_new);
        b_new = Math.max(b, b_new);
        return List.of(a_new, b_new);

    }

    public static void withRange(Map<String, Object> plot) throws Exception {
        List<Map> series = (List<Map>) plot.get("series");
        List<List> data = new ArrayList();
        for (Map s : series) {
            data.addAll((List) s.get("data"));
        }
        if (data.isEmpty()) {
            throw new NoPointsException(plot.toString());//TODO
        }
        double x0 = (double) data.get(0).get(0);
        double y0 = (double) data.get(0).get(1);

        double x_min = x0;
        double x_max = x0;
        double y_min = y0;
        double y_max = y0;


        for (List point : data) {
            double x = (double) point.get(0);
            double y = (double) point.get(1);
            x_min = Math.min(x_min, x);
            x_max = Math.max(x_max, x);
            y_min = Math.min(y_min, y);
            y_max = Math.max(y_max, y);
        }

        List<Double> xRange = broadenRange(x_min, x_max);
        List<Double> yRange;
        if (plot.getOrDefault("logscale", false).equals("y")) {
            yRange = List.of(y_min, y_max);
        } else {
            yRange = broadenRange(y_min, y_max);
        }

        //TODO
        plot.put("xrange", xRange);
        plot.put("yrange", yRange);


    }

    public static List<Map> nemesisOps(List<Map> nemeses, List<Operation> history) {
        for (Map nemesis : nemeses) {
            assert nemesis.containsKey("name");
        }
        List<String> distinctNames = nemeses.stream().map(n -> (String) n.get("name")).distinct().collect(Collectors.toList());
        List<String> names = nemeses.stream().map(n -> (String) n.get("name")).collect(Collectors.toList());
        assert distinctNames.size() == names.size();
        Map index = new HashMap();
        for (Map nemesis : nemeses) {
            index.put(nemesis.get("start"), nemesis.get("name"));
            index.put(nemesis.get("stop"), nemesis.get("name"));
            index.put(nemesis.get("fs"), nemesis.get("name"));

        }

        Map opsByNemesis = history.stream().filter(
                h -> h.getProcess() == -1
        ).collect(Collectors.groupingBy(Operation::getF));//TODO


        for (Map nemesis : nemeses) {
            Object ops = opsByNemesis.get(nemesis.get("name"));
            nemesis.put("ops", ops);
        }

        if (!opsByNemesis.isEmpty()) {
            nemeses.add(Map.of(
                    "name", "nemesis",
                    "ops", opsByNemesis
            ));
        }

        return nemeses;
    }

    public static List<Map> nemesisActivity(List<Map> nemeses, List<Operation> history) {
        return nemesisOps(nemeses, history).stream().map(
                nemesis -> {
                    nemesis.put("interval", Util.nemesisIntervals((List<Map>) nemesis.get("ops"), nemesis));
                    return nemesis;
                }
        ).collect(Collectors.toList());
    }

    public static List<Double> interval2Times(List<Map> interval) {
        Map a = interval.get(0);
        List<Double> res = new ArrayList<>();
        res.add(Util.nanos2secs((double) a.get("time")));
        if (interval.size() > 1) {
            Map b = interval.get(1);
            res.add(Util.nanos2secs((double) b.get("time")));
        } else {
            res.add(null);
        }
        return res;
    }

    public static void nemesisRegions(List<Map> plot, List<Map> nemeses) {
        Map<Integer, Map> nemesesMap = new HashMap<>();
        for (int i = 0; i < nemeses.size(); i++) {
            nemesesMap.put(i, nemeses.get(i));
        }
        for (Map.Entry<Integer, Map> entry : nemesesMap.entrySet()) {
            int i = entry.getKey();
            Map n = entry.getValue();

            String color;
            if (n.containsKey("fill-color")) {
                color = (String) n.get("fill-color");
            } else if (n.containsKey("color")) {
                color = (String) n.get("color");
            } else {
                color = DEFAULT_NEMESIS_COLOR;
            }

            List<Double> transparency = List.of((double) n.get("transparency"), NEMESIS_ALPHA);
            int graphTopEdge = 1;
            double height = 0.0834;
            double padding = 0.00615;
            double bot = graphTopEdge - (height * (i + 1));
            double top = bot + height;

            List<Map> intervals = (List<Map>) n.get("intervals");
            List<List<Double>> times = intervals.stream().map(
                    interval -> interval2Times((List<Map>) interval)
            ).collect(Collectors.toList());

            for (List<Double> time : times) {
                double start = time.get(0);
                double stop = time.get(1);
                //TODO
            }

        }
    }

    public static void withNemeses(Map<String, Object> plot, List<Operation> history, List nemeses) {
        //        nemeses = nemesisActivity(nemeses, history);
        //        plot.put("series", nemesisSeries(plot, nemeses));
        //        plot.put("preamble",ne)
    }

    public static void legendPart(Map series) {

    }

    public static void plot(Map opts) {
        List<Map> series = (List) opts.get("series");
        List<Map> empty = series.stream().filter(s -> ((List) s.get("data")).isEmpty()).collect(Collectors.toList());
        for (Map s : series) {
            assert !((List) s.get("data")).isEmpty() : "Series has no data points\n" + String.join(" ", empty.toString());
        }

        //        while ((boolean) opts.getOrDefault("draw-fewer-on-top?", false)) {
        //            series.sort((s1, s2) -> {
        //                int c1 = ((List) s1.get("data")).size();
        //                int c2 = ((List) s2.get("data")).size();
        //                return c2 - c1;
        //            });
        //            List<Map> newSeries = new ArrayList<>();
        //            for (Map s : series) {
        //                s.remove("title");
        //                newSeries.add(s);
        //            }
        //            // dummy points
        //            for (Map s : series) {
        //                s.put("data", List.of(List.of(0, -1)));
        //                newSeries.add(s);
        //            }
        //            opts.put("series", series);
        //            opts.put("draw-fewer-on-top?", false);
        //        }
        series = (List) opts.get("series");
        Plot plot = (Plot) opts.get("preamble");
        List<Double> xRange = (List<Double>) opts.get("xrange");
        List<Double> yRange = (List<Double>) opts.get("yrange");
        Object logScale = opts.getOrDefault("logscale", false);
        List<List<List<?>>> data = series.stream().map(s -> (List<List<?>>) s.get("data")).collect(Collectors.toList());
        XYSeriesCollection dataset = new XYSeriesCollection();
        for (Map<?, ?> s : series) {
            XYSeries xys = new XYSeries((String) s.get("title"));
            for (List<?> d : (List<List<?>>) s.get("data")) {
                xys.add(Double.parseDouble(d.get(0).toString()), Double.parseDouble(d.get(1).toString()));
            }
            dataset.addSeries(xys);
        }

        ValueAxis xAxis = null, yAxis = null;
        if (logScale.equals("x")) {
            xAxis = new LogAxis(plot.getXLabel());
            yAxis = new NumberAxis(plot.getYLabel());
        } else if (logScale.equals("y")) {
            xAxis = new NumberAxis(plot.getXLabel());
            yAxis = new LogAxis(plot.getYLabel());
        } else {
            xAxis = new NumberAxis(plot.getXLabel());
            yAxis = new NumberAxis(plot.getYLabel());
        }

        Range range = new Range(xRange.get(0), xRange.get(1));
        xAxis.setRange(range);
        Range range1 = new Range(yRange.get(0), yRange.get(1));
        yAxis.setRange(range1);
        XYLineAndShapeRenderer xyLineAndShapeRenderer = new XYLineAndShapeRenderer(plot.isLine(), true);
        xyLineAndShapeRenderer.setDefaultShapesFilled(false);
        XYPlot xyPlot = new XYPlot(dataset, xAxis, yAxis, xyLineAndShapeRenderer);
        JFreeChart chart = new JFreeChart(plot.getTitle(), JFreeChart.DEFAULT_TITLE_FONT, xyPlot, true);
        LegendTitle legendTitle = chart.getLegend();
        legendTitle.setHorizontalAlignment(HorizontalAlignment.CENTER);
        legendTitle.setVerticalAlignment(VerticalAlignment.CENTER);
        legendTitle.setPosition(RectangleEdge.RIGHT);
        try {
            OutputStream out = new FileOutputStream(plot.getOutput().toString());
            ChartUtils.writeChartAsPNG(out, chart, 1000, 500);

        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
        }

    }

    public static Map fs2points(List fs) {
        Map res = new HashMap();
        for (int i = 0; i < fs.size(); i++) {
            res.put(fs.get(i), 2 * (2 + i));
        }
        return res;
    }

    //    public static void latencyPreamble(Map test,){
    //
    //    }

    public static void pointGraph(Map<String, ?> test, List<Operation> history, Map<String, ?> opts) {
        List<?> nemeses = new ArrayList<>();
        String subdirectory = (String) opts.get("subdirectory");
        if (opts.containsKey("nemeses")) {
            nemeses = (List) opts.get("nemeses");
        } else {
            if (test.containsKey("plot")) {
                if (((Map) test.get("plot")).containsKey("nemeses")) {
                    nemeses = (List) ((Map) test.get("plot")).get("nemeses");
                }
            }
        }
        history = Util.history2Latencies(history);
        Map<String, Object> datasets = invokesByFType(history);
        List fs = datasets.keySet().stream().sorted(Comparator.naturalOrder()).collect(Collectors.toList());
        Map fs2Points = fs2points(fs);
        TestInfo testInfo = new TestInfo((String) test.get("name"), LocalDateTime.ofEpochSecond((int) test.get("start-time") / 1000, 0, ZoneOffset.ofHours(8)));
        String[] args = new String[]{subdirectory == null ? "" : subdirectory, "latency-raw.png"};
        String outputPath = "";
        try {
            outputPath = Store.makePathIfNotExists(testInfo, args).getCanonicalPath();
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
        }
        Plot plot = latencyPlot(test, Path.of(outputPath));
        plot.setLine(false);
        List<Map> series = new ArrayList<>();
        for (Object f : fs) {
            for (Operation.Type t : TYPES) {
                if (datasets.containsKey(f)) {
                    if (((Map) datasets.get(f)).containsKey(t)) {
                        List<Operation> data = ((List) ((Map) datasets.get(f)).get(t));
                        Map res = new HashMap(Map.of(
                                "title", f.toString() + " " + t,
                                "with", fs2Points,
                                "linetype", TYPE2COLOR.get(t),
                                "pointtype", fs2points(List.of(f)),
                                "data", data.stream().map(Perf::latencyPoint).collect(Collectors.toList())
                        ));
                        series.add(res);
                    }
                }
            }
        }

        Map<String, Object> res = new HashMap<>(Map.of(
                "preamble", plot,
                "draw-fewer-on-top?", true,
                "logscale", "y",
                "series", series
        ));
        try {
            withRange(res);
            withNemeses(res, history, nemeses);
            plot(res);

        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
        }


    }

    public static void quantilesGraph(Map test, List<Operation> history, Map opts) {
        List<Operation> nemeses = new ArrayList();
        if (opts.containsKey("nemeses")) {
            nemeses = (List) opts.get("nemeses");
        } else {
            if (test.containsKey("nemeses")) {
                if (((Map) test.get("nemeses")).containsKey("plot")) {
                    nemeses = (List) ((Map) test.get("plot")).get("nemeses");
                }
            }
        }
        String subdirectory = (String) opts.get("sundirectory");

        Util.history2Latencies(history);
        int dt = 30;
        List<Double> qs = new ArrayList<>(List.of(0.5, 0.95, 0.99, 1.0));
        Map<Operation.F,List<Operation>> invokes = invokesByF(history);
        Map<Object, Object> datasets = new HashMap<>();
        for (Operation.F f : invokes.keySet()) {
            List<Operation> ops =invokes.get(f);
            List<List<?>> latencies = new ArrayList<>();
            for (Operation op : ops) {
                List<Double> latency = latencyPoint(op);
                latencies.add(latency);
            }
            Map<Double, List<List<?>>> res = latencies2quantiles(dt, qs, latencies);
            datasets.put(f, res);
        }

        List fs = datasets.keySet().stream().sorted().collect(Collectors.toList());
        Map fs2Points = fs2points(fs);
        Map<Double, String> qs2Colors = qs2Colors(qs);
        TestInfo testInfo = new TestInfo((String) test.get("name"), LocalDateTime.ofEpochSecond((int) test.get("start-time") / 1000, 0, ZoneOffset.ofHours(8)));
        String[] args = new String[]{subdirectory == null ? "" : subdirectory, "latency-quantiles.png"};
        String outputPath = "";
        try {
            outputPath = Store.path(testInfo, args).getCanonicalPath();
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
        }

        Plot plot = latencyPlot(test, Path.of(outputPath));
        plot.setLine(true);
        List<Map> series = new ArrayList<>();
        for (Object f : fs) {
            for (double q : qs) {
                Map s = Map.of(
                        "title", f.toString() + " " + q,
                        "with", "linespoints",
                        "linetype", qs2Colors(new ArrayList<>(List.of(q))),
                        "pointtype", fs2points(fs),
                        "data", ((Map) datasets.get(f)).get(q)
                );
                series.add(s);
            }
        }
        Map<String, Object> res = new HashMap<>(Map.of(
                "preamble", plot,
                "series", series,
                "logscale", "y"

        ));
        try {
            withRange(res);
            withNemeses(res, history, nemeses);
            plot(res);
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
        }

    }

    public static Plot ratePlot(TestInfo testInfo, Path outputPath) {
        Plot plot = plot(outputPath);
        plot.setTitle(testInfo.getName() + " rate");
        plot.setYLabel("Throughput (hz)");
        return plot;
    }

    public static void rateGraph(Map test, List<Operation> history, Map opts) {
        List<Operation> nemeses = new ArrayList();
        if (opts.containsKey("nemeses")) {
            nemeses = (List) opts.get("nemeses");
        } else {
            if (test.containsKey("nemeses")) {
                if (((Map) test.get("nemeses")).containsKey("plot")) {
                    nemeses = (List) ((Map) test.get("plot")).get("nemeses");
                }
            }
        }
        String subdirectory = (String) opts.get("subdirectory");
        subdirectory = subdirectory == null ? "" : subdirectory;
        int dt = 10;
        double td = 1.0 / dt;
        double tMax = Util.nanos2secs(history.stream().map(Operation::getTime).reduce(0.0, (x, y) -> x > y ? x : y));
        Map<Object, Object> datasets = new HashMap<>();
        history.removeIf(OpUtil::isInvoke);
        List<Operation> process = history.stream().filter(h -> h.getProcess() >= 0).collect(Collectors.toList());
        for (Operation operation : process) {
            double time = bucketTime(dt, (long) Util.nanos2secs(operation.getTime()));
            if (datasets.containsKey(operation.getF())) {
                Map d1 = (Map) datasets.get(operation.getF());
                if (d1.containsKey(operation.getType())) {
                    Map d2 = (Map) d1.get(operation.getType());
                    if (d2.containsKey(time)) {
                        d2.put(time, td + (double) d2.get(time));
                    } else {
                        d2.put(time, td);
                    }

                } else {
                    d1.put(operation.getType(), new HashMap<>(Map.of(time, td)));
                }
            } else {
                datasets.put(operation.getF(), new HashMap<>(Map.of(operation.getType(), new HashMap<>(Map.of(time, td)))));
            }
        }
        List<Object> fs = datasets.keySet().stream().sorted().collect(Collectors.toList());
        Map fs2points = fs2points(fs);
        TestInfo testInfo = new TestInfo((String) test.get("name"), LocalDateTime.ofEpochSecond(Long.parseLong(test.get("start-time").toString()) / 1000, 0, ZoneOffset.ofHours(8)));
        String[] args = new String[]{subdirectory, "rate.png"};
        String outputPath = "";
        try {
            outputPath = Store.makePathIfNotExists(testInfo, args).getCanonicalPath();

        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
        }
        Plot plot = ratePlot(testInfo, Path.of(outputPath));
        plot.setLine(true);
        List series = new ArrayList();

        for (Object f : fs) {
            for (Operation.Type t : TYPES) {
                Map<String, Object> s = new HashMap<>(Map.of(
                        "title", f + " " + t,
                        "with", "linespoints",
                        "linetype", TYPE2COLOR.get(t),
                        "pointtype", fs2points(List.of(f))
                ));
                List<Object> data = new ArrayList<>();
                Map m = (Map) ((Map) datasets.get(f)).get(t);
                m = m == null ? new HashMap<>() : m;
                List<Double> buckets = buckets(dt, (long) tMax);
                for (double b : buckets) {
                    data.add(List.of(b, m.getOrDefault(b, 0)));
                }
                s.put("data", data);
                series.add(s);
            }
        }

        Map<String, Object> res = new HashMap<>(Map.of(
                "preamble", plot,
                "series", series
        ));
        try {
            withRange(res);
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
        }

        withNemeses(res, history, nemeses);
        plot(res);

    }

    public static void main(String[] args) {
//        Bar bar = new Bar()
//                .setLegend()
//                .setTooltip("item")
//                .addXAxis(new String[]{"Matcha Latte", "Milk Tea", "Cheese Cocoa", "Walnut Brownie"})
//                .addYAxis()
//                .addSeries("2015", new Number[]{43.3, 83.1, 86.4, 72.4})
//                .addSeries("2016", new Number[]{85.8, 73.4, 65.2, 53.9})
//                .addSeries("2017", new Number[]{93.7, 55.1, 82.5, 39.1})
//                .setTitle("hhh");
//        Engine engine = new Engine();
//        // The render method will generate our EChart into a HTML file saved locally in the current directory.
//        // The name of the HTML can also be set by the first parameter of the function.
//        Option option = new Option();
//        //        option.
//        engine.render("index.html", bar);
        DefaultCategoryDataset dataset=new DefaultCategoryDataset();
        dataset.addValue(212,"Letter","A");
        dataset.addValue(504,"Letter","B");
        dataset.addValue(1520,"Letter","C");
        dataset.addValue(1842,"Letter","D");
        dataset.addValue(2991,"Letter","E");
        JFreeChart chart= ChartFactory.createLineChart("Line Chart Demo",
                "Category Axis",
                "Value Axis",
                dataset,
                PlotOrientation.VERTICAL,
                false,
                true,
                false);
        try {
            OutputStream out = new FileOutputStream("jfree.png");
            ChartUtils.writeChartAsPNG(out,chart,900,500);

        }catch (Exception e){
            e.printStackTrace();
            log.error(e.getMessage());
        }

    }

}
