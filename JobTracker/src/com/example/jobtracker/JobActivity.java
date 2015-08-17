package com.example.jobtracker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

public class JobActivity extends Activity {

	Boolean isSaved;
	Job job;
	ImageView shareImage;
	ImageView savImage;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_job);
		if (getIntent().hasExtra("jobObj")) {
			job = (Job) getIntent().getSerializableExtra("jobObj");
			TextView jobTitle = (TextView) findViewById(R.id.jobTitle);
			jobTitle.append(job.getJobTitle());
			TextView cmpny = (TextView) findViewById(R.id.companyName);
			cmpny.append(job.getCompany());
			TextView loc = (TextView) findViewById(R.id.location);
			loc.append(job.getLocation());
			TextView skills = (TextView) findViewById(R.id.skills);
			skills.append(job.getSkills());
			TextView pay = (TextView) findViewById(R.id.pay);
			pay.append(job.getPay());
			TextView empType = (TextView) findViewById(R.id.empType);
			empType.append(job.getEmploymentType());
			TextView exp = (TextView) findViewById(R.id.exp);
			exp.append(job.getExperienceRequired());
			TextView desc = (TextView) findViewById(R.id.jobDesContent);
			desc.append(job.getJobDesc());

			savImage = (ImageView) findViewById(R.id.saveJobs);

			ParseQuery<ParseObject> query = ParseQuery.getQuery("SavedJobs");
			query.whereEqualTo("JobTitle", job.getJobTitle());
			query.getFirstInBackground(new GetCallback<ParseObject>() {
				public void done(ParseObject object, ParseException e) {
					if (object == null) {
						isSaved = false;
						savImage.setImageResource(R.drawable.rating_not_important);
					} else {
						isSaved = true;
						savImage.setImageResource(R.drawable.rating_important);
					}
				}
			});

			savImage.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (!isSaved) {
						ParseObject saved = new ParseObject("SavedJobs");
						saved.put("JobTitle", job.getJobTitle());
						saved.put("JobUrl", job.getJobUrl());
						saved.put("JobCompany", job.getCompany());
						saved.put("Location", job.getLocation());
						saved.put("Skills", job.getSkills());
						saved.put("Pay", job.getPay());
						saved.put("EmpType", job.getEmploymentType());
						saved.put("ExpReq", job.getExperienceRequired());
						saved.put("PostedDate", job.getPostedDate());
						saved.put("JobDesc", job.getJobDesc());
						saved.put("username", ParseUser.getCurrentUser()
								.getUsername());
						saved.saveInBackground();
						savImage.setImageResource(R.drawable.rating_important);
						isSaved = true;
					} else {
						ParseQuery<ParseObject> query = ParseQuery
								.getQuery(AppConstants.SAVED);
						query.whereEqualTo("JobTitle", job.getJobTitle());
						query.getFirstInBackground(new GetCallback<ParseObject>() {
							public void done(ParseObject object,
									ParseException e) {
								if (object == null) {
									savImage.setImageResource(R.drawable.rating_important);
								} else {
									object.deleteInBackground();
									savImage.setImageResource(R.drawable.rating_not_important);
									isSaved = false;
								}
							}
						});
					}

				}
			});
			ImageView webBrowse = (ImageView) findViewById(R.id.checkWebsite);
			webBrowse.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					try {
						Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri
								.parse(job.getJobUrl()));
						startActivity(myIntent);
					} catch (ActivityNotFoundException e) {
						Toast.makeText(
								JobActivity.this,
								"No application can handle this request. Please install a webbrowser",
								Toast.LENGTH_LONG).show();
					}

				}
			});
		}

		shareImage = (ImageView) findViewById(R.id.shareFriends);
		registerForContextMenu(shareImage);
		shareImage.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				AlertDialog.Builder builder = new AlertDialog.Builder(
						JobActivity.this);
				final ParseObject emailSent = new ParseObject("EmailSent");
				builder.setMessage(R.string.suggest_title)
						.setPositiveButton(R.string.email_select,
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int id) {
										final EditText input = new EditText(
												JobActivity.this);
										AlertDialog.Builder builder = new AlertDialog.Builder(
												JobActivity.this);
										builder.setTitle(R.string.email_address)
												.setView(input)
												.setCancelable(false)
												.setPositiveButton(
														R.string.ok,
														new DialogInterface.OnClickListener() {

															@Override
															public void onClick(
																	DialogInterface dialog,
																	int which) {
																String value = input
																		.getText()
																		.toString();
																if (value
																		.isEmpty()) {
																	Toast.makeText(
																			JobActivity.this,
																			"Missing Description",
																			Toast.LENGTH_SHORT)
																			.show();
																} else if (!value
																		.matches(AppConstants.EMAIL_REGEX)) {
																	Toast.makeText(
																			JobActivity.this,
																			"Not a valid Email Address",
																			Toast.LENGTH_SHORT)
																			.show();
																} else {
																	emailSent
																			.put("username",
																					ParseUser
																							.getCurrentUser()
																							.getUsername());
																	emailSent
																			.put("frndemail",
																					value);
																	emailSent
																			.saveInBackground();
																	sendEmail(value);
																}
															}
														})
												.setNegativeButton(
														R.string.cancel,
														new DialogInterface.OnClickListener() {

															@Override
															public void onClick(
																	DialogInterface dialog,
																	int which) {

															}
														});
										AlertDialog alertDialog = builder
												.create();
										alertDialog.show();

									}
								})
						.setNegativeButton(R.string.user_select,
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int id) {
										final ArrayList<User> parseUsers = new ArrayList<User>();
										ParseQuery<ParseUser> query = ParseUser
												.getQuery();
										ParseUser currentUser = ParseUser
												.getCurrentUser();
										query.whereNotEqualTo("username",
												currentUser.getUsername());
										try {
											List<ParseUser> objects = query
													.find();
											for (ParseUser parseUser : objects) {
												User user = new User(
														(String) parseUser
																.getUsername(),
														(String) parseUser
																.get("FirstName"),
														(String) parseUser
																.get("LastName"));
												parseUsers.add(user);
											}
										} catch (ParseException e1) {
											Log.d("Error",
													"Unable to retreive User List"
															+ e1.getMessage());
										}

										ArrayList<User> sortedByLastName = new ArrayList<User>(
												parseUsers);
										Collections.sort(sortedByLastName,
												new Comparator<User>() {

													@SuppressLint("DefaultLocale")
													@Override
													public int compare(
															User lhs, User rhs) {
														return lhs
																.getUserLastName()
																.toLowerCase()
																.compareTo(
																		rhs.getUserLastName()
																				.toLowerCase());
													}
												});

										AlertDialog.Builder builder = new AlertDialog.Builder(
												JobActivity.this);
										ArrayAdapter<User> adapter = new ArrayAdapter<User>(
												JobActivity.this,
												android.R.layout.simple_list_item_1,
												sortedByLastName);
										builder.setTitle(R.string.suggest_title)
												.setAdapter(
														adapter,
														new DialogInterface.OnClickListener() {
															public void onClick(
																	DialogInterface dialog,
																	int which) {
																emailSent
																		.put("username",
																				ParseUser
																						.getCurrentUser()
																						.getUsername());
																emailSent
																		.put("frndemail",
																				parseUsers
																						.get(which)
																						.getUserName());
																emailSent
																		.saveInBackground();
																sendEmail(ParseUser
																		.getCurrentUser()
																		.getUsername());
															}
														});
										AlertDialog userDialog = builder
												.create();
										userDialog.show();
									}
								});

				AlertDialog dialog = builder.create();
				dialog.show();
			}

		});

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.job, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_account) {
			Intent intent = new Intent(JobActivity.this, ProfileActivity.class);
			startActivity(intent);
			return true;
		} else if (id == R.id.action_saved) {
			Intent intent = new Intent(JobActivity.this,
					SavedJobsActivity.class);
			startActivity(intent);
			return true;
		} else if (id == R.id.action_password) {
			Intent intent = new Intent(JobActivity.this, SettingActivity.class);
			startActivity(intent);
			return true;
		} else if (id == R.id.action_logout) {
			ParseUser.logOut();
			finish();
			Intent intent = new Intent(JobActivity.this, MainActivity.class);
			startActivity(intent);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	protected void sendEmail(String email) {
		Log.i("Send email", "");

		String[] TO = { email };
		Intent emailIntent = new Intent(Intent.ACTION_SEND);
		emailIntent.setData(Uri.parse("mailto:"));
		emailIntent.setType("text/plain");

		emailIntent.putExtra(Intent.EXTRA_EMAIL, TO);
		emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Your subject");

		String message = createJobMessage();
		emailIntent.putExtra(Intent.EXTRA_TEXT, message);

		try {
			startActivity(Intent.createChooser(emailIntent, "Send mail..."));
			Log.i("Finished sending email...", "");
		} catch (android.content.ActivityNotFoundException ex) {
			Toast.makeText(JobActivity.this,
					"There is no email client installed.", Toast.LENGTH_SHORT)
					.show();
		}
	}

	private String createJobMessage() {
		StringBuilder builder = new StringBuilder();
		builder.append(job.getJobTitle());
		builder.append("\n");
		builder.append(job.getJobDesc());
		builder.append("\n");
		builder.append(job.getJobUrl());
		return builder.toString();
	}

}