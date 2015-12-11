package com.pawelgorny.yourreply;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;

import net.rim.blackberry.api.invoke.Invoke;
import net.rim.blackberry.api.invoke.MessageArguments;
import net.rim.blackberry.api.mail.Address;
import net.rim.blackberry.api.mail.AttachmentDownloadManager;
import net.rim.blackberry.api.mail.BodyPart;
import net.rim.blackberry.api.mail.BodyPart.ContentType;
import net.rim.blackberry.api.mail.DownloadProgressListener;
import net.rim.blackberry.api.mail.Message;
import net.rim.blackberry.api.mail.Message.Flag;
import net.rim.blackberry.api.mail.MessagingException;
import net.rim.blackberry.api.mail.MimeBodyPart;
import net.rim.blackberry.api.mail.Multipart;
import net.rim.blackberry.api.mail.SupportedAttachmentPart;
import net.rim.blackberry.api.mail.TextBodyPart;
import net.rim.blackberry.api.mail.Transport;
import net.rim.blackberry.api.mail.UnsupportedAttachmentPart;
import net.rim.blackberry.api.mail.event.MessageEvent;
import net.rim.blackberry.api.mail.event.MessageListener;
import net.rim.device.api.io.IOUtilities;
import net.rim.device.api.system.LED;
import net.rim.device.api.ui.component.Dialog;

import com.pawelgorny.yourreply.screen.InProgressScreen;
import com.pawelgorny.yourreply.util.MessageUtil;

public class Worker{
	
	private boolean flagText=false;
	private boolean flagAtt=false;
	AttachmentDownloadManager _adm=new AttachmentDownloadManager();
	DownloadProgress downProgress=new DownloadProgress();
	InProgressScreen progressScreen=new InProgressScreen();
	BodyPart bodyDownload=null;
	Message _message=null;

	public void processMessage(Message message, int type){
		Message newMessage=new Message();
		_message=message;
		try {
		switch (type){
		case Constants.ACTION_FORWARD:
			newMessage.setSubject(Constants.FORWARD_PREFIX+message.getSubject());
			newMessage.setContent(getContent(message, type));
			break;
		case Constants.ACTION_REPLY:
			newMessage.setSubject(Constants.REPLY_PREFIX+message.getSubject());
			newMessage.setContent(getContent(message, type));
			//newMessage.setContent(getContent(message, type, null));
			int length=0;
			/*Address[] wasTo=message.getRecipients(Message.RecipientType.TO); 
			if (wasTo!=null)
				length=wasTo.length;*/
			Address[] addresses=new Address[length+1];
			if (length>0)
			{
				//System.arraycopy(wasTo, 0, addresses, 1, length);
			}
			Address sender=message.getFrom();
			if (sender!=null){
			addresses[0]=sender;
			newMessage.addRecipients(Message.RecipientType.TO, addresses);
			}
			/*Address[] wasCC=message.getRecipients(Message.RecipientType.CC);
			if (wasCC!=null && wasCC.length>0){
				newMessage.addRecipients(Message.RecipientType.CC, wasCC);
			}*/
			break;
		}
			newMessage.setPriority(message.getPriority());
			
		} catch (MessagingException e) {
			Dialog.alert(e.getMessage());
		}
		Invoke.invokeApplication(Invoke.APP_TYPE_MESSAGES, new MessageArguments(newMessage));
	}
	
