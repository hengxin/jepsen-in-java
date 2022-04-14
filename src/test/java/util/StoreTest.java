package util;

import core.checker.vo.TestInfo;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;


class StoreTest {

    @Test
    void path() {
        TestInfo testInfo = new TestInfo("test", LocalDateTime.now());
        System.out.println(Store.path(testInfo).getPath());
    }

    @Test
    void makePathIfNotExists() {
        String[] args = new String[]{"test", "hh"};
        Store.makePathIfNotExists(new TestInfo("test", LocalDateTime.now()), args);
    }
}