package core.checker.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class TestInfo {
    private String name;
    private LocalDateTime startTime;
}
