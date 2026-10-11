import java.awt.image.BufferedImage;

// คลาสแม่ของศัตรูทุกประเภท (Grunt, Shooter, Boss)
public abstract class Enemy {

    public double x, y;
    public double hp, maxHp;
    public double speed;
    public double damage;
    public boolean alive = true;

    protected long lastAttackTime = 0;
    protected long attackCooldownMs;

    public Enemy(double x, double y, double hp, double speed, double damage, long attackCooldownMs) {

        this.x = x;
        this.y = y;

        this.hp = hp;
        this.maxHp = hp;

        this.speed = speed;
        this.damage = damage;

        this.attackCooldownMs = attackCooldownMs;
    }

    public abstract void update(Player player, ProjectileSpawner spawner);

    public abstract BufferedImage getImage();

    public abstract double getSizeScale();

    public void takeDamage(double dmg) {

        hp -= dmg;

        if (hp <= 0) {
            hp = 0;
            alive = false;
        }
    }

    // เดินเข้าหาผู้เล่น:
    // - ถ้ามองเห็นผู้เล่น (ไม่มีกำแพงกั้น) เดินตรงเข้าไปเลย
    // - ถ้ามองไม่เห็น เดินตามทางที่ PathFinder หาไว้ จะได้ไม่ติดกำแพง
    protected void chase(Player player) {

        if (GameMap.hasLineOfSight(x, y, player.x, player.y)) {

            moveToward(player.x, player.y);

        } else {

            double[] next = PathFinder.getNextStep(x, y);

            moveToward(next[0], next[1]);
        }
    }

    protected void moveToward(double tx, double ty) {

        double dx = tx - x;
        double dy = ty - y;

        double dist = Math.hypot(dx, dy);

        if (dist < 0.0001) {
            return;
        }

        double moveX = dx / dist * speed;
        double moveY = dy / dist * speed;

        double nextX = x + moveX;
        double nextY = y + moveY;

        if (!GameMap.isBlocked(nextX, y)) {
            x = nextX;
        }

        if (!GameMap.isBlocked(x, nextY)) {
            y = nextY;
        }
    }

    protected boolean canAttack() {
        return System.currentTimeMillis() - lastAttackTime >= attackCooldownMs;
    }

    protected void resetAttackTimer() {
        lastAttackTime = System.currentTimeMillis();
    }
}
