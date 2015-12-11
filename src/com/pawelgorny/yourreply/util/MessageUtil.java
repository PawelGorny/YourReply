package com.pawelgorny.yourreply.util;

import net.rim.blackberry.api.mail.Address;
import net.rim.blackberry.api.mail.Message;
import net.rim.blackberry.api.mail.MessagingException;
import net.rim.device.api.i18n.SimpleDateFormat;

import com.pawelgorny.yourreply.Constants;

public class MessageUtil {

	public static String addReplyDelim(String body){
		String[] newBody=StringUtil.split(body, "\n");
		StringBuffer sb=new StringBuffer(body.length()+2*(newBody.length+Constants.NEWLINE.length()));
		for (int i=0; i<newBody.length; i++){
			sb.append(Constants.REPLY_DELIM).append(newBody[i]).append(Constants.NEWLINE);
		}
		return sb.toString();
	}
	
	public static String getHeader(Message message, int type) throws MessagingException{
		switch (type){
		case Constants.ACTION_FORWARD:
			return getForwardHeader(message);
		case Constants.ACTION_REPLY:
			return getReplyHeader(message);
		default:
			return null;
	}
	}
	
	public static String getReplyHeader(Message message) throws MessagingException{
		
		SimpleDateFormat sdf=new SimpleDateFormat(Constants.DATETIME_FORMAT);
		String header= StringUtil.replace(Constants.REPLY_HEADER,
				Constants.FIELD_DATE, sdf.format(message.getSentDate()));
		header=StringUtil.replace(header, Constants.FIELD_SENDER, addressToString(message.getFrom()));
		return header;
	}
	
	public static  String getForwardHeader(Message message) throws MessagingException{
		SimpleDateFormat sdf=new SimpleDateFormat(Constants.DATETIME_FORMAT);
		String header= StringUtil.replace(Constants.FORWARD_HEADER,
				Constants.FIELD_DATE, sdf.format(message.getSentDate()));
		header=StringUtil.replace(header, Constants.FIELD_SUBJECT, message.getSubject());
		header=StringUtil.replace(header, Constants.FIELD_SENDER, addressToString(message.getFrom()));
		Address[] to=message.getRecipients(Message.RecipientType.TO);
		StringBuffer recipient=new StringBuffer();
		for (int i=0; i<to.length; i++)
		{
			recipient.append(addressToString(to[i])).append(Constants.NEWLINE);
		}
		to=message.getRecipients(Message.RecipientType.CC);
		for (int i=0; i<to.length; i++)
		{
			recipient.append(addressToString(to[i])).append(Constants.NEWLINE);
		}
		header=StringUtil.replace(header, Constants.FIELD_TO, recipient.toString());
		return header;
	}
	
	public static  String addressToString(Address address){
		if (address==null)
			return Constants.SPACE;
		String ret=StringUtil.replace(Constants.FIELD_PERSON, Constants.FIELD_PERSON_EMAIL, address.getAddr()==null?"":address.getAddr());
		ret=StringUtil.replace(ret, Constants.FIELD_PERSON_NAME, address.getName()==null?"":address.getName());
		return ret;
	}
	
	
}
