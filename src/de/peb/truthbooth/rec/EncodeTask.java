package de.peb.truthbooth.rec;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.util.concurrent.TimeUnit;

import com.xuggle.mediatool.IMediaWriter;

import de.peb.truthbooth.TruthBooth;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import javax.imageio.ImageIO;

/**
 * Media encoding work unit
 * @author Sebastian
 *
 */
public interface EncodeTask extends Comparable<EncodeTask>, Serializable {

    public void encode(IMediaWriter writer);

    public long getTime();

    public class VideoEncodeTask implements EncodeTask, Serializable {

		private static final long serialVersionUID = 1L;
		public transient BufferedImage image;
        public long time;
        private int videoIndex;

        private void writeObject(ObjectOutputStream out) throws IOException {
            out.defaultWriteObject();
            ImageIO.write(image, "png", out);
        }

        private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
            in.defaultReadObject();
            image = ImageIO.read(in);
        }

        private BufferedImage deepCopy(BufferedImage bi) {
            ColorModel cm = bi.getColorModel();
            boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
            WritableRaster raster = bi.copyData(null);
            return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
        }

        public VideoEncodeTask(BufferedImage image, long time, int videoIndex) {
            this.image = deepCopy(image);
            this.time = time;
            this.videoIndex = videoIndex;
        }

        @Override
        public void encode(IMediaWriter writer) {
            BufferedImage bi = new BufferedImage(TruthBooth.VIDEO_WIDTH, TruthBooth.VIDEO_HEIGHT, BufferedImage.TYPE_3BYTE_BGR);
            Graphics g = bi.getGraphics();
            g.drawImage(image, 0, 0, null);
            writer.encodeVideo(videoIndex, bi, time, TimeUnit.NANOSECONDS);
        }

        @Override
        public String toString() {
            return "VideoEncodeTask @ " + time;
        }

        @Override
        public int compareTo(EncodeTask o) {
            return (int) (time - o.getTime());
        }

        @Override
        public long getTime() {
            return time;
        }
    }

    public class AudioEncodeTask implements EncodeTask, Serializable {

		private static final long serialVersionUID = 1L;
		private short[] audioBuffer;
        private long time;
        private int audioIndex;

        public AudioEncodeTask(short[] audioBuffer, long audioPosition, int audioIndex) {
            this.audioBuffer = audioBuffer;
            this.time = audioPosition;
            this.audioIndex = audioIndex;
        }

        @Override
        public void encode(IMediaWriter writer) {
        	if (audioBuffer == null) {
        		System.out.println("buffer == null!");
        		return;
        	}
        	writer.encodeAudio(audioIndex, audioBuffer, time, TimeUnit.NANOSECONDS);
        }

        @Override
        public String toString() {
            return "AudioEncodeTask @ " + time;
        }

        @Override
        public int compareTo(EncodeTask o) {
            long diff = time - o.getTime();
            if (diff < 0) {
                return -1;
            } else if (diff == 0) {
                return 0;
            } else {
                return 1;
            }
        }

        @Override
        public long getTime() {
            return time;
        }
    }
}