	private Object getContent(Message message, int type) throws MessagingException
	{
		/*String obj=getContent(message, type, null);
		if (type==Constants.ACTION_REPLY)
			obj=MessageUtil.addReplyDelim(obj);
		StringBuffer sbuffer=new StringBuffer(MessageUtil.getHeader(message, type));
		sbuffer.append(obj).append(Constants.NEWLINE);
		Multipart mpart=new Multipart(); 
		TextBodyPart bp=new TextBodyPart(mpart);
		bp.setContent(sbuffer.toString());
		bp.setContentType(ContentType.TYPE_TEXT_PLAIN_STRING);
		mpart.addBodyPart(bp);
		
		/*MimeBodyPart mbp=new MimeBodyPart(mp);
		mbp.setContent(MessageUtil.getReplyHeader(message)+obj);
		mbp.setContentType(ContentType.TYPE_TEXT_HTML_STRING);
		mp.addBodyPart(mbp);*/
		Multipart mpart=new Multipart();
		StringBuffer sbuffer=new StringBuffer(MessageUtil.getHeader(message, type));
		String text=getText(message);		
		Object[] att=null;
		switch(type){
		case Constants.ACTION_FORWARD:
			sbuffer.append(text);
			att=getAttachments(message, mpart);
			break;
		case Constants.ACTION_REPLY:
		    text=MessageUtil.addReplyDelim(text);
			sbuffer.append(text).append(Constants.NEWLINE);
			break;
		}
		TextBodyPart bp=new TextBodyPart(mpart);
		bp.setContent(sbuffer.toString());
		bp.setContentType(ContentType.TYPE_TEXT_PLAIN_STRING);
		mpart.addBodyPart(bp);
		message.setFlag(Message.Flag.OPENED, true);
		if (att!=null && att.length>0){
			for (int a=0; a<att.length; a++)
			{
				if (att[a]!=null){
					if (att[a] instanceof UnsupportedAttachmentPart)
					{
						UnsupportedAttachmentPart uap=(UnsupportedAttachmentPart)att[a];
						mpart.addBodyPart(uap);
					}else if (att[a] instanceof SupportedAttachmentPart)
						{
						SupportedAttachmentPart sap=(SupportedAttachmentPart)att[a];
							mpart.addBodyPart(sap);
						}
				}
			}
		}
		return mpart;
	}
	
