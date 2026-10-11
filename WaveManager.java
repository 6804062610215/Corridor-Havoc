import java.util.ArrayList;
import java.util.List;
import java.util.Random;

// จัดการคลื่นศัตรู (Wave), ไอเทม และกระสุนที่อยู่ในด่าน
public class WaveManager implements ProjectileSpawner {

    // ทั้งหมด 5 คลื่นตามเอกสารเกม: คลื่น 1-4 เป็นศัตรูธรรมดา, คลื่นที่ 5 เป็นบอส
    public static final int TOTAL_WAVES = 5;

    // ศัตรูเกิดห่างจากผู้เล่นอย่างน้อยกี่ช่อง (จะได้เดินเข้ามาหาผู้เล่น ไม่โผล่ข้างตัว)
    private static final double MIN_SPAWN_DISTANCE = 6.0;

    // ไอเทมสุ่มเกิดบนพื้นทุก ๆ กี่มิลลิวินาที และมีบนแผนที่ได้มากสุดกี่ชิ้น
    private static final long ITEM_SPAWN_INTERVAL_MS = 15000;
    private static final int MAX_ITEMS_ON_MAP = 5;

    private int currentWave = 0;
    private boolean bossWaveActive = false;

    private long lastItemSpawnTime = 0;

    private final List<Enemy> enemies = new ArrayList<>();
    private final List<Item> items = new ArrayList<>();
    private final List<Projectile> projectiles = new ArrayList<>();

    private final List<double[]> spawnPoints;
    private final Random random = new Random();

    public WaveManager(List<double[]> spawnPoints) {
        this.spawnPoints = spawnPoints;
    }

    public void reset() {
        currentWave = 0;
        bossWaveActive = false;
        lastItemSpawnTime = System.currentTimeMillis();
        enemies.clear();
        items.clear();
        projectiles.clear();
    }

    private double[] pickSpawnPoint(Player player) {

        double[] fallback = spawnPoints.get(random.nextInt(spawnPoints.size()));

        for (int tries = 0; tries < 20; tries++) {

            double[] candidate = spawnPoints.get(random.nextInt(spawnPoints.size()));

            if (Math.hypot(candidate[0] - player.x, candidate[1] - player.y) > MIN_SPAWN_DISTANCE) {
                return candidate;
            }
        }

        return fallback;
    }

    // ========================================
    // WAVES
    // ========================================

    public void startNextWave(Player player) {

        currentWave++;

        enemies.clear();

        int gruntCount = 2 + currentWave;
        int shooterCount = currentWave - 1;

        for (int i = 0; i < gruntCount; i++) {

            double[] p = pickSpawnPoint(player);

            enemies.add(new Grunt(p[0], p[1]));
        }

        for (int i = 0; i < shooterCount; i++) {

            double[] p = pickSpawnPoint(player);

            enemies.add(new Shooter(p[0], p[1]));
        }

        Sound.play("wave_start.wav");
    }

    public void startBossWave(Player player) {

        currentWave++;

        bossWaveActive = true;

        enemies.clear();

        double[] p = pickSpawnPoint(player);

        enemies.add(new Boss(p[0], p[1]));

        Sound.play("boss_appear.wav");
    }

    public boolean isWaveCleared() {

        for (Enemy e : enemies) {

            if (e.alive) {
                return false;
            }
        }

        return true;
    }

    // ยังมีคลื่นศัตรูธรรมดาเหลืออีกไหม (คลื่นสุดท้ายเป็นของบอส)
    public boolean hasMoreRegularWaves() {
        return currentWave < TOTAL_WAVES - 1;
    }

    public boolean isBossWave() {
        return bossWaveActive;
    }

    public int getCurrentWave() {
        return currentWave;
    }

    // ========================================
    // GETTERS
    // ========================================

    public List<Enemy> getEnemies() {
        return enemies;
    }

    public List<Item> getItems() {
        return items;
    }

    public List<Projectile> getProjectiles() {
        return projectiles;
    }

    // ========================================
    // ITEMS
    // ========================================

    // ศัตรูตายแล้วมีโอกาสทิ้งไอเทม
    public void maybeDropItem(double x, double y) {

        if (random.nextDouble() < 0.35) {
            items.add(new Item(x, y, randomItemType()));
        }
    }

    // ไอเทมสุ่มเกิดบนพื้นเป็นระยะ ๆ ระหว่างต่อสู้ (ตามเอกสารเกม)
    public void updateItemSpawning() {

        long now = System.currentTimeMillis();

        if (now - lastItemSpawnTime < ITEM_SPAWN_INTERVAL_MS) {
            return;
        }

        lastItemSpawnTime = now;

        if (items.size() >= MAX_ITEMS_ON_MAP) {
            return;
        }

        double[] p = spawnPoints.get(random.nextInt(spawnPoints.size()));

        items.add(new Item(p[0], p[1], randomItemType()));
    }

    private ItemType randomItemType() {

        ItemType[] types = ItemType.values();

        return types[random.nextInt(types.length)];
    }

    // ========================================
    // UPDATE
    // ========================================

    @Override
    public void addProjectile(Projectile p) {

        projectiles.add(p);

        if (!p.fromPlayer) {
            Sound.play("enemy_shoot.wav");
        }
    }

    public void updateEnemies(Player player) {

        // คำนวณทางเดินไปหาผู้เล่นใหม่ทุกเฟรม ให้ศัตรูทุกตัวใช้ร่วมกัน
        PathFinder.update(player.x, player.y);

        for (Enemy e : enemies) {

            if (e.alive) {
                e.update(player, this);
            }
        }
    }

    public void updateProjectiles(double deltaSeconds, Player player) {

        for (int i = projectiles.size() - 1; i >= 0; i--) {

            Projectile p = projectiles.get(i);

            p.update(deltaSeconds);

            // ลูกไฟของศัตรูชนผู้เล่น
            if (p.alive && !p.fromPlayer) {

                double dist = Math.hypot(p.x - player.x, p.y - player.y);

                if (dist < 0.35) {
                    player.takeDamage(p.damage);
                    p.alive = false;
                }
            }

            if (!p.alive) {
                projectiles.remove(i);
            }
        }
    }
}
