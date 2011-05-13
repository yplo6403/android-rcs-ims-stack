/*******************************************************************************
 * Software Name : RCS IMS Stack
 *
 * Copyright © 2010 France Telecom S.A.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.orangelabs.rcs.service.api.server.messaging;

import java.util.List;

import android.os.RemoteCallbackList;
import android.os.RemoteException;

import com.orangelabs.rcs.core.ims.service.im.chat.ChatError;
import com.orangelabs.rcs.core.ims.service.im.chat.ChatSession;
import com.orangelabs.rcs.core.ims.service.im.chat.ChatSessionListener;
import com.orangelabs.rcs.core.ims.service.im.chat.ChatUtils;
import com.orangelabs.rcs.core.ims.service.im.chat.imdn.ImdnDocument;
import com.orangelabs.rcs.provider.messaging.RichMessaging;
import com.orangelabs.rcs.provider.settings.RcsSettings;
import com.orangelabs.rcs.service.api.client.eventslog.EventsLogApi;
import com.orangelabs.rcs.service.api.client.messaging.IChatEventListener;
import com.orangelabs.rcs.service.api.client.messaging.IChatSession;
import com.orangelabs.rcs.service.api.client.messaging.InstantMessage;
import com.orangelabs.rcs.utils.logger.Logger;

/**
 * IM session
 * 
 * @author jexa7410
 */
public class ImSession extends IChatSession.Stub implements ChatSessionListener {
	
	/**
	 * Core session
	 */
	private ChatSession session;

	/**
	 * List of listeners
	 */
	private RemoteCallbackList<IChatEventListener> listeners = new RemoteCallbackList<IChatEventListener>();

    /**
	 * The logger
	 */
	private Logger logger = Logger.getLogger(this.getClass().getName());

	/**
	 * Constructor
	 * 
	 * @param session Session
	 */
	public ImSession(ChatSession session) {
		this.session = session;
		session.addListener(this);
	}

	/**
	 * Get session ID
	 * 
	 * @return Session ID
	 */
	public String getSessionID() {
		return session.getSessionID();
	}
	
	/**
	 * Get remote contact
	 * 
	 * @return Contact
	 */
	public String getRemoteContact() {
		return session.getRemoteContact();
	}
	
	/**
	 * Is chat group
	 * 
	 * @return Boolean
	 */
	public boolean isChatGroup() {
		return session.isChatGroup();
	}
	
	/**
	 * Get subject
	 * 
	 * @return Subject
	 */
	public String getSubject() {
		return session.getSubject();
	}

	/**
	 * Accept the session invitation
	 */
	public void acceptSession() {
		if (logger.isActivated()) {
			logger.info("Accept session invitation");
		}
				
		// Accept invitation
		session.acceptSession();

	}
	
	/**
	 * Reject the session invitation
	 */
	public void rejectSession() {
		if (logger.isActivated()) {
			logger.info("Reject session invitation");
		}

		if (isChatGroup()){
			RichMessaging.getInstance().addGroupChatTermination(session.getParticipants().getList(), session.getSessionID());
		}else{
			RichMessaging.getInstance().addOneToOneChatTermination(getRemoteContact(), session.getSessionID());
		}
		
        // Reject invitation
		session.rejectSession();

	}

	/**
	 * Cancel the session
	 */
	public void cancelSession() {
		if (logger.isActivated()) {
			logger.info("Cancel session");
		}
		
		// Update rich messaging history
		if (!RichMessaging.getInstance().isSessionTerminated(session.getSessionID())){
			if (isChatGroup()){
				RichMessaging.getInstance().addGroupChatTermination(session.getParticipants().getList(), session.getSessionID());
			}else{
				RichMessaging.getInstance().addOneToOneChatTermination(getRemoteContact(), session.getSessionID());
			}
		}
		
		// Abort the session
		session.abortSession();
	}
	
	
	/**
	 * Get list of participants to the session
	 * 
	 * @return List
	 */
	public List<String> getParticipants() {
		if (logger.isActivated()) {
			logger.info("Get list of participants in the session");
		}
		return session.getParticipants().getList();
	}
	
