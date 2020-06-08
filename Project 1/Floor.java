import java.util.Random;
import java.util.concurrent.locks.ReentrantLock;

public class Floor {
    private Station[][] stations;
    private final int NUM_STATIONS = 40;
    public static final int MAX_WEIGHT = 255;
    public static final int FLOOR_SIDE = 7;
    private ReentrantLock lock = new ReentrantLock();

    public Floor() {
        stations = new Station[FLOOR_SIDE][FLOOR_SIDE];
        initialize();
    }

    //Duplicate a starting floor
    public Floor(Floor f) {
        stations = new Station[f.getStations().length][f.getStations()[0].length];
        for(int i = 0; i < f.getStations().length; i++) {
            for(int j = 0; j < f.getStations()[i].length; j++) {
                stations[i][j] = f.getStations()[i][j];
            }
        }
    }

    //Within the grid, set 32 stations with random weights from 1-255
    private void initialize() {
        for(;;) {
            if(lock.tryLock()) {
                Random r = new Random();
                for (int i = 0; i < NUM_STATIONS; i++) {
                    for (; ; ) {
                        int x = r.nextInt(stations.length);
                        int y = r.nextInt(stations[0].length);
                        if (stations[x][y] == null) {
                            stations[x][y] = new Station(r.nextInt(MAX_WEIGHT - 1) + 1);
                            break;
                        }
                    }
                }
                lock.unlock();
                break;
            }
        }
    }

    public Station[][] getStations() {
        return stations;
    }

    //Swap two stations (even if one or both are null)
    public void swap(Point p1, Point p2) {
        for(;;) {
            if (lock.tryLock()) {
                Station temp = stations[p1.x][p1.y];
                stations[p1.x][p1.y] = stations[p2.x][p2.y];
                stations[p2.x][p2.y] = temp;
                lock.unlock();
                break;
            }
        }
    }

    //Get the total affinity of the floor, as a sum of ((weight1 * weight2) / distance^2) for every pair of stations
    public double getAffinity() {
        double totalAffinity;
        for(;;) {
            if (lock.tryLock()) {
                totalAffinity = 0;
                for (int i = 0; i < stations.length; i++) {
                    for (int j = 0; j < stations[i].length; j++) {

                        if (stations[i][j] != null) {

                            for (int k = 0; k < stations.length; k++) {
                                for (int l = 0; l < stations[k].length; l++) {

                                    if (stations[k][l] != null && !(i == k && j == l)) {
                                        double distance = Math.pow((i - k), 2) + Math.pow((j - l), 2);
                                        double combinedWeight = stations[i][j].getWeight() * stations[k][l].getWeight();
                                        double affinity = (combinedWeight / distance) / 1000;
                                        totalAffinity += affinity;
                                    }
                                }
                            }
                        }
                    }
                }
                lock.unlock();
                break;
            }
        }
        return totalAffinity;
    }
}