	private Object[] getAttachments(Message message, Multipart newMultipart) throws MessagingException{
		flagAtt=false;
		Object[] attachments=null;
		Object obj=message.getContent();
		AttachmentDownloadManager adm=new AttachmentDownloadManager();
		progressScreen=new InProgressScreen();
		if (obj instanceof MimeBodyPart)
		{
			MimeBodyPart mbp = (MimeBodyPart)obj;
			if (mbp.getContentType().equals(ContentType.TYPE_MULTIPART_MIXED_STRING) ||
		    		   mbp.getContentType().equals(ContentType.TYPE_MULTIPART_ALTERNATIVE_STRING))
			   {
				Multipart mp = (Multipart)mbp.getContent();
		    	attachments=new Object[mp.getCount()];
		    	for(int i=mp.getCount()-1; i>=0; i--)
			      {
			    	 if (mp.getBodyPart(i) instanceof SupportedAttachmentPart)  
			   	   		{
			    		  SupportedAttachmentPart sapObj=(SupportedAttachmentPart)mp.getBodyPart(i);
			    		  boolean hasMore=sapObj.hasMore();
			    		  boolean moreRequestSent=sapObj.moreRequestSent();
			    		  if (hasMore && !moreRequestSent
			    	    		  && (Dialog.ask(Dialog.D_OK_CANCEL,Constants.attachInfo(sapObj)+Constants.APP_RETRIEVE_FILE)==Dialog.OK))
			    		  {
			    			  flagAtt=false;
			    		  try {
			    			progressScreen.open();
							adm.download(sapObj, null, new DownloadProgress());
			    		  } catch (IOException e) {
			    			  progressScreen.close();
							e.printStackTrace();
							Dialog.alert(e.getMessage());
			    		  }
			    		  waitForTransportAtt();
			    		  }
			    		  byte[] content= (byte[])sapObj.getContent();
			    		  if (content==null || content.length==0)
			    		  {
			    			  try {
				    			  progressScreen.open();
				    			  flagAtt=false;
									adm.download(sapObj, null, new DownloadProgress());
					    		  } catch (Exception e) {
					    			  progressScreen.close();
					    			  e.printStackTrace();
					    		  }
			    			  waitForTransportAtt();
			    			  if(adm.isDownloaded(sapObj))
			    			  {	
			    			  content=fileToBytes(adm.getDownloadedFileName(sapObj));
			    			  }
			    		  }
			    		  progressScreen.close();
			    		  if (content!=null && content.length>0){
			    		  SupportedAttachmentPart att=new SupportedAttachmentPart(newMultipart,
			    				  sapObj.getContentType(),
			    				  sapObj.getFilename(),
			    				  content
			    				  );
			    		  attachments[i]=att;
			    		  }
			   	   		}
			   	   		else if (mp.getBodyPart(i) instanceof UnsupportedAttachmentPart) 
			   	   		{
			   	   			/*UnsupportedAttachmentPart uapObj=(UnsupportedAttachmentPart)mp.getBodyPart(i);
			   	   			if (uapObj.hasMore() && !uapObj.moreRequestSent()
			   	   					&& (Dialog.ask(Dialog.D_OK_CANCEL, Constants.APP_RETRIEVE_DATA)==Dialog.OK)){
			   	   				flag=false;
			   	   				Transport.more((BodyPart)uapObj, true);
			   	   				waitForTransport(uapObj);
			   	   			}
			   	   		attachments[i]=uapObj;
			   	   		isAttachment=true;*/
			   	   		}	
			      }
			   }
		}
		if(obj instanceof Multipart)
		{
			Multipart mp = (Multipart)obj;
	    	  attachments=new Object[mp.getCount()];
	    	  for(int i=mp.getCount()-1; i>=0; i--)
		      {
		    	  if (mp.getBodyPart(i) instanceof SupportedAttachmentPart)  
		   	   		{
		    		  SupportedAttachmentPart sapObj=(SupportedAttachmentPart)mp.getBodyPart(i);
		    		  boolean hasMore=sapObj.hasMore();
		    		  boolean moreRequestSent=sapObj.moreRequestSent();
		    		  if (hasMore && !moreRequestSent
		    	    		  && (Dialog.ask(Dialog.D_OK_CANCEL, Constants.attachInfo(sapObj)+Constants.APP_RETRIEVE_FILE)==Dialog.OK))
		    		  {
		    			  flagAtt=false;
		    		  try {
		    			  progressScreen.open();
						adm.download(sapObj, null, new DownloadProgress());
		    		  } catch (IOException e) {
		    			  progressScreen.close();
						e.printStackTrace();
						Dialog.alert(e.getMessage());
		    		  }
		    		  waitForTransportAtt();
		    		  }
		    		  byte[] content= (byte[])sapObj.getContent();
		    		  if (content==null || content.length==0)
		    		  {
		    			  try {
			    			  progressScreen.open();
			    			  flagAtt=false;
								adm.download(sapObj, null, new DownloadProgress());
								waitForTransport();
				    		  } catch (Exception e) {
				    			  progressScreen.close();
				    			  e.printStackTrace();
				    		  }
		    			  waitForTransportAtt();
		    			  if(adm.isDownloaded(sapObj))
		    			  {	
		    			  content=fileToBytes(adm.getDownloadedFileName(sapObj));
		    			  }
		    		  }
		    		  progressScreen.close();
		    		  if (content!=null && content.length>0){
		    		  SupportedAttachmentPart att=new SupportedAttachmentPart(newMultipart,
		    				  sapObj.getContentType(),
		    				  sapObj.getFilename(),
		    				  content
		    				  );
		    		  attachments[i]=att;
		    		  }
		   	   		}
		   	   		else if (mp.getBodyPart(i) instanceof UnsupportedAttachmentPart) 
		   	   		{/*
		   	   			UnsupportedAttachmentPart uapObj=(UnsupportedAttachmentPart)mp.getBodyPart(i);
	   	   				if (uapObj.hasMore() && !uapObj.moreRequestSent()
	   	   					&& (Dialog.ask(Dialog.D_OK_CANCEL, Constants.APP_RETRIEVE_DATA)==Dialog.OK)){
	   	   				flag=false;
	   	   				Transport.more((BodyPart)uapObj, true);
	   	   				waitForTransport(uapObj);
	   	   				}
	   	   				attachments[i]=uapObj;
	   	   				isAttachment=true;*/
		   	   		}	
		      }
		}//Multipart
		return attachments;
	}
		
