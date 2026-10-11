import java.awt.image.BufferedImage;

// ถังระเบิด: ขวางทางเดิน ยิงแล้วระเบิด ทำดาเมจทุกคนที่อยู่ใกล้ (ทั้งศัตรูและผู้เล่น)
public class ExplosiveBarrel extends Obstacle {

    public static final double BLAST_RADIUS = 2.0;
    public static final double DAMAGE_TO_ENEMY = 60;
    public static final double DAMAGE_TO_PLAYER = 25;

    // แสดงภาพระเบิดนานเท่าไหร่ก่อนหายไป
    private static final long EXPLOSION_SHOW_MS = 400;

    private static BufferedImage barrelImage = ImageLoader.load("barrel.png");
    private static BufferedImage explosionImage = ImageLoader.load("explosion.png");

    private boolean exploded = false;
    private long explodeTime = 0;

    public ExplosiveBarrel(double x, double y) {
        super(x, y);

        // ถังวางขวางช่องนี้ไว้ เดินผ่านไม่ได้
        GameMap.setBlocked((int) x, (int) y, true);
    }

    public boolean isExploded() {
        return exploded;
    }

    public void explode() {

        exploded = true;
        explodeTime = System.currentTimeMillis();

        // ถังหายไปแล้ว ช่องนี้เดินผ่านได้
        GameMap.setBlocked((int) x, (int) y, false);

        Sound.play("explosion.wav");
    }

    @Override
    public void update(Player player) {
        // ถังเฉย ๆ ไม่ทำอะไร จะระเบิดเมื่อถูกยิงเท่านั้น
    }

    @Override
    public BufferedImage getImage() {

        if (exploded) {
            return explosionImage;
        }

        return barrelImage;
    }

    @Override
    public double getSizeScale() {

        if (exploded) {
            return 1.0;
        }

        return 0.5;
    }

    @Override
    public boolean isGrounded() {
        return !exploded;
    }

    @Override
    public boolean isFinished() {
        return exploded && System.currentTimeMillis() - explodeTime > EXPLOSION_SHOW_MS;
    }
}
