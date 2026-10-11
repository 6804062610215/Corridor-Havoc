import java.awt.image.BufferedImage;

// กระสุนที่วิ่งเป็นเส้นตรง ใช้ทั้งลูกไฟของศัตรูและกระสุนของผู้เล่น
public class Projectile {

    private static BufferedImage fireballImage = ImageLoader.load("fireball.png");
    private static BufferedImage bulletImage = ImageLoader.load("bullet.png");

    public double x, y;
    public double dirX, dirY;

    public double speed;       // ช่องต่อวินาที
    public double traveled = 0;
    public double maxRange;

    public double damage;
    public boolean fromPlayer; // true = กระสุนผู้เล่น, false = ลูกไฟศัตรู

    public boolean alive = true;

    public Projectile(double x, double y, double angle, double speed,
            double maxRange, double damage, boolean fromPlayer) {

        this.x = x;
        this.y = y;

        this.dirX = Math.cos(angle);
        this.dirY = Math.sin(angle);

        this.speed = speed;
        this.maxRange = maxRange;
        this.damage = damage;
        this.fromPlayer = fromPlayer;
    }

    public void update(double deltaSeconds) {

        double moveDist = speed * deltaSeconds;

        x += dirX * moveDist;
        y += dirY * moveDist;

        traveled += moveDist;

        // วิ่งครบระยะ หรือชนกำแพง / ถังระเบิด
        if (traveled >= maxRange || GameMap.isBlocked(x, y)) {
            alive = false;
        }
    }

    public BufferedImage getImage() {

        if (fromPlayer) {
            return bulletImage;
        }

        return fireballImage;
    }

    public double getSizeScale() {

        if (fromPlayer) {
            return 0.12;
        }

        return 0.3;
    }
}
