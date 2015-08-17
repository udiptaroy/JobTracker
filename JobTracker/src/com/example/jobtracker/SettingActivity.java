package com.example.jobtracker;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.parse.ParseUser;

public class SettingActivity extends Activity {

	EditText accountName;
	EditText editNewPwd;
	EditText editConfirmPwd;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setting);

		final ParseUser currentUser = ParseUser.getCurrentUser();
		accountName = (EditText) findViewById(R.id.accountName);
		accountName.setText(currentUser.getString("FirstName") + " "
				+ currentUser.getString("LastName"));
		editNewPwd = (EditText) findViewById(R.id.editNewPassword);
		editConfirmPwd = (EditText) findViewById(R.id.editConfirmPassword);

		if (currentUser.getString("Type").equalsIgnoreCase(AppConstants.GOOGLE)) {
			findViewById(R.id.passwordLayout).setVisibility(View.GONE);
		}

		findViewById(R.id.buttonSaveAccount).setOnClickListener(
				new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						String editPwdStr = editNewPwd.getText().toString();
						String confPwdStr = editConfirmPwd.getText().toString();

						if (editPwdStr.equals(""))
							Toast.makeText(SettingActivity.this,
									"Password is required", Toast.LENGTH_SHORT)
									.show();
						else if (confPwdStr.equals(""))
							Toast.makeText(SettingActivity.this,
									"Confirm Password is required",
									Toast.LENGTH_SHORT).show();
						else if (!editPwdStr.equalsIgnoreCase(confPwdStr))
							Toast.makeText(SettingActivity.this,
									"Passwords do not match",
									Toast.LENGTH_SHORT).show();
						else {
							String fullName = accountName.getText().toString();
							String[] nameArray = fullName.split(" ");
							currentUser.put("FirstName", nameArray[0]);
							if (nameArray.length > 0) {
								currentUser.put("LastName", nameArray[1]);
							}

							currentUser.setPassword(editPwdStr);
							currentUser.saveInBackground();
							accountName.setText(accountName.getText()
									.toString());
						}

					}
				});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.setting, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_account) {
			Intent intent = new Intent(SettingActivity.this,
					ProfileActivity.class);
			startActivity(intent);
			return true;
		} else if (id == R.id.action_saved) {
			Intent intent = new Intent(SettingActivity.this,
					SavedJobsActivity.class);
			startActivity(intent);
			return true;
		} else if (id == R.id.action_password) {
			return true;
		} else if (id == R.id.action_logout) {
			ParseUser.logOut();
			finish();
			Intent intent = new Intent(SettingActivity.this, MainActivity.class);
			startActivity(intent);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
