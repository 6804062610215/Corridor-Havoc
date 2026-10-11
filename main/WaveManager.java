import java.util.ArrayList;
import java.util.List;
import java.util.Random;

// จัดการทุกอย่างที่อยู่ในด่าน: คลื่นศัตรู ไอเทม กระสุน และอุปสรรค
public class WaveManager implements ProjectileSpawner {

    // ศัตรูเกิดห่างจากผู้เล่นอย่างน้อยกี่ช่อง (จะได้เดินเข้ามาหาผู้เล่น ไม่โผล่ข้างตัว)
    private static final double MIN_SPAWN_DISTANCE = 6.0;

    // ไอเทมสุ่มเกิดบนพื้นทุก ๆ กี่มิลลิวินาที และมีบนแผนที่ได้มากสุดกี่ชิ้น
    private static final long ITEM_SPAWN_INTERVAL_MS = 15000;
    private static final int MAX_ITEMS_ON_MAP = 5;

    private Level level;

    private int currentWave = 0;
    private boolean bossWaveActive = false;

    private long lastItemSpawnTime = 0;

    private final List<Enemy> enemies = new ArrayList<>();
    private final List<Item> items = new ArrayList<>();
    private final List<Projectile> projectiles = new ArrayList<>();
    private final List<Obstacle> obstacles = new ArrayList<>();

    private List<double[]> spawnPoints = new ArrayList<>();
    private final Random random = new Random();

    // ========================================
    // LEVEL
    // ========================================

    // เริ่มด่านใหม่ (ต้องเรียก GameMap.load() ก่อน)
    public void startLevel(Level level, Player player) {

        this.level = level;

        currentWave = 0;
        bossWaveActive = false;
        lastItemSpawnTime = System.currentTimeMillis();

        enemies.clear();
        items.clear();
        projectiles.clear();
        obstacles.clear();

        spawnPoints = GameMap.getFloorSpawnPoints();

        loadObstacles();

        startNextWave(player);
    }

    // อ่านตัวอักษรในแผนที่ แล้วสร้างอุปสรรคตามชนิด
    private void loadObstacles() {

        for (int row = 0; row < GameMap.getRows(); row++) {

            for (int col = 0; col < GameMap.getCols(); col++) {

                char c = GameMap.MAP[row].charAt(col);

                double x = col + 0.5;
                double y = row + 0.5;

                if (c == 'F') {
                    obstacles.add(new FirePit(x, y));
                } else if (c == 'S') {
                    obstacles.add(new SpikeTrap(x, y));
                } else if (c == 'B') {
                    obstacles.add(new ExplosiveBarrel(x, y));
                }
            }
        }
    }

    // ด่านนี้ผ่านแล้วหรือยัง (ศัตรูหมดทุกคลื่น รวมถึงบอสถ้ามี)
    public boolean isLevelCleared() {

        if (!isWaveCleared() || hasMoreRegularWaves()) {
            return false;
        }

        if (level.hasBoss && !bossWaveActive) {
            return false;
        }

        return true;
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

        // ด่านยิ่งสูง ศัตรูยิ่งเยอะ
        int gruntCount = 1 + currentWave + level.number;
        int shooterCount = currentWave - 1 + (level.number - 1);

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

    // ยังมีคลื่นศัตรูธรรมดาเหลืออีกไหม (ถ้าด่านมีบอส คลื่นสุดท้ายเป็นของบอส)
    public boolean hasMoreRegularWaves() {

        int regularWaves = level.totalWaves;

        if (level.hasBoss) {
            regularWaves = level.totalWaves - 1;
        }

        return currentWave < regularWaves;
    }

    public boolean isBossWave() {
        return bossWaveActive;
    }

    public int getCurrentWave() {
        return currentWave;
    }

    public int getTotalWaves() {
        return level.totalWaves;
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

    public List<Obstacle> getObstacles() {
        return obstacles;
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
    // OBSTACLES
    // ========================================

    public void updateObstacles(Player player) {

        for (int i = obstacles.size() - 1; i >= 0; i--) {

            Obstacle o = obstacles.get(i);

            o.update(player);

            if (o.isFinished()) {
                obstacles.remove(i);
            }
        }
    }

    // ถังระเบิดแตก: ทำดาเมจศัตรูและผู้เล่นที่อยู่ในรัศมี และทำให้ถังข้าง ๆ ระเบิดต่อ
    public void explodeBarrel(ExplosiveBarrel barrel, Player player) {

        if (barrel.isExploded()) {
            return;
        }

        barrel.explode();

        for (Enemy e : enemies) {

            if (e.alive && Math.hypot(e.x - barrel.x, e.y - barrel.y) < ExplosiveBarrel.BLAST_RADIUS) {

                e.takeDamage(ExplosiveBarrel.DAMAGE_TO_ENEMY);

                if (!e.alive) {
                    maybeDropItem(e.x, e.y);
                }
            }
        }

        if (Math.hypot(player.x - barrel.x, player.y - barrel.y) < ExplosiveBarrel.BLAST_RADIUS) {
            player.takeDamage(ExplosiveBarrel.DAMAGE_TO_PLAYER);
        }

        // ถังที่อยู่ใกล้ ๆ ระเบิดตามไปด้วย (เรียกเมธอดตัวเองซ้ำ = recursion)
        for (Obstacle o : obstacles) {

            if (o instanceof ExplosiveBarrel) {

                ExplosiveBarrel other = (ExplosiveBarrel) o;

                if (!other.isExploded() &&
                        Math.hypot(other.x - barrel.x, other.y - barrel.y) < ExplosiveBarrel.BLAST_RADIUS) {

                    explodeBarrel(other, player);
                }
            }
        }
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
