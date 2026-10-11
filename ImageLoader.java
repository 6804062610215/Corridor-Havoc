import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

// โหลดรูปจากโฟลเดอร์ res/images
public class ImageLoader {

    public static BufferedImage load(String fileName) {

        BufferedImage image = null;

        try {
            image = ImageIO.read(new File("res/images/" + fileName));
        } catch (Exception e) {
            System.out.println("Cannot load image: " + fileName);
        }

        // ถ้าโหลดไม่ได้ ใช้รูปตารางสีชมพู-ดำแทน เกมจะได้ไม่พัง
        if (image == null) {
            image = createMissingImage();
        }

        return image;
    }

    private static BufferedImage createMissingImage() {

        BufferedImage image = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);

        for (int y = 0; y < 16; y++) {

            for (int x = 0; x < 16; x++) {

                if ((x / 4 + y / 4) % 2 == 0) {
                    image.setRGB(x, y, 0xFFFF00FF);
                } else {
                    image.setRGB(x, y, 0xFF000000);
                }
            }
        }

        return image;
    }
}
