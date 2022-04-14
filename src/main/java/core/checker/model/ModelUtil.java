package core.checker.model;

public class ModelUtil {
    public static boolean isInconsistent(Model model) {
        return model instanceof Inconsistent;
    }
}
