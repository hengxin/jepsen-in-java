package core.checker;

import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;

/**
 * Takes positive integer limit and a checker. Puts an upper bound on the
 * number of concurrent executions of this checker. Use this when a checker is
 * particularly thread or memory intensive, to reduce context switching and
 * memory cost.
 */
@Slf4j
public class ConcurrencyLimit implements Checker {
    Semaphore sem;
    Checker checker;

    public ConcurrencyLimit(int limit, Checker checker) {
        this.sem = new Semaphore(limit, true);
        this.checker = checker;
    }

    @Override
    public Map check(Map test, List<Map> history, Map opts) {

        try {
            sem.acquire();
            return checker.check(test, history, opts);
        } catch (Exception e) {
            log.info(e.getMessage());
        } finally {
            sem.release();
        }
        return null;
    }
}
