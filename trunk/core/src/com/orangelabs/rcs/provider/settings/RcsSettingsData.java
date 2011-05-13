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

package com.orangelabs.rcs.provider.settings;

import android.net.ConnectivityManager;
import android.net.Uri;

/**
 * RCS settings data constants
 * 
 * @author jexa7410
 */
public class RcsSettingsData {
	/**
	 * Database URI
	 */
	static final Uri CONTENT_URI = Uri.parse("content://com.orangelabs.rcs.settings/settings"); 
	
	/**
	 * Column name
	 */
	static final String KEY_ID = "_id";
	
	/**
	 * Column name
	 */
	static final String KEY_KEY = "key";
	
	/**
	 * Column name
	 */
	static final String KEY_VALUE = "value";
	
	// ---------------------------------------------------------------------------
	// Constants
	// ---------------------------------------------------------------------------
	
	/**
	 * Boolean value "true"
	 */
	public static final String TRUE = Boolean.toString(true);

	/**
	 * Boolean value "false"
	 */
	public static final String FALSE = Boolean.toString(false);	
	
	/**
	 * GIBA authentication
	 */
	public static final String GIBA_AUTHENT = "GIBA";
	
	/**
	 * HTTP Digest authentication
	 */
	public static final String DIGEST_AUTHENT = "DIGEST";	

	/**
	 * Any access
	 */
	public static final int ANY_ACCESS = -1;	

	/**
	 * Mobile access
	 */
	public static final int MOBILE_ACCESS = ConnectivityManager.TYPE_MOBILE;	

	/**
	 * Wi-Fi access
	 */
	public static final int WIFI_ACCESS = ConnectivityManager.TYPE_WIFI;	

	// ---------------------------------------------------------------------------
	// Parameters which can be modified by the end user. These parameters may be
	// displayed at UI level (e.g. settings application).
	// ---------------------------------------------------------------------------
	
	/**
	 * Service activation parameter which indicates if the RCS service may be started or not 
	 */
	public static final String SERVICE_ACTIVATED = "ServiceActivated";
	
	/**
	 * Roaming authorization parameter which indicates if the RCS service may be used or not in roaming 
	 */
	public static final String ROAMING_AUTHORIZED = "RoamingAuthorized";
	
	/**
	 * Ringtone which is played when a social presence sharing invitation is received 
	 */
	public static final String PRESENCE_INVITATION_RINGTONE = "PresenceInvitationRingtone";

	/**
	 * Vibrate or not when a social presence sharing invitation is received 
	 */
	public static final String PRESENCE_INVITATION_VIBRATE = "PresenceInvitationVibrate";

	/**
	 * Ringtone which is played when a content sharing invitation is received 
	 */
	public static final String CSH_INVITATION_RINGTONE = "CShInvitationRingtone";

	/**
	 * Vibrate or not when a content sharing invitation is received 
	 */
	public static final String CSH_INVITATION_VIBRATE = "CShInvitationVibrate";

	/**
	 * Make a beep or not when content sharing is available during a call 
	 */
	public static final String CSH_AVAILABLE_BEEP = "CShAvailableBeep";

	/**
	 * Video format for video share 
	 */
	public static final String CSH_VIDEO_FORMAT = "CShVideoFormat";
	
	/**
	 * Video size for video share 
	 */
	public static final String CSH_VIDEO_SIZE = "CShVideoSize";
	
	/**
	 * Ringtone which is played when a file transfer invitation is received 
	 */
	public static final String FILETRANSFER_INVITATION_RINGTONE = "FileTransferInvitationRingtone";

	/**
	 * Vibrate or not when a file transfer invitation is received 
	 */
	public static final String FILETRANSFER_INVITATION_VIBRATE = "FileTransferInvitationVibrate";
	
	/**
	 * Ringtone which is played when a chat invitation is received 
	 */
	public static final String CHAT_INVITATION_RINGTONE = "ChatInvitationRingtone";

