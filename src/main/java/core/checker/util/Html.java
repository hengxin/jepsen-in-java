package core.checker.util;

import core.checker.checker.Checker;
import core.checker.checker.Operation;
import core.checker.vo.Result;
import core.checker.vo.TestInfo;
import lombok.extern.slf4j.Slf4j;
import util.Store;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;

@Slf4j
public class Html implements Checker {
    @Override
    public Result check(Map test, List<Operation> history, Map opts) {
        TimeLine.subIndex(history);
        List<List> pairs = TimeLine.pairs(history);
        int pairCount = pairs.size();
        int opLimit = TimeLine.OP_LIMIT;
        boolean truncated = opLimit < pairCount;
        pairs = pairs.subList(0, opLimit);
        String res = "<html>" +
                "<head>" +
                "<style>" + TimeLine.STYLE_SHEET + "</style>" +
                "</head>" +
                "<body>" + TimeLine.breadCrumbs(test, (String) opts.get("history-key")) +
                "<h1>" + test.get("name") + " key " + opts.get("history-key") + "</h1>";
        if (truncated) {
            res += "<div class=\"truncation-warning\">Showing only " + opLimit + " of " + pairCount + " operations in this history." +
                    "</div>";
        }
        StringBuilder ops = new StringBuilder();
        for (List p : pairs) {
            ops.append(TimeLine.pair2div(history, test, TimeLine.processIndex(history), (Map) p.get(0), (Map) p.get(1)));
        }
        res += "<div class=\"ops\">" + ops + "</div>";
        try {
            TestInfo testInfo = new TestInfo((String) test.get("name"), LocalDateTime.ofEpochSecond((long) test.get("start-time")/1000,0, ZoneOffset.ofHours(8)));
            String[] args = new String[]{(String) opts.get("sub-directory"), "timeline.html"};
            BufferedWriter out = new BufferedWriter(new FileWriter(Store.path(testInfo, args)));
            out.write(res);
            out.close();
            log.info("successfully write to timeline.html");
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
        }
        Result result = new Result();
        result.setValid(true);
        return result;

    }
}
