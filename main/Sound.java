import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.io.File;
import java.util.HashMap;

// เล่นเสียงจากโฟลเดอร์ res/sounds (ต้องเป็นไฟล์ .wav)
public class Sound {

    private static HashMap<String, Clip> clips = new HashMap<>();

    private static final String[] ALL_SOUNDS = {
            "shoot_assault.wav", "shoot_shotgun.wav", "shoot_sniper.wav",
            "empty.wav", "reload.wav",
            "enemy_hit.wav", "enemy_death.wav", "enemy_shoot.wav",
            "player_hurt.wav", "pickup.wav",
            "wave_start.wav", "boss_appear.wav",
            "game_over.wav", "mission_complete.wav",
            "explosion.wav", "level_complete.wav",
            "music.wav"
    };

    // โหลดเสียงทั้งหมดไว้ก่อนตอนเปิดเกม จะได้ไม่กระตุกตอนเล่น
    public static void loadAll() {

        for (int i = 0; i < ALL_SOUNDS.length; i++) {
            getClip(ALL_SOUNDS[i]);
        }
    }

    private static Clip getClip(String fileName) {

        if (clips.containsKey(fileName)) {
            return clips.get(fileName);
        }

        Clip clip = null;

        try {
            AudioInputStream audio = AudioSystem.getAudioInputStream(new File("res/sounds/" + fileName));
            clip = AudioSystem.getClip();
            clip.open(audio);
        } catch (Exception e) {
            System.out.println("Cannot load sound: " + fileName);
            clip = null;
        }

        // เก็บไว้แม้เป็น null จะได้ไม่พยายามโหลดซ้ำทุกครั้ง
        clips.put(fileName, clip);

        return clip;
    }

    public static void play(String fileName) {

        Clip clip = getClip(fileName);

        if (clip == null) {
            return;
        }

        clip.stop();
        clip.setFramePosition(0);
        clip.start();
    }

    public static void loop(String fileName) {

        Clip clip = getClip(fileName);

        if (clip == null) {
            return;
        }

        clip.stop();
        clip.setFramePosition(0);
        clip.loop(Clip.LOOP_CONTINUOUSLY);
    }

    public static void stop(String fileName) {

        Clip clip = getClip(fileName);

        if (clip != null) {
            clip.stop();
        }
    }
}
