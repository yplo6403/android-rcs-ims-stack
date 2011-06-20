/*******************************************************************************
 * Software Name : RCS IMS Stack
 * Version : 2.0.0
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
package com.orangelabs.rcs.settings;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.orangelabs.rcs.R;
import com.orangelabs.rcs.provider.settings.RcsSettings;

/**
 * User profile settings display
 * 
 * @author jexa7410
 */
public class UserProfileSettingsDisplay extends PreferenceActivity implements Preference.OnPreferenceChangeListener {
	private static final int DIALOG_GET_MSISDN = 1;
	
	private EditTextPreference username;
	private EditTextPreference displayName;
	private EditTextPreference privateId;
	private EditTextPreference userPassword;
	private EditTextPreference domain;
	private EditTextPreference proxyAddr;
	private EditTextPreference xdmAddr;
	private EditTextPreference xdmLogin;
	private EditTextPreference xdmPassword;
	private boolean modified = false;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        addPreferencesFromResource(R.xml.rcs_settings_provisioning_preferences);
        setTitle(R.string.title_settings);
        
        // Instanciate the settings manager
        RcsSettings.createInstance(getApplicationContext());

        // Display current settings
        username = (EditTextPreference)findPreference("username");
        username.setPersistent(false);
        username.setOnPreferenceChangeListener(this);
		String user = RcsSettings.getInstance().getUserProfileUserName();
		if ((user != null) && (user.length() > 0)) {
			username.setSummary(user);
			username.setText(user);
		} else {
			username.setSummary(getString(R.string.label_mandatory_value));
		}

        displayName = (EditTextPreference)findPreference("display_name");
        displayName.setPersistent(false);
        displayName.setOnPreferenceChangeListener(this);
		String name = RcsSettings.getInstance().getUserProfileDisplayName();
		if ((name != null) && (name.length() > 0)) {
			displayName.setSummary(name);
			displayName.setText(name);
		} else {
			displayName.setSummary(getString(R.string.label_empty_displayname));
		}
        
        privateId = (EditTextPreference)findPreference("private_uri");
        privateId.setPersistent(false);
        privateId.setOnPreferenceChangeListener(this);
		String privateName = RcsSettings.getInstance().getUserProfilePrivateId();
		if ((privateName != null) && (privateName.length() > 0)) {
			privateId.setSummary(privateName);
			privateId.setText(privateName);
		} else {
			privateId.setSummary(getString(R.string.label_mandatory_value));
		}

        userPassword = (EditTextPreference)findPreference("user_password");
        userPassword.setPersistent(false);
        userPassword.setOnPreferenceChangeListener(this);
		String pwd = RcsSettings.getInstance().getUserProfilePassword();
		if ((pwd != null) && (pwd.length() > 0)) {
	        userPassword.setSummary(pwd);
	        userPassword.setText(pwd);
		} else {
			userPassword.setSummary(getString(R.string.label_mandatory_value));
		}
		
        domain = (EditTextPreference)findPreference("domain");
        domain.setPersistent(false);
        domain.setOnPreferenceChangeListener(this);
		String home = RcsSettings.getInstance().getUserProfileDomain();
		if ((home != null) && (home.length() > 0)) {
	        domain.setSummary(home);
	        domain.setText(home);
		} else {
			domain.setSummary(getString(R.string.label_mandatory_value));
		}
		
        proxyAddr = (EditTextPreference)findPreference("sip_proxy_addr");
        proxyAddr.setPersistent(false);
        proxyAddr.setOnPreferenceChangeListener(this);
		String proxy = RcsSettings.getInstance().getUserProfileProxy();
		if ((proxy != null) && (proxy.length() > 0)) {
	        proxyAddr.setSummary(proxy);
	        proxyAddr.setText(proxy);
		} else {
			proxyAddr.setSummary(getString(R.string.label_mandatory_value));
		}
		
        xdmAddr = (EditTextPreference)findPreference("xdm_server_addr");
        xdmAddr.setPersistent(false);
        xdmAddr.setOnPreferenceChangeListener(this);
		String server = RcsSettings.getInstance().getUserProfileXdmServer();
		if ((server != null) && (server.length() > 0)) {
	        xdmAddr.setSummary(server);
	        xdmAddr.setText(server);
		} else {
			xdmAddr.setSummary(getString(R.string.label_mandatory_value));
		}
		
        xdmLogin = (EditTextPreference)findPreference("xdm_login");
        xdmLogin.setPersistent(false);
        xdmLogin.setOnPreferenceChangeListener(this);
		String login = RcsSettings.getInstance().getUserProfileXdmLogin();
		if ((login != null) && (login.length() > 0)) {
	        xdmLogin.setSummary(login);
	        xdmLogin.setText(login);
		} else {
			xdmLogin.setSummary(getString(R.string.label_mandatory_value));
		}
		