	/**
	 * Vibrate or not when a chat invitation is received 
	 */
	public static final String CHAT_INVITATION_VIBRATE = "ChatInvitationVibrate";

	/**
	 * Auto-accept mode for chat invitation 
	 */
	public static final String CHAT_INVITATION_AUTO_ACCEPT = "ChatInvitationAutoAccept";
	
	/**
	 * Predefined freetext 
	 */
	public static final String FREETEXT1 = "Freetext1";
	
	/**
	 * Predefined freetext 
	 */
	public static final String FREETEXT2 = "Freetext2";
	
	/**
	 * Predefined freetext 
	 */
	public static final String FREETEXT3 = "Freetext3";

	/**
	 * Predefined freetext 
	 */
	public static final String FREETEXT4 = "Freetext4";
	
	// ---------------------------------------------------------------------------
	// Parameters which CAN'T be modified by the end user. These parameters are
	// mainly used by UI.
	// ---------------------------------------------------------------------------

	/**
	 * Max photo-icon size
	 */
	public static final String MAX_PHOTO_ICON_SIZE = "MaxPhotoIconSize";

	/**
	 * Max length of the freetext
	 */
	public static final String MAX_FREETXT_LENGTH = "MaxFreetextLength";

	/**
	 * Max number of participants in a group chat
	 */
	public static final String MAX_CHAT_PARTICIPANTS = "MaxChatParticipants";
	
	/**
	 * Max length of a chat message
	 */
	public static final String MAX_CHAT_MSG_LENGTH = "MaxChatMessageLength";

	/**
	 * Idle duration of a chat session
	 */
	public static final String CHAT_IDLE_DURATION = "ChatIdleDuration";

	/**
	 * Max size of a file transfer
	 */
	public static final String MAX_FILE_TRANSFER_SIZE = "MaxFileTransferSize";

	/**
	 * Warning threshold for file transfer size
	 */
	public static final String WARN_FILE_TRANSFER_SIZE = "WarnFileTransferSize";

	/**
	 * Max size of an image share
	 */
	public static final String MAX_IMAGE_SHARE_SIZE = "MaxImageShareSize";

	/**
	 * Max duration of a video share
	 */
	public static final String MAX_VIDEO_SHARE_DURATION = "MaxVideoShareDuration";

	/**
	 * Max number of simultaneous chat sessions
	 */
	public static final String MAX_CHAT_SESSIONS = "MaxChatSessions";

	/**
	 * Max number of simultaneous file transfer sessions
	 */
	public static final String MAX_FILE_TRANSFER_SESSIONS = "MaxFileTransferSessions";

	/**
	 * Activate or not SMS fallback service
	 */
	public static final String SMS_FALLBACK_SERVICE = "SmsFallbackService";
	
	/**
	 * Display a warning if Store & Forward service is activated
	 */
	public static final String WARN_SF_SERVICE = "StoreForwardServiceWarning";
	
	// ---------------------------------------------------------------------------
	// Parameters of the end user profile
	// ---------------------------------------------------------------------------

	/**
	 * IMS username or username part of the IMPU (for HTTP Digest only)
	 */
	public static final String USERPROFILE_IMS_USERNAME = "ImsUsername";

	/**
	 * IMS display name 
	 */
	public static final String USERPROFILE_IMS_DISPLAY_NAME = "ImsDisplayName";

	/**
	 * IMS private URI or IMPI (for HTTP Digest only) 
	 */
	public static final String USERPROFILE_IMS_PRIVATE_ID = "ImsPrivateId";
	
	/**
	 * IMS password (for HTTP Digest only) 
	 */
	public static final String USERPROFILE_IMS_PASSWORD = "ImsPassword";
	
	/**
	 * IMS home domain (for HTTP Digest only)
	 */
	public static final String USERPROFILE_IMS_HOME_DOMAIN = "ImsHomeDomain";
	
