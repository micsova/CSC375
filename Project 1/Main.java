import java.util.ArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;

public class Main {
    private static boolean ready = false;
    private static ReentrantLock lock = new ReentrantLock();

    public static void setReady(boolean b) {
        for(;;) {
            if(lock.tryLock()) {
                ready = b;
                lock.unlock();
                break;
            }
        }
    }

    public static boolean getReady() {
        boolean ret;
        for(;;) {
            if(lock.tryLock()) {
                ret = ready;
                lock.unlock();
                break;
            }
        }
        return ret;
    }

    public static void main(String[] args) {
        Floor f = new Floor();
        Display d = new Display(f);
        Executor displayEx = Executors.newSingleThreadExecutor();
        displayEx.execute(d);
        double startAffin = f.getAffinity();
        Solution.setSolution(startAffin, new ArrayList<>(), f);
        for(;;) {
            if(getReady()) break;
        }
        for(int i = 0; i < Integer.parseInt(args[0]) ; i++) {
            Swapper s = new Swapper(new Floor(f));
            Executor ex = Executors.newSingleThreadExecutor();
            ex.execute(s);
        }
    }
}