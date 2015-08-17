package com.example.jobtracker;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.util.Xml;

public class Utils {

	static public class jobSAXParser extends DefaultHandler {
		StringBuilder xmlInnerText;
		Job job;
		ArrayList<Job> jobList;
		StringBuilder skill;

		static public ArrayList<Job> parseJob(InputStream in)
				throws IOException, SAXException {
			jobSAXParser parser = new jobSAXParser();
			Xml.parse(in, Xml.Encoding.UTF_8, parser);
			return parser.getjobList();
		}

		public ArrayList<Job> getjobList() {
			return jobList;
		}

		@Override
		public void startDocument() throws SAXException {
			super.startDocument();
			xmlInnerText = new StringBuilder();
			jobList = new ArrayList<Job>();
		}

		@Override
		public void startElement(String uri, String localName, String qName,
				Attributes attributes) throws SAXException {
			super.startElement(uri, localName, qName, attributes);
			if (localName.equals("JobSearchResult")) {
				job = new Job();
				skill = new StringBuilder();
			}
		}

		@Override
		public void endElement(String uri, String localName, String qName)
				throws SAXException {
			super.endElement(uri, localName, qName);
			if (localName.equals("JobSearchResult")) {
				jobList.add(job);
			} else if (localName.equals("JobTitle")) {
				job.setJobTitle(xmlInnerText.toString().trim());
			} else if (localName.equals("Company")) {
				job.setCompany(xmlInnerText.toString().trim());
			} else if (localName.equals("DescriptionTeaser")) {
				job.setJobDesc(xmlInnerText.toString().trim());
			} else if (localName.equals("Location")) {
				job.setLocation(xmlInnerText.toString().trim());
			} else if (localName.equals("PostedDate")) {
				job.setPostedDate(xmlInnerText.toString().trim());
			} else if (localName.equals("JobDetailsURL")) {
				job.setJobUrl(xmlInnerText.toString().trim());
			} else if (localName.equals("Pay")) {
				job.setPay(xmlInnerText.toString().trim());
			} else if (localName.equals("EmploymentType")) {
				job.setEmploymentType(xmlInnerText.toString().trim());
			} else if (localName.equals("EducationRequired")) {
				job.setEducationRequired(xmlInnerText.toString().trim());
			} else if (localName.equals("ExperienceRequired")) {
				job.setExperienceRequired(xmlInnerText.toString().trim());
			} else if (localName.equals("Skill")) {
				skill.append(xmlInnerText.toString().trim() + ", ");
			} else if (localName.equals("Skills")) {
				if (skill.length() > 2)
					job.setSkills(skill.substring(0, skill.length() - 2));
			}
			xmlInnerText.setLength(0);
		}

		@Override
		public void characters(char[] ch, int start, int length)
				throws SAXException {
			super.characters(ch, start, length);
			xmlInnerText.append(ch, start, length);
		}

		@Override
		public void endDocument() throws SAXException {
			super.endDocument();
		}

	}

}