import java.awt.image.BufferedImage;

// ศัตรูระยะไกล: รักษาระยะห่างแล้วยิงผู้เล่นถ้ามองเห็น
public class Shooter extends Enemy {

    private static final double PREFERRED_RANGE = 3.5;
    private static final double MIN_RANGE = 2.5;
    private static final double FIRE_RANGE = 6.0;
    private static final double FIREBALL_SPEED = 3.5;

    private static BufferedImage image = ImageLoader.load("shooter.png");

    public Shooter(double x, double y) {
        super(x, y, 20, 0.015, 8, 1400);
    }

    @Override
    public void update(Player player, ProjectileSpawner spawner) {

        double dist = Math.hypot(player.x - x, player.y - y);

        boolean canSeePlayer = GameMap.hasLineOfSight(x, y, player.x, player.y);

        if (dist > PREFERRED_RANGE || !canSeePlayer) {

            // ยังไกลไป หรือมีกำแพงบัง ให้เดินเข้าหาผู้เล่น
            chase(player);

        } else if (dist < MIN_RANGE) {

            // ใกล้เกินไป ถอยห่างจากผู้เล่น
            moveToward(2 * x - player.x, 2 * y - player.y);
        }

        if (canAttack() && dist <= FIRE_RANGE && canSeePlayer) {

            // ยิงลูกไฟไปทางผู้เล่น ผู้เล่นจะโดนดาเมจเมื่อลูกไฟชนตัวเท่านั้น (หลบได้)
            double angle = Math.atan2(player.y - y, player.x - x);

            spawner.addProjectile(new Projectile(x, y, angle, FIREBALL_SPEED, 10, damage, false));

            resetAttackTimer();
        }
    }

    @Override
    public BufferedImage getImage() {
        return image;
    }

    @Override
    public double getSizeScale() {
        return 0.6;
    }
}
