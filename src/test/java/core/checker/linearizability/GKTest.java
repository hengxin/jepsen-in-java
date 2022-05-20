package core.checker.linearizability;

import core.checker.checker.Operation;
import core.checker.model.Model;
import core.checker.model.Register;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import us.bpsm.edn.parser.Parseable;
import us.bpsm.edn.parser.Parser;
import us.bpsm.edn.parser.Parsers;
import util.Support;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static us.bpsm.edn.Keyword.newKeyword;

class GKTest {

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

    public List<Op> constructOps(String path) {
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
    void sequential() {
        List<Op> history = new ArrayList<>(List.of(
                new Op(0, Operation.F.WRITE, Operation.Type.INVOKE, Integer.valueOf(1)),
                new Op(0, Operation.F.WRITE, Operation.Type.OK, Integer.valueOf(1))

        ));

        GK gk=new GK(history);
        boolean res=gk.linearizable();
        Assertions.assertTrue(res);
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
        GK gk=new GK(history);
        Assertions.assertFalse(gk.linearizable());

    }

    @Test
    void readWrongInitialValueTest() {
        Model model = new Register(0);
        List<Op> history = new ArrayList<>(List.of(
                new Op(0, Operation.F.READ, Operation.Type.INVOKE, Integer.valueOf(1)),
                new Op(0, Operation.F.READ, Operation.Type.OK, Integer.valueOf(1))
        ));
        GK gk=new GK(history);
        Assertions.assertFalse(gk.linearizable());
    }

    @Test
    void badAnalysisTest() {
        List<Op> history = constructOps("data/cas-register/bad/bad-analysis.edn");
        GK gk=new GK(history);
        Assertions.assertFalse(gk.linearizable());

    }

    @Test
    void rethinkFailMinimalTest() {
        List<Op> history = constructOps("data/cas-register/bad/rethink-fail-minimal.edn");
        GK gk=new GK(history);
        Assertions.assertFalse(gk.linearizable());
    }

    @Test
    void rethinkFailSmallerTest() {
        List<Op> history = constructOps("data/cas-register/bad/rethink-fail-smaller.edn");
        GK gk=new GK(history);
        Assertions.assertFalse(gk.linearizable());
    }

    @Test
    void readWriteGood(){
        ArrayList<Operation> operations = Support.TxtToOperations("output/oceanbase/read_write_client/good.txt");
        List<Op> history=operations.stream().map(Op::new).collect(Collectors.toList());
        GK gk=new GK(history);
        Assertions.assertTrue(gk.linearizable());
    }

    @Test
    void readWriteBad(){
        ArrayList<Operation> operations = Support.TxtToOperations("output/oceanbase/read_write_client/bad.txt");
        List<Op> history=operations.stream().map(Op::new).collect(Collectors.toList());
        GK gk=new GK(history);
        Assertions.assertFalse(gk.linearizable());
    }
}