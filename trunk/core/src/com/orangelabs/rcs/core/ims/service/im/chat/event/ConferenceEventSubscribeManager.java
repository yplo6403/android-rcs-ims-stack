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

package com.orangelabs.rcs.core.ims.service.im.chat.event;

import java.io.ByteArrayInputStream;
import java.util.Vector;

import javax.sip.header.ExpiresHeader;
import javax.sip.header.SubscriptionStateHeader;

import org.xml.sax.InputSource;

import com.orangelabs.rcs.core.ims.ImsModule;
import com.orangelabs.rcs.core.ims.network.sip.SipManager;
import com.orangelabs.rcs.core.ims.network.sip.SipMessageFactory;
import com.orangelabs.rcs.core.ims.network.sip.SipUtils;
import com.orangelabs.rcs.core.ims.protocol.sip.SipDialogPath;
import com.orangelabs.rcs.core.ims.protocol.sip.SipRequest;
import com.orangelabs.rcs.core.ims.protocol.sip.SipResponse;
import com.orangelabs.rcs.core.ims.protocol.sip.SipTransactionContext;
import com.orangelabs.rcs.core.ims.service.SessionAuthenticationAgent;
import com.orangelabs.rcs.core.ims.service.im.InstantMessagingService;
import com.orangelabs.rcs.core.ims.service.im.chat.ChatError;
import com.orangelabs.rcs.core.ims.service.im.chat.ChatSession;
import com.orangelabs.rcs.platform.registry.RegistryFactory;
import com.orangelabs.rcs.provider.settings.RcsSettings;
import com.orangelabs.rcs.utils.PeriodicRefresher;
import com.orangelabs.rcs.utils.PhoneUtils;
import com.orangelabs.rcs.utils.logger.Logger;

/**
 * Conference event subscribe manager
 * 
 * @author jexa7410
 */
public class ConferenceEventSubscribeManager extends PeriodicRefresher {
	/**
	 * Last min expire period (in seconds)
	 */
	private static final String REGISTRY_MIN_EXPIRE_PERIOD = "MinSubscribeConferenceEventExpirePeriod";

	/**
     * IMS module
     */
    private ImsModule imsModule;
    
    /**
     * IM session
     */
    private ChatSession session;
    
    /**
     * Dialog path
     */
    private SipDialogPath dialogPath = null;
    
    /**
     * Expire period
     */
    private int expirePeriod;
    
    /**
     * Subscription flag
     */
    private boolean subscribed = false;
    
	/**
	 * Authentication agent
	 */
	private SessionAuthenticationAgent authenticationAgent = new SessionAuthenticationAgent();

	/**
     * The logger
     */
    private Logger logger = Logger.getLogger(this.getClass().getName());

    /**
     * Constructor
     * 
     * @param session IM session
     */
    public ConferenceEventSubscribeManager(ChatSession session) {
    	this.session  = session;
    	this.imsModule = session.getImsService().getImsModule();
    	
    	int defaultExpirePeriod = RcsSettings.getInstance().getSubscribeExpirePeriod();
    	int minExpireValue = RegistryFactory.getFactory().readInteger(REGISTRY_MIN_EXPIRE_PERIOD, -1);
    	if ((minExpireValue != -1) && (defaultExpirePeriod < minExpireValue)) {
        	this.expirePeriod = minExpireValue;
    	} else {
    		this.expirePeriod = defaultExpirePeriod;
    	}
    }

    /**
     * Is subscribed
     * 
     * @return Boolean
     */
	public boolean isSubscribed() {
		return subscribed;
	}

    /**
     * Returns the presentity
     * 
     * @return Presentity
     */
    public String getPresentity() {
    	return session.getImSessionIdentity();
    }
	
