package com.example.jobtracker;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;

import org.xml.sax.SAXException;

import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

public class SearchFragment extends Fragment {

	private EditText txtSpeechInput;
	private final int REQ_CODE_SPEECH_INPUT = 100;
	ProgressDialog progressDialog;
	JobListAdapter adapter;
	ListView listView;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_search, container,
				false);
		txtSpeechInput = (EditText) rootView.findViewById(R.id.searchtext);
		listView = (ListView) rootView.findViewById(R.id.searchJobList);
		progressDialog = new ProgressDialog(getActivity());
		progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progressDialog.setMessage("Loading Jobs");
		rootView.findViewById(R.id.micImg).setOnClickListener(
				new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						promptSpeechInput();
					}
				});
		rootView.findViewById(R.id.searchImg).setOnClickListener(
				new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						executeSearchResults();
					}
				});
		return rootView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
	}

	private void promptSpeechInput() {
		Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
		intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
				RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
		intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
		intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
				getString(R.string.speech_prompt));
		try {
			startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
		} catch (ActivityNotFoundException a) {
			Toast.makeText(getActivity().getApplicationContext(),
					getString(R.string.speech_not_supported),
					Toast.LENGTH_SHORT).show();
		}
	}

	/**
	 * Receiving speech input
	 * */
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		switch (requestCode) {
		case REQ_CODE_SPEECH_INPUT: {
			if (null != data) {
				ArrayList<String> result = data
						.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
				txtSpeechInput.setText(result.get(0));
				executeSearchResults();
			}
			break;
		}

		}
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
	
	public void executeSearchResults(){
		RequestParams requestParams = new RequestParams("GET",
				AppConstants.CAREERBUILDER_URL);
		requestParams.addParam("DeveloperKey", AppConstants.CAREERBUILDER_KEY);
		requestParams.addParam("Keywords", txtSpeechInput
				.getText().toString());
		new GetCareerBuilderJobs().execute(requestParams
				.getEncodedURL());
	}
}
