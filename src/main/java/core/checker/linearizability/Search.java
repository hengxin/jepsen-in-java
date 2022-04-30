package core.checker.linearizability;

import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

@Slf4j
public abstract class Search {
    public abstract void abort(Object cause);

    public abstract void report();

    public abstract void results();

    public abstract void results(double timeout, double timeVal);

    public  void run(){
        run(new HashMap<>());
    }

    public void reporter(double interval){
        boolean running=true;
//        Future<Object>
    }

    public void run(Map<String,Object> opts){
        Callable<Object> abort=()->{
            log.warn("Out of memory; aborting search");
            this.abort("out-of-memory");
            return null;
        };


    }
}