	/**
	 * P-CSCF or outbound proxy address & port for mobile access
	 */
	public static final String USERPROFILE_IMS_PROXY_MOBILE = "ImsOutboundProxyAddrForMobile";
	
	/**
	 * P-CSCF or outbound proxy address & port for Wi-Fi access
	 */
	public static final String USERPROFILE_IMS_PROXY_WIFI = "ImsOutboundProxyAddrForWifi";

	/**
	 * XDM server address & port
	 */
	public static final String USERPROFILE_XDM_SERVER = "XdmServerAddr";

	/**
	 * XDM server login (for HTTP Digest only)
	 */
	public static final String USERPROFILE_XDM_LOGIN= "XdmServerLogin";

	/**
	 * XDM server password (for HTTP Digest only)
	 */
	public static final String USERPROFILE_XDM_PASSWORD = "XdmServerPassword";

	/**
	 * IM conference URI for group chat session
	 */
	public static final String USERPROFILE_IM_CONF_URI = "ImConferenceUri";

	/**
	 * Country code 
	 */
	public static final String USERPROFILE_COUNTRY_CODE = "CountryCode";

	// ---------------------------------------------------------------------------
	// Parameters which CAN'T be modified by the end user. These parameters are
	// used by the stack only.
	// ---------------------------------------------------------------------------
	
	/**
	 * Polling period used before each IMS connection attempt
	 */
	public static final String IMS_CONNECTION_POLLING_PERIOD = "ImsConnectionPollingPeriod";

	/**
	 * Polling period used before each IMS service check (e.g. test subscription state for presence service)
	 */
	public static final String IMS_SERVICE_POLLING_PERIOD = "ImsServicePollingPeriod";
	
	/**
	 * Default SIP port 
	 */
	public static final String SIP_DEFAULT_PORT = "SipListeningPort";
	
	/**
	 * Default SIP protocol 
	 */
	public static final String SIP_DEFAULT_PROTOCOL = "SipDefaultProtocol";

	/**
	 * SIP transaction timeout used to wait a SIP response
	 */
	public static final String SIP_TRANSACTION_TIMEOUT = "SipTransactionTimeout";
	
	/**
	 * Default TCP port for MSRP session 
	 */
	public static final String MSRP_DEFAULT_PORT = "DefaultMsrpPort";
	
	/**
	 * Default UDP port for RTP session 
	 */
	public static final String RTP_DEFAULT_PORT = "DefaultRtpPort";

	/**
	 * MSRP transaction timeout used to wait MSRP response
	 */
	public static final String MSRP_TRANSACTION_TIMEOUT = "MsrpTransactionTimeout";	
	
	/**
	 * Registration expire period 
	 */
	public static final String REGISTER_EXPIRE_PERIOD = "RegisterExpirePeriod";
	
	/**
	 * Publish expire period
	 */
	public static final String PUBLISH_EXPIRE_PERIOD = "PublishExpirePeriod";
	
	/**
	 * Revoke timeout  
	 */
	public static final String REVOKE_TIMEOUT = "RevokeTimeout";
	
	/**
	 * IMS authentication procedure for mobile access
	 */
	public static final String IMS_AUTHENT_PROCEDURE_MOBILE = "ImsAuhtenticationProcedureForMobile";
	
	/**
	 * IMS authentication procedure for Wi-Fi access
	 */
	public static final String IMS_AUTHENT_PROCEDURE_WIFI = "ImsAuhtenticationProcedureForWifi";

	/**
	 * Activate or not Tel-URI format
	 */
	public static final String TEL_URI_FORMAT = "TelUriFormat";
	
	/**
	 * Ringing session period. At the end of the period the session is cancelled  
	 */
	public static final String RINGING_SESSION_PERIOD = "RingingPeriod";
	
	/**
	 * Subscribe expiration timeout
	 */
	public static final String SUBSCRIBE_EXPIRE_PERIOD = "SubscribeExpirePeriod";
	
