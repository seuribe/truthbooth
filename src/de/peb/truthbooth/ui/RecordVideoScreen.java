package de.peb.truthbooth.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JDialog;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.peb.truthbooth.TruthBooth;
import de.peb.truthbooth.rec.AudioFeed;
import de.peb.truthbooth.rec.VideoRecorder;
import de.peb.truthbooth.rec.WebcamFeed;

public class RecordVideoScreen extends TBPanel {

	public static final String SCREEN_ID = "RecordVideo";

	private static final long serialVersionUID = 1L;

	private static final Logger logger = LoggerFactory.getLogger("TruthBooth.RecordVideoScreen");

	private VideoRecorder rec;
	private JDialog videoPreviewDialog;
//	private TBButton nextButton;
	private TBButton recButton;
//	private TBButton quitButton;
	private TBImage titleImage;
	private WebcamFeed videoFeed;
	private AudioFeed audioFeed;
	private VideoMonitoringPanel videoPanel;
//	private AudioMonitoringPanel audioPanel;
	private ProgressBar progress;
	private ClockLabel clock;
//	private boolean cancel;
	private boolean restart;
	
	private ScreenState state;

	private JDialog stopDialog;
//	private JDialog quitDialog;
    private TBAnim anim;

	private enum ScreenState {
		Inactive, Preview, WaitForStart, Recording, WaitForProcessing, Finished;
	}
	
	private void setState(ScreenState newState) {
		if (newState == state) {
			return;
		}
    	logger.info("set state " + newState.toString());

		switch (newState) {
			case Inactive:
//				nextButton.setEnabled(false);
//				nextButton.setStateOff();
				
//				recButton.setStateNormal();
//				recButton.setEnabled(true);
				
				break;
			case Preview:
//				cancel = false;
				restart = false;
//				nextButton.setEnabled(false);
//				nextButton.setStateOff();
				
				recButton.setStateNormal();
				recButton.setEnabled(true);

				progress.resetImage();

				initFeeds();

				break;
			case WaitForStart:

//				nextButton.setEnabled(false);
//				nextButton.setStateOff();
				
				recButton.setStateNormal();
				recButton.setEnabled(false);

				initFile();
				
				if (TruthBooth.userInformation.recordVideo()) {
					videoPreviewDialog.setVisible(false);
					videoFeed.deleteObserver(videoPanel);
					videoFeed.pause();
				}
				audioFeed.pause();
		        anim.setVisible(true);

				break;
			case Recording:
//				nextButton.setEnabled(false);
//				nextButton.setStateOff();
				progress.resetImage();
				changeTitleImage("text_press_stop");

				recButton.setStateOn();
				recButton.setEnabled(true);
				
				clock.startCountdown(TruthBooth.RECORD_TIME_MS, new Runnable() {
					public void run() {
//						stopRecording();
						TruthBooth.setTimeout(TruthBooth.ResetTimer.SaveDialog);

						frame.showDialog(stopDialog);
						anim.stop();
					}
				});
				audioFeed.addObserver(progress);
				anim.start();

				break;
			case WaitForProcessing:
//				nextButton.setEnabled(false);
//				nextButton.setStateOff();

				recButton.setStateOn();
				recButton.setEnabled(false);

				audioFeed.deleteObserver(progress);
				clock.stopCountdown();
				anim.stop();
				anim.setVisible(false);
				break;
			case Finished:
//				nextButton.setEnabled(true);
//				nextButton.setStateNormal();

				recButton.setStateOn();
				recButton.setEnabled(false);
				anim.stop();
				anim.setVisible(false);
				TruthBooth.showScreen(ArchiveTypeScreen.SCREEN_ID);

				break;
		}
		this.state = newState;
		repaint();
	}
	
