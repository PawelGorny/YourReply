package com.pawelgorny.yourreply.menuitem;

import com.pawelgorny.yourreply.Constants;
import com.pawelgorny.yourreply.Worker;

import net.rim.blackberry.api.mail.Message;
import net.rim.blackberry.api.menuitem.ApplicationMenuItem;
import net.rim.device.api.ui.component.Dialog;

public class CustomMenuItem extends ApplicationMenuItem {

	private Worker worker;
	private int type;
	private String menuTitle;
	
	public CustomMenuItem(Worker worker, int type){
		super(type);
		
		switch (type){
		case Constants.ACTION_FORWARD:
			menuTitle=Constants.FORWARD_MENU;
			break;
		case Constants.ACTION_REPLY:
			menuTitle=Constants.REPLY_MENU;
			break;
		}
		this.worker=worker;
		this.type=type;
	}
	
	public Object run(Object arg0) {
		if (arg0==null || 
			false==(arg0 instanceof net.rim.blackberry.api.mail.Message))
		{
			Dialog.alert(Constants.NOT_EMAIL);
			return null;
		}
		worker.processMessage((Message)arg0, type);
		return null;
	}

	public String toString() {
		return menuTitle;
	}

	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ ((menuTitle == null) ? 0 : menuTitle.hashCode());
		result = prime * result + type;
		return result;
	}

	
}
