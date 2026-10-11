import javax.swing.JFrame;
import java.awt.GraphicsEnvironment;

// จุดเริ่มต้นของโปรแกรม: สร้างหน้าต่างเต็มจอแล้วใส่ GamePanel เข้าไป
public class Main {

    public static void main(String[] args) {

        JFrame window = new JFrame("Corridor Havoc");

        GamePanel game = new GamePanel();

        window.add(game);

        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // ไม่มีแถบหัวหน้าต่าง (ปุ่มปิด/ย่อ) เพราะจะแสดงแบบเต็มจอ
        window.setUndecorated(true);

        // แสดงหน้าต่างแบบเต็มจอ (กด ESC เพื่อออกจากเกม)
        GraphicsEnvironment.getLocalGraphicsEnvironment()
                .getDefaultScreenDevice()
                .setFullScreenWindow(window);

        game.requestFocusInWindow();
    }
}