	public RecordVideoScreen(final TBFrame frame) {
		super(frame);
		
		videoPanel = new VideoMonitoringPanel();
		videoPreviewDialog = new JDialog(frame);
		videoPreviewDialog.setSize(TruthBooth.VIDEO_PREVIEW_WIDTH, TruthBooth.VIDEO_PREVIEW_HEIGHT);
		videoPreviewDialog.setLocation(Margin.Right.value() - (TruthBooth.VIDEO_PREVIEW_WIDTH/2), Margin.Top.value());
//		setInCorner(videoPreviewDialog, Margin.Right, Margin.Top);
		videoPreviewDialog.setUndecorated(true);
		videoPreviewDialog.setContentPane(videoPanel);

//		audioPanel = new AudioMonitoringPanel();

		progress = new ProgressBar(900, 80, TruthBooth.RECORD_TIME_MS / 1000, TruthBooth.AUDIO_RATE);
		progress.setBounds(frame.getWidth()/2 - 500, 660, 900, 80);
		add(progress);
		
//		audioPanel.setBounds(10, 10, 20, 200);
//		add(audioPanel);

		clock = new ClockLabel();
		add(clock);
		clock.setLocation(1390, 650);
		
		TBImage mic = new TBImage(TruthBooth.getImageIcon("icon_mic.png"));
		add(mic);
		mic.setLocation(210,  580);
		
		setState(ScreenState.Inactive);
	}

	private JDialog createStopDialog() {
		TBButton btnQuit = TBButton.getStatefullButton("btn_dialog_delete_quit");
		btnQuit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
		    	logger.info("quit clicked, deleting temp file and quitting");
				frame.hideDialog();
//				rec.forceFileClose();
				TruthBooth.userInformation.deleteTemporaryFile();
				quitRecording();
			}
		});

		TBButton btnSave = TBButton.getStatefullButton("btn_dialog_save_on");
		btnSave.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
		    	logger.info("save clicked");
				frame.hideDialog();
				saveRecording();
			}
		});
		return TBDialog.createDialog(frame, TruthBooth.getImageIcon("text_dialog_stop.png"), new TBButton[] { btnQuit, btnSave });
	}
	
//	private JDialog createQuitDialog() {
//		TBButton btnResume = TBButton.getStatefullButton("btn_dialog_restart_on");
//		btnResume.addActionListener(new ActionListener() {
//			@Override
//			public void actionPerformed(ActionEvent arg0) {
//		    	logger.info("resume clicked");
//				frame.hideDialog();
//				restartRercording();
////				resumeRecording();
//			}
//		});
//		TBButton btnQuit = TBButton.getStatefullButton("btn_dialog_quit");
//		btnQuit.addActionListener(new ActionListener() {
//			@Override
//			public void actionPerformed(ActionEvent e) {
//		    	logger.info("quit clicked");
//				frame.hideDialog();
//				quitRecording();
//			}
//		});
//
//		return TBDialog.createDialog(frame, TruthBooth.getImageIcon("text_dialog_quit.png"), new TBButton[] { btnResume, btnQuit });
//	}

	private void quitRecording() {
		logger.info("Quit recording - should restart");
		audioFeed.deleteObserver(progress);
		clock.stopCountdown();
		if (rec != null) {
			rec.stop();
		}
//		frame.showScreen(LanguageScreen.SCREEN_ID);
		TruthBooth.exit();
	}
	
	// TODO: fix progress bar drawing on pause -- Seu / Check
	private void pauseRecording() {
		anim.stop();
		audioFeed.deleteObserver(progress);
		clock.pause();
		if (rec != null) {
			rec.pause();
		}
	}
