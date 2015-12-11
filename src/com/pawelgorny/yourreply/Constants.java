package com.pawelgorny.yourreply;

import net.rim.blackberry.api.mail.SupportedAttachmentPart;
import net.rim.device.api.i18n.ResourceBundle;

import com.pawelgorny.yourreply.translation.LanguageResource;

public class Constants implements LanguageResource{
	
	private static ResourceBundle _resources=ResourceBundle.getBundle(BUNDLE_ID, BUNDLE_NAME);
	
	public static final String NOT_EMAIL=_resources.getString(LanguageResource.NOT_EMAIL);
	public static final String REPLY_MENU=_resources.getString(LanguageResource.REPLY_MENU);
	public static final int REPLY_MENU_NUMBER=10;
	public static final String FORWARD_MENU=_resources.getString(LanguageResource.FORWARD_MENU);
	public static final int FORWARD_MENU_NUMBER=11;
	
	public static final int ACTION_REPLY=REPLY_MENU_NUMBER;
	public static final int ACTION_FORWARD=FORWARD_MENU_NUMBER;
	
	public static final String APP_STARTED=_resources.getString(LanguageResource.APP_STARTED);
	public static final String APP_RETRIEVE_DATA=_resources.getString(LanguageResource.APP_RETRIEVE_DATA);
	public static final String APP_RETRIEVE_FILE=_resources.getString(LanguageResource.Q_DOWNLOAD);
	public static final String APP_WELCOME_SCREEN1=_resources.getString(LanguageResource.APP_WELCOME_SCREEN_1);
	public static final String APP_WELCOME_SCREEN2=_resources.getString(LanguageResource.APP_WELCOME_SCREEN_2);
	public static final String APP_WELCOME_SCREEN3=_resources.getString(LanguageResource.APP_WELCOME_SCREEN_3);
	public static final String APP_WELCOME_SCREEN4=_resources.getString(LanguageResource.APP_WELCOME_SCREEN_4);
	
	public static final String ERROR_ADD_STORE=_resources.getString(LanguageResource.ERROR_ADD_STORE);
	public static final String ERROR_ADD_ITEMS=_resources.getString(LanguageResource.ERROR_ADD_ITEMS);
		
	public static final String EMAIL_ENCODING="UTF-8";
	public static final String SPACE=" ";
	public static final String MINUS3="---";
	
	public static final String REPLY_PREFIX=_resources.getString(LanguageResource.REPLY_PREFIX)+SPACE;
	public static final String FORWARD_PREFIX=_resources.getString(LanguageResource.FORWARD_PREFIX)+SPACE;
	
	public static final String DATETIME_FORMAT="EEEE, dd MMM yyyy HH:mm";
	public static final String NEWLINE="\r\n";
	public static final String REPLY_DELIM="> ";
	public static final String FIELD_DATE="[DATE]",
							   FIELD_SENDER="[SENDER]",
							   FIELD_SUBJECT="[SUBJECT]",
							   FIELD_TO="[TO]",
							   FIELD_PERSON="[NAME] <[EMAIL]>",
							   FIELD_PERSON_EMAIL="[EMAIL]",
							   FIELD_PERSON_NAME="[NAME]";
	public static final String REPLY_HEADER=NEWLINE+FIELD_DATE+SPACE+FIELD_SENDER+_resources.getString(LanguageResource.REPLY_HEADER_WROTE)+": "+NEWLINE;
	public static final String FORWARD_HEADER=NEWLINE+MINUS3+SPACE+_resources.getString(LanguageResource.FORWARD_HEADER_ORIGINAL_MESSAGE)+SPACE+MINUS3+NEWLINE+
			_resources.getString(LanguageResource.FORWARD_HEADER_SUBJECT)+":	"+FIELD_SUBJECT+NEWLINE+
			_resources.getString(LanguageResource.FORWARD_HEADER_DATE)+":		"+FIELD_DATE+NEWLINE+
			_resources.getString(LanguageResource.FORWARD_HEADER_FROM)+":		"+FIELD_SENDER+NEWLINE+
			_resources.getString(LanguageResource.FORWARD_HEADER_TO)+":		"+FIELD_TO+NEWLINE;
	
	public static final String POPUP_DOWNLOADING=_resources.getString(LanguageResource.POPUP_DOWNLOADING);
	
	public static final String FONT1="BBAlpha Sans";
	
	public static final String attachInfo(SupportedAttachmentPart sap){
		if (sap==null)
			return "";
		return sap.getFilename()+SPACE+(net.rim.device.api.util.MathUtilities.round(((float)sap.getSize())/1024))+"KB"+SPACE;
	}
	
}
