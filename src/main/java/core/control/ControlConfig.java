package core.control;

import core.db.Node;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;


@Data
@AllArgsConstructor
public class ControlConfig {
    private String dbName;
    private ArrayList<Node> nodes;
    private int clientCount;
}