/*
	private void resumeRecording() {
		audioFeed.addObserver(progress);
		if (rec != null) {
			rec.resume();
		}
		clock.resume();
	}
*/
//	private void restartRercording() {
//		audioFeed.deleteObserver(progress);
//		clock.stopCountdown();
//		if (rec != null) {
//			rec.stop();
//		}
//		frame.showScreen(SCREEN_ID);
//
///*		
//		restart = true;
//		if (rec != null) {
//			rec.stop();
//		}
////		stopRecording();
//		// wait some time for recording to stop -- should convert it to event handler/runnable/conditions/whatever -- Seu
//
//		try {
//			Thread.sleep(500);
//		} catch (Exception e) {
//			//
//		}
//		setState(ScreenState.Preview);
//*/		
//	}

	private void startRecording() {
		setState(ScreenState.WaitForStart);
		anim.start();

		new Thread() {
			public void run() {
				try {
					// recording started
					Runnable onStarted = new Runnable() {
						@Override
						public void run() {
							setState(ScreenState.Recording);
						}
					};
					
					// processing...
					Runnable onEndRecording = new Runnable() {
						@Override
						public void run() {
							if (restart) {
								setState(ScreenState.Preview);
							} else {
								setState(ScreenState.WaitForProcessing);
							}
						}
					};

					rec.start(TruthBooth.RECORD_TIME_MS, onStarted, onEndRecording);
					setState(ScreenState.Finished);
//					if (!cancel) {
//						setState(ScreenState.Finished);
//					}

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}.start();
	}

	private void saveRecording() {
		logger.info("Save recording - waiting for processing end");

		audioFeed.deleteObserver(progress);
		if (rec != null) {
			rec.stop();
		}
		setState(ScreenState.WaitForProcessing);
	}

	@Override
	public String getId() {
		return SCREEN_ID;
	}

	private void initFeeds() {
		if (TruthBooth.userInformation.recordVideo()) {
			videoPreviewDialog.setVisible(true);
			videoFeed = new WebcamFeed(TruthBooth.webcam);
			videoFeed.addObserver(videoPanel);
			videoFeed.start();
		}		

		audioFeed = new AudioFeed(TruthBooth.line);
//		audioFeed.addObserver(audioPanel);
		audioFeed.start();
	}
	
	private void initFile() {
		File recordFile = TruthBooth.userInformation.getTemporaryFile();
		recordFile.getParentFile().mkdirs();
		rec = new VideoRecorder(recordFile.getAbsolutePath(), videoFeed, audioFeed);
	}
	
	private void changeTitleImage(String resource) {
		if (titleImage != null) {
			remove(titleImage);
		}
		titleImage = addTitleImage(resource);
	}
	
	private void removeButtons() {
		if (recButton != null) {
			remove(recButton);
		}
//		if (quitButton != null) {
//			remove(quitButton);
//		}
	}
	
	private void addButtons() {

//		quitDialog = createQuitDialog();
		stopDialog = createStopDialog();
		
		recButton = new TBButton(TruthBooth.getImageIcon("btn_record.png"), TruthBooth.getImageIcon("btn_stop.png"), TruthBooth.getImageIcon("btn_stop.png"));
		add(recButton);
		centerComponent(recButton);
		recButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				switch (state) {
					case Preview:
						startRecording();
						break;
					case Recording:
						//stopRecording();
						pauseRecording();
						TruthBooth.setTimeout(TruthBooth.ResetTimer.SaveDialog);

						frame.showDialog(stopDialog);
						break;
					default:
						break;
				}
			}
		});
		
        anim = new TBAnim("anim_now_recording", 2, TruthBooth.BLINK_DELAY_MS);
        add(anim);
        anim.setBounds(855, 150, 250, 23);
        anim.setVisible(false);
		
//		nextButton = addNextButton(ArchiveTypeScreen.SCREEN_ID);
/*
		quitButton = TBButton.getQuitButton();
		setInCorner(quitButton, Margin.Left, Margin.Bottom);
		quitButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				pauseRecording();
				frame.showDialog(quitDialog);
			}
		});
		add(quitButton);
*/
	}
	
	@Override
	public void onShow() {
		changeTitleImage("text_press_record");
		
		removeButtons();
		addButtons();

		setState(ScreenState.Preview);
	}

	@Override
	public void onHide() {
		if (videoPreviewDialog != null && videoPreviewDialog.isVisible()) {
			videoPreviewDialog.setVisible(false);
//			videoPreviewDialog = null;
		}
		frame.hideDialog();
		clock.stopCountdown();
		if (rec != null) {
			rec.stop();
		}
	}

}
