package core.nemesis;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;

import static util.Constant.*;


@Slf4j
@AllArgsConstructor
public class NemesisGenerators {

    // 静态方法，根据对应名字产生需要的generator
    public static NemesisGenerators ParseNemesisGenerators(String kinds) {
        ArrayList<NemesisGenerator> generators = new ArrayList<>();
        for(String kind: kinds.split(" ")){
            kind = kind.trim();
            switch (kind) {
                case "":
                    continue;
                case NEMESIS_GENERATOR_RANDOM_KILL: case NEMESIS_GENERATOR_ALL_KILL:
                    generators.add(new KillGenerator(kind));
                    break;
                case NEMESIS_GENERATOR_SYMMETRIC_NETWORK_PARTITION: case NEMESIS_GENERATOR_ASYMMETRIC_NETWORK_PARTITION:
                    generators.add(new PartitionGenerator(kind));
                    break;
                default:
                    log.warn("unknown kind generator");
            }
        }
        return new NemesisGenerators(generators, 0);
    }

    ArrayList<NemesisGenerator> generators;
    int index;


    public boolean HasNext() {
        return index < generators.size();
    }

    public NemesisGenerator Next() {
        NemesisGenerator generator = generators.get(index);
        index++;
        return generator;
    }

    public void Reset() {
        index = 0;
    }

}
