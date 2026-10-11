import java.awt.image.BufferedImage;

// คลาสแม่ของอุปสรรคในด่าน (หลุมไฟ, กับดักหนาม, ถังระเบิด)
public abstract class Obstacle {

    public double x, y;

    public Obstacle(double x, double y) {
        this.x = x;
        this.y = y;
    }

    // ทำงานทุกเฟรม เช่น ทำดาเมจถ้าผู้เล่นยืนทับ
    public abstract void update(Player player);

    public abstract BufferedImage getImage();

    // ขนาดของรูปเทียบกับความสูงกำแพง (0.5 = วางพอดีพื้น)
    public double getSizeScale() {
        return 0.5;
    }

    // true = วางอยู่บนพื้น, false = ลอยกลางจอ
    public boolean isGrounded() {
        return true;
    }

    // true = หมดหน้าที่แล้ว ให้เอาออกจากด่าน
    public boolean isFinished() {
        return false;
    }

    protected boolean isPlayerOnTop(Player player) {
        return Math.hypot(player.x - x, player.y - y) < 0.5;
    }
}
