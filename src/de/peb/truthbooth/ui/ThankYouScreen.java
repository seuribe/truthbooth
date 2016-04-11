package de.peb.truthbooth.ui;

import de.peb.truthbooth.TruthBooth;



public class ThankYouScreen extends TBPanel {

	public static final String SCREEN_ID = "ThankYou";
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private TBImage textImage = null;
	public ThankYouScreen(TBFrame frame) {
		super(frame);
		addRestartButton();
	}

	@Override
	public String getId() {
		return SCREEN_ID;
	}

	@Override
	public void onShow() {
		TruthBooth.userInformation.renameToFinal();
		
		if (textImage != null) {
			remove(textImage);
		}
		textImage = addCenteredImage("text_thank_you");
	}

}
