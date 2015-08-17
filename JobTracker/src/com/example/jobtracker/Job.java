package com.example.jobtracker;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Job implements Serializable, Comparable<Job> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String jobTitle;
	private String company;
	private String jobDesc;
	private String location;
	private String skills;
	private String postedDate;
	private String jobUrl;
	private String pay;
	private String employmentType;
	private String educationRequired;
	private String experienceRequired;

	public String getJobTitle() {
		return jobTitle;
	}

	public void setJobTitle(String jobTitle) {
		this.jobTitle = jobTitle;
	}

	public String getCompany() {
		return company;
	}

	public void setCompany(String company) {
		this.company = company;
	}

	public String getJobDesc() {
		return jobDesc;
	}

	public void setJobDesc(String jobDesc) {
		this.jobDesc = jobDesc;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getSkills() {
		return skills;
	}

	public void setSkills(String skills) {
		this.skills = skills;
	}

	public String getPostedDate() {
		return postedDate;
	}

	public void setPostedDate(String postedDate) {
		this.postedDate = postedDate;
	}

	public String getJobUrl() {
		return jobUrl;
	}

	public void setJobUrl(String jobUrl) {
		this.jobUrl = jobUrl;
	}

	public String getPay() {
		return pay;
	}

	public void setPay(String pay) {
		this.pay = pay;
	}

	public String getEmploymentType() {
		return employmentType;
	}

	public void setEmploymentType(String employmentType) {
		this.employmentType = employmentType;
	}

	public String getEducationRequired() {
		return educationRequired;
	}

	public void setEducationRequired(String educationRequired) {
		this.educationRequired = educationRequired;
	}

	public String getExperienceRequired() {
		return experienceRequired;
	}

	public void setExperienceRequired(String experienceRequired) {
		this.experienceRequired = experienceRequired;
	}

	@Override
	public int compareTo(Job another) {
		SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy",
				Locale.US);
		try {
			Date date1 = formatter.parse(postedDate);
			Date date2 = formatter.parse(another.getPostedDate());
			return date2.compareTo(date1);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}
}
