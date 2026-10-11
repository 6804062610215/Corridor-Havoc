import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

// วาดสิ่งที่อยู่บนจอระหว่างเล่น: ปืนในมือ และแถบ HP / AMMO / WAVE ด้านล่าง
public class Hud {

    static final int SCREEN_WIDTH = GamePanel.SCREEN_WIDTH;
    static final int SCREEN_HEIGHT = GamePanel.SCREEN_HEIGHT;

    static final int BAR_HEIGHT = 90;

    private BufferedImage muzzleFlashImage;

    public Hud() {
        muzzleFlashImage = ImageLoader.load("muzzle_flash.png");
    }

    // ========================================
    // GUN (ปืนในมือผู้เล่น)
    // ========================================

    public void drawGun(Graphics2D g, Player player, long lastFlashTime) {

        int gunSize = 200;

        int gunX = SCREEN_WIDTH / 2 - gunSize / 2;
        int gunY = SCREEN_HEIGHT - 70 - gunSize;

        // ตอนรีโหลด ปืนลดต่ำลง
        if (player.reloading) {
            gunY += 80;
        }

        boolean justFired = System.currentTimeMillis() - lastFlashTime < 80;

        // แรงสะท้อน: ปืนขยับลงนิดหน่อยตอนยิง
        if (justFired) {
            gunY += 12;
        }

        g.drawImage(player.weapon.image, gunX, gunY, gunSize, gunSize, null);

        // ไฟที่ปากกระบอกปืน
        if (justFired) {

            int flashSize = 90;

            g.drawImage(muzzleFlashImage,
                    SCREEN_WIDTH / 2 - flashSize / 2,
                    gunY - flashSize / 2,
                    flashSize, flashSize, null);
        }
    }

    // ========================================
    // BOTTOM BAR (HP / AMMO / WAVE)
    // ========================================

    public void drawBar(Graphics2D g, Player player, WaveManager waveManager) {

        int barTop = SCREEN_HEIGHT - BAR_HEIGHT;

        g.setColor(new Color(10, 10, 10, 210));
        g.fillRect(0, barTop, SCREEN_WIDTH, BAR_HEIGHT);

        // แถบช่อง HP ด้านบนของ HUD
        int slotCount = 20;
        int slotSize = 18;
        int slotGap = 6;
        int rowWidth = slotCount * (slotSize + slotGap) - slotGap;
        int rowStartX = (SCREEN_WIDTH - rowWidth) / 2;
        int rowY = barTop + 12;

        int filledSlots = (int) ((player.hp / player.maxHp) * slotCount);

        for (int i = 0; i < slotCount; i++) {

            if (i < filledSlots) {
                g.setColor(new Color(160, 120, 80));
            } else {
                g.setColor(new Color(50, 45, 40));
            }

            g.fillRect(rowStartX + i * (slotSize + slotGap), rowY, slotSize, slotSize / 2);
        }

        g.setColor(new Color(220, 40, 40));
        g.setFont(new Font("Arial", Font.BOLD, 16));

        // HP
        g.drawString(String.format("HP: %.0f / %.0f", player.hp, player.maxHp), 20, SCREEN_HEIGHT - 15);

        // AMMO
        String ammoText;

        if (player.reloading) {
            ammoText = "RELOADING...";
        } else {
            ammoText = "AMMO: " + player.magazineAmmo + " / " + player.reserveAmmo;
        }

        FontMetrics fm = g.getFontMetrics();
        g.drawString(ammoText, SCREEN_WIDTH / 2 - fm.stringWidth(ammoText) / 2, SCREEN_HEIGHT - 15);

        // WAVE
        String waveText = "WAVE " + waveManager.getCurrentWave() + "/" + WaveManager.TOTAL_WAVES;
        g.drawString(waveText, SCREEN_WIDTH - 160, SCREEN_HEIGHT - 15);

        g.setColor(new Color(150, 150, 150));
        g.setFont(new Font("Arial", Font.PLAIN, 12));
        g.drawString("[R] Reload", SCREEN_WIDTH / 2 - 40, SCREEN_HEIGHT - 32);

        if (player.isDamageBuffActive()) {
            g.setColor(new Color(200, 100, 240));
            g.setFont(new Font("Arial", Font.BOLD, 16));
            g.drawString("DAMAGE BOOST!", 20, barTop - 10);
        }
    }
}
