import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;

// หน้าจอที่ไม่ใช่ตอนเล่น: หน้าเลือกอาวุธ และหน้าจอจบเกม
public class MenuScreen {

    static final int SCREEN_WIDTH = GamePanel.SCREEN_WIDTH;
    static final int SCREEN_HEIGHT = GamePanel.SCREEN_HEIGHT;

    static final int CARD_WIDTH = 180;
    static final int CARD_HEIGHT = 220;
    static final int CARD_GAP = 30;
    static final int CARD_Y = 220;

    // กรอบของการ์ดอาวุธแต่ละใบ ใช้ทั้งตอนวาดและตอนเช็คว่าคลิกโดนใบไหน
    private Rectangle[] cardRects;

    public MenuScreen() {

        Weapon[] weapons = Weapon.values();

        cardRects = new Rectangle[weapons.length];

        int totalWidth = weapons.length * CARD_WIDTH + (weapons.length - 1) * CARD_GAP;
        int startX = (SCREEN_WIDTH - totalWidth) / 2;

        for (int i = 0; i < weapons.length; i++) {

            int cardX = startX + i * (CARD_WIDTH + CARD_GAP);

            cardRects[i] = new Rectangle(cardX, CARD_Y, CARD_WIDTH, CARD_HEIGHT);
        }
    }

    // คืนค่าลำดับการ์ดที่ถูกคลิก หรือ -1 ถ้าไม่โดนการ์ดใบไหนเลย
    public int getClickedCard(int mouseX, int mouseY) {

        for (int i = 0; i < cardRects.length; i++) {

            if (cardRects[i].contains(mouseX, mouseY)) {
                return i;
            }
        }

        return -1;
    }

    // ========================================
    // LOADOUT SELECT
    // ========================================

    public void drawLoadout(Graphics2D g) {

        g.setColor(Color.BLACK);
        g.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 32));
        drawCentered(g, "Select Loadout", SCREEN_WIDTH / 2, 120);

        Weapon[] weapons = Weapon.values();

        for (int i = 0; i < weapons.length; i++) {

            Weapon w = weapons[i];
            Rectangle r = cardRects[i];

            g.setColor(w.color);
            g.fillRect(r.x, r.y, r.width, r.height);

            g.setColor(Color.WHITE);
            g.drawRect(r.x, r.y, r.width, r.height);

            g.setFont(new Font("Arial", Font.BOLD, 18));
            drawCentered(g, w.displayName, r.x + r.width / 2, r.y + 30);

            g.setFont(new Font("Arial", Font.PLAIN, 14));
            drawCentered(g, "HP 100", r.x + r.width / 2, r.y + 70);
            drawCentered(g, "DMG " + (int) w.damage, r.x + r.width / 2, r.y + 95);
            drawCentered(g, String.format("SPD %.1f", w.fireSpeed), r.x + r.width / 2, r.y + 120);
        }

        int cardBottom = CARD_Y + CARD_HEIGHT;

        g.setFont(new Font("Arial", Font.PLAIN, 16));
        drawCentered(g, "Choose 1 of 3 starting weapons", SCREEN_WIDTH / 2, cardBottom + 50);

        g.setFont(new Font("Arial", Font.PLAIN, 14));
        g.setColor(new Color(180, 180, 180));
        drawCentered(g, "W/S Move   A/D Strafe   Mouse Turn   Left Click Fire   R Reload",
                SCREEN_WIDTH / 2, cardBottom + 80);

        drawCentered(g, "Arrow Keys also turn   ESC Quit", SCREEN_WIDTH / 2, cardBottom + 102);
    }

    // ========================================
    // MISSION COMPLETE / GAME OVER
    // ========================================

    public void drawEndScreen(Graphics2D g, String message, Color color) {

        g.setColor(Color.BLACK);
        g.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);

        g.setColor(color);
        g.setFont(new Font("Arial", Font.BOLD, 48));
        drawCentered(g, message, SCREEN_WIDTH / 2, SCREEN_HEIGHT / 2);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.PLAIN, 18));
        drawCentered(g, "Press R to restart   ESC to quit", SCREEN_WIDTH / 2, SCREEN_HEIGHT / 2 + 50);
    }

    // ========================================
    // LEVEL COMPLETE (ผ่านด่าน ไปด่านถัดไป)
    // ========================================

    public void drawLevelComplete(Graphics2D g, Level finished, Level next) {

        g.setColor(Color.BLACK);
        g.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);

        g.setColor(new Color(120, 220, 120));
        g.setFont(new Font("Arial", Font.BOLD, 44));
        drawCentered(g, "LEVEL " + finished.number + " COMPLETE", SCREEN_WIDTH / 2, SCREEN_HEIGHT / 2 - 20);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.PLAIN, 20));
        drawCentered(g, "Next: Level " + next.number + " - " + next.name, SCREEN_WIDTH / 2, SCREEN_HEIGHT / 2 + 30);

        g.setColor(new Color(180, 180, 180));
        g.setFont(new Font("Arial", Font.PLAIN, 16));
        drawCentered(g, "HP is fully restored", SCREEN_WIDTH / 2, SCREEN_HEIGHT / 2 + 60);
        drawCentered(g, "Press ENTER to continue   ESC to quit", SCREEN_WIDTH / 2, SCREEN_HEIGHT / 2 + 100);
    }

    // วาดข้อความให้อยู่กึ่งกลางตามแนวนอน
    private void drawCentered(Graphics2D g, String text, int centerX, int y) {

        FontMetrics fm = g.getFontMetrics();

        int width = fm.stringWidth(text);

        g.drawString(text, centerX - width / 2, y);
    }
}
