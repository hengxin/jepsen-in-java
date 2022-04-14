package core.checker.linearizability;

public class Memo {
    public static Object unwrap(Object x) {
        if (x instanceof Wrapper) {
            return ((Wrapper) x).getModel();
        } else {
            return x;

        }
    }
}
