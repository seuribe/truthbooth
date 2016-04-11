package de.peb.truthbooth.ui;

import de.peb.truthbooth.DeviceSelector;
import de.peb.truthbooth.TruthBooth;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;

/**
 * @author seu
 */
public class ConfigPanel extends javax.swing.JPanel {
	private static final long serialVersionUID = 1L;
	private DeviceSelector ds;

    private JButton okButton;

    private JComboBox<String> micCombo;
    private JComboBox<String> webcamCombo;

    /**
     * Creates new form ConfigPanel
     */
    public ConfigPanel(DeviceSelector ds) {
        this.ds = ds;
		initComponents();
    }
        
    private void initComponents() {

        webcamCombo = new JComboBox<String>();
        micCombo = new JComboBox<String>();
        okButton = new JButton();

        webcamCombo.setModel(new DefaultComboBoxModel<String>(ds.webcamNames));
        micCombo.setModel(new DefaultComboBoxModel<String>(ds.micNames));

        okButton.setText("Ok");
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });
        setLayout(null);
        add(webcamCombo);
        webcamCombo.setBounds(20, 20, 500, 20);
        add(micCombo);
        micCombo.setBounds(20, 60, 500, 20);
        add(okButton);
        okButton.setBounds(20, 100, 60, 20);
    }

    private void start() {
        TruthBooth.showScreen(LanguageScreen.SCREEN_ID);
        TruthBooth.resetIdleTimer();
    }
    
    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {
    	int webcamIndex = webcamCombo.getSelectedIndex();
    	int micIndex = micCombo.getSelectedIndex();
    	if (webcamIndex == -1 || micIndex == -1) {
    		return;
    	}
        TruthBooth.setWebcam(ds.webcams.get(webcamIndex));
        TruthBooth.setMicLine(ds.lines.get(micIndex).line);
    	start();
    }
}