    /**
     * Receive a notification
     * 
     * @param notify Received notify
     */
    public void receiveNotification(SipRequest notify) {
    	if (logger.isActivated()) {
			logger.debug("New conference event notification received");
		}    	
    	
	    // Parse XML part
	    byte[] content = notify.getContentBytes();
		if (content != null) {
			try {
		    	InputSource pidfInput = new InputSource(new ByteArrayInputStream(content));
		    	ConferenceInfoParser confParser = new ConferenceInfoParser(pidfInput);
		    	ConferenceInfoDocument conference = confParser.getConferenceInfo();
		    	if (conference != null) {
			    	Vector<User> users = conference.getUsers();
			    	for(int i=0; i < users.size(); i++) {
			    		User user = (User)users.elementAt(i);
			    		String entity = PhoneUtils.extractNumberFromUri(user.getEntity());
				    	if (logger.isActivated()) {
				    		logger.debug("Conference info notification for " + entity);
				    	}
				    	
				    	String me = ImsModule.IMS_USER_PROFILE.getPublicUri();
				    	if (user.isMe() || PhoneUtils.compareNumbers(entity, me)) {
			    			// By-pass me
			    			continue;
			    		}
			    		
			    		// Update the participants list
			    		if (user.getState().equals(User.STATE_CONNECTED)) {
			    			// A participant has joined the session
			    			session.getParticipants().addParticipant(entity);
			    		} else
			    		if (user.getState().equals(User.STATE_DISCONNECTED)) {
			    			// A participant has quit the session
			    			session.getParticipants().removeParticipant(entity);
			    		}
			    		
			    		// Notify session listener
			    		session.getListener().handleConferenceEvent(entity, user.getState());
			    	}
		    	}
	    	} catch(Exception e) {
	    		if (logger.isActivated()) {
	    			logger.error("Can't parse XML notification", e);
	    		}
	    	}    	
		}
		
		// Check subscription state
    	SubscriptionStateHeader stateHeader = (SubscriptionStateHeader)notify.getHeader(SubscriptionStateHeader.NAME);
		if ((stateHeader != null) && stateHeader.getState().equalsIgnoreCase("terminated")) {
			if (logger.isActivated()) {
				logger.info("Conference event subscription has been terminated by server");
			}
			terminatedByServer();
		}    	
    }

    /**
     * Check if the received notification if for this subscriber
     * 
     * @param SipRequest notify
     * @return Boolean
     */
    public boolean isNotifyForThisSubscriber(SipRequest notify) {
    	boolean result = false;
    	if ((dialogPath != null) && notify.getCallId().equals(dialogPath.getCallId())) {
    		result = true;
    	}
    	return result;
    }
    
    /**
     * Subscription has been terminated by server
     */
    public void terminatedByServer() {
    	if (!subscribed) {
    		// Already unsubscribed
        	return;
    	}
    	
    	if (logger.isActivated()) {
    		logger.info("Subscription has been terminated by server");
    	}
    	
    	// Stop periodic subscription
    	stopTimer();

    	// Reset dialog path attributes
        resetDialogPath();

        // Force subscription flag to false
        subscribed = false;
    }
    
    /**
     * Terminate manager
     */
    public void terminate() {
    	if (logger.isActivated()) {
    		logger.info("Terminate the subscribe manager");
    	}
    	
    	// Stop periodic subscription
    	stopTimer();

    	// Unsubscribe before to quit
		if ((imsModule.getCurrentNetworkInterface() != null) &&
				imsModule.getCurrentNetworkInterface().isRegistered()) {
				unSubscribe();
    	}
		
        if (logger.isActivated()) {
        	logger.info("Subscribe manager is terminated");
        }
    }

    /**
     * Create a SUBSCRIBE request
     * 
	 * @param dialog SIP dialog path
	 * @param expirePeriod Expiration period
	 * @param accessInfo Access info
	 * @return SIP request
	 * @throws Exception
     */
    private SipRequest createSubscribe(SipDialogPath dialog, int expirePeriod, String accessInfo) throws Exception {
    	// Create SUBSCRIBE message
    	SipRequest subscribe = SipMessageFactory.createSubscribe(dialog, expirePeriod, accessInfo);

        // Set feature tags
        SipUtils.setFeatureTags(subscribe, InstantMessagingService.CHAT_FEATURE_TAGS);    	
    	
    	// Set the Event header
    	subscribe.addHeader("Event", "conference");

    	// Set the Accept header
    	subscribe.addHeader("Accept", "application/conference-info+xml");

    	return subscribe;
    }
    	

    /**
     * Subscription refresh processing
     */
    public void periodicProcessing() {
        // Make a subscribe
    	if (logger.isActivated()) {
    		logger.info("Execute re-subscribe");
    	}

    	// Send SUBSCRIBE request
    	subscribe();
    }
    
