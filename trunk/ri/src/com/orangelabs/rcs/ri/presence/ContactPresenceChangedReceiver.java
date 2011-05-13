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

package com.orangelabs.rcs.ri.presence;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.orangelabs.rcs.provider.settings.RcsSettings;
import com.orangelabs.rcs.ri.R;
import com.orangelabs.rcs.utils.PhoneUtils;

/**
 * Contact presence info update event receiver
 */
public class ContactPresenceChangedReceiver extends BroadcastReceiver{

	@Override
	public void onReceive(Context context, Intent intent) {	
		// Get invitation info
		String contact = intent.getStringExtra("contact");

		// Instanciate settings
        RcsSettings.createInstance(context);

        // Initialize the country code
		PhoneUtils.initialize(context);        

		// Display a toast
		Toast.makeText(context, context.getString(R.string.label_presence_info_changed, contact), Toast.LENGTH_LONG).show();
	}
}
