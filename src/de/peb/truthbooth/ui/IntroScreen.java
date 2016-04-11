package de.peb.truthbooth.ui;

public class IntroScreen extends TBPanel {

	public static final String SCREEN_ID = "Intro";
	private static final long serialVersionUID = 1L;

	private TBImage textImage;

	public IntroScreen(TBFrame frame) {
		super(frame);
		addBackButton(LanguageScreen.SCREEN_ID);
		addQuitButton();
		addNextButton(RecordingMethodScreen.SCREEN_ID);
	}

	@Override
	public String getId() {
		return SCREEN_ID;
	}

	@Override
	public void onShow() {
		if (textImage != null) {
			remove(textImage);
		}
		textImage = addCenteredImage("text_intro");
	}
    
}
