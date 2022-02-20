package core.nemesis;

import java.util.ArrayList;

public class NemesisGenerators {

    // 静态方法，根据对应名字产生需要的generator
    public static NemesisGenerators ParseNemesisGenerators(String kinds) {
        ArrayList<NemesisGenerator> generators = new ArrayList<>();
        for(String kind: kinds.split(",")){
            kind = kind.trim();
            switch (kind) {
                case "":
                    continue;
                case "random_kill": case "all_kill":
                    generators.add(new KillGenerator(kind));
                    break;
                default:
                    System.out.println("unknown kind generator");        // TODO 把所有sout和exception.print都变成日志[log4j]
            }
        }
        return new NemesisGenerators(generators, 0);
    }

    ArrayList<NemesisGenerator> generators;
    int index;

    public NemesisGenerators(ArrayList<NemesisGenerator> generators, int index) {
        this.generators = generators;
        this.index = index;
    }

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
