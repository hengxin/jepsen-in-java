package core.checker.util;

import core.checker.util.Clock;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import util.ClojureCaller;

import java.util.List;

class ClockTest {
    Clock clock = new Clock();
//
    @Test
    void shortNodeNames() {
        List<String> test = List.of("hahjp.hpp.jljl", "hhhhap.hp.jljl", "h2ap.hp.jljl");
        List<String> res1 = (List) ClojureCaller.call("jepsen.checker.clock", "short-node-names", test);
        List<String> res2 = clock.shortNodeNames(test);
        Assertions.assertEquals(res1.size(), res2.size());
        for (int i = 0; i < res1.size();i++) {
            Assertions.assertEquals(res1.get(i), res2.get(i));
        }
    }
}