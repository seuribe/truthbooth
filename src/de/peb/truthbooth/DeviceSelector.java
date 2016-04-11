package de.peb.truthbooth;

import java.util.ArrayList;
import java.util.List;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.TargetDataLine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.sarxos.webcam.Webcam;

import de.peb.truthbooth.TruthBooth.MicInfo;

public class DeviceSelector {

	private final String defaultCam;
	private final String defaultMic;
	
	public String[] webcamNames = null;
	public List<Webcam> webcams;

	public String[] micNames = null;
	public List<MicInfo> lines = null;

    private static final Logger logger = LoggerFactory.getLogger("Devices");

    public DeviceSelector(String defaultCam, String defaultMic) {
        this.defaultCam = defaultCam;
		this.defaultMic = defaultMic;

		logger.info("checking for devices");

		webcams = Webcam.getWebcams();
        webcamNames = new String[webcams.size()];
        for (int i = 0 ; i < webcams.size() ; i++) {
            webcamNames[i] = webcams.get(i).getName();
            logger.debug("found webcam: " + webcamNames[i]);
        }
        
        lines = listAllLines(TruthBooth.audioFormat);
        micNames = new String[lines.size()];
        for (int i = 0 ; i < lines.size() ; i++) {
            micNames[i] = lines.get(i).desc;
            logger.debug("found mic: " + micNames[i]);
        }
    }

    public boolean checkAndSetDefaults() {
    	boolean audioSelected = false;
    	boolean videoSelected = false;
    	if (defaultCam != null) {
    		for (Webcam cam : webcams) {
    			if (defaultCam.equals(cam.getName())) {
    				TruthBooth.setWebcam(cam);
    				videoSelected = true;
    				break;
    			}
    		}
    	}
    	if (defaultMic != null) {
    		for (MicInfo mic : lines) {
    			if (defaultMic.equals(mic.desc)) {
    				TruthBooth.setMicLine(mic.line);
    				audioSelected = true;
    				break;
    			}
    		}
    	}
    	return audioSelected && videoSelected;
    }
    
    private List<MicInfo> listAllLines(AudioFormat audioFormat) {
        DataLine.Info recordLineInfo = new DataLine.Info(TargetDataLine.class, audioFormat);

        List<MicInfo> mics = new ArrayList<>();
        Mixer.Info[] infos = AudioSystem.getMixerInfo();
        for (Mixer.Info info : infos) {
            Mixer mixer = AudioSystem.getMixer(info);
            boolean supportsRecord = mixer.isLineSupported(recordLineInfo);
            if (supportsRecord) {
                try {
                    String desc = mixer.getMixerInfo().getVendor() + " " + mixer.getMixerInfo().getName() + " " + mixer.getMixerInfo().getDescription();
                    TargetDataLine line = AudioSystem.getTargetDataLine(audioFormat, mixer.getMixerInfo());
                    mics.add(new MicInfo(line, desc));
                } catch (LineUnavailableException e) {
                    e.printStackTrace();
                }
            }
        }
        return mics;
    }
}
