import java.util.LinkedList;
import java.util.Queue;

// หาทางเดินให้ศัตรูไปหาผู้เล่นโดยไม่ติดกำแพง ด้วย BFS (Breadth-First Search)
//
// วิธีคิด: เริ่มจากช่องที่ผู้เล่นยืน แล้วกระจายออกไปทีละชั้นเหมือนคลื่นน้ำ
// แต่ละช่องจะได้ตัวเลข "ระยะ (จำนวนช่อง) ถึงผู้เล่น"
// ศัตรูแค่เดินไปยังช่องข้าง ๆ ที่มีตัวเลขน้อยกว่าช่องตัวเอง ก็จะถึงผู้เล่นเสมอ
public class PathFinder {

    // distance[row][col] = จำนวนช่องที่ต้องเดินถึงผู้เล่น (-1 = ไปไม่ถึง / เดินผ่านไม่ได้)
    private static int[][] distance;

    // ทิศทั้ง 4: ขวา, ซ้าย, ลง, ขึ้น
    private static final int[] DIR_COL = { 1, -1, 0, 0 };
    private static final int[] DIR_ROW = { 0, 0, 1, -1 };

    // คำนวณระยะทุกช่องใหม่ (เรียกทุกเฟรม เพราะผู้เล่นเดินไปเรื่อย ๆ)
    public static void update(double playerX, double playerY) {

        int rows = GameMap.getRows();
        int cols = GameMap.getCols();

        // แต่ละด่านแผนที่ขนาดไม่เท่ากัน สร้างอาร์เรย์ใหม่ถ้าขนาดเปลี่ยน
        if (distance == null || distance.length != rows || distance[0].length != cols) {
            distance = new int[rows][cols];
        }

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                distance[row][col] = -1;
            }
        }

        int startCol = (int) playerX;
        int startRow = (int) playerY;

        Queue<int[]> queue = new LinkedList<>();

        distance[startRow][startCol] = 0;
        queue.add(new int[] { startCol, startRow });

        while (!queue.isEmpty()) {

            int[] cell = queue.poll();

            int col = cell[0];
            int row = cell[1];

            for (int d = 0; d < 4; d++) {

                int nextCol = col + DIR_COL[d];
                int nextRow = row + DIR_ROW[d];

                // เป็นกำแพง ถังระเบิด หรือนอกแผนที่ ข้ามไป
                if (GameMap.isBlocked(nextCol, nextRow)) {
                    continue;
                }

                // เคยไปถึงช่องนี้แล้ว ข้ามไป
                if (distance[nextRow][nextCol] != -1) {
                    continue;
                }

                distance[nextRow][nextCol] = distance[row][col] + 1;

                queue.add(new int[] { nextCol, nextRow });
            }
        }
    }

    // คืนจุดกึ่งกลางของช่องถัดไปที่ศัตรูควรเดินไป (ช่องข้าง ๆ ที่ใกล้ผู้เล่นที่สุด)
    public static double[] getNextStep(double x, double y) {

        int col = (int) x;
        int row = (int) y;

        int bestCol = col;
        int bestRow = row;
        int bestDistance = distance[row][col];

        if (bestDistance == -1) {
            bestDistance = Integer.MAX_VALUE;
        }

        for (int d = 0; d < 4; d++) {

            int nextCol = col + DIR_COL[d];
            int nextRow = row + DIR_ROW[d];

            if (GameMap.isBlocked(nextCol, nextRow)) {
                continue;
            }

            int nextDistance = distance[nextRow][nextCol];

            if (nextDistance != -1 && nextDistance < bestDistance) {
                bestCol = nextCol;
                bestRow = nextRow;
                bestDistance = nextDistance;
            }
        }

        return new double[] { bestCol + 0.5, bestRow + 0.5 };
    }
}
