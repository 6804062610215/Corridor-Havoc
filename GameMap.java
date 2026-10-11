import java.util.ArrayList;
import java.util.List;

public class GameMap {

    // # = กำแพง, . = พื้น
    // ห้องบน 2 ห้อง, ทางเดินยาวตรงกลาง, ห้องล่าง 3 ห้อง เชื่อมกันด้วยทางเดินแคบ
    public static final String[] MAP = {
            "########################",
            "#......#########.......#",
            "#......#########.......#",
            "#......................#",
            "#......#########.......#",
            "#......#########.......#",
            "###.################.###",
            "###.################.###",
            "#......................#",
            "#......................#",
            "###.#######.########.###",
            "###.#######.########.###",
            "#......#.......#.......#",
            "#......#.......#.......#",
            "#..............#.......#",
            "#......#.......#.......#",
            "#......#.......#.......#",
            "########################"
    };

    // จุดเกิดของผู้เล่น: ห้องซ้ายบน หันหน้าไปทางทางเดิน
    public static final double PLAYER_START_X = 3.5;
    public static final double PLAYER_START_Y = 3.5;

    public static boolean isWall(double x, double y) {

        int mapX = (int) x;
        int mapY = (int) y;

        if (mapX < 0 ||
                mapX >= MAP[0].length() ||
                mapY < 0 ||
                mapY >= MAP.length) {
            return true;
        }

        return MAP[mapY].charAt(mapX) == '#';
    }

    // ใช้เช็คว่าศัตรูระยะไกลมองเห็นผู้เล่นหรือไม่ (ไม่มีกำแพงกั้น)
    public static boolean hasLineOfSight(double x1, double y1, double x2, double y2) {

        double dx = x2 - x1;
        double dy = y2 - y1;

        double dist = Math.hypot(dx, dy);

        int steps = (int) (dist / 0.1) + 1;

        for (int i = 1; i < steps; i++) {

            double t = (double) i / steps;

            if (isWall(x1 + dx * t, y1 + dy * t)) {
                return false;
            }
        }

        return true;
    }

    public static List<double[]> getFloorSpawnPoints() {

        List<double[]> points = new ArrayList<>();

        for (int row = 0; row < MAP.length; row++) {

            for (int col = 0; col < MAP[row].length(); col++) {

                if (MAP[row].charAt(col) == '.') {
                    points.add(new double[] { col + 0.5, row + 0.5 });
                }
            }
        }

        return points;
    }
}
