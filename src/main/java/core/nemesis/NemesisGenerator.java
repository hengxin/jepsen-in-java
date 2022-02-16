package core.nemesis;

import core.db.Zone;

import java.util.ArrayList;

public interface NemesisGenerator {

    // generator对所有zone产生nemesis
    ArrayList<NemesisOperation> Generate(ArrayList<Zone> zones);

}
