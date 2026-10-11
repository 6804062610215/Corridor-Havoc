import java.awt.AWTException;
import java.awt.Point;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

// รับคีย์บอร์ดและเมาส์ แล้วเก็บไว้ว่าตอนนี้กดปุ่มอะไรอยู่
public class InputHandler implements KeyListener, MouseListener, MouseMotionListener {

    // หมุนกี่เรเดียนต่อการขยับเมาส์ 1 พิกเซล
    static final double MOUSE_SENSITIVITY = 0.003;

    private GamePanel game;

    // Robot ใช้ย้ายเมาส์กลับมากลางจอ เมาส์จะได้ไม่ชนขอบจอ
    private Robot robot;

    public boolean forward;
    public boolean backward;
    public boolean strafeLeft;
    public boolean strafeRight;
    public boolean turnLeft;
    public boolean turnRight;
    public boolean mouseHeldDown;

    public InputHandler(GamePanel game) {

        this.game = game;

        try {
            robot = new Robot();
        } catch (AWTException e) {
            System.out.println("Mouse look is not available, use arrow keys to turn");
            robot = null;
        }
    }

    // ========================================
    // KEYBOARD
    // ========================================

    @Override
    public void keyPressed(KeyEvent e) {
        setKey(e.getKeyCode(), true);

        if (e.getKeyCode() == KeyEvent.VK_R) {
            game.onReloadKey();
        }

        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            game.onEnterKey();
        }

        // ESC = ออกจากเกม (เพราะหน้าต่างเต็มจอไม่มีปุ่มปิด)
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            System.exit(0);
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        setKey(e.getKeyCode(), false);
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    // จำว่าปุ่มไหนถูกกดอยู่ (pressed = true) หรือปล่อยแล้ว (pressed = false)
    private void setKey(int keyCode, boolean pressed) {

        switch (keyCode) {

            case KeyEvent.VK_W:
                forward = pressed;
                break;

            case KeyEvent.VK_S:
                backward = pressed;
                break;

            case KeyEvent.VK_A:
                strafeLeft = pressed;
                break;

            case KeyEvent.VK_D:
                strafeRight = pressed;
                break;

            case KeyEvent.VK_LEFT:
                turnLeft = pressed;
                break;

            case KeyEvent.VK_RIGHT:
                turnRight = pressed;
                break;
        }
    }

    // ========================================
    // MOUSE
    // ========================================

    @Override
    public void mousePressed(MouseEvent e) {

        if (game.gameState == GameState.LOADOUT_SELECT) {

            // แปลงตำแหน่งบนจอจริง เป็นตำแหน่งในภาพเกม 800x600 ก่อน
            int gameX = game.toGameX(e.getX());
            int gameY = game.toGameY(e.getY());

            int index = game.menuScreen.getClickedCard(gameX, gameY);

            if (index != -1) {
                game.startNewRun(Weapon.values()[index]);
            }

        } else if (game.gameState == GameState.PLAYING && e.getButton() == MouseEvent.BUTTON1) {

            mouseHeldDown = true;
            game.fireWeapon();
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {

        if (e.getButton() == MouseEvent.BUTTON1) {
            mouseHeldDown = false;
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        turnWithMouse(e.getX());
    }

    // ขยับเมาส์ขณะกดปุ่มค้าง (เช่น ยิงปืนกลไปด้วยหมุนไปด้วย)
    @Override
    public void mouseDragged(MouseEvent e) {
        turnWithMouse(e.getX());
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    // ========================================
    // MOUSE LOOK
    // ========================================

    // หมุนมุมมองตามระยะที่เมาส์ขยับออกจากกลางจอ แล้วดึงเมาส์กลับมากลางจอ
    private void turnWithMouse(int mouseX) {

        if (game.gameState != GameState.PLAYING || robot == null) {
            return;
        }

        int centerX = game.getWidth() / 2;

        int dx = mouseX - centerX;

        // dx = 0 คือเมาส์อยู่กลางจอแล้ว (เช่น เพิ่งถูก Robot ดึงกลับมา)
        if (dx == 0) {
            return;
        }

        game.player.angle += dx * MOUSE_SENSITIVITY;

        moveMouseToCenter();
    }

    public void moveMouseToCenter() {

        if (robot == null) {
            return;
        }

        // Robot ใช้พิกัดบนจอ จึงต้องบวกตำแหน่งของ GamePanel บนจอเข้าไป
        Point panelOnScreen = game.getLocationOnScreen();

        robot.mouseMove(
                panelOnScreen.x + game.getWidth() / 2,
                panelOnScreen.y + game.getHeight() / 2);
    }
}