	private String getText(Message message) throws MessagingException{
		String result=getText(message, false);
		try{progressScreen.close();}
		catch (Exception e) {	}
		return result;
	}
	
	private String getText(Message message, boolean noTransport) throws MessagingException{
		if (!noTransport){
		flagText=false;
		message.addMessageListener(new MyMessageListener());
		}
		Object obj=message.getContent();
		if (obj instanceof TextBodyPart)
		   {
		      return readEmailBody((TextBodyPart) obj);	      
		   }//TextBodyPart
		if (obj instanceof MimeBodyPart)
		{
			MimeBodyPart mbp = (MimeBodyPart)obj;
		      if (mbp.getContentType().indexOf(ContentType.TYPE_TEXT_HTML_STRING) != -1
		    		  ||mbp.getContentType().indexOf(ContentType.TYPE_TEXT_PLAIN_STRING) != -1
		    		  )
		      {
		    	if (noTransport)
		    		return readEmailNoTransportAndQuiet(mbp);
		    	String[] result=readEmailBody(mbp);
		    	if ("T".equals(result[1])){
		    		return result[0];
		    	}else
		    	{//TODO html2text
		    		return result[0];
		    	}
		      }//MimeBodyPart text
		      if (mbp.getContentType().equals(ContentType.TYPE_MULTIPART_MIXED_STRING) ||
		    		   mbp.getContentType().equals(ContentType.TYPE_MULTIPART_ALTERNATIVE_STRING))
			   {
		    	  Multipart mp = (Multipart)mbp.getContent();
		    	  for(int i=mp.getCount()-1; i>=0; i--)
			      {
			    	  if (mp.getBodyPart(i)instanceof TextBodyPart)
			    	  {
					      return readEmailBody((TextBodyPart)mp.getBodyPart(i));
			    	  }
			    	  else if(mp.getBodyPart(i)instanceof MimeBodyPart)
			    	  {
			    		MimeBodyPart mbpp=(MimeBodyPart)mp.getBodyPart(i);
			    		if (mbpp.getContentType().indexOf(ContentType.TYPE_TEXT_PLAIN_STRING) != -1)
			    		{
			    			if (noTransport)
					    		return readEmailNoTransportAndQuiet(mbpp);
			    			String[] result=readEmailBody(mbpp);
					    	if ("T".equals(result[1])){
					    		return result[0];
					    	}else
					    	{//TODO html2text
					    		return result[0];
					    	}
			    		}
			    	  }
			      }
			   }//MimeBodyPart multi
		}//MimeBodyPart
		if(obj instanceof Multipart)
		{
			Multipart mp = (Multipart)obj;
	    	  for(int i=mp.getCount()-1; i>=0; i--)
		      {
		    	  if (mp.getBodyPart(i)instanceof TextBodyPart)
		    	  {
		    		  return readEmailBody((TextBodyPart)mp.getBodyPart(i));
		    	  }
		    	  else if(mp.getBodyPart(i)instanceof MimeBodyPart)
		    	  {
		    		MimeBodyPart mbp=(MimeBodyPart)mp.getBodyPart(i);
		    		return getContent(message, mbp);
		    	  }
		      }
		}//Multipart
		return null;
	}
	
