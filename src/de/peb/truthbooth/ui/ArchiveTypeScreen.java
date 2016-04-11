package de.peb.truthbooth.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JDialog;

import de.peb.truthbooth.TruthBooth;
import de.peb.truthbooth.UserInformation.Privacy;


public class ArchiveTypeScreen extends TBPanel {
	public static final String SCREEN_ID = "Archive";

	private static final long serialVersionUID = 1L;

    private ChoiceButtonGroup group;
    private TBImage titleImage;
    private TBImage bottomImage;
    
    public ArchiveTypeScreen(final TBFrame frame) {
		super(frame);
		
        TBButton publicButton = TBButton.getStatefullButton("btn_archive_public");
        TBButton privateButton = TBButton.getStatefullButton("btn_archive_research");

        add(publicButton);
        add(privateButton);
        
        addQuitButton();

//        TBButton nextButton = addNextButton(ThankYouScreen.SCREEN_ID);

        group = new ChoiceButtonGroup();
//        group.setNext(nextButton);
        group.add(publicButton, new Runnable() {
        	public void run() {
        		TruthBooth.userInformation.setArchivePrivacy(Privacy.Public);
        		frame.showDialog(makeClickWrapDialog(Privacy.Public));
        	}
        });
        group.add(privateButton, new Runnable() {
        	public void run() {
                TruthBooth.userInformation.setArchivePrivacy(Privacy.Research);
        		frame.showDialog(makeClickWrapDialog(Privacy.Research));
        	}
        });
	}
	
    @Override
    public void onShow() {
    	group.setSelected(null);
        group.position(frame);
        TruthBooth.setTimeout(TruthBooth.ResetTimer.Normal);

        if (titleImage != null) {
			remove(titleImage);
		}
		titleImage = addTitleImage("text_select_archive");

        if (bottomImage != null) {
			remove(bottomImage);
		}
        bottomImage = new TBImage(TruthBooth.getImageIcon("text_archive_bottom" + ".png"));
		add(bottomImage);
		bottomImage.setLocation((frame.getWidth() - bottomImage.getWidth())/2, 660);
    }
    
    private JDialog makeClickWrapDialog(Privacy privacy) {

		TBButton btnAgree = new TBButton(TruthBooth.getImageIcon("btn_dialog_agree.png"));
		btnAgree.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				frame.hideDialog();
				TruthBooth.showScreen(ThankYouScreen.SCREEN_ID);
			}
		});
		TBButton btnBack = new TBButton(TruthBooth.getImageIcon("btn_dialog_back.png"));
		btnBack.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				frame.hideDialog();
			}
		});
		TBButton[] buttons = new TBButton[] { btnBack, btnAgree };

		return TBClickWrapDialog.createDialog(frame, privacy, buttons);
    }
    
	@Override
	public String getId() {
		return SCREEN_ID;
	}

}
