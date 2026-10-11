import java.awt.image.BufferedImage;

// หลุมไฟ: ยืนทับแล้วโดนไฟลวกทุก 0.5 วินาที
public class FirePit extends Obstacle {

    private static final double DAMAGE = 8;
    private static final long DAMAGE_INTERVAL_MS = 500;

    private static BufferedImage image = ImageLoader.load("fire.png");

    private long lastDamageTime = 0;

    public FirePit(double x, double y) {
        super(x, y);
    }

    @Override
    public void update(Player player) {

        long now = System.currentTimeMillis();

        if (isPlayerOnTop(player) && now - lastDamageTime >= DAMAGE_INTERVAL_MS) {

            player.takeDamage(DAMAGE);

            lastDamageTime = now;
        }
    }

    @Override
    public BufferedImage getImage() {
        return image;
    }
}