    /**
     * Subscribe
     * 
     * @return Boolean
     */
    public synchronized boolean subscribe() {
    	if (logger.isActivated()) {
    		logger.info("Subscribe to " + getPresentity());
    	}

    	try {
            // Create a dialog path if necessary
            if (dialogPath == null) {
	            // Set Call-Id
	        	String callId = imsModule.getSipManager().getSipStack().generateCallId();
	
	        	// Set target
	        	String target = getPresentity();
	
	            // Set local party
	        	String localParty = ImsModule.IMS_USER_PROFILE.getPublicUri();
	
	            // Set remote party
	        	String remoteParty = getPresentity();
	
	        	// Set the route path
	        	Vector<String> route = imsModule.getSipManager().getSipStack().getServiceRoutePath();
	
	        	// Create a dialog path
	            dialogPath = new SipDialogPath(
	            		imsModule.getSipManager().getSipStack(),
	            		callId,
	            		1,
	            		target,
	            		localParty,
	            		remoteParty,
	            		route);
            } else {
    	    	// Increment the Cseq number of the dialog path
    	        dialogPath.incrementCseq();
            }
            
            // Create a SUBSCRIBE request
	        SipRequest subscribe = createSubscribe(dialogPath,
	        		expirePeriod,
	        		imsModule.getCurrentNetworkInterface().getAccessInfo());
	        
            // Send SUBSCRIBE request
	        sendSubscribe(subscribe);
	        
        } catch (Exception e) {
        	if (logger.isActivated()) {
        		logger.error("Subscribe has failed", e);
        	}
        	handleError(new ChatError(ChatError.UNEXPECTED_EXCEPTION, e.getMessage()));
        }        
        return subscribed;
    }

	/**
     * Unsubscribe
     */
    public synchronized void unSubscribe() {
    	if (!subscribed) {
    		// Already unsubscribed
        	return;
    	}

    	if (logger.isActivated()) {
    		logger.info("Unsubscribe to " + getPresentity());
    	}

    	try {
	    	// Stop periodic subscription
	        stopTimer();

        	// Increment the Cseq number of the dialog path

	        dialogPath.incrementCseq();

            // Create a SUBSCRIBE with expire 0
            SipRequest subscribe = createSubscribe(dialogPath, 0,
            		imsModule.getCurrentNetworkInterface().getAccessInfo());

            // Send SUBSCRIBE request
	        sendSubscribe(subscribe);
            
        } catch (Exception e) {
            if (logger.isActivated()) {
            	logger.error("UnSubscribe has failed", e);
            }
        }

        // Force subscription flag to false
        subscribed = false;

        // Reset dialog path attributes
        resetDialogPath();
    }

    /**
     * Reset the dialog path
     */
    private void resetDialogPath() {
        dialogPath = null;
    }

	/**
     * Retrieve the expire period
     * 
     * @param resp SIP response
     */
	private void retrieveExpirePeriod(SipResponse response) {
        // Extract expire value from Expires header
        ExpiresHeader expiresHeader = (ExpiresHeader)response.getHeader(ExpiresHeader.NAME);
    	if (expiresHeader != null) {
    		int expires = expiresHeader.getExpires();
		    if (expires != -1) {
	    		expirePeriod = expires;
	    	}
        }
	}
	
	/**
	 * Send SUBSCRIBE message
	 * 
	 * @param subscribe SIP SUBSCRIBE
	 * @throws Exception
	 */
	private void sendSubscribe(SipRequest subscribe) throws Exception {
        if (logger.isActivated()) {
        	logger.info("Send SUBSCRIBE, expire=" + subscribe.getExpires());
        }

        if (subscribed) {
	        // Set the Authorization header
            authenticationAgent.setProxyAuthorizationHeader(subscribe);
        }
        
        // Send SUBSCRIBE request
        SipTransactionContext ctx = imsModule.getSipManager().sendSipMessageAndWait(subscribe);

        // Wait response
        if (logger.isActivated()) {
        	logger.info("Wait response");
        }
        ctx.waitResponse(SipManager.TIMEOUT);
        
        // Analyze the received response 
        if (ctx.isSipResponse()) {
        	// A response has been received
            if (ctx.getStatusCode() == 200) {
        		if (subscribe.getExpires() != 0) {
        			handle200OK(ctx);
        		} else {
        			handle200OkUnsubscribe(ctx);
        		}
            } else
            if (ctx.getStatusCode() == 202) {
            	// 202 Accepted
    			handle200OK(ctx);
            } else
            if (ctx.getStatusCode() == 407) {
            	// 407 Proxy Authentication Required
            	handle407Authentication(ctx);
            } else
            if (ctx.getStatusCode() == 423) {
            	// 423 Interval Too Brief
            	handle423IntervalTooBrief(ctx);
            } else {
            	// Other error response
    			handleError(new ChatError(ChatError.SUBSCRIBE_CONFERENCE_FAILED,
    					ctx.getStatusCode() + " " + ctx.getReasonPhrase()));    					
            }
        } else {
    		if (logger.isActivated()) {
        		logger.debug("No response received for SUBSCRIBE");
        	}

    		// No response received: timeout
        	handleError(new ChatError(ChatError.SUBSCRIBE_CONFERENCE_FAILED));
        }
	}    

