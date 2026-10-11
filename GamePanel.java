import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.List;

// หัวใจของเกม: เก็บสถานะเกม, game loop, การเดิน และการยิง
public class GamePanel extends JPanel implements ActionListener {

    // ========================================
    // GAME SETTINGS
    // ========================================

    public static final int SCREEN_WIDTH = 800;
    public static final int SCREEN_HEIGHT = 600;

    static final double MOVE_SPEED = 0.05;
    static final double ROTATION_SPEED = Math.toRadians(3);

    // มุมมองเล็งต้องอยู่ใกล้กากบาทกลางจอแค่ไหนถึงจะยิงโดน
    static final double SHOOT_ANGLE_TOLERANCE = Math.toRadians(4.0);

    // ========================================
    // GAME OBJECTS
    // ========================================

    GameState gameState = GameState.LOADOUT_SELECT;

    Player player;
    WaveManager waveManager;

    Renderer renderer;
    Hud hud;
    MenuScreen menuScreen;
    InputHandler input;

    long lastShotTime = 0;
    long lastFlashTime = 0;   // เวลาที่ยิงออกจริงล่าสุด ใช้แสดงไฟปากกระบอกและแรงสะท้อน

    // ภาพเกมขนาด 800x600 จะถูกขยายให้เต็มจอ
    BufferedImage screen;

    // ค่าที่ใช้ขยายภาพเกมให้เต็มจอ (คำนวณใหม่ทุกครั้งที่วาด)
    double scale = 1.0;
    int offsetX = 0;
    int offsetY = 0;

    // เคอร์เซอร์ที่มองไม่เห็น ใช้ซ่อนเมาส์ระหว่างเล่น
    Cursor blankCursor;

    Timer timer;

    // ========================================
    // CONSTRUCTOR
    // ========================================

    public GamePanel() {

        setPreferredSize(new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT));

        setFocusable(true);

        // พื้นที่ที่เหลือจากการขยายภาพ (แถบด้านข้าง) เป็นสีดำ
        setBackground(Color.BLACK);

        screen = new BufferedImage(SCREEN_WIDTH, SCREEN_HEIGHT, BufferedImage.TYPE_INT_RGB);

