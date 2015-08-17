package com.example.jobtracker;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.xml.sax.SAXException;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

public class RecommendedFragment extends Fragment {

	ProgressDialog progressDialog;
	JobListAdapter adapter;
	ListView listView;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View rootView = inflater.inflate(R.layout.fragment_recommended,
				container, false);
		listView = (ListView) rootView.findViewById(R.id.recJobsListView);
		return rootView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
		progressDialog = new ProgressDialog(getActivity());
		progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progressDialog.setMessage("Loading Jobs");

		new GetCareerBuilderJobs().execute(getRequestParm());
	}

	public String getRequestParm() {
		RequestParams requestParams = new RequestParams("GET",
				AppConstants.CAREERBUILDER_URL);
		requestParams.addParam("DeveloperKey", AppConstants.CAREERBUILDER_KEY);
		ParseUser parseUser = ParseUser.getCurrentUser();
		ParseQuery<ParseObject> query = ParseQuery.getQuery("Profile");
		query.whereEqualTo("UserName", parseUser.getUsername());
		try {
			List<ParseObject> profileList = query.find();
			if (profileList.size() > 0
					&& !profileList.get(0).getString("Skills").isEmpty()) {
				requestParams.addParam("Skills",
						profileList.get(0).getString("Skills"));
			}
		} catch (ParseException e) {
			Log.d("ParseException", e.getMessage());
		}

		return requestParams.getEncodedURL();
	}

	class GetCareerBuilderJobs extends AsyncTask<String, Void, ArrayList<Job>> {
		protected ArrayList<Job> doInBackground(String... params) {
			try {
				URL url = new URL(params[0]);
				HttpURLConnection con = (HttpURLConnection) url
						.openConnection();
				con.connect();
				int statusCode = con.getResponseCode();
				if (statusCode == HttpURLConnection.HTTP_OK) {
					InputStream in = con.getInputStream();
					return Utils.jobSAXParser.parseJob(in);
				} else
					Toast.makeText(getActivity(),
							"Unable to make a connection", Toast.LENGTH_SHORT)
							.show();
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (SAXException e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			progressDialog.show();
		}

		@Override
		protected void onPostExecute(ArrayList<Job> result) {
			super.onPostExecute(result);
			progressDialog.dismiss();
			if (result != null)
				createJobsList(result);
			else
				Toast.makeText(getActivity(), "Unable to retreive Jobs List",
						Toast.LENGTH_SHORT).show();
		}
	}

	public void createJobsList(final ArrayList<Job> result) {
		Collections.sort(result);
		adapter = new JobListAdapter(getActivity(), R.layout.job_list_layout,
				result);
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long arg3) {
				Intent intent = new Intent(getActivity(), JobActivity.class);
				intent.putExtra("jobObj", adapter.getItem(position));
				startActivity(intent);
			}

		});

		adapter.setNotifyOnChange(true);
	}

	@Override
	public void onResume() {
		super.onResume();
		new GetCareerBuilderJobs().execute(getRequestParm());
	}

}