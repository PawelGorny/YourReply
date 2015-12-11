package com.pawelgorny.yourreply;

import net.rim.blackberry.api.menuitem.ApplicationMenuItemRepository;
import net.rim.device.api.system.RuntimeStore;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.util.StringUtilities;

import com.pawelgorny.yourreply.menuitem.CustomMenuItem;

public class YourReply extends UiApplication{
	
	static String ApplicationID = YourReply.class.getName();
	static long ID=StringUtilities.stringHashToLong(ApplicationID);
	com.pawelgorny.yourreply.screen.WelcomeScreen welcomeScreen;
	private static boolean isReplyAvailable=true;
	private static boolean isForwardAvailable=true;
	
    public static void main(String[] args){
    	YourReply yourReply=new YourReply();
    	yourReply.enterEventDispatcher();
    }
    
    public YourReply(){
   	
    	RuntimeStore store = RuntimeStore.getRuntimeStore();
    	//CodeSigningKey codeSigningKey = CodeSigningKey.get( moduleHandle, ApplicationID );   
    	
    	Object objFromStore = store.get(ID);
    	
    	if(objFromStore == null){
    		try 
    		{
    		ApplicationMenuItemRepository repository = ApplicationMenuItemRepository.getInstance();
    		Worker worker;
    		if (isReplyAvailable){
    		worker=new Worker();
    		CustomMenuItem customMenuItemReply=new CustomMenuItem(worker, Constants.ACTION_REPLY);
        	repository.addMenuItem(ApplicationMenuItemRepository.MENUITEM_MESSAGE_LIST, customMenuItemReply);
        	repository.addMenuItem(ApplicationMenuItemRepository.MENUITEM_EMAIL_VIEW, customMenuItemReply);
    		}
        	if (isForwardAvailable){
        	worker=new Worker();
        	CustomMenuItem customMenuItemForward=new CustomMenuItem(worker, Constants.ACTION_FORWARD);
        	repository.addMenuItem(ApplicationMenuItemRepository.MENUITEM_MESSAGE_LIST, customMenuItemForward);
        	repository.addMenuItem(ApplicationMenuItemRepository.MENUITEM_EMAIL_VIEW, customMenuItemForward);
        	}
    		store.put( ID, ApplicationID );
    		showWelcomeScreen();
    		} catch(IllegalArgumentException e) 
    		{ 
    			Dialog.alert(Constants.ERROR_ADD_STORE+e.getMessage());
    		}
    		catch(Exception e) 
    		{ 
    			Dialog.alert(Constants.ERROR_ADD_ITEMS+e.getMessage());
    		}
    	}//store = null
    	showWelcomeScreen();
    }
    
    private void showWelcomeScreen(){
    	welcomeScreen=new com.pawelgorny.yourreply.screen.WelcomeScreen();
    	pushScreen(welcomeScreen);
    }
    
}
