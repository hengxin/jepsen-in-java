package core.checker.checker;

import core.checker.linearizability.Op;
import core.checker.model.Register;
import core.checker.vo.Result;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import us.bpsm.edn.parser.Parseable;
import us.bpsm.edn.parser.Parser;
import us.bpsm.edn.parser.Parsers;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static us.bpsm.edn.Keyword.newKeyword;

class LinearizableTest {

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

    List<core.checker.checker.Operation> constructOps(String path) {
        String edn = readEdn(path);
        Parseable pbr = Parsers.newParseable(edn);
        Parser p = Parsers.newParser(Parsers.defaultConfiguration());
        List<core.checker.checker.Operation> ops = new ArrayList<>();
        List<?> value = (List<?>) p.nextValue(pbr);
        for (Object v : value) {
            Map<?, ?> m = (Map<?, ?>) v;
            Op op = new Op(m.get(newKeyword("process")), m.get(newKeyword("f")), m.get(newKeyword("type")), m.get(newKeyword("value")));
            ops.add(op);
        }


        return ops;
    }


    @Test
    void check() {
        String file = "data/cas-register/bad/bad-analysis.edn";
        List<Operation> history = constructOps(file);
        Map<String,Object> opts=new HashMap<>(Map.of(
                "algorithm","wgl",
                "model",new Register(0)
        ));
        Linearizable linearizable=new Linearizable(opts);
        linearizable.check(new HashMap<>(Map.of("name","bad-analysis","start-time", LocalDateTime.now())),history,new HashMap());
    }

    @Test
    void checkGK() {
        String file = "data/cas-register/bad/bad-analysis.edn";
        List<Operation> history = constructOps(file);
        Map<String,Object> opts=new HashMap<>(Map.of(
                "algorithm","gk",
                "model",new Register(0)
        ));
        Linearizable linearizable=new Linearizable(opts);
        Result result=linearizable.check(new HashMap<>(Map.of("name","bad-analysis","start-time", LocalDateTime.now())),history,new HashMap<>());
        Assertions.assertEquals(result.getValid(),false);
    }
}