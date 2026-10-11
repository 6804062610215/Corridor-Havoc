public class Player {

    public double x, y;
    public double angle;

    public double hp;
    public final double maxHp = 100;

    public int magazineAmmo;
    public int reserveAmmo;

    public boolean reloading = false;
    private long reloadEndTime = 0;

    public Weapon weapon;

    private long damageBuffEndTime = 0;

    public Player(double x, double y, Weapon weapon) {

        this.x = x;
        this.y = y;
        this.angle = 0;

        this.hp = maxHp;

        this.weapon = weapon;
        this.magazineAmmo = weapon.magazineSize;
        this.reserveAmmo = weapon.startingReserve;
    }

    public void takeDamage(double dmg) {

        hp -= dmg;

        if (hp < 0) {
            hp = 0;
        }

        Sound.play("player_hurt.wav");
    }

    public void heal(double amount) {
        hp = Math.min(maxHp, hp + amount);
    }

    public void addAmmo(int amount) {
        reserveAmmo += amount;
    }

    public boolean isDead() {
        return hp <= 0;
    }

    public void activateDamageBuff(long durationMs) {
        damageBuffEndTime = System.currentTimeMillis() + durationMs;
    }

    public boolean isDamageBuffActive() {
        return System.currentTimeMillis() < damageBuffEndTime;
    }

    public double getCurrentDamage() {
        return weapon.damage * (isDamageBuffActive() ? 2.0 : 1.0);
    }

    // ========================================
    // RELOAD
    // ========================================

    public boolean canReload() {
        return !reloading && magazineAmmo < weapon.magazineSize && reserveAmmo > 0;
    }

    public void startReload() {

        if (!canReload()) {
            return;
        }

        reloading = true;
        reloadEndTime = System.currentTimeMillis() + weapon.reloadTimeMs;

        Sound.play("reload.wav");
    }

    public void updateReload() {

        if (reloading && System.currentTimeMillis() >= reloadEndTime) {

            int needed = weapon.magazineSize - magazineAmmo;
            int loaded = Math.min(needed, reserveAmmo);

            magazineAmmo += loaded;
            reserveAmmo -= loaded;

            reloading = false;
        }
    }

    public double getReloadProgress() {

        if (!reloading) {
            return 1.0;
        }

        long remaining = reloadEndTime - System.currentTimeMillis();

        double progress = 1.0 - ((double) remaining / weapon.reloadTimeMs);

        return Math.max(0.0, Math.min(1.0, progress));
    }
}
