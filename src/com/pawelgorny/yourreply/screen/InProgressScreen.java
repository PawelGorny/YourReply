package com.pawelgorny.yourreply.screen;

import com.pawelgorny.yourreply.Constants;

import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.container.DialogFieldManager;
import net.rim.device.api.ui.container.PopupScreen;

public class InProgressScreen {
	
	private PopupScreen popup;
	private LabelField label;
	private String LABEL_DEFAULT_TEXT=Constants.POPUP_DOWNLOADING;
	
	public InProgressScreen(){
	DialogFieldManager dialogFieldManager=new DialogFieldManager();
	popup=new PopupScreen(dialogFieldManager);
	label=new LabelField(LABEL_DEFAULT_TEXT);
	dialogFieldManager.addCustomField(label);
	//popup.add(label);
	}
	
	public void open(){
		open(LABEL_DEFAULT_TEXT);
	}
	
	public void open(String text){
		if (popup.isDisplayed())
			return;
		label.setText(text);
		UiApplication.getUiApplication().pushScreen(popup);
		/*UiApplication.getUiApplication().invokeLater(new Runnable() {		
			public void run() {
				if (popup.isDisplayed()){
					UiApplication.getUiApplication().pushScreen(popup);
				}
			}
		})*/;
		popup.doPaint();
	}
	
	public void close(){
		UiApplication.getUiApplication().invokeLater(new Runnable() {
			public void run() {
				if (popup.isDisplayed()){
					UiApplication.getUiApplication().popScreen(popup);
				}
			}
		});
	}
}
