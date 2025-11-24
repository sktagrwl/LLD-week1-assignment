// MediaLSPIsp.java
// Messy starter: Fat interface + LSP surprises (violates ISP + LSP)

interface Player {
    void play(String source);
    void pause();
}

interface LiveStreamPlayer extends Player{
    void streamLive(String url);
}

interface iAudioPlayer extends Player{
    void download(String sourceUrl);
}
interface iCameraStreamPlayer extends LiveStreamPlayer{
    void record(String destination);
}

class AudioPlayer implements iAudioPlayer {
    private boolean playing = false;
    @Override
    public void play(String source) {
        playing = true;
    }
    @Override
    public void pause() {
        playing = false;
    }

    @Override
    public void download(String sourceUrl) {
        // pretend
    }
    public boolean isPlaying() {
        return playing;
    }
}

class CameraStreamPlayer implements iCameraStreamPlayer {
    private boolean liveStarted = false;
    private boolean playing = false;
    @Override
    public void play(String source) {
        // Surprise: needs streamLive first for “real” play
        if (!liveStarted) {
            streamLive(source);
            System.out.println("[WARN] playing after live stream started.");
        }
        playing = true;
    }
    @Override
    public void pause() { playing = false; }
    @Override
    public void record(String destination) {
        // pretend
    }
    @Override
    public void streamLive(String url) { liveStarted = true; }
    public boolean isPlaying() {
        return playing;
    }
    public boolean isLive() {
        return liveStarted;
    }
}

public class MediaLSTIsp {

    private final iAudioPlayer audioPlayer;
    private final iCameraStreamPlayer cameraStreamPlayer;

    public MediaLSTIsp(iAudioPlayer iAudioPlayer, iCameraStreamPlayer cameraStreamPlayer) {
        this.audioPlayer = iAudioPlayer;
        this.cameraStreamPlayer = cameraStreamPlayer;
    }

    public static void main(String[] args) {
        AudioPlayer ap = new AudioPlayer();
        ap.play("song.mp3");
        System.out.println("Audio playing: " + ap.isPlaying());
        ap.pause();

        CameraStreamPlayer cam = new CameraStreamPlayer();
        cam.play("rtsp://camera");       // warning surprise
        cam.streamLive("rtsp://camera"); // required order
        cam.play("rtsp://camera");
    }
}
