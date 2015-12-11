package com.pawelgorny.yourreply.screen;

import net.rim.device.api.system.ApplicationDescriptor;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.FontFamily;
import net.rim.device.api.ui.Ui;
import net.rim.device.api.ui.component.RichTextField;
import net.rim.device.api.ui.container.MainScreen;

import com.pawelgorny.yourreply.Constants;

public class WelcomeScreen extends MainScreen {
	
	private boolean saving=false;
	
	public WelcomeScreen(){
		super( MainScreen.VERTICAL_SCROLL | MainScreen.VERTICAL_SCROLLBAR );
		ApplicationDescriptor desc= ApplicationDescriptor.currentApplicationDescriptor();
		if (desc!=null)
			setTitle(desc.getName()+Constants.SPACE+ desc.getVersion());
		//Dialog.inform(Constants.APP_STARTED);
		RichTextField richText1 = new RichTextField(Constants.APP_WELCOME_SCREEN1,
									RichTextField.TEXT_ALIGN_LEFT);
		RichTextField richText2 = new RichTextField(Constants.APP_WELCOME_SCREEN2,
				RichTextField.TEXT_ALIGN_LEFT);
		RichTextField richText3 = new RichTextField(Constants.APP_WELCOME_SCREEN3,
				RichTextField.TEXT_ALIGN_LEFT);
		RichTextField richText4 = new RichTextField(Constants.APP_WELCOME_SCREEN4,
				RichTextField.TEXT_ALIGN_LEFT);
		FontFamily ffamily=null;		
		try {
			ffamily = FontFamily.forName(Constants.FONT1);
		} catch (ClassNotFoundException e) {
			ffamily = FontFamily.getFontFamilies()[0];
		}
		Font myFont1=ffamily.getFont(Font.PLAIN, 9, Ui.UNITS_pt);
		Font myFont2=ffamily.getFont(Font.ITALIC, 9, Ui.UNITS_pt);
		richText1.setFont(myFont1);
		richText1.setEditable(false);
		richText1.setNonSpellCheckable(true);
		add(richText1);
		richText2.setFont(myFont2);
		richText2.setEditable(false);
		richText2.setNonSpellCheckable(true);
		add(richText2);
		richText3.setFont(myFont1);
		richText3.setEditable(false);
		richText3.setNonSpellCheckable(true);
		add(richText3);
		richText4.setFont(myFont2);
		richText4.setEditable(false);
		richText4.setNonSpellCheckable(true);
		add(richText4);
		
	}	
	
	protected boolean onSavePrompt(){
		if (saving){
			saving=false;
			return onSave();
		}
		return true;
	}

	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (saving ? 1231 : 1237);
		return result;
	}

	
}