	/**
	 * Add a participant to the session
	 * 
	 * @param participant Participant
	 */
	public void addParticipant(String participant) {
		if (logger.isActivated()) {
			logger.info("Add participant " + participant + " to the session");
		}
		
		// TODO: check max participants
		
		// Add a participant to the session
		session.addParticipant(participant);
	}
	
	/**
	 * Add a list of participants to the session
	 * 
	 * @param participants List of participants
	 */
	public void addParticipants(List<String> participants) {
		if (logger.isActivated()) {
			logger.info("Add " + participants.size() + " participants to the session");
		}
		
		// TODO: check max participants

		// Add a list of participants to the session
		session.addParticipants(participants);
	}
	
	/**
	 * Send a text message
	 * 
	 * @param text Text message
	 */
	public void sendMessage(String text) {
		String msgId = null;
		if (RcsSettings.getInstance().isImReportsActivated()){
			msgId = ChatUtils.generateMessageId();
	    	// Send text message with IMDN headers
			session.sendTextMessage(text,msgId);
		}else{
	    	// Send text message
			session.sendTextMessage(text);
		}

		// Update rich messaging history
		RichMessaging.getInstance().addChatMessageInitiation(new InstantMessage(msgId, getRemoteContact(), text), session);
	}

	/**
	 * Set is composing status
	 * 
	 * @param status Status
	 */
	public void setIsComposingStatus(boolean status) {
		session.sendIsComposingStatus(status);
	}

	/**
	 * Set message delivery status
	 * 
	 * @param msgId Message ID
	 * @param contact Contact that sent the delivery status
	 * @param status Delivery status
	 */
	public void setMessageDeliveryStatus(String msgId, String contact, String status) {

		//TODO for now, we ignore all statuses except for "displayed"
		if (status.equalsIgnoreCase(ImdnDocument.DELIVERY_STATUS_DISPLAYED)){
			if (logger.isActivated()){
				logger.debug("We mark the message "+msgId+ " as reported and send the delivery status to the remote");
			}
			try{
				// Send MSRP delivery status
				session.sendMsrpMessageDeliveryStatus(msgId, contact, status);

				// Update rich messaging history
				RichMessaging.getInstance().setMessageDeliveryStatus(msgId, contact, EventsLogApi.STATUS_REPORTED, getParticipants().size());
			}catch(Exception e){
				if (logger.isActivated()){
					logger.error("Could not send MSRP delivery status",e);
				}
			}
		}
	}
	
	/**
	 * Add session listener
	 * 
	 * @param listener Listener
	 */
	public void addSessionListener(IChatEventListener listener) {
		if (logger.isActivated()) {
			logger.info("Add an event listener");
		}

		listeners.register(listener);
	}
	
	/**
	 * Remove session listener
	 * 
	 * @param listener Listener
	 */
	public void removeSessionListener(IChatEventListener listener) {
		if (logger.isActivated()) {
			logger.info("Remove an event listener");
		}

		listeners.unregister(listener);
	}
	
	/**
	 * Session is started
	 */
    public void handleSessionStarted() {
		if (logger.isActivated()) {
			logger.info("Session started");
		}

  		// Notify event listeners
		final int N = listeners.beginBroadcast();
        for (int i=0; i < N; i++) {
            try {
            	listeners.getBroadcastItem(i).handleSessionStarted();
            } catch (RemoteException e) {
            	if (logger.isActivated()) {
            		logger.error("Can't notify listener", e);
            	}
            }
        }
        listeners.finishBroadcast();		
    }

    /**
     * Session has been aborted
     */
    public void handleSessionAborted() {
		if (logger.isActivated()) {
			logger.info("Session aborted");
		}

		// Update rich messaging history
		if (!RichMessaging.getInstance().isSessionTerminated(session.getSessionID())){
			if (isChatGroup()) {
				RichMessaging.getInstance().addGroupChatTermination(session.getParticipants().getList(), session.getSessionID());
			} else {
				RichMessaging.getInstance().addOneToOneChatTermination(getRemoteContact(), session.getSessionID());
			}
		}
		
  		// Notify event listeners
		final int N = listeners.beginBroadcast();
        for (int i=0; i < N; i++) {
            try {
            	listeners.getBroadcastItem(i).handleSessionAborted();
            } catch (RemoteException e) {
            	if (logger.isActivated()) {
            		logger.error("Can't notify listener", e);
            	}
            }
        }
        listeners.finishBroadcast();		
    }
    
