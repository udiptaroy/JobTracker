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
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

public class MainActivity extends Activity implements ConnectionCallbacks,
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
		setContentView(R.layout.activity_main);
		Parse.initialize(this, AppConstants.APP_ID, AppConstants.CLIENT_KEY);

		// Initializing google plus api client
		mGoogleApiClient = new GoogleApiClient.Builder(this)
				.addConnectionCallbacks(this)
				.addOnConnectionFailedListener(this).addApi(Plus.API)
				.addScope(Plus.SCOPE_PLUS_LOGIN).build();

		findViewById(R.id.btn_sign_in).setOnClickListener(
				new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						signInWithGplus();
					}
				});

		findViewById(R.id.buttonCreateNewAccount).setOnClickListener(
				new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						Intent intent = new Intent(MainActivity.this,
								SignUpActivity.class);
						startActivity(intent);
						finish();
					}
				});

		findViewById(R.id.buttonLogin).setOnClickListener(
				new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						EditText username = (EditText) findViewById(R.id.editTextEmail);
						EditText passText = (EditText) findViewById(R.id.editTextPassword);
						String email = username.getText().toString();
						String password = passText.getText().toString();
						if (email.equals(""))
							Toast.makeText(MainActivity.this, "Enter email",
									Toast.LENGTH_SHORT).show();
						else if (password.equals(""))
							Toast.makeText(MainActivity.this, "Enter password",
									Toast.LENGTH_SHORT).show();
						else if (!email.matches(AppConstants.EMAIL_REGEX))
							Toast.makeText(MainActivity.this,
									email + " is not a valid email address",
									Toast.LENGTH_SHORT).show();
						else {
							loginUser(email, password);
						}
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
		getProfileInformation();

		updateUI(true);
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
					loginUser(email, AppConstants.GOOGLE);
				} else {
					SignUpActivity activity = new SignUpActivity();
					activity.signUpUser(email, AppConstants.GOOGLE, personName,
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

	@Override
	public void onConnectionSuspended(int arg0) {
		mGoogleApiClient.connect();
		updateUI(false);
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

	public void loginUser(String email, String password) {
		ParseUser.logInInBackground(email, password, new LogInCallback() {
			@Override
			public void done(ParseUser user, ParseException e) {
				if (user != null) {
					Intent intent = new Intent(MainActivity.this,
							TrackerActivity.class);
					startActivity(intent);
					finish();
				} else {
					Toast.makeText(MainActivity.this, "Unable to Login",
							Toast.LENGTH_SHORT).show();
					e.printStackTrace();
				}

			}
		});
	}
}
