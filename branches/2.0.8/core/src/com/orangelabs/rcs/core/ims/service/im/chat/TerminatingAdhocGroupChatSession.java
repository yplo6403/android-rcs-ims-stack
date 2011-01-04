/*******************************************************************************
 * Software Name : RCS IMS Stack
 * Version : 2.0
 * 
 * Copyright � 2010 France Telecom S.A.
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
package com.orangelabs.rcs.core.ims.service.im.chat;

import java.util.Vector;

import com.orangelabs.rcs.core.ims.ImsModule;
import com.orangelabs.rcs.core.ims.network.sip.SipManager;
import com.orangelabs.rcs.core.ims.network.sip.SipMessageFactory;
import com.orangelabs.rcs.core.ims.network.sip.SipUtils;
import com.orangelabs.rcs.core.ims.protocol.msrp.MsrpEventListener;
import com.orangelabs.rcs.core.ims.protocol.msrp.MsrpSession;
import com.orangelabs.rcs.core.ims.protocol.sdp.MediaAttribute;
import com.orangelabs.rcs.core.ims.protocol.sdp.MediaDescription;
import com.orangelabs.rcs.core.ims.protocol.sdp.SdpParser;
import com.orangelabs.rcs.core.ims.protocol.sdp.SdpUtils;
import com.orangelabs.rcs.core.ims.protocol.sip.SipRequest;
import com.orangelabs.rcs.core.ims.protocol.sip.SipResponse;
import com.orangelabs.rcs.core.ims.protocol.sip.SipTransactionContext;
import com.orangelabs.rcs.core.ims.service.ImsService;
import com.orangelabs.rcs.core.ims.service.ImsServiceSession;
import com.orangelabs.rcs.core.ims.service.im.InstantMessageError;
import com.orangelabs.rcs.core.ims.service.im.InstantMessageSession;
import com.orangelabs.rcs.utils.logger.Logger;

/**
 * Terminating ad-hoc group chat session
 * 
 * @author jexa7410
 */
public class TerminatingAdhocGroupChatSession extends InstantMessageSession implements MsrpEventListener {
	/**
     * The logger
     */
    private Logger logger = Logger.getLogger(this.getClass().getName());

    /**
     * Constructor
     * 
	 * @param parent IMS service
	 * @param invite Initial INVITE request
	 */
	public TerminatingAdhocGroupChatSession(ImsService parent, SipRequest invite) {
		super(parent, invite.getFrom(), invite.getHeader("Subject"));

		// Create dialog path
		createTerminatingDialogPath(invite);
	}