	/**
	 * Handle 200 0K response 
	 * 
	 * @param ctx SIP transaction context
	 */
	private void handle200OK(SipTransactionContext ctx) {
        // 200 OK response received
        if (logger.isActivated()) {
            logger.info("200 OK response received");
        }
        subscribed = true;
        
    	SipResponse resp = ctx.getSipResponse();

    	// Set the remote tag
    	dialogPath.setRemoteTag(resp.getToTag());
    	
    	// Set the target
    	dialogPath.setTarget(resp.getContactURI());

        // Set the Proxy-Authorization header
    	authenticationAgent.readProxyAuthenticateHeader(resp);

    	// Retrieve the expire value in the response
		retrieveExpirePeriod(resp);
        
        // Start the periodic subscribe
        startTimer(expirePeriod, 0.5);
	}	
	
	/**
	 * Handle 200 0K response of UNSUBSCRIBE
	 * 
	 * @param ctx SIP transaction context
	 */
	private void handle200OkUnsubscribe(SipTransactionContext ctx) {
        // 200 OK response received
        if (logger.isActivated()) {
            logger.info("200 OK response received");
        }
	}
		
	/**
	 * Handle 407 response 
	 * 
	 * @param ctx SIP transaction context
	 * @throws Exception
	 */
	private void handle407Authentication(SipTransactionContext ctx) throws Exception {
        // 407 response received
    	if (logger.isActivated()) {
    		logger.info("407 response received");
    	}

    	SipResponse resp = ctx.getSipResponse();

    	// Set the Proxy-Authorization header
    	authenticationAgent.readProxyAuthenticateHeader(resp);

        // Increment the Cseq number of the dialog path
        dialogPath.incrementCseq();

        // Create a second SUBSCRIBE request with the right token
        if (logger.isActivated()) {
        	logger.info("Send second SUBSCRIBE");
        }
    	SipRequest subscribe = createSubscribe(dialogPath,
    			ctx.getTransaction().getRequest().getExpires().getExpires(),
    			imsModule.getCurrentNetworkInterface().getAccessInfo());
    	
        // Set the Authorization header
        authenticationAgent.setProxyAuthorizationHeader(subscribe);
    	
        // Send SUBSCRIBE request
    	sendSubscribe(subscribe);
	}	
	
	/**
	 * Handle 423 response 
	 * 
	 * @param ctx SIP transaction context
	 */
	private void handle423IntervalTooBrief(SipTransactionContext ctx) throws Exception {
		// 423 response received
    	if (logger.isActivated()) {
    		logger.info("423 interval too brief response received");
    	}

    	SipResponse resp = ctx.getSipResponse();

    	// Increment the Cseq number of the dialog path
        dialogPath.incrementCseq();

        // Extract the Min-Expire value
        int minExpire = SipUtils.getMinExpiresPeriod(resp);
        if (minExpire == -1) {
            if (logger.isActivated()) {
            	logger.error("Can't read the Min-Expires value");
            }
        	handleError(new ChatError(ChatError.SUBSCRIBE_CONFERENCE_FAILED, "No Min-Epires value found"));
        	return;
        }
        
        // Save the min expire value in the terminal registry
        RegistryFactory.getFactory().writeInteger(REGISTRY_MIN_EXPIRE_PERIOD, minExpire);

        // Set the default expire value
    	expirePeriod = minExpire;
    	
        // Create a new SUBSCRIBE request with the right expire period
        SipRequest subscribe = createSubscribe(dialogPath,
        		expirePeriod,
        		imsModule.getCurrentNetworkInterface().getAccessInfo());

		// Set the Authorization header
		authenticationAgent.setProxyAuthorizationHeader(subscribe);

        // Send SUBSCRIBE request
        sendSubscribe(subscribe);
	}	
	
	/**
	 * Handle error response 
	 * 
	 * @param error Error
	 */
	private void handleError(ChatError error) {
        // Error
    	if (logger.isActivated()) {
    		logger.info("Subscribe has failed: " + error.getErrorCode() + ", reason=" + error.getMessage());
    	}
        subscribed = false;
        
        // Subscribe has failed, stop the periodic subscribe
		stopTimer();
        
        // Reset dialog path attributes
        resetDialogPath();
	}
}