        xdmPassword = (EditTextPreference)findPreference("xdm_password");
        xdmPassword.setPersistent(false);
        xdmPassword.setOnPreferenceChangeListener(this);
		String serverPwd = RcsSettings.getInstance().getUserProfileXdmPassword();
		if ((serverPwd != null) && (serverPwd.length() > 0)) {
	        xdmPassword.setSummary(serverPwd);
	        xdmPassword.setText(serverPwd);
		} else {
			xdmPassword.setSummary(getString(R.string.label_mandatory_value));
		}
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        
        if (modified) {
    		Toast.makeText(UserProfileSettingsDisplay.this, getString(R.string.label_warning), Toast.LENGTH_LONG).show();
    	}
    }
    
    public boolean onPreferenceChange(Preference preference, Object objValue) {
    	String value = (String)objValue;
        if (preference.getKey().equals("username")) {
        	RcsSettings.getInstance().setUserProfileUserName(value);
	        username.setSummary(value);
	        modified = true;
        } else        	
        if (preference.getKey().equals("display_name")) {
        	RcsSettings.getInstance().setUserProfileDisplayName(value);
	        displayName.setSummary(value);
	        modified = true;
        } else        	
        if (preference.getKey().equals("private_uri")) {
        	RcsSettings.getInstance().setUserProfilePrivateId(value);
	        privateId.setSummary(value);
	        modified = true;
        } else        	
        if (preference.getKey().equals("user_password")) {
        	RcsSettings.getInstance().setUserProfilePassword(value);
	        userPassword.setSummary(value);
	        modified = true;
        } else
        if (preference.getKey().equals("domain")) {
        	RcsSettings.getInstance().setUserProfileDomain(value);
    		domain.setSummary(value);
	        modified = true;
        } else
        if (preference.getKey().equals("sip_proxy_addr")) {
        	RcsSettings.getInstance().setUserProfileProxy(value);
	        proxyAddr.setSummary(value);
	        modified = true;
        } else
        if (preference.getKey().equals("xdm_server_addr")) {
        	RcsSettings.getInstance().setUserProfileXdmServer(value);
	        xdmAddr.setSummary(value);
	        modified = true;
    	} else
        if (preference.getKey().equals("xdm_login")) {
        	RcsSettings.getInstance().setUserProfileXdmLogin(value);
	        xdmLogin.setSummary(value);
	        modified = true;
    	} else
        if (preference.getKey().equals("xdm_password")) {
        	RcsSettings.getInstance().setUserProfileXdmPassword(value);
	        xdmPassword.setSummary(value);
	        modified = true;
        }
        return true;
    }
    
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		new MenuInflater(getApplication()).inflate(R.menu.rcs_settings_provisioning_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int i = item.getItemId();
		if (i == R.id.menu_default_profile) {
			showDialog(DIALOG_GET_MSISDN);			
			return true;
		}		
		return super.onOptionsItemSelected(item);
	}
	
    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
	        case DIALOG_GET_MSISDN:
				LayoutInflater factory = LayoutInflater.from(this);
	            final View textEntryView = factory.inflate(R.layout.rcs_settings_set_msisdn_layout, null);
	            return new AlertDialog.Builder(this)
	                .setTitle(R.string.label_get_msisdn)
	                .setView(textEntryView)
	                .setPositiveButton(R.string.label_ok, new DialogInterface.OnClickListener() {
	                    public void onClick(DialogInterface dialog, int whichButton) {
	            	        // Generate default settings
	            			EditText textEdit = (EditText)textEntryView.findViewById(R.id.msisdn);
	            			String number = textEdit.getText().toString();
	                    	String pId = number + "@sip.ofr.com";
	            			String pwd = "nsnims2008";
	            			String home = "sip.ofr.com";
	            			String proxy = "80.12.197.66:5060";
	            			String xdms = "10.194.117.38:8080/services";
	            			String login = "sip:"+ number + "@sip.ofr.com";
	                    	
	            			// Update UI & save date
	                    	RcsSettings.getInstance().setUserProfileUserName(number);
	                    	username.setSummary(number);
	            			username.setText(number);
	                    	RcsSettings.getInstance().setUserProfileDisplayName(number);
	            			displayName.setSummary(number);
	            			displayName.setText(number);
	                    	RcsSettings.getInstance().setUserProfilePrivateId(pId);
	            			privateId.setSummary(pId);
	            			privateId.setText(pId);
	                    	RcsSettings.getInstance().setUserProfilePassword(pwd);
	            	        userPassword.setSummary(pwd);
	            	        userPassword.setText(pwd);
	                    	RcsSettings.getInstance().setUserProfileDomain(home);
	            	        domain.setSummary(home);
	            	        domain.setText(home);
	                    	RcsSettings.getInstance().setUserProfileProxy(proxy);
	            	        proxyAddr.setSummary(proxy);
	            	        proxyAddr.setText(proxy);
	                    	RcsSettings.getInstance().setUserProfileXdmServer(xdms);
	            	        xdmAddr.setSummary(xdms);
	            	        xdmAddr.setText(xdms);
	                    	RcsSettings.getInstance().setUserProfileXdmLogin(login);
	            	        xdmLogin.setSummary(login);
	            	        xdmLogin.setText(login);
	                    	RcsSettings.getInstance().setUserProfileXdmPassword(pwd);
	            	        xdmPassword.setSummary(pwd);
	            	        xdmPassword.setText(pwd);
	            	        modified = true;
            	        }
	                })
	                .setNegativeButton(R.string.label_cancel, null)
	                .create();
        }
        return null;
    }    
}