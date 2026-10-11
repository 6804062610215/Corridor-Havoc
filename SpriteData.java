import java.awt.image.BufferedImage;

// ข้อมูลของสิ่งที่จะวาดเป็นรูปในฉาก 3D (ศัตรู / ไอเทม / กระสุนศัตรู)
public class SpriteData {

    public double x, y;
    public BufferedImage image;
    public double sizeScale;
    public boolean grounded;
    public double distance;

    public SpriteData(double x, double y, BufferedImage image, double sizeScale, boolean grounded) {
        this.x = x;
        this.y = y;
        this.image = image;
        this.sizeScale = sizeScale;
        this.grounded = grounded;
    }
}
