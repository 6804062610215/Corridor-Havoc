import java.util.ArrayList;
import java.util.List;

// แผนที่ของด่านปัจจุบัน
// # = กำแพง, . = พื้น, F = หลุมไฟ, S = กับดักหนาม, B = ถังระเบิด
public class GameMap {

    // แผนที่ของด่านที่กำลังเล่น (เปลี่ยนทุกครั้งที่ขึ้นด่านใหม่)
    public static String[] MAP;

    // ช่องที่มีสิ่งของขวางทาง (ถังระเบิด) เดินผ่านไม่ได้ แต่ไม่ใช่กำแพง
    private static boolean[][] blocked;

    public static void load(String[] map) {
        MAP = map;
        blocked = new boolean[map.length][map[0].length()];
    }

    public static int getRows() {
        return MAP.length;
    }

    public static int getCols() {
        return MAP[0].length();
    }

    // กำแพงจริง ๆ (ใช้ตอนวาดฉาก 3D)
    public static boolean isWall(double x, double y) {

        int mapX = (int) x;
        int mapY = (int) y;

        if (mapX < 0 ||
                mapX >= getCols() ||
                mapY < 0 ||
                mapY >= getRows()) {
            return true;
        }

        return MAP[mapY].charAt(mapX) == '#';
    }

    // เดินผ่านไม่ได้: เป็นกำแพง หรือมีถังระเบิดขวางอยู่ (ใช้ตอนเดินและหาทาง)
    public static boolean isBlocked(double x, double y) {

        if (isWall(x, y)) {
            return true;
        }

        return blocked[(int) y][(int) x];
    }

    public static void setBlocked(int col, int row, boolean value) {
        blocked[row][col] = value;
    }

    // เช็คว่ามองเห็นกันไหม (ไม่มีกำแพงหรือถังระเบิดบังระหว่างสองจุด)
    public static boolean hasLineOfSight(double x1, double y1, double x2, double y2) {

        double dx = x2 - x1;
        double dy = y2 - y1;

        double dist = Math.hypot(dx, dy);

        int steps = (int) (dist / 0.1) + 1;

        for (int i = 1; i < steps; i++) {

            double t = (double) i / steps;

            if (isBlocked(x1 + dx * t, y1 + dy * t)) {
                return false;
            }
        }

        return true;
    }

    // ช่องพื้นว่างทั้งหมด ใช้เป็นจุดเกิดของศัตรูและไอเทม (ไม่รวมช่องที่มีอุปสรรค)
    public static List<double[]> getFloorSpawnPoints() {

        List<double[]> points = new ArrayList<>();

        for (int row = 0; row < getRows(); row++) {

            for (int col = 0; col < getCols(); col++) {

                if (MAP[row].charAt(col) == '.') {
                    points.add(new double[] { col + 0.5, row + 0.5 });
                }
            }
        }

        return points;
    }
}
