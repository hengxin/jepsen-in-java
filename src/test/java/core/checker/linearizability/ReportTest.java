package core.checker.linearizability;

import core.checker.model.CASRegister;
import core.checker.model.Mutex;
import core.checker.model.Register;
import org.junit.jupiter.api.Test;
import us.bpsm.edn.parser.Parseable;
import us.bpsm.edn.parser.Parser;
import us.bpsm.edn.parser.Parsers;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static us.bpsm.edn.Keyword.newKeyword;

class ReportTest {

    String readEdn(String path) {
        try {

            InputStream inputStream = getClass().getClassLoader().getResourceAsStream(path);
            StringBuilder stringBuilder = new StringBuilder();
            assert inputStream != null;
            Reader reader = new BufferedReader(new InputStreamReader(inputStream));
            int c;
            while ((c = reader.read()) != -1) {
                stringBuilder.append((char) c);
            }
            return stringBuilder.toString();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
        }
        return "";

    }

    List<Op> constructOps(String path) {
        String edn = readEdn(path);
        Parseable pbr = Parsers.newParseable(edn);
        Parser p = Parsers.newParser(Parsers.defaultConfiguration());
        List<Op> ops = new ArrayList<>();
        List<?> value = (List<?>) p.nextValue(pbr);
        for (Object v : value) {
            Map<?, ?> m = (Map<?, ?>) v;
            Op op = new Op(m.get(newKeyword("process")), m.get(newKeyword("f")), m.get(newKeyword("type")), m.get(newKeyword("value")));
            ops.add(op);
        }


        return ops;
    }


    @Test
    void renderAnalysis() {
        String file = "data/cas-register/bad/cas-failure.edn";
        List<Op> history = constructOps(file);
        Map<String, Object> analysis = Wgl.analysis(new CASRegister(null), history);
        String filename = "report.svg";
        history = constructOps(file);
        Report.renderAnalysis(history, analysis, filename);
    }

    @Test
    void badAnalysisTest() {
        String file = "data/cas-register/bad/bad-analysis.edn";
        List<Op> history = constructOps(file);
        Map<String, Object> analysis = Wgl.analysis(new Register(0), history);
        String filename = "bad-analysis-wgl.svg";
        Report.renderAnalysis(history, analysis, filename);
    }

    @Test
    void rethinkAnalysis(){
        String file = "data/cas-register/bad/rethink-fail-smaller.edn";
        List<Op> history = constructOps(file);
        Map<String, Object> analysis = Wgl.analysis(new CASRegister(0), history);
        String filename = "rethink-fail-smaller-wgl.svg";
        Report.renderAnalysis(history, analysis, filename);
    }

    @Test
    void casFailure(){
        String file = "data/cas-register/bad/cas-failure.edn";
        List<Op> history = constructOps(file);
        Map<String, Object> analysis = Wgl.analysis(new CASRegister(null), history);
        String filename = "cas-failure-wgl.svg";
        Report.renderAnalysis(history, analysis, filename);
    }

    @Test
    void etcdMutex(){
        String file = "data/mutex/bad/etcd.edn";
        List<Op> history = constructOps(file);
        Map<String, Object> analysis = Wgl.analysis(new Mutex(false), history);
        String filename = "etcd-wgl.svg";
        Report.renderAnalysis(history, analysis, filename);
    }
}