import java.awt.image.BufferedImage;

// ศัตรูตัวสุดท้าย: HP สูง โจมตีได้ทั้งประชิดและระยะไกล
public class Boss extends Enemy {

    private static final double MELEE_RANGE = 1.0;
    private static final double FIRE_RANGE = 6.0;
    private static final double FIREBALL_SPEED = 4.0;

    private static BufferedImage image = ImageLoader.load("boss.png");

    public Boss(double x, double y) {
        super(x, y, 250, 0.012, 20, 1200);
    }

    @Override
    public void update(Player player, ProjectileSpawner spawner) {

        double dist = Math.hypot(player.x - x, player.y - y);

        if (dist > MELEE_RANGE) {
            chase(player);
        }

        if (canAttack()) {

            if (dist <= MELEE_RANGE) {

                player.takeDamage(damage);

                resetAttackTimer();

            } else if (dist <= FIRE_RANGE &&
                    GameMap.hasLineOfSight(x, y, player.x, player.y)) {

                double angle = Math.atan2(player.y - y, player.x - x);

                spawner.addProjectile(new Projectile(x, y, angle, FIREBALL_SPEED, 10, damage * 0.75, false));

                resetAttackTimer();
            }
        }
    }

    @Override
    public BufferedImage getImage() {
        return image;
    }

    @Override
    public double getSizeScale() {
        return 1.1;
    }
}
