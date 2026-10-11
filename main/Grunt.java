import java.awt.image.BufferedImage;

// ศัตรูประชิดตัว: วิ่งเข้าหาผู้เล่นแล้วโจมตีระยะประชิด
public class Grunt extends Enemy {

    private static final double MELEE_RANGE = 0.6;

    private static BufferedImage image = ImageLoader.load("grunt.png");

    public Grunt(double x, double y) {
        super(x, y, 30, 0.020, 10, 900);
    }

    @Override
    public void update(Player player, ProjectileSpawner spawner) {

        double dist = Math.hypot(player.x - x, player.y - y);

        if (dist > MELEE_RANGE) {

            chase(player);

        } else if (canAttack()) {

            player.takeDamage(damage);

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
