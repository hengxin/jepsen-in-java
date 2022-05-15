package core.checker.linearizability;

import core.checker.checker.Operation;
import core.checker.model.CASRegister;
import core.checker.model.Model;
import core.checker.model.Register;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import us.bpsm.edn.parser.Parseable;
import us.bpsm.edn.parser.Parser;
import us.bpsm.edn.parser.Parsers;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertNull;
import static us.bpsm.edn.Keyword.newKeyword;

class WglTest {
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

    public   List<Op> constructOps(String path) {
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
    void timeout() {
        Model model = new CASRegister(0);
        List<Op> history = constructOps("data/cas-register/good/memstress3-12.edn");
        Map<String, Object> a = Wgl.analysis(model, history, new HashMap<>(
                Map.of(
                        "time-limit", 1
                )
        ));
        Assertions.assertEquals(a.get("valid?"), true);
        Assertions.assertEquals(((CASRegister) a.get("model")).getValue(), 3L);
        Assertions.assertEquals(a.get("analyzer"), "wgl");
    }

    @Test
    void noTimeOut() {
        Model model = new Register(0);
        List<Op> history = new ArrayList<>(List.of(
                new Op(0, Operation.F.WRITE, Operation.Type.INVOKE, Integer.valueOf(1)),
                new Op(0, Operation.F.WRITE, Operation.Type.OK, Integer.valueOf(1))

        ));
        Map<String, Object> a = Wgl.analysis(model, history, new HashMap<>(
                Map.of(
                        "time-limit", 1
                )
        ));
        Assertions.assertEquals(a.get("valid?"), true);
        Assertions.assertEquals(((Register) a.get("model")).getValue(), 1);
        Assertions.assertEquals(a.get("analyzer"), "wgl");
    }

    @Test
    void sequential() {
        Model model = new Register(0);
        List<Op> history = new ArrayList<>(List.of(
                new Op(0, Operation.F.WRITE, Operation.Type.INVOKE, Integer.valueOf(1)),
                new Op(0, Operation.F.WRITE, Operation.Type.OK, Integer.valueOf(1))

        ));
        Map<String, Object> a = Wgl.analysis(model, history, new HashMap<>(
                Map.of(
                        "time-limit", 1
                )
        ));
        Assertions.assertEquals(a.get("valid?"), true);
        Assertions.assertEquals(((Register) a.get("model")).getValue(), 1);
        Assertions.assertEquals(a.get("analyzer"), "wgl");
    }

    @Test
    void preservesIndicesTest() {
        Model model = new Register(0);
        List<Op> history = new ArrayList<>(List.of(
                new Op(0, Operation.F.WRITE, Operation.Type.INVOKE, 1, 5),
                new Op(0, Operation.F.WRITE, Operation.Type.OK, 1, 7),
                new Op(0, Operation.F.READ, Operation.Type.INVOKE, 2, 99),
                new Op(0, Operation.F.READ, Operation.Type.OK, 2, 100)
        ));
        Map<String, Object> a = Wgl.analysis(model, history);
        Assertions.assertEquals(a.get("valid?"), false);
        Assertions.assertEquals(a.get("analyzer"), "wgl");
        Assertions.assertEquals(((Op) a.get("op")).getIndex(), 100);
        Assertions.assertEquals(((Op) a.get("previous-ok")).getIndex(), 7);
        HashSet<?> finalPaths = (HashSet<?>) a.get("final-paths");
        List<?> paths = (List<?>) finalPaths.iterator().next();
        Map<?, ?> p1 = (Map<?, ?>) paths.get(0);
        Map<?, ?> p2 = (Map<?, ?>) paths.get(1);
        Op op1 = (Op) p1.get("op");
        Op op2 = (Op) p2.get("op");
        Assertions.assertEquals(op1.getIndex(), 7);
        Assertions.assertEquals(op2.getIndex(), 100);
    }

    @Test
    void readWrongInitialValueTest() {
        Model model = new Register(0);
        List<Op> history = new ArrayList<>(List.of(
                new Op(0, Operation.F.READ, Operation.Type.INVOKE, Integer.valueOf(1)),
                new Op(0, Operation.F.READ, Operation.Type.OK, Integer.valueOf(1))
        ));
        Map<String, Object> a = Wgl.analysis(model, history);
        Assertions.assertEquals(a.get("valid?"), false);
        Assertions.assertEquals(a.get("analyzer"), "wgl");
        Assertions.assertEquals(((Op) a.get("op")).getType(), Operation.Type.OK);
        assertNull(a.get("previous-ok"));

    }

    @Test
    void badAnalysisTest() {
        Model model = new CASRegister(0);
        List<Op> history = constructOps("data/cas-register/bad/bad-analysis.edn");
        Map<String, Object> a = Wgl.analysis(model, history);
        Assertions.assertEquals(a.get("valid?"), false);
        Assertions.assertEquals(((Op) a.get("op")).getIndex(), 14);
        Assertions.assertEquals(((Op) a.get("previous-ok")).getIndex(), 11);

    }

    @Test
    void badAnalysisTest2() {
        Model model = new CASRegister(0);
        List<Op> history = constructOps("data/cas-register/bad/cas-failure.edn");
        Map<String, Object> a = Wgl.analysis(model, history);
        Assertions.assertEquals(a.get("valid?"), false);
        Assertions.assertEquals(((Op) a.get("op")).getIndex(), 491);
        Assertions.assertEquals(((Op) a.get("previous-ok")).getIndex(), 478);
        Set<List<Map<String, Object>>> finalPaths = (Set<List<Map<String, Object>>>) a.get("final-paths");
        Assertions.assertEquals(finalPaths.size(), 2);
    }

    @Test
    void rethinkFailMinimalTest() {
        Model model = new CASRegister(null);
        List<Op> history = constructOps("data/cas-register/bad/rethink-fail-minimal.edn");
        Map<String, Object> a = Wgl.analysis(model, history);
        Assertions.assertEquals(a.get("valid?"), false);
        Assertions.assertEquals(((Op) a.get("op")).getIndex(), 4);
    }

    @Test
    void rethinkFailSmallerTest() {
        Model model = new CASRegister(null);
        List<Op> history = constructOps("data/cas-register/bad/rethink-fail-smaller.edn");
        Map<String, Object> a = Wgl.analysis(model, history);
        Assertions.assertEquals(((Op) a.get("op")).getIndex(), 217);
    }

    @Test
    void casFailureTest() {
        Model model = new CASRegister(0);
        List<Op> history = constructOps("data/cas-register/bad/cas-failure.edn");
        Map<String, Object> a = Wgl.analysis(model, history);
        Assertions.assertEquals(((Op) a.get("op")).getIndex(), 491);
    }

    @Test
    void volatileLinearizableTest() {

    }

    @Test
    void ExampleTest() {

    }


}