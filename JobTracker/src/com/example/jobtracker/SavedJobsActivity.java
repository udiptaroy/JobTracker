package com.example.jobtracker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

public class SavedJobsActivity extends Activity {

	ProgressDialog progressDialog;
	JobListAdapter adapter;
	ListView listView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_saved_jobs);
		listView = (ListView) findViewById(R.id.savedJobsListView);
		progressDialog = new ProgressDialog(SavedJobsActivity.this);
		progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progressDialog.setMessage("Loading Jobs");
		createJobsList(getList(AppConstants.SAVED));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.saved_jobs, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_account) {
			Intent intent = new Intent(SavedJobsActivity.this,
					ProfileActivity.class);
			startActivity(intent);
			return true;
		} else if (id == R.id.action_saved) {
			return true;
		} else if (id == R.id.action_password) {
			Intent intent = new Intent(SavedJobsActivity.this,
					SettingActivity.class);
			startActivity(intent);
			return true;
		} else if (id == R.id.action_logout) {
			ParseUser.logOut();
			finish();
			Intent intent = new Intent(SavedJobsActivity.this,
					MainActivity.class);
			startActivity(intent);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public void createJobsList(final ArrayList<Job> result) {
		Collections.sort(result);
		adapter = new JobListAdapter(this, R.layout.job_list_layout, result);
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long arg3) {
				Intent intent = new Intent(SavedJobsActivity.this,
						JobActivity.class);
				intent.putExtra("jobObj", adapter.getItem(position));
				startActivityForResult(intent, AppConstants.UPDATE_REQ_CODE);
			}

		});

		adapter.setNotifyOnChange(true);
	}

	private ArrayList<Job> getList(String className) {
		final ArrayList<Job> jobList = new ArrayList<Job>();
		ParseQuery<ParseObject> query = ParseQuery.getQuery(className);
		query.whereEqualTo("username", ParseUser.getCurrentUser().getUsername());
		try {
			List<ParseObject> jobs = query.find();
			for (ParseObject object : jobs) {
				Job job = new Job();
				job.setJobTitle((String) object.get("JobTitle"));
				job.setCompany((String) object.get("JobCompany"));
				job.setLocation((String) object.get("Location"));
				job.setSkills((String) object.get("Skills"));
				job.setPay((String) object.get("Pay"));
				job.setEmploymentType((String) object.get("EmpType"));
				job.setExperienceRequired((String) object.get("ExpReq"));
				job.setPostedDate((String) object.get("PostedDate"));
				job.setJobDesc((String) object.get("JobDesc"));
				job.setJobUrl((String) object.get("JobUrl"));
				jobList.add(job);
			}
		} catch (ParseException e) {
			Log.d("Error", "Error retreiving data : " + e.getMessage());
		}

		return jobList;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == AppConstants.UPDATE_REQ_CODE) {
			adapter.clear();
			adapter.addAll(getList(AppConstants.SAVED));
			adapter.notifyDataSetChanged();
		}
	}

}
