/**
 * No Rights Reserved.
 * This program and the accompanying materials
 * are made available under the terms of the Public Domain.
 */
package logbook.gui.logic;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

import logbook.config.AppConfig;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * サウンドを操作します
 *
 */
public final class Sound {

    /** 再生待ち */
    private static ArrayBlockingQueue<File> soundfileQueue = new ArrayBlockingQueue<File>(10);

    /** ロガー */
    private static final Logger LOG = LogManager.getLogger(Sound.class);

    /** バッファーサイズ */
    private static final int BUFFER_SIZE = 1024 * 8;

    /** 拡張子 */
    private static final String[] EXTENSIONS = { "wav" };

    /**
     * サウンドファイルを再生待ちキューに入れます
     * 
     * @param file ファイル
     */
    public static void addQueue(File file) {
        soundfileQueue.offer(file);
    }

    /**
     * サウンドファイルを再生します
     * 
     * @param file ファイル
     */
    public static void play(File file) {
        try {
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(file);
            try {
                AudioFormat audioFormat = audioInputStream.getFormat();

                // データラインの情報オブジェクトを生成します
                DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
                // 指定されたデータライン情報に一致するラインを取得します
                SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info);

                try {
                    // 指定されたオーディオ形式でラインを開きます
                    line.open(audioFormat);
                    // ラインでのデータ入出力を可能にします
                    line.start();

                    // ゲインのコントロールを取得します
                    FloatControl control = (FloatControl) line.getControl(FloatControl.Type.MASTER_GAIN);
                    // サウンド音量を設定
                    controlByLinearScalar(control, AppConfig.get().getSoundLevel());

                    int nBytesRead = 0;
                    byte[] abData = new byte[BUFFER_SIZE];
                    while (nBytesRead != -1) {
                        // オーディオストリームからデータを読み込みます
                        nBytesRead = audioInputStream.read(abData, 0, abData.length);
                        if (nBytesRead >= 0) {
                            // オーディオデータをミキサーに書き込みます
                            line.write(abData, 0, nBytesRead);
                        }
                    }
                    line.drain();
                } finally {
                    line.close();
                }
            } finally {
                audioInputStream.close();
            }
        } catch (UnsupportedAudioFileException e) {
            LOG.warn("サポートされていないサウンドファイル形式です", file);
        } catch (Exception e) {
            LOG.warn("サウンドの再生に失敗しました", e);
        }
    }

    /**
     * リストからランダムにサウンドファイルを再生します
     * 
     * @param files ファイルリスト
     */
    public static void randomPlay(List<File> files) {
        if (files.size() > 0) {
            addQueue(files.get((int) (Math.random() * files.size())));
        }
    }

    /**
     * 遠征から帰投した時に再生するサウンドを再生します
     * 
     */
    public static void randomExpeditionSoundPlay() {
        randomPlay(getExpeditionSoundFiles());
    }

    /**
     * お風呂からあがる時に再生するサウンドを再生します
     * 
     */
    public static void randomDockSoundPlay() {
        randomPlay(getDockSoundFiles());
    }

    /**
     * 遠征から帰投した時に再生するサウンドを取得します
     * 
     * @return サウンドファイル
     */
    private static List<File> getExpeditionSoundFiles() {
        File expedition = new File("./sound/expedition");
        if (expedition.exists() && expedition.isDirectory()) {
            return new ArrayList<File>(FileUtils.listFiles(expedition, EXTENSIONS, true));
        }
        return Collections.emptyList();
    }

    /**
     * お風呂からあがる時に再生するサウンドを取得します
     * 
     * @return サウンドファイル
     */
    private static List<File> getDockSoundFiles() {
        File dock = new File("./sound/dock");
        if (dock.exists() && dock.isDirectory()) {
            return new ArrayList<File>(FileUtils.listFiles(dock, EXTENSIONS, true));
        }
        return Collections.emptyList();
    }

    /**
     * 音量を調節する
     * 
     * @param control
     * @param linearScalar
     */
    private static void controlByLinearScalar(FloatControl control, double linearScalar) {
        control.setValue((float) Math.log10(linearScalar) * 20);
    }

    /**
     * プレイヤースレッド
     * 
     */
    public static class PlayerThread extends Thread {

        /** ロガー */
        private static final Logger LOG = LogManager.getLogger(PlayerThread.class);

        /**
         * プレイヤースレッド
         */
        public PlayerThread() {
            this.setName("logbook_sound.player_thread");
        }

        @Override
        public void run() {
            try {
                while (true) {
                    File file = soundfileQueue.take();
                    if (file != null) {
                        play(file);
                    }
                    //Thread.sleep(500);
                }
            } catch (Exception e) {
                LOG.fatal("スレッドが異常終了しました", e);
                throw new RuntimeException(e);
            }
        }
    }
}
