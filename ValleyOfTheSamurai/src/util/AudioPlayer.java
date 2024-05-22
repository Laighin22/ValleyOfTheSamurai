package util;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

/*
Submission by Andrew Roberts (Student Number: 20450942)
 */

public class AudioPlayer {

    private long timeStamp = 0;

    public AudioPlayer() {
    }
    private Clip clip;
    public void playAudio(String audioFilePath) {
        try {
            File audioFile = new File(audioFilePath);
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(audioFile);
            clip = AudioSystem.getClip();
            clip.open(audioInputStream);
            FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            gainControl.setValue(-20.0f);
            clip.start();
            //System.out.println("Playing audio: " + audioFilePath);
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            System.out.println("Error can't play audio\n");
        }
    }

    public void playMusic(String audioFilePath) {
        try {
            File audioFile = new File(audioFilePath);
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(audioFile);
            clip = AudioSystem.getClip();
            clip.open(audioInputStream);
            clip.loop(Clip.LOOP_CONTINUOUSLY);
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            System.out.println("Error can't play audio\n");
        }
    }

    public void stopAudio() {
        clip.stop();
    }

    public void pauseAudio() {
        timeStamp = clip.getMicrosecondPosition();
        clip.stop();
    }

    public void resumeAudio() {
        clip.setMicrosecondPosition(timeStamp);
        clip.loop(Clip.LOOP_CONTINUOUSLY);
    }

    public boolean isPlaying() {
        if (clip==null) {
            return false;
        } else return clip.isRunning();
    }

    public void setVolume(float level) {
        FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
        gainControl.setValue(level);
        System.out.println("Volume at: " + gainControl.getValue());
    }
}
