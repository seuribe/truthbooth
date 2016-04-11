package de.peb.truthbooth.ui;

import java.awt.Point;

import de.peb.truthbooth.TruthBooth;
import de.peb.truthbooth.UserInformation.RecordType;

public class RecordingMethodScreen extends TBPanel {

	public static final String SCREEN_ID = "RecordingMethod";
	private static final long serialVersionUID = 1L;

	private TBImage textImage;
	private ChoiceButtonGroup group;

	public RecordingMethodScreen(TBFrame frame) {
		super(frame);

		TBButton audioMethod = TBButton.getStatefullButton("btn_method_audio");
		TBButton videoMethod = TBButton.getStatefullButton("btn_method_video");

        add(audioMethod);
        add(videoMethod);
        audioMethod.setLocation(new Point(250, 300));
        videoMethod.setLocation(new Point(658, 300));

        addQuitButton();
        addBackButton(IntroScreen.SCREEN_ID);
        TBButton nextButton = addNextButton(RecordVideoScreen.SCREEN_ID);

        group = new ChoiceButtonGroup();
        group.setNext(nextButton);
        group.add(audioMethod, new Runnable() {
        	public void run() {
        		TruthBooth.userInformation.setRecordType(RecordType.Audio);
        	}
        });
        group.add(videoMethod, new Runnable() {
        	public void run() {
        		TruthBooth.userInformation.setRecordType(RecordType.Video);
        	}
        });
	}

	@Override
	public String getId() {
		return SCREEN_ID;
	}

	@Override
	public void onShow() {
        group.setSelected(null);
        group.position(frame);
		if (textImage != null) {
			remove(textImage);
		}
		textImage = addTitleImage("text_select_method");
	}

}
