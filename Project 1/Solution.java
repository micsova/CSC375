import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantLock;

public class Solution {
    private static ArrayList<Swap> swaps = null;
    private static double affinity = 0;
    private static Floor floor = null;
    private static Floor uncheckedFloor = null;
    private static ReentrantLock solutionLock = new ReentrantLock();
    private static ReentrantLock uncheckedFloorLock = new ReentrantLock();

    public static void setSolution(double newAffinity, ArrayList<Swap> newSwaps, Floor newFloor) {
        for(;;) {
            if(solutionLock.tryLock()) {
                try {
                    //Stores the swaps, affinity, and floor
                    //Get two thirds of the swaps for the solution to pass on (if there is just 1 swap, get all of them)
                    int endIndex = (2 * newSwaps.size()) / 3;
                    if(endIndex != 0) swaps = new ArrayList<>(newSwaps.subList(0, endIndex));
                    else swaps = newSwaps;
                    //Just get all the swaps
//                    swaps = newSwaps;
                    affinity = newAffinity;
                    floor = newFloor;
                    System.out.println("Got a good solution (" + affinity + ", " + swaps.size() + ")");
                    Display.interrupt(floor);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    solutionLock.unlock();
                }
                break;
            }
        }
    }

    public static Floor getSolutionFloor() {
        Floor ret;
        for(;;) {
            if(solutionLock.tryLock()) {
                ret = floor;
                solutionLock.unlock();
                break;
            }
        }
        return ret;
    }

    public static Floor getUncheckedFloor() {
        Floor ret;
        for(;;) {
            if(uncheckedFloorLock.tryLock()) {
                ret = uncheckedFloor;
                uncheckedFloorLock.unlock();
                break;
            }
        }
        return ret;
    }

    public static void setUncheckedFloor(Floor f) {
        for(;;) {
            if(uncheckedFloorLock.tryLock()) {
                uncheckedFloor = f;
                uncheckedFloorLock.unlock();
                break;
            }
        }
    }

    public static ArrayList<Swap> getSolutionSwaps() {
        ArrayList<Swap> ret;
        for(;;) {
            if(solutionLock.tryLock()) {
                ret = swaps;
                solutionLock.unlock();
                break;
            }
        }
        return ret;
    }

    public static double getSolutionAffinity() {
        double ret;
        for(;;) {
            if(solutionLock.tryLock()) {
                ret = affinity;
                solutionLock.unlock();
                break;
            }
        }
        return ret;
    }
}