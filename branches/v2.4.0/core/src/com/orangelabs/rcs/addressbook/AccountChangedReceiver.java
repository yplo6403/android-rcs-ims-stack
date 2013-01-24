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

package com.orangelabs.rcs.addressbook;

import com.orangelabs.rcs.R;
import com.orangelabs.rcs.platform.AndroidFactory;
import com.orangelabs.rcs.platform.registry.RegistryFactory;
import com.orangelabs.rcs.service.LauncherUtils;
import com.orangelabs.rcs.service.api.client.ClientApi;
import com.orangelabs.rcs.utils.logger.Logger;

import android.accounts.Account;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.widget.Toast;

/**
 * The user changed an account (modify, delete or add)
 * 
 * <br>We cannot prevent the user deleting the account but we can detect the deletion
 */
public class AccountChangedReceiver extends BroadcastReceiver {
	/**
	 * Account has been manually deleted
	 */
	private static final String REGISTRY_RCS_ACCOUNT_MANUALLY_DELETED = "RcsAccountManuallyDeleted";
	
	/**
     * The logger
     */
    private Logger logger = Logger.getLogger(this.getClass().getName());
	
	@Override
	public void onReceive(final Context context, Intent intent) {
		// Set the factory
		AndroidFactory.setApplicationContext(context);
		
		// Verify that the RCS account is still here
		Account mAccount = AuthenticationService.getAccount(context, context.getString(R.string.rcs_core_account_username));
		if (mAccount == null) {
			if (logger.isActivated()){
				logger.debug("RCS account has been deleted");
			}
			
			// Set the user account manually deleted flag
			RegistryFactory.getFactory().writeBoolean(REGISTRY_RCS_ACCOUNT_MANUALLY_DELETED, true);
			
			if (ClientApi.isServiceStarted(context)){
				
				if (logger.isActivated()){
					logger.debug("RCS service is running, we stop it");
				}
				
				// RCS account was deleted
				// Warn the user we stop the service
				// The account will be recreated when the service will be restarted
				Handler handler = new Handler();
				handler.post(new Runnable(){
					public void run(){
						Toast.makeText(context, context.getString(R.string.rcs_core_account_stopping_after_deletion), Toast.LENGTH_LONG).show();
					}
				});

                // Stop the service
                LauncherUtils.stopRcsService(context);
			}		
		} else {
			// Set the user account manually deleted flag
			RegistryFactory.getFactory().writeBoolean(REGISTRY_RCS_ACCOUNT_MANUALLY_DELETED, false);
		}
	}
	
	/**
	 * Is user account reset manually by end user
	 * 
	 * @return Boolean
	 */
	public static boolean isAccountResetByEndUser() {
		return RegistryFactory.getFactory().readBoolean(REGISTRY_RCS_ACCOUNT_MANUALLY_DELETED, false);
	}
}