    /**
     * Session has been terminated by remote
     */
    public void handleSessionTerminatedByRemote() {
		if (logger.isActivated()) {
			logger.info("Session terminated by remote");
		}

		if (isChatGroup()){
			RichMessaging.getInstance().addGroupChatTermination(session.getParticipants().getList(), session.getSessionID());
		}else{
			RichMessaging.getInstance().addOneToOneChatTermination(getRemoteContact(), session.getSessionID());
		}
		
  		// Notify event listeners
		final int N = listeners.beginBroadcast();
        for (int i=0; i < N; i++) {
            try {
            	listeners.getBroadcastItem(i).handleSessionTerminatedByRemote();
            } catch (RemoteException e) {
            	if (logger.isActivated()) {
            		logger.error("Can't notify listener", e);
            	}
            }
        }
        listeners.finishBroadcast();		
    }

	/**
	 * New text message received
	 * 
	 * @param text Text message
	 */
    public void handleReceiveMessage(InstantMessage message) {
		if (logger.isActivated()) {
			logger.info("New IM received");
		}
		
		// Update rich messaging history
		RichMessaging.getInstance().addChatMessageReception(message, session);
		
  		// Notify event listeners
		final int N = listeners.beginBroadcast();
        for (int i=0; i < N; i++) {
            try {
            	listeners.getBroadcastItem(i).handleReceiveMessage(message);
            } catch (RemoteException e) {
            	if (logger.isActivated()) {
            		logger.error("Can't notify listener", e);
            	}
            }
        }
        listeners.finishBroadcast();		
    }
    
    /**
	 * Message has been transfered
	 */
    public void handleMessageTransfered() {
    	// TODO
    }
    
    /**
     * IM session error
     * 
     * @param error Error
     */
    public void handleImError(ChatError error) {
		if (logger.isActivated()) {
			logger.info("IM error received");
		}
		
		// Update rich messaging history
    	switch(error.getErrorCode()){
	    	case ChatError.SESSION_INITIATION_DECLINED:
	    	case ChatError.SESSION_INITIATION_CANCELLED:
				if (isChatGroup()){
					RichMessaging.getInstance().addGroupChatTermination(session.getParticipants().getList(), session.getSessionID());
				}else{
					RichMessaging.getInstance().addOneToOneChatTermination(getRemoteContact(), session.getSessionID());
				}
	    		break;
	    	default:
				if (isChatGroup()){
					RichMessaging.getInstance().addGroupChatError(session.getParticipants().getList(), session.getSessionID());
				}else{
					RichMessaging.getInstance().addOneToOneChatError(getRemoteContact(), session.getSessionID());
				}
	    		break;
    	}
    	
  		// Notify event listeners
		final int N = listeners.beginBroadcast();
        for (int i=0; i < N; i++) {
            try {
            	listeners.getBroadcastItem(i).handleImError(error.getErrorCode());
            } catch (RemoteException e) {
            	if (logger.isActivated()) {
            		logger.error("Can't notify listener", e);
            	}
            }
        }
        listeners.finishBroadcast();	
    }

    /**
	 * Is composing event
	 * 
	 * @param contact Contact
	 * @param status Status
	 */
	public void handleIsComposingEvent(String contact, boolean status) {
		if (logger.isActivated()) {
			logger.info(contact + " is composing status set to " + status);
		}

  		// Notify event listeners
		final int N = listeners.beginBroadcast();
        for (int i=0; i < N; i++) {
            try {
            	listeners.getBroadcastItem(i).handleIsComposingEvent(contact, status);
            } catch (RemoteException e) {
            	if (logger.isActivated()) {
            		logger.error("Can't notify listener", e);
            	}
            }
        }
        listeners.finishBroadcast();
	}

