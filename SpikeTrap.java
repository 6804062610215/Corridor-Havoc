import java.awt.image.BufferedImage;

// กับดักหนาม: หนามยื่นขึ้นและหดลงเป็นจังหวะ อันตรายเฉพาะตอนหนามยื่นขึ้น
public class SpikeTrap extends Obstacle {

    private static final double DAMAGE = 20;

    // 1 รอบ = หดลง 2 วินาที แล้วยื่นขึ้น 1.5 วินาที
    private static final long CYCLE_MS = 3500;
    private static final long DOWN_MS = 2000;

    private static BufferedImage downImage = ImageLoader.load("spikes_down.png");
    private static BufferedImage upImage = ImageLoader.load("spikes_up.png");

    // กับดักแต่ละอันเริ่มไม่พร้อมกัน จะได้ไม่ยื่นขึ้นพร้อมกันหมด
    private long offsetMs;

    private long lastDamageTime = 0;

    public SpikeTrap(double x, double y) {
        super(x, y);
        offsetMs = (long) ((x + y) * 700);
    }

    public boolean isUp() {

        long timeInCycle = (System.currentTimeMillis() + offsetMs) % CYCLE_MS;

        return timeInCycle >= DOWN_MS;
    }

    @Override
    public void update(Player player) {

        long now = System.currentTimeMillis();

        // โดนได้ไม่เกิน 1 ครั้งต่อการยื่นขึ้นหนึ่งรอบ
        if (isUp() && isPlayerOnTop(player) && now - lastDamageTime >= CYCLE_MS - DOWN_MS) {

            player.takeDamage(DAMAGE);

            lastDamageTime = now;
        }
    }

    @Override
    public BufferedImage getImage() {

        if (isUp()) {
            return upImage;
        }

        return downImage;
    }
}
