package de.peb.truthbooth.ui;

import de.peb.truthbooth.TruthBooth;

public class LanguageScreen extends TBPanel {

    public static final String SCREEN_ID = "Language";
    private static final long serialVersionUID = 1L;
    private ChoiceButtonGroup group;
    
    public LanguageScreen(TBFrame frame) {
        super(frame);

        group = new ChoiceButtonGroup();
        for (final String lang : TruthBooth.LANGUAGES) {
        	TBButton langButton = new TBButton(TruthBooth.getImageIcon(lang, "btn_lang_select.png"));
        	add(langButton);
            group.add(langButton, new Runnable() {
            	public void run() {
            		selectLanguage(lang);
            	}
            });
        }
    }

    private void selectLanguage(String lang) {
		TruthBooth.setLanguage(lang);
    }
    
    @Override
    public String getId() {
        return SCREEN_ID;
    }

    @Override
    public void onShow() {
		frame.resetUserInformation();
		TruthBooth.resetScreens();

        group.setSelected(null);
        group.position(frame);
//        TruthBooth.setTimeout(TruthBooth.ResetTimer.None);
    }

}