    /**
     * Conference event
     * 
	 * @param contact Contact
     * @param state State associated to the contact
     */
    public void handleConferenceEvent(String contact, String state) {
		if (logger.isActivated()) {
			logger.info("New conference event " + state + " for " + contact);
		}
		
		// Update rich messaging history
		RichMessaging.getInstance().newConferenceEvent(contact, session.getSessionID(), state);

  		// Notify event listeners
		final int N = listeners.beginBroadcast();
        for (int i=0; i < N; i++) {
            try {
            	listeners.getBroadcastItem(i).handleConferenceEvent(contact, state);
            } catch (RemoteException e) {
            	if (logger.isActivated()) {
            		logger.error("Can't notify listener", e);
            	}
            }
        }
        listeners.finishBroadcast();
    }

    /**
     * New message delivery status
     * 
	 * @param msgId Message ID
	 * @param contact Contact who delivers the status
     * @param status Delivery status
     */
    public void handleMessageDeliveryStatus(String msgId, String contact, String status) {
		if (logger.isActivated()) {
			logger.info("New message delivery status for msgId "+msgId+",from "+contact+", status "+status);
		}

		// Update rich messaging history
		if (status.equalsIgnoreCase(ImdnDocument.DELIVERY_STATUS_DISPLAYED)){
			// Update rich messaging history
			RichMessaging.getInstance().setMessageDeliveryStatus(msgId, contact, EventsLogApi.STATUS_DISPLAYED, getParticipants().size());
		}else if (status.equalsIgnoreCase(ImdnDocument.DELIVERY_STATUS_DELIVERED)){
			// Update rich messaging history
			RichMessaging.getInstance().setMessageDeliveryStatus(msgId, contact, EventsLogApi.STATUS_DELIVERED, getParticipants().size());			
		}else if ((status.equalsIgnoreCase(ImdnDocument.DELIVERY_STATUS_ERROR)) ||
				(status.equalsIgnoreCase(ImdnDocument.DELIVERY_STATUS_FAILED)) ||
				(status.equalsIgnoreCase(ImdnDocument.DELIVERY_STATUS_FORBIDDEN))){
			// Update rich messaging history
			RichMessaging.getInstance().setMessageDeliveryStatus(msgId, contact, EventsLogApi.STATUS_FAILED, getParticipants().size());
		}
		
  		// Notify event listeners
		final int N = listeners.beginBroadcast();
        for (int i=0; i < N; i++) {
            try {
            	listeners.getBroadcastItem(i).handleMessageDeliveryStatus(msgId, status);
            } catch (RemoteException e) {
            	if (logger.isActivated()) {
            		logger.error("Can't notify listener", e);
            	}
            }
        }
        listeners.finishBroadcast();
    }
    
    
    /**
     * Request to add participant is successful
     */
    public void handleAddParticipantSuccessful() {
		if (logger.isActivated()) {
			logger.info("Add participant request is successful");
		}

		// Update rich messaging history
		// TODO
		
  		// Notify event listeners
		final int N = listeners.beginBroadcast();
        for (int i=0; i < N; i++) {
            try {
            	listeners.getBroadcastItem(i).handleAddParticipantSuccessful();
            } catch (RemoteException e) {
            	if (logger.isActivated()) {
            		logger.error("Can't notify listener", e);
            	}
            }
        }
        listeners.finishBroadcast();
    }
    
    /**
     * Request to add participant has failed
     * 
     * @param reason Error reason
     */
    public void handleAddParticipantFailed(String reason) {
		if (logger.isActivated()) {
			logger.info("Add participant request has failed " + reason);
		}

		// Update rich messaging history
		// TODO
		
  		// Notify event listeners
		final int N = listeners.beginBroadcast();
        for (int i=0; i < N; i++) {
            try {
            	listeners.getBroadcastItem(i).handleAddParticipantFailed(reason);
            } catch (RemoteException e) {
            	if (logger.isActivated()) {
            		logger.error("Can't notify listener", e);
            	}
            }
        }
        listeners.finishBroadcast();    	
    }  
}
