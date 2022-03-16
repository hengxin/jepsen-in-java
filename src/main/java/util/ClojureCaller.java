package util;

import clojure.java.api.Clojure;
import clojure.lang.IFn;

public class ClojureCaller {
    static IFn require = Clojure.var("clojure.core", "require");

    public static Object call(String ns, String fn, Object var1) {
        require.invoke(Clojure.read(ns));
        IFn func = Clojure.var(ns, fn);
        return func.invoke(var1);
    }

    public static Object call(String ns, String fn, Object var1, Object var2) {
        require.invoke(Clojure.read(ns));
        IFn func = Clojure.var(ns, fn);
        return func.invoke(var1, var2);
    }

    public static Object call(String ns, String fn, Object var1, Object var2, Object var3) {
        require.invoke(Clojure.read(ns));
        IFn func = Clojure.var(ns, fn);
        return func.invoke(var1, var2, var3);
    }

    public static Object call(String ns, String fn, Object var1, Object var2, Object var3, Object var4) {
        require.invoke(Clojure.read(ns));
        IFn func = Clojure.var(ns, fn);
        return func.invoke(var1, var2, var3, var4);
    }

    public static Object call(String ns, String fn, Object var1, Object var2, Object var3, Object var4, Object var5) {
        require.invoke(Clojure.read(ns));
        IFn func = Clojure.var(ns, fn);
        return func.invoke(var1, var2, var3, var4, var5);
    }

    public static Object call(String ns, String fn, Object var1, Object var2, Object var3, Object var4, Object var5,Object... var6) {
        require.invoke(Clojure.read(ns));
        IFn func = Clojure.var(ns, fn);
        return func.invoke(var1, var2, var3, var4, var5,var6);
    }

}