	private String getContent(Message message, Object obj) throws MessagingException{
		if (obj==null)
			obj=message.getContent();
		if (obj instanceof TextBodyPart)
		   {
		      TextBodyPart tbp = (TextBodyPart) obj;
		      return((readEmailBody(tbp)));
		   }
		if (obj instanceof MimeBodyPart)
		{
			MimeBodyPart mbp = (MimeBodyPart)obj;
		      if (mbp.getContentType().indexOf(ContentType.TYPE_TEXT_HTML_STRING) != -1||
		    		  mbp.getContentType().indexOf(ContentType.TYPE_TEXT_PLAIN_STRING) != -1
		    		  )
		      {
		    	String[] result=readEmailBody(mbp);
		    	if ("T".equals(result[1])){
		    		return((result[0]));
		    	}else
		    	{//TODO html2text
		    		return result[0];
		    	}
		      }
		      if (mbp.getContentType().equals(ContentType.TYPE_MULTIPART_MIXED_STRING) ||
		    		   mbp.getContentType().equals(ContentType.TYPE_MULTIPART_ALTERNATIVE_STRING))
			   {    
			      //The message has attachments or we are at the top level of the message.
			      //Extract all of the parts within the MimeBodyPart message.
			      return getContent(message, mbp.getContent());
			   }
		}
		if(obj instanceof Multipart)
		   {
		      Multipart mp = (Multipart)obj;
		      String result="";
		      int isPlainText=-1;
		      try{
			      for(int i=mp.getCount()-1; i>=0; i--)
			      {
			    	  if (mp.getBodyPart(i)instanceof TextBodyPart)
			    	  {
			    		  isPlainText=i;
			    		  break;
			    	  }
			    	  else if(mp.getBodyPart(i)instanceof MimeBodyPart)
			    	  {
			    		MimeBodyPart mbp=(MimeBodyPart)mp.getBodyPart(i);
			    		if (mbp.getContentType().indexOf(ContentType.TYPE_TEXT_PLAIN_STRING) != -1)
			    		{
			    			isPlainText=i;
			    			break;
			    		}
			    	  }
			      }
		      }catch(Exception e){}
		      if (isPlainText!=-1)
		      {
		    	  return getContent(message, mp.getBodyPart(isPlainText));
		      }
		      for(int count=0; count < mp.getCount(); ++count)
		      {
		        String bodyPart=(String)getContent(message, mp.getBodyPart(count));
		        if (bodyPart!=null)
		        {
		        	result+=bodyPart;
		        }
		      }
		      return result;
		   }
		
		return null;
	}
	
	private String readEmailBody(TextBodyPart tbp)
	{
	  String _plainTextMessage = (String)tbp.getContent();

	  boolean hasMore=tbp.hasMore();
	  boolean moreRequestSent=tbp.moreRequestSent();
	   if (hasMore && !moreRequestSent)
	   {
	      try
	      {
	//If more data is available for the TextBodyPart object, invoke
	//Transport.more()  to retrieve the rest of the TextBodyPart object.
	    	 flagText=false;
	         Transport.more((BodyPart)tbp, true);
	         //waitForTransport(tbp);
	         _plainTextMessage = (String)tbp.getContent();
	      }
	      catch (Exception ex)
	      {
	         Dialog.alert("Exception: " + ex.toString());
	      }
	   }
	   return (_plainTextMessage);
	}   
	
