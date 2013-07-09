/*******************************************************************************
 * Software Name : RCS IMS Stack
 *
 * Copyright (C) 2010 France Telecom S.A.
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
package com.orangelabs.rcs.service.api.client;

/**
 * IMS API intents
 *  
 * @author jexa7410
 */
public class ImsApiIntents {
    /**
     * Stop reason "unknown"
     */
    public final static int REASON_UNKNOWN = 0;

    /**
     * Stop reason "battery low"
     */
    public final static int STOP_REASON_BATTERY_LOW = 1;

    /**
     * Intent broadcasted when the IMS registration status has changed (see boolean attribute "status"). 
     * 
     * <p>The intent will have the following extra values:
     * <ul>
     *   <li><em>status</em> - Registration status.</li>
     *   <li><em>reason</em> - Reason.</li>
     * </ul>
     * </ul>
     */
	public final static String IMS_STATUS = "com.orangelabs.rcs.IMS_STATUS";
}