package com.example.jobtracker;

import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

public class ProfileActivity extends Activity {

	EditText editSkills;
	EditText editDesiredSalary;
	EditText editDesiredLocation;
	EditText editUniversity;
	EditText editDegree;
	ParseObject parseObject;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_profile);

		editSkills = (EditText) findViewById(R.id.editSkills);
		editDesiredSalary = (EditText) findViewById(R.id.editDesiredSalary);
		editDesiredLocation = (EditText) findViewById(R.id.editDesiredLocation);
		editUniversity = (EditText) findViewById(R.id.edituniversity);
		editDegree = (EditText) findViewById(R.id.editdegree);

		ParseQuery<ParseObject> query = ParseQuery.getQuery("Profile");
		query.whereEqualTo("UserName", ParseUser.getCurrentUser().getUsername());
		try {
			List<ParseObject> userList = query.find();
			if (userList.size() > 0) {
				parseObject = userList.get(0);
				editSkills.setText(parseObject.getString("Skills"));
				editDesiredSalary.setText(parseObject
						.getString("DesiredSalary"));
				editDesiredLocation.setText(parseObject
						.getString("DesiredLocation"));
				editUniversity.setText(parseObject.getString("University"));
				editDegree.setText(parseObject.getString("Degree"));
			} else {
				parseObject = new ParseObject("Profile");
				parseObject.put("UserName", ParseUser.getCurrentUser()
						.getUsername());
			}

		} catch (ParseException e) {
			Log.d("ParseException", e.getMessage());
		}

		findViewById(R.id.buttonSaveProfileEdits).setOnClickListener(
				new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						parseObject.put("Skills", editSkills.getText()
								.toString());
						parseObject.put("DesiredSalary", editDesiredSalary
								.getText().toString());
						parseObject.put("DesiredLocation", editDesiredLocation
								.getText().toString());
						parseObject.put("University", editUniversity.getText()
								.toString());
						parseObject.put("Degree", editDegree.getText()
								.toString());
						parseObject.saveInBackground();
						Toast.makeText(ProfileActivity.this,
								"Profile successfully Updated",
								Toast.LENGTH_SHORT).show();
					}
				});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.profile, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_account) {
			return true;
		} else if (id == R.id.action_saved) {
			Intent intent = new Intent(ProfileActivity.this,
					SavedJobsActivity.class);
			startActivity(intent);
			return true;
		} else if (id == R.id.action_password) {
			Intent intent = new Intent(ProfileActivity.this,
					SettingActivity.class);
			startActivity(intent);
			return true;
		} else if (id == R.id.action_logout) {
			ParseUser.logOut();
			finish();
			Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
			startActivity(intent);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

}
