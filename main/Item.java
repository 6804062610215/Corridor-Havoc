import java.awt.image.BufferedImage;

// ไอเทมที่ตกอยู่บนพื้น: ชุดปฐมพยาบาล / กระสุนเสริม / บัฟดาเมจชั่วคราว
public class Item {

    private static BufferedImage healthImage = ImageLoader.load("health.png");
    private static BufferedImage ammoImage = ImageLoader.load("ammo.png");
    private static BufferedImage damageImage = ImageLoader.load("damage.png");

    public double x, y;
    public ItemType type;
    public boolean collected = false;

    public Item(double x, double y, ItemType type) {
        this.x = x;
        this.y = y;
        this.type = type;
    }

    public BufferedImage getImage() {

        switch (type) {

            case HEALTH:
                return healthImage;

            case AMMO:
                return ammoImage;

            default:
                return damageImage;
        }
    }

    public void apply(Player player) {

        switch (type) {

            case HEALTH:
                player.heal(30);
                break;

            case AMMO:
                player.addAmmo(player.weapon.getAmmoPickupAmount());
                break;

            case DAMAGE_BUFF:
                player.activateDamageBuff(10000);
                break;
        }

        collected = true;

        Sound.play("pickup.wav");
    }
}
