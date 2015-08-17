package com.example.jobtracker;

import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

public class SignUpActivity extends Activity implements ConnectionCallbacks,
		OnConnectionFailedListener {

	// Google client to interact with Google API
	private GoogleApiClient mGoogleApiClient;
	private boolean mIntentInProgress;
	private boolean mSignInClicked;
	private ConnectionResult mConnectionResult;
	private static final int RC_SIGN_IN = 0;
	private String email, personName;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sign_up);

		// Initializing google plus api client
		mGoogleApiClient = new GoogleApiClient.Builder(this)
				.addConnectionCallbacks(this)
				.addOnConnectionFailedListener(this).addApi(Plus.API)
				.addScope(Plus.SCOPE_PLUS_LOGIN).build();

		findViewById(R.id.buttonSignup).setOnClickListener(
				new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						EditText editText = (EditText) findViewById(R.id.signupTextUserName);
						EditText editPwd = (EditText) findViewById(R.id.signupTextPassword);
						EditText confPwd = (EditText) findViewById(R.id.signupTextPasswordConfirm);
						EditText editMail = (EditText) findViewById(R.id.signupTextEmail);
						String editTextStr = editText.getText().toString();
						String editPwdStr = editPwd.getText().toString();
						String confPwdStr = confPwd.getText().toString();
						String editMailStr = editMail.getText().toString();

						if (editTextStr.equals(""))
							Toast.makeText(SignUpActivity.this,
									"Username is required", Toast.LENGTH_SHORT)
									.show();
						else if (editMailStr.equals(""))
							Toast.makeText(SignUpActivity.this,
									"Email is required", Toast.LENGTH_SHORT)
									.show();
						else if (!editMailStr.matches(AppConstants.EMAIL_REGEX))
							Toast.makeText(
									SignUpActivity.this,
									editMailStr
											+ " is not a valid email address",
									Toast.LENGTH_SHORT).show();
						else if (editPwdStr.equals(""))
							Toast.makeText(SignUpActivity.this,
									"Password is required", Toast.LENGTH_SHORT)
									.show();
						else if (confPwdStr.equals(""))
							Toast.makeText(SignUpActivity.this,
									"Confirm Password is required",
									Toast.LENGTH_SHORT).show();
						else if (!editPwdStr.equalsIgnoreCase(confPwdStr))
							Toast.makeText(SignUpActivity.this,
									"Passwords not matching",
									Toast.LENGTH_SHORT).show();
						else {
							signUpUser(editMailStr,editPwdStr,editTextStr,"parse");
						}
					}
				});

		findViewById(R.id.buttonCancel).setOnClickListener(
				new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						Intent intent = new Intent(SignUpActivity.this,
								MainActivity.class);
						startActivity(intent);
						finish();
					}
				});

		findViewById(R.id.btn_sign_up).setOnClickListener(
				new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						signInWithGplus();
					}
				});
	}

	protected void onStop() {
		super.onStop();
		if (mGoogleApiClient.isConnected()) {
			mGoogleApiClient.disconnect();
		}
	}

	@Override
	public void onConnectionFailed(ConnectionResult result) {
		if (!result.hasResolution()) {
			GooglePlayServicesUtil.getErrorDialog(result.getErrorCode(), this,
					0).show();
			return;
		}

		if (!mIntentInProgress) {
			// Store the ConnectionResult for later usage
			mConnectionResult = result;

			if (mSignInClicked) {
				// The user has already clicked 'sign-in' so we attempt to
				// resolve all
				// errors until the user is signed in, or they cancel.
				resolveSignInError();
			}
		}

	}

	@Override
	protected void onActivityResult(int requestCode, int responseCode,
			Intent intent) {
		if (requestCode == RC_SIGN_IN) {
			if (responseCode != RESULT_OK) {
				mSignInClicked = false;
			}

			mIntentInProgress = false;

			if (!mGoogleApiClient.isConnecting()) {
				mGoogleApiClient.connect();
			}
		}
	}

	@Override
	public void onConnected(Bundle arg0) {
		mSignInClicked = false;
		Toast.makeText(this, "User is connected!", Toast.LENGTH_LONG).show();

		// Get user's information
		getProfileInformation();

		// Update the UI after signin
		updateUI(true);

	}

	@Override
	public void onConnectionSuspended(int arg0) {
		mGoogleApiClient.connect();
		updateUI(false);
	}

	/**
	 * Updating the UI, showing/hiding buttons and profile layout
	 * */
	private void updateUI(boolean isSignedIn) {
		if (isSignedIn) {
			ParseQuery<ParseUser> query = ParseUser.getQuery();
			query.whereEqualTo("username", email);
			try {
				List<ParseUser> objects = query.find();
				if (objects.size() > 0) {
					ParseUser.logInInBackground(email, "google", new LogInCallback() {
						@Override
						public void done(ParseUser user, ParseException e) {
							if (user != null) {
								Intent intent = new Intent(SignUpActivity.this,
										TrackerActivity.class);
								startActivity(intent);
								finish();
							} else {
								Toast.makeText(SignUpActivity.this, "Unable to Login",
										Toast.LENGTH_SHORT).show();
								e.printStackTrace();
							}

						}
					});
				} else {
					signUpUser(email, AppConstants.GOOGLE, personName,
							AppConstants.GOOGLE);
				}
			} catch (ParseException e) {
				e.printStackTrace();
			}

		} else {
			Toast.makeText(this, "User to login user", Toast.LENGTH_LONG)
					.show();
		}
	}

	/**
	 * Sign-in into google
	 * */
	private void signInWithGplus() {
		mGoogleApiClient.connect();
		if (!mGoogleApiClient.isConnecting()) {
			mSignInClicked = true;
			resolveSignInError();
		}
	}

	/**
	 * Method to resolve any signin errors
	 * */
	private void resolveSignInError() {
		if (mConnectionResult.hasResolution()) {
			try {
				mIntentInProgress = true;
				mConnectionResult.startResolutionForResult(this, RC_SIGN_IN);
			} catch (SendIntentException e) {
				mIntentInProgress = false;
				mGoogleApiClient.connect();
			}
		}
	}

	private void getProfileInformation() {
		try {
			if (Plus.PeopleApi.getCurrentPerson(mGoogleApiClient) != null) {
				Person currentPerson = Plus.PeopleApi
						.getCurrentPerson(mGoogleApiClient);
				personName = currentPerson.getDisplayName();
				email = Plus.AccountApi.getAccountName(mGoogleApiClient);

				Log.d("demo", "Name: " + personName + "email: " + email);
			} else {
				Toast.makeText(getApplicationContext(),
						"Person information is null", Toast.LENGTH_LONG).show();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void signUpUser(String email, String password, String fullName,
			String type) {
		ParseUser user = new ParseUser();
		user.setUsername(email);
		user.setPassword(password);
		user.setEmail(email);

		String[] nameArray = fullName.split(" ", 2);
		user.put("FirstName", nameArray[0]);
		user.put("Type", type);
		if (nameArray.length > 1)
			user.put("LastName", nameArray[1]);
		else
			user.put("LastName", "");
		user.signUpInBackground(new SignUpCallback() {

			@Override
			public void done(ParseException e) {
				if (e == null) {
					Toast.makeText(SignUpActivity.this,
							"Signing up Successful", Toast.LENGTH_SHORT).show();
					Intent intent = new Intent(SignUpActivity.this,
							TrackerActivity.class);
					startActivity(intent);
					finish();
				} else {
					Toast.makeText(SignUpActivity.this,
							"Acccount already exists. Use different Email Id",
							Toast.LENGTH_SHORT).show();
				}

			}
		});
	}
}
