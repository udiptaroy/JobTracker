package com.example.jobtracker;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class JobListAdapter extends ArrayAdapter<Job> {

	Context mContext;
	List<Job> mData;
	int mResource;
	ImageView starImage;

	public JobListAdapter(Context context, int resource, List<Job> objects) {
		super(context, resource, objects);
		this.mContext = context;
		this.mResource = resource;
		this.mData = objects;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			LayoutInflater inflater = (LayoutInflater) mContext
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(mResource, parent, false);
		}

		Job job = mData.get(position);

		((TextView) convertView.findViewById(R.id.listJobTitle)).setText(job
				.getJobTitle());
		((TextView) convertView.findViewById(R.id.listJobLocation)).setText(job
				.getLocation());
		((TextView) convertView.findViewById(R.id.listJobDate)).setText(job
				.getPostedDate());

		return convertView;
	}
}
