import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.net.URL;

// โหลดรูปจากโฟลเดอร์ res/images
// ใช้ getResource() หาไฟล์จาก classpath จึงใช้ได้ทั้งตอนรัน java Main และตอนรันจากไฟล์ .jar
public class ImageLoader {

    public static BufferedImage load(String fileName) {

        BufferedImage image = null;

        try {
            URL url = ImageLoader.class.getResource("/res/images/" + fileName);

            if (url != null) {
                image = ImageIO.read(url);
            }
        } catch (Exception e) {
            image = null;
        }

        if (image == null) {
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
