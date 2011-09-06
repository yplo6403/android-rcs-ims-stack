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
package com.orangelabs.rcs.core.ims.service.toip;

import java.util.Vector;

import com.orangelabs.rcs.core.ims.network.sip.SipManager;
import com.orangelabs.rcs.core.ims.network.sip.SipMessageFactory;
import com.orangelabs.rcs.core.ims.network.sip.SipUtils;
import com.orangelabs.rcs.core.ims.protocol.rtp.MediaRtpReceiver;
import com.orangelabs.rcs.core.ims.protocol.rtp.MediaRtpSender;
import com.orangelabs.rcs.core.ims.protocol.rtp.format.text.RedFormat;
import com.orangelabs.rcs.core.ims.protocol.rtp.format.text.T140Format;
import com.orangelabs.rcs.core.ims.protocol.sdp.MediaDescription;
import com.orangelabs.rcs.core.ims.protocol.sdp.SdpParser;
import com.orangelabs.rcs.core.ims.protocol.sdp.SdpUtils;
import com.orangelabs.rcs.core.ims.protocol.sip.SipException;
import com.orangelabs.rcs.core.ims.protocol.sip.SipRequest;
import com.orangelabs.rcs.core.ims.protocol.sip.SipResponse;
import com.orangelabs.rcs.core.ims.protocol.sip.SipTransactionContext;
import com.orangelabs.rcs.core.ims.service.ImsService;
import com.orangelabs.rcs.core.media.MediaListener;
import com.orangelabs.rcs.core.media.MediaPlayer;
import com.orangelabs.rcs.core.media.MediaRenderer;
import com.orangelabs.rcs.utils.NetworkRessourceManager;
import com.orangelabs.rcs.utils.logger.Logger;

/**
 * Originating ToIP session
 * 
 * @author jexa7410
 */
public class OriginatingToIpSession extends ToIpSession implements MediaListener {
	/**
	 * Text format
	 */
	private T140Format 	textFormat = null;

	/**
	 * Redundant text format
	 */
	private RedFormat  	redFormat  = null;
		
	/**
	 * Clock rate
	 */
	private double clockRate;

	/**
     * The logger
     */
    private Logger logger = Logger.getLogger(this.getClass().getName());

	/**
	 * Constructor
	 * 
	 * @param parent IMS service
	 * @param player Media player
	 * @param renderer Media renderer
	 * @param contact Remote contact
	 */
	public OriginatingToIpSession(ImsService parent, MediaPlayer player, MediaRenderer renderer, String contact) {
		super(parent, contact);
		
		// Get clock rate
		clockRate = parent.getConfig().getDouble("TextClockRate", 1000);

		// Set default codecs
		textFormat = new T140Format();
		redFormat  = new RedFormat();
		
		// Set media player 
		setMediaPlayer(player);
        getMediaPlayer().addListener(this);

		// Set media renderer 
		setMediaRenderer(renderer);
        getMediaRenderer().addListener(this);

        // Create dialog path
		createOriginatingDialogPath();
	}

	/**
	 * Background processing
	 */
	public void run() {
		try {
	    	if (logger.isActivated()) {
	    		logger.info("Initiate a new ToIP session as originating");
	    	}

	    	// Create the RTP manager
			int localRtpPort = NetworkRessourceManager.generateLocalRtpPort();
            setRtpReceiver(new MediaRtpReceiver(localRtpPort));
	    	
	    	// Build SDP part
			int payload_text = textFormat.getPayload();
			int payload_red  = redFormat.getPayload();
	    	String ntpTime = SipUtils.constructNTPtime(System.currentTimeMillis());
			String sdp =
	    		"v=0" + SipUtils.CRLF +
	            "o=- " + ntpTime + " " + ntpTime + " IN IP4 " + getDialogPath().getSipStack().getLocalIpAddress() + SipUtils.CRLF +
	            "s=-" + SipUtils.CRLF +
				"c=IN IP4 " + getDialogPath().getSipStack().getLocalIpAddress() + SipUtils.CRLF +
	            "t=0 0" + SipUtils.CRLF +
	            "m=text " + localRtpPort + " RTP/AVP " + payload_red + " "+ payload_text + SipUtils.CRLF + 
	            "a=rtpmap:" + payload_red + " " + redFormat.getCodec() + "/" + clockRate + SipUtils.CRLF +
	            "a=rtpmap:" + payload_text + " " + textFormat.getCodec() + "/" + clockRate + SipUtils.CRLF +
	            "a=fmtp:" + payload_red + " " + textFormat.getPayload()+ "/" + textFormat.getPayload()+ "/" + textFormat.getPayload() + SipUtils.CRLF +
	            "a=sendrecv" + SipUtils.CRLF;
			
			// Set the local SDP part in the dialog path
	        getDialogPath().setLocalSdp(sdp);

	        // Send an INVITE
	        if (logger.isActivated()) {
	        	logger.info("Send INVITE");
	        }
	        SipRequest invite = SipMessageFactory.createInvite(getDialogPath(), -1, sdp);
	        
	        // Set initial request in the dialog path
	        getDialogPath().setInvite(invite);
	        
	        // Send the INVITE request
	        sendInvite(invite);	        
		} catch(Exception e) {
        	if (logger.isActivated()) {
        		logger.error("Session initiation has failed", e);
        	}

        	// Unexpected error
			handleError(new ToIpError(ToIpError.UNEXPECTED_EXCEPTION, e.getMessage()));
		}
	}
	
