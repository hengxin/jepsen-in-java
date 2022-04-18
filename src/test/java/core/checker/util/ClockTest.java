package core.checker.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

class ClockTest {
    Clock clock = new Clock();
//
    @Test
    void shortNodeNames() {
        List<String> test = List.of("hahjp.hpp.jljl", "hhhhap.hp.jljl", "h2ap.hp.jljl");
        List<String> res = clock.shortNodeNames(test);
        List<String> resExpected = List.of("hahjp.hpp", "hhhhap.hp","h2ap.hp");
        Assertions.assertEquals(resExpected, res);
    }
}