	/**
	 * Background processing
	 */
	public void run() {
		try {
	    	if (logger.isActivated()) {
	    		logger.info("Initiate a new ad-hoc group chat session as terminating");
	    	}
	    		
	    	// Send a 180 Ringing
			send180Ringing(getDialogPath().getInvite(), getDialogPath().getLocalTag());
			
			// Wait invitation answer
	    	int answer = waitInvitationAnswer();
			if (answer == ImsServiceSession.INVITATION_REJECTED) {
				if (logger.isActivated()) {
					logger.debug("Session has been rejected by user");
				}
				
		    	// Remove the current session
		    	getImsService().removeSession(this);

		    	// Notify listener
		        if (getListener() != null) {
	        		getListener().handleSessionAborted();
		        }
				return;
			} else
			if (answer == ImsServiceSession.INVITATION_NOT_ANSWERED) {
				if (logger.isActivated()) {
					logger.debug("Session has been rejected on timeout");
				}

				// Ringing period timeout
				send603Decline(getDialogPath().getInvite(), getDialogPath().getLocalTag());
				
		    	// Remove the current session
		    	getImsService().removeSession(this);

		    	// Notify listener
		        if (getListener() != null) {
	        		getListener().handleSessionAborted();
		        }
				return;
			}
			
		    // Extract the SDP part
			byte[] remoteSdp = null;
		    String content = getDialogPath().getInvite().getContent();
    		String boundary = "--" + SipUtils.extractBoundary(getDialogPath().getInvite().getContentType());
    		int index  = 0;
    		while(index != -1) {
    			int begin = content.indexOf(boundary, index);
    			int end = content.indexOf(boundary, begin+boundary.length());
    			if ((begin != -1) && (end != -1)) {
    				String part = content.substring(begin+boundary.length(), end);
    				if (part.indexOf("application/sdp") != -1) {
    	    			// SDP
        				String contentPart = part.substring(part.indexOf("v=")); // TODO : to be changed
    					remoteSdp = contentPart.getBytes();
    				} else
    				if (part.indexOf("application/resource-lists+xml") != -1) {
    					// Resource list: nothing done here
    				}
    			}
    			index = end; 
    		}

        	// Parse the remote SDP part
        	SdpParser parser = new SdpParser(remoteSdp);
    		Vector<MediaDescription> media = parser.getMediaDescriptions();
			MediaDescription desc = media.elementAt(0);
			MediaAttribute attr1 = desc.getMediaAttribute("path");
            String remotePath = attr1.getValue();
    		String remoteHost = SdpUtils.extractRemoteHost(parser.sessionDescription.connectionInfo);
    		int remotePort = desc.port;
			
            // Extract the "setup" parameter
            String remoteSetup = "passive";
			MediaAttribute attr4 = desc.getMediaAttribute("setup");
			if (attr4 != null) {
				remoteSetup = attr4.getValue();
			}
            if (logger.isActivated()){
				logger.debug("Remote setup attribute is " + remoteSetup);
			}
            
    		// Set setup mode
            String localSetup = "passive";
            if (remoteSetup.equals("active")) {
            	// Passive mode: the terminal should wait a media connection
    			localSetup = "passive";
            } else 
            if (remoteSetup.equals("passive")) {
            	// Active mode: the terminal should initiate a media connection
    			localSetup = "active";
            } else {
            	// The terminal is active by default
    			localSetup = "active";
            }
            if (logger.isActivated()){
				logger.debug("Local setup attribute is " + localSetup);
			}

			// Build SDP part
	    	String ntpTime = SipUtils.constructNTPtime(System.currentTimeMillis());
	    	String sdp =
	    		"v=0" + SipUtils.CRLF +
	            "o=" + ImsModule.IMS_USER_PROFILE.getUsername() + " "
						+ ntpTime + " " + ntpTime + " IN IP4 "
						+ getDialogPath().getSipStack().getLocalIpAddress() + SipUtils.CRLF +
	            "s=-" + SipUtils.CRLF +
				"c=IN IP4 " + getDialogPath().getSipStack().getLocalIpAddress() + SipUtils.CRLF +
	            "t=0 0" + SipUtils.CRLF +			
	            "m=message " + getMsrpMgr().getLocalMsrpPort() + " TCP/MSRP *" + SipUtils.CRLF +
	            "a=connection:new" + SipUtils.CRLF +
	            "a=setup:" + localSetup + SipUtils.CRLF +
	            "a=accept-types:message/cpim" + SipUtils.CRLF +
	            "a=path:" + getMsrpMgr().getLocalMsrpPath() + SipUtils.CRLF +
	    		"a=sendrecv" + SipUtils.CRLF;

	    	// Set the local SDP part in the dialog path
	        getDialogPath().setLocalSdp(sdp);

	        // Test if the session should be interrupted
			if (isInterrupted()) {
				if (logger.isActivated()) {
					logger.debug("Session has been interrupted: end of processing");
				}
				return;
			}
	        
    		// Create the MSRP server session
            if (localSetup.equals("passive")) {
            	// Passive mode: client wait a connection
            	MsrpSession session = getMsrpMgr().createMsrpServerSession(remotePath, this);
    			session.setFailureReportOption(false);
    			session.setSuccessReportOption(false);
            }
            
            // Send a 200 OK response
        	if (logger.isActivated()) {
        		logger.info("Send 200 OK");
        	}
            SipResponse resp = SipMessageFactory.create200OkInviteResponse(getDialogPath(), sdp);

	        // Set feature tags
	        String[] tags = {SipUtils.FEATURE_OMA_IM};
	        SipUtils.setFeatureTags(getDialogPath().getInvite(), tags);

	        // Send the response
            SipTransactionContext ctx = getImsService().getImsModule().getSipManager().sendSipMessageAndWait(resp);
    		
	        // The signalisation is established
	        getDialogPath().sigEstablished();

            // Wait response
            ctx.waitResponse(SipManager.TIMEOUT);
            
            // Analyze the received response 
            if (ctx.isSipAck()) {
    	        // ACK received
    			if (logger.isActivated()) {
    				logger.info("ACK request received");
    			}

                // The session is established
    	        getDialogPath().sessionEstablished();

        		// Create the MSRP client session
                if (localSetup.equals("active")) {
                	// Active mode: client should connect
                	MsrpSession session = getMsrpMgr().createMsrpClientSession(remoteHost, remotePort, remotePath, this);
        			session.setFailureReportOption(false);
        			session.setSuccessReportOption(false);
                }

                // Notify listener
    	        if (getListener() != null) {
    	        	getListener().handleSessionStarted();
    	        }
            } else {
        		if (logger.isActivated()) {
            		logger.debug("No ACK received for INVITE");
            	}

        		// No response received: timeout
            	handleError(new InstantMessageError(InstantMessageError.SESSION_INITIATION_FAILED));
            }
		} catch(Exception e) {
        	if (logger.isActivated()) {
        		logger.error("Session initiation has failed", e);
        	}

        	// Unexpected error
			handleError(new InstantMessageError(InstantMessageError.UNEXPECTED_EXCEPTION,
					e.getMessage()));
		}		
	}
}
