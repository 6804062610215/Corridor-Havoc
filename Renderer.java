import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

// วาดฉาก 3D: เพดาน/พื้น, กำแพง (Raycasting แบบ DDA), ศัตรู/ไอเทม/กระสุน และกากบาท
public class Renderer {

    static final int SCREEN_WIDTH = GamePanel.SCREEN_WIDTH;
    static final int SCREEN_HEIGHT = GamePanel.SCREEN_HEIGHT;

    static final double FOV = Math.toRadians(60);

    static final int TEXTURE_SIZE = 64;

    private BufferedImage screen;

    // สีของ texture กำแพงแต่ละพิกเซล [x][y] เก็บไว้ล่วงหน้าจะได้ไม่ต้องอ่านจากรูปทุกเฟรม
    private Color[][] wallColors;

    // ระยะถึงกำแพงของแต่ละคอลัมน์บนจอ ใช้เช็คว่ากำแพงบังศัตรูหรือไม่
    private double[] zBuffer = new double[SCREEN_WIDTH];

    public Renderer(BufferedImage screen) {

        this.screen = screen;

        BufferedImage wallTexture = createWallTexture();

        wallColors = new Color[TEXTURE_SIZE][TEXTURE_SIZE];

        for (int x = 0; x < TEXTURE_SIZE; x++) {

            for (int y = 0; y < TEXTURE_SIZE; y++) {
                wallColors[x][y] = new Color(wallTexture.getRGB(x, y));
            }
        }
    }

    // ระยะถึงกำแพงตรงกลางจอ (ตรงกากบาท) ใช้กำหนดว่ากระสุนวิ่งไปได้ไกลแค่ไหน
    public double getCenterWallDistance() {
        return zBuffer[SCREEN_WIDTH / 2];
    }

    public void drawWorld(Graphics2D g, Player player, WaveManager waveManager) {

        // ====================================
        // CEILING / FLOOR
        // ====================================

        g.setColor(new Color(40, 40, 40));
        g.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT / 2);

        g.setColor(new Color(80, 80, 80));
        g.fillRect(0, SCREEN_HEIGHT / 2, SCREEN_WIDTH, SCREEN_HEIGHT / 2);

        drawWalls(player);

        drawSprites(player, waveManager);

        // ====================================
        // CROSSHAIR
        // ====================================

        g.setColor(Color.WHITE);

        g.drawLine(
                SCREEN_WIDTH / 2 - 5, SCREEN_HEIGHT / 2,
                SCREEN_WIDTH / 2 + 5, SCREEN_HEIGHT / 2);