	private String readEmailNoTransportAndQuiet(BodyPart mbp){
		String mimeType = mbp.getContentType();
		Object obj = mbp.getContent();
		String body = null;
		   if (obj instanceof String)
		   {
		      body = (String)obj;
		   }
		   else if (obj instanceof byte[])
		   {
		      try {
				body = new String((byte[])obj, Constants.EMAIL_ENCODING); } catch (UnsupportedEncodingException e) {}
		   }
		if (mimeType.indexOf(ContentType.TYPE_TEXT_PLAIN_STRING) != -1)
		   {
		      String _plainTextMessage = body;

		      boolean hasMore=mbp.hasMore();
			  boolean moreRequestSent=mbp.moreRequestSent();
		      if (hasMore && !moreRequestSent
		    		 /* && (Dialog.ask(Dialog.D_OK_CANCEL, Constants.APP_RETRIEVE_DATA)==Dialog.OK)*/)
		      {
		         try
		         {
		            obj = mbp.getContent();
			         if (obj instanceof String)
			  	   	{
			        	 _plainTextMessage = (String)obj;
			  	   	}
			  	   else if (obj instanceof byte[])
			  	   {
			  		 _plainTextMessage = new String((byte[])obj, Constants.EMAIL_ENCODING);
			  	   }
		         }
		         catch (Exception ex)
		         {
		         }
		      }//if hasMore
		      return ( _plainTextMessage) ;
		   }//plain
		   else if (mimeType.indexOf(ContentType.TYPE_TEXT_HTML_STRING) != -1)
		   {
		      String _htmlMessage = body;
		      if (mbp.hasMore() && !mbp.moreRequestSent() )
		      {
		         try
		         {
		            obj = mbp.getContent();
			         if (obj instanceof String)
			  	   	{
			        	 _htmlMessage = (String)obj;
			  	   	}
			  	   else if (obj instanceof byte[])
			  	   {
			  		 _htmlMessage = new String((byte[])obj, Constants.EMAIL_ENCODING);
			  	   }			         
		         }
		         catch (Exception ex)
		         { 
		         }
		      }//if hasMore
		    return _htmlMessage; 
		   }//html
		   return null;
	}
	
	//Create a method that takes a MimeBodyPart object as a parameter.
	private String[] readEmailBody(MimeBodyPart mbp)
	{
	  String[] result=new String[2]; 
	   Object obj = mbp.getContent();
	   String mimeType = mbp.getContentType();

	   String body = null;

	   if (obj instanceof String)
	   {
	      body = (String)obj;
	   }
	   else if (obj instanceof byte[])
	   {
	      try {
			body = new String((byte[])obj, Constants.EMAIL_ENCODING);
		} catch (UnsupportedEncodingException e) {
			Dialog.alert("Exception: " + e.getMessage());
		}
	   }

	   if (mimeType.indexOf(ContentType.TYPE_TEXT_PLAIN_STRING) != -1)
	   {
	      String _plainTextMessage = body;

	      boolean hasMore=mbp.hasMore();
		  boolean moreRequestSent=mbp.moreRequestSent();
	      if (hasMore && !moreRequestSent
	    		 /* && (Dialog.ask(Dialog.D_OK_CANCEL, Constants.APP_RETRIEVE_DATA)==Dialog.OK)*/)
	      {
	    	 bodyDownload=mbp;
	         try
	         {
	        	flagText=false;
	        	progressScreen.open();
	            Transport.more((BodyPart)mbp, true);
	            obj=waitForTransport(true);
	            //obj = mbp.getContent();
		         if (obj instanceof String)
		  	   	{
		        	 _plainTextMessage = (String)obj;
		  	   	}
		  	   else if (obj instanceof byte[])
		  	   {
		  		 _plainTextMessage = new String((byte[])obj, Constants.EMAIL_ENCODING);
		  	   }
		         progressScreen.close();
		         bodyDownload=null;
	         }
	         catch (Exception ex)
	         {
	            Dialog.alert("Exception: " + ex.getMessage());
	         }
	      }//if hasMore
	      result[0]=_plainTextMessage;
	      result[1]= "T";
	      return ( result) ;
	   }//plain
	   else if (mimeType.indexOf(ContentType.TYPE_TEXT_HTML_STRING) != -1)
	   {
	      String _htmlMessage = body;
	      if (mbp.hasMore() && !mbp.moreRequestSent() 
	    		  && (Dialog.ask(Dialog.D_OK_CANCEL, Constants.APP_RETRIEVE_DATA)==Dialog.OK))
	      {
	         try
	         {
	        	flagText=false;
	        	progressScreen.open();
	            Transport.more((BodyPart)mbp, true);
	            obj=waitForTransport(true);
	            //obj = mbp.getContent();
		         if (obj instanceof String)
		  	   	{
		        	 _htmlMessage = (String)obj;
		  	   	}
		  	   	else if (obj instanceof byte[])
		  	   	{
		  		 _htmlMessage = new String((byte[])obj, Constants.EMAIL_ENCODING);
		  	   	}
		         progressScreen.close();
		         bodyDownload=null;
	         }
	         catch (Exception ex)
	         { 
	            Dialog.alert("Exception: " + ex.toString());
	         }
	      }//if hasMore
	      result[0]=_htmlMessage;
	      result[1]="H";
	    return result; 
	   }//html
	   return null;
	}//readEmailBody
	