        blankCursor = getToolkit().createCustomCursor(
                new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB), new Point(0, 0), "blank");

        waveManager = new WaveManager(GameMap.getFloorSpawnPoints());

        renderer = new Renderer(screen);
        hud = new Hud();
        menuScreen = new MenuScreen();

        input = new InputHandler(this);

        addKeyListener(input);
        addMouseListener(input);
        addMouseMotionListener(input);

        Sound.loadAll();

        // Timer จะเรียก actionPerformed() ทุก 16 ms (ประมาณ 60 ครั้งต่อวินาที)
        timer = new Timer(16, this);
        timer.start();
    }

    // ========================================
    // GAME LOOP
    // ========================================

    @Override
    public void actionPerformed(ActionEvent e) {

        if (gameState == GameState.PLAYING) {

            updatePlayerMovement();

            player.updateReload();

            // ปืนกล (automatic) ยิงต่อเนื่องเมื่อกดเมาส์ค้าง
            if (input.mouseHeldDown && player.weapon.automatic) {
                fireWeapon();
            }

            updateEnemiesAndItems();
            checkWaveProgression();
            checkGameOverOrComplete();
        }

        renderGame();

        repaint();
    }

    // ========================================
    // GAME FLOW
    // ========================================

    public void startNewRun(Weapon weapon) {

        player = new Player(GameMap.PLAYER_START_X, GameMap.PLAYER_START_Y, weapon);

        waveManager.reset();
        waveManager.startNextWave(player);

        gameState = GameState.PLAYING;

        Sound.loop("music.wav");

        // ซ่อนเมาส์ แล้วย้ายเมาส์ไปไว้กลางจอก่อนเริ่มเล่น
        setCursor(blankCursor);
        input.moveMouseToCenter();
    }

    // ปุ่ม R: ระหว่างเล่น = รีโหลด, หลังจบเกม = กลับไปหน้าเลือกอาวุธ
    public void onReloadKey() {

        if (gameState == GameState.PLAYING) {

            player.startReload();

        } else if (gameState == GameState.GAME_OVER || gameState == GameState.MISSION_COMPLETE) {

            gameState = GameState.LOADOUT_SELECT;
        }
    }

    void checkWaveProgression() {

        if (!waveManager.isWaveCleared()) {
            return;
        }

        if (waveManager.isBossWave()) {
            // บอสตายแล้ว รอ checkGameOverOrComplete จัดการ MISSION_COMPLETE
            return;
        }

        if (waveManager.hasMoreRegularWaves()) {
            waveManager.startNextWave(player);
        } else {
            waveManager.startBossWave(player);
        }
    }

    void checkGameOverOrComplete() {

        if (player.isDead()) {

            gameState = GameState.GAME_OVER;

            Sound.stop("music.wav");
            Sound.play("game_over.wav");

            setCursor(Cursor.getDefaultCursor());

        } else if (waveManager.isBossWave() && waveManager.isWaveCleared()) {

            gameState = GameState.MISSION_COMPLETE;

            Sound.stop("music.wav");
            Sound.play("mission_complete.wav");

            setCursor(Cursor.getDefaultCursor());
        }
    }

    // ========================================
    // UPDATE - PLAYER MOVEMENT
    // ========================================

    void updatePlayerMovement() {

        if (input.turnLeft) {
            player.angle -= ROTATION_SPEED;
        }

        if (input.turnRight) {
            player.angle += ROTATION_SPEED;
        }

        double moveX = 0;
        double moveY = 0;

        if (input.forward) {
            moveX += Math.cos(player.angle) * MOVE_SPEED;
            moveY += Math.sin(player.angle) * MOVE_SPEED;
        }

        if (input.backward) {
            moveX -= Math.cos(player.angle) * MOVE_SPEED;
            moveY -= Math.sin(player.angle) * MOVE_SPEED;
        }

        // เดินสไลด์ = เดินไปทางที่ตั้งฉากกับทิศที่มอง (มุม +/- 90 องศา)
        if (input.strafeLeft) {
            moveX += Math.cos(player.angle - Math.PI / 2) * MOVE_SPEED;
            moveY += Math.sin(player.angle - Math.PI / 2) * MOVE_SPEED;
        }

        if (input.strafeRight) {
            moveX += Math.cos(player.angle + Math.PI / 2) * MOVE_SPEED;
            moveY += Math.sin(player.angle + Math.PI / 2) * MOVE_SPEED;
        }

        double nextX = player.x + moveX;
        double nextY = player.y + moveY;

        if (!GameMap.isWall(nextX, player.y)) {
            player.x = nextX;
        }

        if (!GameMap.isWall(player.x, nextY)) {
            player.y = nextY;
        }
    }

    // ========================================
    // UPDATE - ENEMIES / ITEMS
    // ========================================

    void updateEnemiesAndItems() {

        waveManager.updateEnemies(player);
        waveManager.updateProjectiles(0.016, player);
        waveManager.updateItemSpawning();

        List<Item> items = waveManager.getItems();

        for (int i = items.size() - 1; i >= 0; i--) {

            Item item = items.get(i);

            double dist = Math.hypot(player.x - item.x, player.y - item.y);

            if (dist < 0.5) {

                item.apply(player);

                items.remove(i);
            }
        }
    }

    // ========================================
    // SHOOTING
    // ========================================

    public void fireWeapon() {

        if (player.reloading) {
            return;
        }

        long now = System.currentTimeMillis();

        if (now - lastShotTime < player.weapon.getCooldownMs()) {
            return;
        }

        lastShotTime = now;

        if (player.magazineAmmo <= 0) {
            Sound.play("empty.wav");
            return;
        }

        player.magazineAmmo--;
        lastFlashTime = now;

        Sound.play(player.weapon.shootSound);

        // ----------------------------
        // หาศัตรูที่อยู่ตรงกากบาทและใกล้ที่สุด
        // ----------------------------

        Enemy target = null;
        double bestDist = Double.MAX_VALUE;

        for (Enemy e : waveManager.getEnemies()) {

            if (!e.alive) {
                continue;
            }

            double dx = e.x - player.x;
            double dy = e.y - player.y;

            double dist = Math.hypot(dx, dy);

            double angleToTarget = normalizeAngle(Math.atan2(dy, dx) - player.angle);

            if (Math.abs(angleToTarget) <= SHOOT_ANGLE_TOLERANCE && dist < bestDist) {

                // ต้องไม่มีกำแพงบังระหว่างผู้เล่นกับศัตรู
                if (GameMap.hasLineOfSight(player.x, player.y, e.x, e.y)) {
                    target = e;
                    bestDist = dist;
                }
            }
        }

        // ----------------------------
        // กระสุนที่มองเห็นได้ วิ่งไปจนถึงศัตรูที่โดน หรือถึงกำแพงตรงกลางจอ
        // ----------------------------

        double bulletRange;

        if (target != null) {
            bulletRange = bestDist;
        } else {
            bulletRange = renderer.getCenterWallDistance();
        }

        double startDist = 0.6;

        if (bulletRange > startDist) {

            double bulletX = player.x + Math.cos(player.angle) * startDist;
            double bulletY = player.y + Math.sin(player.angle) * startDist;

            waveManager.addProjectile(new Projectile(
                    bulletX, bulletY, player.angle, 25, bulletRange - startDist, 0, true));
        }

        // ----------------------------
        // ทำดาเมจ
        // ----------------------------

        if (target != null) {

            target.takeDamage(player.getCurrentDamage());

            if (target.alive) {
                Sound.play("enemy_hit.wav");
            } else {
                Sound.play("enemy_death.wav");
                waveManager.maybeDropItem(target.x, target.y);
            }
        }
    }

    // ทำให้มุมอยู่ในช่วง -PI ถึง PI
    public static double normalizeAngle(double angle) {

        while (angle > Math.PI) {
            angle -= 2 * Math.PI;
        }

        while (angle < -Math.PI) {
            angle += 2 * Math.PI;
        }

        return angle;
    }

    // ========================================
    // RENDER
    // ========================================

    void renderGame() {

        Graphics2D g = screen.createGraphics();

        switch (gameState) {

            case LOADOUT_SELECT:
                menuScreen.drawLoadout(g);
                break;

            case PLAYING:
                renderer.drawWorld(g, player, waveManager);
                hud.drawGun(g, player, lastFlashTime);
                hud.drawBar(g, player, waveManager);
                break;

            case MISSION_COMPLETE:
                menuScreen.drawEndScreen(g, "MISSION COMPLETE", new Color(255, 210, 40));
                break;

            case GAME_OVER:
                menuScreen.drawEndScreen(g, "GAME OVER", new Color(220, 40, 40));
                break;
        }

        g.dispose();
    }

    @Override
    protected void paintComponent(Graphics g) {

        super.paintComponent(g);

        // ขยายภาพเกม 800x600 ให้ใหญ่ที่สุดที่ยังพอดีจอ โดยรักษาสัดส่วนไว้
        double scaleX = (double) getWidth() / SCREEN_WIDTH;
        double scaleY = (double) getHeight() / SCREEN_HEIGHT;

        scale = Math.min(scaleX, scaleY);

        int drawWidth = (int) (SCREEN_WIDTH * scale);
        int drawHeight = (int) (SCREEN_HEIGHT * scale);

        // วางภาพไว้กลางจอ
        offsetX = (getWidth() - drawWidth) / 2;
        offsetY = (getHeight() - drawHeight) / 2;

        g.drawImage(screen, offsetX, offsetY, drawWidth, drawHeight, null);
    }

    // แปลงตำแหน่งเมาส์บนจอจริง ให้เป็นตำแหน่งในภาพเกม 800x600
    public int toGameX(int panelX) {
        return (int) ((panelX - offsetX) / scale);
    }

    public int toGameY(int panelY) {
        return (int) ((panelY - offsetY) / scale);
    }
}
