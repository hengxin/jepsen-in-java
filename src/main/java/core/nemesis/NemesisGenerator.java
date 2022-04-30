package core.nemesis;

import core.db.Node;

import java.util.ArrayList;

public interface NemesisGenerator {

    // generator对所有node产生nemesis
    ArrayList<NemesisOperation> Generate(ArrayList<Node> nodes);

}