	/**
	 * "Is-composing" timeout for chat service
	 */
	public static final String IS_COMPOSING_TIMEOUT = "IsComposingTimeout";
	
	/**
	 * SIP session refresh expire period
	 */
	public static final String SESSION_REFRESH_EXPIRE_PERIOD = "SessionRefreshExpirePeriod";
	
	/**
	 * Activate or not permanent state mode
	 */
	public static final String PERMANENT_STATE_MODE = "PermanentState";

	/**
	 * Activate or not the logger
	 */
	public static final String TRACE_ACTIVATION = "TraceActivation";

	/**
	 * Logger trace level
	 */
	public static final String TRACE_LEVEL = "TraceLevel";

	/**
	 * Activate or not the SIP trace
	 */
	public static final String SIP_TRACE_ACTIVATION = "SipTraceActivation";

	/**
	 * Activate or not the media trace
	 */
	public static final String MEDIA_TRACE_ACTIVATION = "MediaTraceActivation";

	/**
	 * Capability refresh timeout used to avoid too many requests in a short time
	 */
	public static final String CAPABILITY_REFRESH_TIMEOUT = "CapabilityRefreshTimeout";
	
	/**
	 * Capability refresh timeout used to decide when to refresh contact capabilities
	 */
	public static final String CAPABILITY_EXPIRY_TIMEOUT = "CapabilityExpiryTimeout";
	
	/**
	 * Polling period used to decide when to refresh contacts capabilities
	 */
	public static final String CAPABILITY_POLLING_PERIOD = "CapabilityPollingPeriod";
	
	/**
	 * Presence service activation
	 */
	public static final String USE_PRESENCE_SERVICE = "UsePresenceService";
	
	/**
	 * Rich call service activation
	 */
	public static final String USE_RICHCALL_SERVICE = "UseRichcallService";

	/**
	 * CS video capability
	 */
	public static final String CAPABILITY_CS_VIDEO = "CapabilityCsVideo";

	/**
	 * Image sharing capability
	 */
	public static final String CAPABILITY_IMAGE_SHARING = "CapabilityImageShare";

	/**
	 * Video sharing capability
	 */
	public static final String CAPABILITY_VIDEO_SHARING = "CapabilityVideoShare";

	/**
	 * Instant Messaging session capability
	 */
	public static final String CAPABILITY_IM_SESSION = "CapabilityImSession";

	/**
	 * File transfer capability
	 */
	public static final String CAPABILITY_FILE_TRANSFER = "CapabilityFileTransfer";

	/**
	 * Presence discovery capability
	 */
	public static final String CAPABILITY_PRESENCE_DISCOVERY = "CapabilityPresenceDiscovery";
	
	/**
	 * Social presence capability
	 */
	public static final String CAPABILITY_SOCIAL_PRESENCE = "CapabilitySocialPresence";

	/**
	 * RCS extensions capability 
	 */
	public static final String CAPABILITY_RCS_EXTENSIONS = "CapabilityRcsExtensions";

	/**
	 * Instant messaging is always on (Store & Forward server) 
	 */
	public static final String IM_CAPABILITY_ALWAYS_ON = "ImAlwaysOn";
	
	/**
	 * Instant messaging use report 
	 */
	public static final String IM_USE_REPORTS = "ImUseReports";
	
	/**
	 * Network access authorized
	 */
	public static final String NETWORK_ACCESS = "NetworkAccess";
	
	/**
	 * SIP stack timer T1 
	 */
	public static final String SIP_TIMER_T1 = "SipTimerT1";
	
	/**
	 * SIP stack timer T2 
	 */
	public static final String SIP_TIMER_T2 = "SipTimerT2";
	
	/**
	 * SIP stack timer T4 
	 */
	public static final String SIP_TIMER_T4 = "SipTimerT4";
	
	/**
	 * Use SIP keep alive 
	 */
	public static final String USE_SIP_KEEP_ALIVE = "UseSipKeepAlive";
}