	/**
	 * Send INVITE message
	 * 
	 * @param invite SIP INVITE
	 * @throws SipException
	 */
	private void sendInvite(SipRequest invite) throws SipException {
		// Send INVITE request
		SipTransactionContext ctx = getImsService().getImsModule().getSipManager().sendSipMessageAndWait(invite);
		
        // Wait response
        ctx.waitResponse(SipManager.TIMEOUT);
        
        // Analyze the received response 
        if (ctx.isSipResponse()) {
	        // A response has been received
            if (ctx.getStatusCode() == 200) {
            	// 200 OK
            	handle200OK(ctx.getSipResponse());
            } else
            if (ctx.getStatusCode() == 407) {
            	// 407 Proxy Authentication Required
            	handle407Authentication(ctx.getSipResponse());
            } else {
            	// Error response
                if (ctx.getStatusCode() == 603) {
                	handleError(new ToIpError(ToIpError.SESSION_INITIATION_DECLINED,
        					ctx.getReasonPhrase()));
                } else
                if (ctx.getStatusCode() == 487) {
                	handleError(new ToIpError(ToIpError.SESSION_INITIATION_CANCELLED,
        					ctx.getReasonPhrase()));
                } else {
                	handleError(new ToIpError(ToIpError.SESSION_INITIATION_FAILED,
                			ctx.getStatusCode() + " " + ctx.getReasonPhrase()));
                }
            }
        } else {
        	// No response received: timeout
        	handleError(new ToIpError(ToIpError.SESSION_INITIATION_FAILED, "timeout"));
        }
	}	

	/**
	 * Handle 200 0K response 
	 * 
	 * @param resp 200 OK response
	 */
	public void handle200OK(SipResponse resp) {
		try {
	        // 200 OK received
			if (logger.isActivated()) {
				logger.info("200 OK response received");
			}

	        // The signalisation is established
	        getDialogPath().sigEstablished();

	        // Set the remote tag
	        getDialogPath().setRemoteTag(resp.getToTag());
	        
	        // Set the target
	        getDialogPath().setTarget(resp.getContactURI());
	
	        // Set the route path with the Record-Route header
	        Vector<String> newRoute = SipUtils.routeProcessing(resp, true);
			getDialogPath().setRoute(newRoute);
	
	        // Set the remote SDP part
	        getDialogPath().setRemoteSdp(resp.getContent());
	                      		
	        // The session is established
	        getDialogPath().sessionEstablished();

	        // Parse the remote SDP part
        	SdpParser parser = new SdpParser(getDialogPath().getRemoteSdp().getBytes());
            String remoteHost = SdpUtils.extractRemoteHost(parser.sessionDescription.connectionInfo);    		
            MediaDescription text = parser.getMediaDescription("text");
            int remotePort = text.port;
	        
            // Extract the payload type
            String rtpmap = text.getMediaAttribute("rtpmap").getValue();

            // Extract the text encoding
            String encoding = rtpmap.substring(rtpmap.indexOf(text.payload)+text.payload.length()+1);
            String codec = encoding.toLowerCase().trim();
            int index = encoding.indexOf("/");
			if (index != -1) {
				codec = encoding.substring(0, index);
			}
            
            // Prepare the RTP sessions
			if ((codec.equalsIgnoreCase(RedFormat.ENCODING))){				
				setRtpSender(new MediaRtpSender(redFormat));
	    		getRtpReceiver().prepareSession(getMediaRenderer(), redFormat);	
			} else {
				setRtpSender(new MediaRtpSender(textFormat));
	    		getRtpReceiver().prepareSession(getMediaRenderer(), textFormat);
			}
            getRtpSender().prepareSession(getMediaPlayer(), remoteHost, remotePort);
	        
	        // Send ACK message
	        if (logger.isActivated()) {
	        	logger.info("Send ACK");
	        }
	        getImsService().getImsModule().getSipManager().sendSipAck(getDialogPath());
	        
	        // Start the player
	        getMediaPlayer().start();

	        // Start the renderer
	        getMediaRenderer().start();

	        // Start the RTP sessions
	        getRtpReceiver().startSession();
	        getRtpSender().startSession();

	        // Notify listener
	        if (getListener() != null) {
	        	getListener().handleSessionStarted();
	        }
				        
		} catch(Exception e) {
        	if (logger.isActivated()) {
        		logger.error("Session initiation has failed", e);
        	}

        	// Unexpected error
			handleError(new ToIpError(ToIpError.UNEXPECTED_EXCEPTION, e.getMessage()));
        }
	}
	