        g.drawLine(
                SCREEN_WIDTH / 2, SCREEN_HEIGHT / 2 - 5,
                SCREEN_WIDTH / 2, SCREEN_HEIGHT / 2 + 5);
    }

    // ========================================
    // CREATE TEXTURE
    // ========================================

    private BufferedImage createWallTexture() {

        BufferedImage texture = new BufferedImage(
                TEXTURE_SIZE,
                TEXTURE_SIZE,
                BufferedImage.TYPE_INT_RGB);

        Graphics2D g = texture.createGraphics();

        // พื้น texture
        g.setColor(new Color(120, 120, 120));

        g.fillRect(0, 0, TEXTURE_SIZE, TEXTURE_SIZE);

        // สร้างลายอิฐ
        for (int y = 0; y < TEXTURE_SIZE; y += 16) {

            for (int x = 0; x < TEXTURE_SIZE; x += 32) {

                int offset = (y / 16 % 2) * 16;

                g.setColor(new Color(150, 150, 150));

                g.fillRect(x + offset, y, 30, 14);
            }
        }

        // เส้นปูน
        g.setColor(new Color(60, 60, 60));

        for (int y = 0; y < TEXTURE_SIZE; y += 16) {
            g.drawLine(0, y, TEXTURE_SIZE, y);
        }

        g.dispose();

        return texture;
    }

    // ========================================
    // WALLS - RAYCASTING (DDA)
    // ========================================

    private void drawWalls(Player player) {

        for (int x = 0; x < SCREEN_WIDTH; x++) {

            // ------------------------------------
            // มุมของรังสีสำหรับคอลัมน์นี้
            // ------------------------------------

            double cameraX = (double) x / SCREEN_WIDTH - 0.5;

            double rayAngle = player.angle + cameraX * FOV;

            double rayDirX = Math.cos(rayAngle);
            double rayDirY = Math.sin(rayAngle);

            // ------------------------------------
            // ช่องบนแผนที่ที่ผู้เล่นยืนอยู่
            // ------------------------------------

            int mapX = (int) player.x;
            int mapY = (int) player.y;

            // ------------------------------------
            // ระยะระหว่างเส้นกริดแนว X และแนว Y
            // ------------------------------------

            double deltaDistX;

            if (rayDirX == 0) {
                deltaDistX = Double.MAX_VALUE;
            } else {
                deltaDistX = Math.abs(1.0 / rayDirX);
            }

            double deltaDistY;

            if (rayDirY == 0) {
                deltaDistY = Double.MAX_VALUE;
            } else {
                deltaDistY = Math.abs(1.0 / rayDirY);
            }

            // ------------------------------------
            // ทิศทางการก้าว และระยะถึงเส้นกริดแรก
            // ------------------------------------

            int stepX;
            int stepY;

            double sideDistX;
            double sideDistY;

            if (rayDirX < 0) {
                stepX = -1;
                sideDistX = (player.x - mapX) * deltaDistX;
            } else {
                stepX = 1;
                sideDistX = (mapX + 1.0 - player.x) * deltaDistX;
            }

            if (rayDirY < 0) {
                stepY = -1;
                sideDistY = (player.y - mapY) * deltaDistY;
            } else {
                stepY = 1;
                sideDistY = (mapY + 1.0 - player.y) * deltaDistY;
            }

            // ------------------------------------
            // DDA: ก้าวไปทีละช่องจนชนกำแพง
            // ------------------------------------

            boolean hitWall = false;
            int side = 0;

            while (!hitWall) {

                // ไป X หรือ Y ก่อน?
                if (sideDistX < sideDistY) {
                    sideDistX += deltaDistX;
                    mapX += stepX;
                    side = 0;
                } else {
                    sideDistY += deltaDistY;
                    mapY += stepY;
                    side = 1;
                }

                if (GameMap.isWall(mapX, mapY)) {
                    hitWall = true;
                }
            }

            // ------------------------------------
            // ระยะถึงกำแพง
            // ------------------------------------

            double distance;

            if (side == 0) {
                distance = (mapX - player.x + (1 - stepX) / 2.0) / rayDirX;
            } else {
                distance = (mapY - player.y + (1 - stepY) / 2.0) / rayDirY;
            }

            // ป้องกันค่าติดลบหรือศูนย์
            if (distance < 0.01) {
                distance = 0.01;
            }

            zBuffer[x] = distance;

            // ------------------------------------
            // ความสูงของกำแพงบนจอ
            // ------------------------------------

            int wallHeight = (int) (SCREEN_HEIGHT / distance);
            int wallTop = (SCREEN_HEIGHT - wallHeight) / 2;
            int wallBottom = wallTop + wallHeight;

            // ------------------------------------
            // ตำแหน่ง x ใน texture
            // ------------------------------------

            double wallHit;

            if (side == 0) {
                wallHit = player.y + distance * rayDirY;   // ชนด้าน X
            } else {
                wallHit = player.x + distance * rayDirX;   // ชนด้าน Y
            }

            // เอาเฉพาะส่วนหลังจุดทศนิยม
            wallHit -= Math.floor(wallHit);

            int textureX = (int) (wallHit * TEXTURE_SIZE);
            textureX = Math.max(0, Math.min(TEXTURE_SIZE - 1, textureX));

            // ------------------------------------
            // ความมืดตามระยะทาง
            // ------------------------------------

            double shade = 1.0 / (1.0 + distance * 0.25);

            // ด้านที่หันออกด้านข้างมืดกว่า
            if (side == 1) {
                shade *= 0.7;
            }

            // ------------------------------------
            // วาดกำแพงทีละพิกเซลในคอลัมน์นี้
            // ------------------------------------

            for (int y = Math.max(0, wallTop); y < Math.min(SCREEN_HEIGHT, wallBottom); y++) {

                double wallPosition = (double) (y - wallTop) / wallHeight;

                int textureY = (int) (wallPosition * TEXTURE_SIZE);
                textureY = Math.max(0, Math.min(TEXTURE_SIZE - 1, textureY));

                Color textureColor = wallColors[textureX][textureY];

                int red = Math.max(20, (int) (textureColor.getRed() * shade));
                int green = Math.max(20, (int) (textureColor.getGreen() * shade));
                int blue = Math.max(20, (int) (textureColor.getBlue() * shade));

                screen.setRGB(x, y, new Color(red, green, blue).getRGB());
            }
        }
    }

    // ========================================
    // SPRITES (ศัตรู / ไอเทม / กระสุน)
    // ========================================

    private void drawSprites(Player player, WaveManager waveManager) {

        ArrayList<SpriteData> sprites = new ArrayList<>();

        // ----------------------------
        // รวบรวมทุกอย่างที่ต้องวาด
        // ----------------------------

        for (Enemy e : waveManager.getEnemies()) {

            if (e.alive) {
                sprites.add(new SpriteData(e.x, e.y, e.getImage(), e.getSizeScale(), false));
            }
        }

        for (Item item : waveManager.getItems()) {
            sprites.add(new SpriteData(item.x, item.y, item.getImage(), 0.3, true));
        }

        for (Projectile p : waveManager.getProjectiles()) {
            sprites.add(new SpriteData(p.x, p.y, p.getImage(), p.getSizeScale(), false));
        }

        // ----------------------------
        // คำนวณระยะห่างจากผู้เล่น
        // ----------------------------

        for (SpriteData s : sprites) {
            s.distance = Math.hypot(s.x - player.x, s.y - player.y);
        }

        // ----------------------------
        // เรียงจากไกลไปใกล้ (Bubble Sort)
        // ตัวที่ใกล้กว่าจะถูกวาดทีหลัง จึงทับตัวที่อยู่ไกล
        // ----------------------------

        for (int i = 0; i < sprites.size() - 1; i++) {

            for (int j = 0; j < sprites.size() - 1 - i; j++) {

                if (sprites.get(j).distance < sprites.get(j + 1).distance) {

                    SpriteData temp = sprites.get(j);
                    sprites.set(j, sprites.get(j + 1));
                    sprites.set(j + 1, temp);
                }
            }
        }

        for (SpriteData s : sprites) {
            drawSprite(s, player);
        }
    }

    private void drawSprite(SpriteData s, Player player) {

        double dx = s.x - player.x;
        double dy = s.y - player.y;

        double distance = s.distance;

        if (distance < 0.15) {
            return;
        }

        // มุมระหว่างทิศที่ผู้เล่นมอง กับทิศไปยัง sprite
        double angleToSprite = GamePanel.normalizeAngle(Math.atan2(dy, dx) - player.angle);

        // อยู่นอกมุมมอง ไม่ต้องวาด
        if (Math.abs(angleToSprite) > FOV / 2 + 0.4) {
            return;
        }

        // ------------------------------------
        // ตำแหน่งและขนาดบนจอ
        // ------------------------------------

        double cameraX = angleToSprite / FOV;

        int screenX = (int) ((cameraX + 0.5) * SCREEN_WIDTH);

        int spriteSize = (int) (SCREEN_HEIGHT / distance * s.sizeScale);

        if (spriteSize < 1) {
            return;
        }

        int centerY = SCREEN_HEIGHT / 2;

        // ไอเทมวางอยู่บนพื้น จึงเลื่อนลงมา
        if (s.grounded) {
            centerY = SCREEN_HEIGHT / 2 + spriteSize / 2;
        }

        int drawStartY = centerY - spriteSize / 2;
        int drawEndY = centerY + spriteSize / 2;

        int drawStartX = screenX - spriteSize / 2;
        int drawEndX = screenX + spriteSize / 2;

        double shade = Math.max(0.35, 1.0 / (1.0 + distance * 0.2));

        int imageWidth = s.image.getWidth();
        int imageHeight = s.image.getHeight();

        // ------------------------------------
        // วาดทีละคอลัมน์
        // ------------------------------------

        for (int col = Math.max(0, drawStartX); col < Math.min(SCREEN_WIDTH, drawEndX); col++) {

            // มีกำแพงบังอยู่ข้างหน้า ไม่ต้องวาดคอลัมน์นี้
            if (distance >= zBuffer[col]) {
                continue;
            }

            // ตำแหน่ง x ในรูป (เหมือนการหา textureX ของกำแพง)
            int textureX = (col - drawStartX) * imageWidth / spriteSize;
            textureX = Math.max(0, Math.min(imageWidth - 1, textureX));

            for (int row = Math.max(0, drawStartY); row < Math.min(SCREEN_HEIGHT, drawEndY); row++) {

                int textureY = (row - drawStartY) * imageHeight / spriteSize;
                textureY = Math.max(0, Math.min(imageHeight - 1, textureY));

                // true = อ่านค่าความโปร่งใส (alpha) มาด้วย
                Color pixel = new Color(s.image.getRGB(textureX, textureY), true);

                // พิกเซลโปร่งใส ข้ามไป
                if (pixel.getAlpha() < 128) {
                    continue;
                }

                int red = (int) (pixel.getRed() * shade);
                int green = (int) (pixel.getGreen() * shade);
                int blue = (int) (pixel.getBlue() * shade);

                screen.setRGB(col, row, new Color(red, green, blue).getRGB());
            }
        }
    }
}
