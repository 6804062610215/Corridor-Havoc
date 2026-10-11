import java.awt.Color;

// ข้อมูลของด่านหนึ่งด่าน: แผนที่ จุดเกิด จำนวนคลื่น และสีของฉาก
public class Level {

    public final int number;
    public final String name;
    public final String[] map;

    public final double startX;
    public final double startY;
    public final double startAngle;

    // จำนวนคลื่นทั้งหมดของด่าน (ถ้ามีบอส คลื่นสุดท้ายคือคลื่นบอส)
    public final int totalWaves;
    public final boolean hasBoss;

    public final Color wallColor;
    public final Color ceilingColor;
    public final Color floorColor;

    public Level(int number, String name, String[] map,
            double startX, double startY, double startAngle,
            int totalWaves, boolean hasBoss,
            Color wallColor, Color ceilingColor, Color floorColor) {

        this.number = number;
        this.name = name;
        this.map = map;
        this.startX = startX;
        this.startY = startY;
        this.startAngle = startAngle;
        this.totalWaves = totalWaves;
        this.hasBoss = hasBoss;
        this.wallColor = wallColor;
        this.ceilingColor = ceilingColor;
        this.floorColor = floorColor;
    }
}