	/**
	 * Handle 407 Proxy Authentication Required 
	 * 
	 * @param resp 407 response
	 */
	public void handle407Authentication(SipResponse resp) {
		try {
	        if (logger.isActivated()) {
	        	logger.info("407 response received");
	        }
	
	        // Set the remote tag
	        getDialogPath().setRemoteTag(resp.getToTag());
	
	        // Update the authentication agent
	    	getAuthenticationAgent().readProxyAuthenticateHeader(resp);            
	
	        // Increment the Cseq number of the dialog path
	        getDialogPath().incrementCseq();
	
	        // Send a second INVITE with the right token
	        if (logger.isActivated()) {
	        	logger.info("Send second INVITE");
	        }
	        SipRequest invite = SipMessageFactory.createInvite(
	        		getDialogPath(),
					-1,
					getDialogPath().getLocalSdp());
	               
	        // Reset initial request in the dialog path
	        getDialogPath().setInvite(invite);
	        
	        // Set the Proxy-Authorization header
	        getAuthenticationAgent().setProxyAuthorizationHeader(invite);
	
	        // Send the second INVITE request
	        sendInvite(invite);
	        
        } catch(Exception e) {
        	if (logger.isActivated()) {
        		logger.error("Session initiation has failed", e);
        	}

        	// Unexpected error
			handleError(new ToIpError(ToIpError.UNEXPECTED_EXCEPTION, e.getMessage()));
        }
	}

	/**
	 * Handle error 
	 * 
	 * @param error Error
	 */
	public void handleError(ToIpError error) {
        // Error	
    	if (logger.isActivated()) {
    		logger.info("Session error: " + error.getErrorCode() + ", reason=" + error.getMessage());
    	}

        // Close RTP session
    	closeRtpSession();
    	
		// Remove the current session
    	getImsService().removeSession(this);

		// Notify listener
    	if ((!isInterrupted()) && (getListener() != null)) {
        	getListener().handleToIpError(error);
        }
	}
	
	/**
	 * Media player is started
	 */
	public void mediaStarted() {
		if (logger.isActivated()) {
			logger.debug("Media player is started");
		}
	}
	
	/**
	 * Media player is stopped
	 */
	public void mediaStopped() {
		if (logger.isActivated()) {
			logger.debug("Media player is stopped");
		}
	}

	/**
	 * Media player has failed
	 * 
	 * @param error Error
	 */
	public void mediaError(String error) {
		if (logger.isActivated()) {
			logger.error("Media player has failed: " + error);
		}
		
        // Close RTP session
    	closeRtpSession();
    	
    	// Terminate session
    	terminateSession();
    	
		// Remove the current session
    	getImsService().removeSession(this);

		// Notify listener
    	if ((!isInterrupted()) && (getListener() != null)) {
        	getListener().handleToIpError(new ToIpError(ToIpError.MEDIA_FAILED, error));
        }
	}

	/**
	 * Close the RTP session
	 */
	private void closeRtpSession() {
    	if (getMediaPlayer() != null) {
    		getMediaPlayer().stop();
    	}
    	if (getMediaRenderer() != null) {
    		getMediaRenderer().stop();
    	}
    	
    	if (getRtpSender() != null) {
    		getRtpSender().stopSession();
    	}
    	if (getRtpReceiver() != null) {
    		getRtpReceiver().stopSession();
    	}
	}

	/**
	 * Close media session
	 */
	public void closeMediaSession() {
        // Close RTP session
        closeRtpSession();
	}
}