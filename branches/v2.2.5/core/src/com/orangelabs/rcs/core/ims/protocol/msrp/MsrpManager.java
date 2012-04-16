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

package com.orangelabs.rcs.core.ims.protocol.msrp;

import java.io.IOException;
import java.io.InputStream;

import com.orangelabs.rcs.utils.logger.Logger;

/**
 * MSRP manager
 * 
 * @author jexa7410
 */
public class MsrpManager {
	/**
	 * MRSP timeout (in seconds)
	 */
	public static int TIMEOUT = 10;

	/**
     * Local MSRP address
     */
    private String localMsrpAddress;

    /**
     * Local MSRP port
     */
    private int localMsrpPort;

    /**
     * MSRP session
     */
    private MsrpSession msrpSession = null;
    
    /**
     * Session Id
     */
    private long sessionId;
   
    /**
     * The logger
     */
    private Logger logger = Logger.getLogger(this.getClass().getName());

    /**
     * Constructor
     * 
     * @param localIpAddress Local MSRP address
     * @param localMsrpPort Local MSRP port
     */    
    public MsrpManager(String localMsrpAddress, int localMsrpPort) {
    	this.localMsrpAddress = localMsrpAddress;
    	this.localMsrpPort = localMsrpPort;
    	this.sessionId = System.currentTimeMillis();
    }

	/**
	 * Returns the local MSRP port
	 * 
	 * @return Port number
	 */
	public int getLocalMsrpPort() {
		return localMsrpPort;
	}
    
    /**
     * Get the local MSRP path
     * 
     * @return MSRP path
     */
    public String getLocalMsrpPath() {
    	return "msrp://" + localMsrpAddress + ":" + localMsrpPort + "/" + sessionId + ";tcp";
    }
    
	/**
	 * Return the MSRP session
	 * 
	 * @return MSRP session
	 */
	public MsrpSession getMsrpSession() {
		return msrpSession;
	}
	
	/**
	 * Open the MSRP session
	 * 
	 * @throws IOException
	 */
	public void openMsrpSession() throws IOException {
		if ((msrpSession == null) || (msrpSession.getConnection() == null)) {
			throw new IOException("Session not yet created");
		}
		
		msrpSession.getConnection().open();
	}
	
	/**
	 * Open the connection with SO_TIMEOUT on the socket
	 * 
	 * @param soTimeout SO_TIMEOUT value (in seconds)
	 * @throws IOException
	 */
	public void openMsrpSession(int soTimeout) throws IOException {
		if ((msrpSession == null) || (msrpSession.getConnection() == null)) {
			throw new IOException("Session not yet created");
		}

		msrpSession.getConnection().open(soTimeout);
	}
	
	/**
	 * Create a MSRP client session
	 * 
	 * @param remoteHost Remote host
	 * @param remotePort Remote port
     * @param remoteMsrpPath Remote MSRP path
     * @param listener Event listener
     * @return Created session
	 * @throws MsrpException
	 */
	public MsrpSession createMsrpClientSession(String remoteHost, int remotePort, String remoteMsrpPath, MsrpEventListener listener) throws MsrpException {
        try {
	        if (logger.isActivated()) {
				logger.info("Create MSRP client end point at " + remoteHost + ":" + remotePort);
			}
	
			// Create a new MSRP session
			msrpSession = new MsrpSession();
			msrpSession.setFrom(getLocalMsrpPath());
			msrpSession.setTo(remoteMsrpPath);

			// Create a MSRP client connection
			final MsrpConnection connection = new MsrpClientConnection(msrpSession, remoteHost, remotePort);

			// Associate the connection to the session
			msrpSession.setConnection(connection);
			
			// Add event listener
			msrpSession.addMsrpEventListener(listener);
			
        	// Return the created session
        	return msrpSession;
		} catch(Exception e) {
			if (logger.isActivated()) {
				logger.error("Can't create the MSRP client session", e);
			}
			throw new MsrpException("Create MSRP client session has failed");
		}
	}
	
	/**
	 * Create a MSRP server session
	 *
     * @param remoteMsrpPath Remote MSRP path
     * @param listener Event listener
     * @return Created session
	 * @throws MsrpException
	 */
	public MsrpSession createMsrpServerSession(String remoteMsrpPath, MsrpEventListener listener) throws MsrpException {
		if (logger.isActivated()) {
			logger.info("Create MSRP server end point at " + localMsrpPort);
		}

		// Create a MSRP session
		msrpSession = new MsrpSession();
		msrpSession.setFrom(getLocalMsrpPath());
		msrpSession.setTo(remoteMsrpPath);

		// Create a MSRP server connection
		final MsrpConnection connection = new MsrpServerConnection(msrpSession, localMsrpPort);

		// Associate the connection to the session
		msrpSession.setConnection(connection);
		
		// Add event listener
		msrpSession.addMsrpEventListener(listener);

    	// Return the created session
    	return msrpSession;
	}

	/**
     * Send data chunks
     * 
     * @param inputStream Input stream
     * @param contentType Content type
     * @param contentSize Content size
     * @throws MsrpException
     */
    public void sendChunks(InputStream inputStream, String contentType,	long contentSize) throws MsrpException {
        if (msrpSession == null) {
        	throw new MsrpException("MSRP session is null");
        }

        msrpSession.sendChunks(inputStream, contentType, contentSize);
    }

	/**
     * Send an empty chunk
     * 
     * @throws MsrpException
     */
    public void sendEmptyChunk() throws MsrpException {
        if (msrpSession == null) {
        	throw new MsrpException("MSRP session is null");
        }

		msrpSession.sendEmptyChunk();
    }
    
    /**
     * Close the MSRP session
     */
    public synchronized void closeSession() {
        if (msrpSession != null) {
        	if (logger.isActivated()) {
        		logger.info("Close the MSRP session");
        	}
        	try {
	        	msrpSession.close();
        	} catch(Exception e) {}
            msrpSession = null;
    	}
    }
}