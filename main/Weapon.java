import java.awt.Color;
import java.awt.image.BufferedImage;

// อาวุธเริ่มต้น 3 แบบตามเอกสารเกม: Assault, Shotgun, Sniper
public enum Weapon {

    // name, damage, fireSpeed, color, magazineSize, startingReserve, reloadTimeMs, automatic, shootSound, image
    ASSAULT("Assault", 5, 20.0, new Color(60, 110, 200), 40, 200, 1400, true,
            "shoot_assault.wav", "gun_assault.png"),
    SHOTGUN("Shotgun", 25, 5.0, new Color(200, 140, 40), 6, 24, 2000, false,
            "shoot_shotgun.wav", "gun_shotgun.png"),
    SNIPER("Sniper", 40, 4.0, new Color(150, 70, 210), 5, 20, 2300, false,
            "shoot_sniper.wav", "gun_sniper.png");

    public final String displayName;
    public final double damage;
    public final double fireSpeed;
    public final Color color;
    public final int magazineSize;
    public final int startingReserve;
    public final long reloadTimeMs;
    public final boolean automatic;
    public final String shootSound;
    public final BufferedImage image;

    Weapon(String displayName, double damage, double fireSpeed, Color color,
            int magazineSize, int startingReserve, long reloadTimeMs, boolean automatic,
            String shootSound, String imageFile) {

        this.displayName = displayName;
        this.damage = damage;
        this.fireSpeed = fireSpeed;
        this.color = color;
        this.magazineSize = magazineSize;
        this.startingReserve = startingReserve;
        this.reloadTimeMs = reloadTimeMs;
        this.automatic = automatic;
        this.shootSound = shootSound;
        this.image = ImageLoader.load(imageFile);
    }

    public long getCooldownMs() {
        return (long) (3000 / fireSpeed);
    }
}
