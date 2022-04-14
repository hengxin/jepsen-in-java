package core.checker.vo;

import lombok.Data;

import java.nio.file.Path;

@Data
public class Plot {
    Path output;
    int[] size;
    String title;
    String xLabel;
    String yLabel;
    Object chart;
    String logScale;
}