	private String waitForTransport(boolean useMessage){
		int chanceNb=0;
		String content=null;
		if (useMessage){
			while (!flagText && chanceNb<150 && (content==null || content.length()==0)){
	        	try {
					Thread.sleep(100);
					//synchronized (_message) 
					{
						content=getText(_message, true);	
					}
	        	} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (MessagingException e) {
					e.printStackTrace();
				}
	        	chanceNb++;
	        }
			if (content!=null && content.length()!=0){
				setOpened(_message);
			}
		}
		else
		{
			waitForTransport();
			try {
				content=getText(_message, true);
			} catch (MessagingException e) {
				e.printStackTrace();
			}
		}
		return content;
	}
	
	private void waitForTransport(){
		int chanceNb=0;
        while (!flagText && chanceNb<600){
        	try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
        	chanceNb++;
        }
        progressScreen.close();
	}
	
	private void waitForTransportAtt(){
		int chanceNb=0;
        while (!flagAtt && chanceNb<600){
        	try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
        	chanceNb++;
        }
        progressScreen.close();
	}
	
	private byte[] fileToBytes(String path){
		byte [] result=null;
		try{
			FileConnection file=(FileConnection)Connector.open(path, Connector.READ);
			if (!file.exists()|| !file.canRead()){
					return null;
				}
			//java.io.InputStream is=file.openDataInputStream();
			java.io.InputStream is=file.openInputStream();
			int length=(int)file.fileSize();
			result=new byte[length];
			result = IOUtilities.streamToBytes(is);
		}catch (Exception e) {
			Dialog.alert(e.getMessage());
		}
		
		return result;
	}
	
	private class MyMessageListener implements MessageListener{
		{
			flagText=false;
		}
		public void changed(MessageEvent e) {
				if (bodyDownload!=null && !bodyDownload.moreRequestSent()){
					synchronized (_message) 
					{
						_message=e.getMessage();
						String content=null;
						try {
							content = getText(_message, true);
						} catch (MessagingException e1) {}
				         if (content!=null && content.length()>0)
				         {
						 flagText=true;
						 bodyDownload=null;
						 setOpened(_message);
						 try{
							 progressScreen.close();
						 	}catch(Exception exc){}
				         }	
					}//syn
					}
		}
	}//mylistener
	
	private void setOpened(Message message){
		message.setFlag(Flag.OPENED, true);
		LED.setState(LED.STATE_OFF);
	}
	
	private class DownloadProgress implements DownloadProgressListener{
		
		public void downloadCancelled(Object element) {
			flagAtt=true;
			//BodyPart bodyPart=(BodyPart)element;
			//Dialog.alert("Failed to download: "+_adm.getFileName(bodyPart));
		}

		public void downloadCompleted(Object element) {
			BodyPart bodyPart=(BodyPart)element;
			if (bodyPart.hasMore() && !bodyPart.moreRequestSent())
				try {
					_adm.download(bodyPart, null, this);
				} catch (IOException e) {
					e.printStackTrace();
				}
			try{
			
			}catch(Exception e){
				
			}
			//String filenameCompleted=_adm.getFileName(bodyPart);
			//Dialog.alert("Download complete: "+_adm.getFileName(bodyPart));
			flagAtt=true;
		}

		public void updateProgress(Object element, int current, int total) {
			// TODO Auto-generated method stub
		}
		
	}

}
