import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.locks.ReentrantLock;

public class Swapper implements Runnable {
    private static int numSwaps = 10;
    private static ReentrantLock lock = new ReentrantLock();
    private Floor floor;
    private final Floor STARTING_FLOOR;
    Random r = new Random();
    ArrayList<Swap> swaps = new ArrayList<>();

    public Swapper(Floor f) {
        floor = f;
        STARTING_FLOOR = f;
    }

    private void swap() {
        int currentNumSwaps = numSwaps;
        Floor solutionFloor = Solution.getSolutionFloor();
        ArrayList<Swap> solutionSwaps = Solution.getSolutionSwaps();
        //If you take part of the solution instead of the whole thing do each swap individually instead of taking the whole solution floor
        floor = STARTING_FLOOR;
        for(Swap swap : solutionSwaps) {
            floor.swap(swap.p1, swap.p2);
            swaps.add(swap);
        }
//        floor = new Floor(solutionFloor);
//        swaps.addAll(solutionSwaps);
        //Do random swaps until you hit the currentNumSwaps
        for(int i = swaps.size(); i < currentNumSwaps; i++) {
            Point point1, point2;
            for(;;) {
                point1 = new Point(r.nextInt(floor.getStations().length), r.nextInt(floor.getStations()[0].length));
                point2 = new Point(r.nextInt(floor.getStations().length), r.nextInt(floor.getStations()[0].length));
                if(point1.x != point2.x || point1.y != point2.y) {
                    break;
                }
            }
            floor.swap(point1, point2);
            Swap recent = new Swap(point1, point2);
            swaps.add(recent);
            Solution.setUncheckedFloor(floor);
            //After every swap, check if the solution is better than the current solution
            double affinity = floor.getAffinity();
            if(affinity > Solution.getSolutionAffinity()) {
                Solution.setSolution(affinity, swaps, floor);
            //30% of the time, reverse the swap if it makes the solution worse
            } else if (r.nextInt(10) > 3){
                floor.swap(point2, point1);
                swaps.remove(recent);
            }
        }
        //Reset the swap list for the next iteration
        swaps = new ArrayList<>();
    }

    public void increaseNumSwaps() {
        for(;;) {
            if(lock.tryLock()) {
                try {
                    int size = Solution.getSolutionSwaps().size();
                    //Function that decreases the factor of increase as the solution set gets bigger
                    double maxSwaps = (size == 0) ? 1 : size * (Math.sqrt(500 / size) + 2.1);
                    if (numSwaps <= maxSwaps) {
                        numSwaps++;
                    } else {
                        numSwaps = (int) maxSwaps;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    lock.unlock();
                }
                break;
            }
        }
    }

    @Override
    public void run() {
        for(;;) {
            swap();
            increaseNumSwaps();
        }
    }
}
