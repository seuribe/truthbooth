package de.peb.truthbooth.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

/**
 * Allows easy grouping and management of choice buttons (e.g. choose language)
 * 
 * @author Sebastian
 *
 */
public class ChoiceButtonGroup {
	private List<TBButton> options;
	private TBButton next;

	public ChoiceButtonGroup() {
		this.options = new ArrayList<TBButton>();
	}
	
	public void setNext(TBButton next) {
		this.next = next;
	}

	public void add(final TBButton button, final Runnable onSelect) {
		options.add(button);
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent action) {
				setSelected(button);
				onSelect.run();
			}
		});
	}
	
	public void position(TBFrame frame) {
		int margin = 160;
		int total = margin * (options.size() - 1);
		for (TBButton btn : options) {
			total += btn.getWidth();
		}
		int x = (frame.getWidth() - total) / 2;
		int midY = frame.getHeight() / 2;
		for (TBButton btn : options) {
			btn.setLocation(x, midY - btn.getHeight()/2);
			x += margin + btn.getWidth();
		}
	}
	
    protected void setSelected(TBButton selected) {
        for (TBButton button : options) {
            if (button != selected) {
            	button.setStateOff();
            } else {
            	button.setStateOn();
            }
        }
        if (next == null) {
        	return;
        }
        if (selected != null) {
        	next.setStateNormal();
        } else {
        	next.setStateOff();
        }
        next.setEnabled(selected != null);
    }
}