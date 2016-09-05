package com.ovtn;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.ovtn.JsonUtils;
import com.ovtn.JsonUtils.RequestType;

public class SncfApiReader {
	private static final String sApiKey = "374817c5-d83d-4eb0-a42e-2ad1811d6794:";
	private static final String sSncfbaseUrl = "https://api.sncf.com/v1/coverage/sncf/";
	private static final String sStopAreasKeyword = "stop_areas";
	private static final String sStopAreaId = "stop_area";
	private static final String sDeparturesKeyWord = "departures";
	private static final String sDatetimeKeyWord = "datetime";
	private static final SimpleDateFormat sApiDateFormat = new SimpleDateFormat("yyyyMMdd'T'HHmmss");
	private static final SimpleDateFormat sReadableDateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");


	public static List<String> getNextDeparturesForDate(String iStationCode, String iFormattedDate) {
		List<String> lNextDepartures = new ArrayList<String>();

		String lUrl = sSncfbaseUrl + sStopAreasKeyword + "/"
				+ sStopAreaId + ":" + iStationCode + "/" + sDeparturesKeyWord
				+ "?" + sDatetimeKeyWord + "=" + iFormattedDate;
		JSONObject lJson = JsonUtils.getJsonFromUrl(lUrl, sApiKey, RequestType.GET);
		if (lJson == null) {
			System.err.println("Empty response.");
			return lNextDepartures;
		}
		JSONArray lDepartures = (JSONArray) lJson.get(sDeparturesKeyWord);
		/*
		 * Iterate over results but don't use the last entry since it's doesn't
		 * have the same purpose.
		 */
		for (int i = 0 ; i < lDepartures.size() - 1 ; i++) {
			JSONObject lCurrentObject = (JSONObject)lDepartures.get(i);
			String lDepartureTime = JsonUtils.getJsonObject(lCurrentObject,
					"stop_date_time/departure_date_time");
			String lLineName = JsonUtils.getJsonObject(lCurrentObject,
					"route/line/name");
			try {
				lDepartureTime = sReadableDateFormat.format(sApiDateFormat.parse(lDepartureTime)).toString();
			} catch (ParseException iException) {
				// Nothing to do here, the formatting will just be odd
				System.err.println(lDepartureTime);
			}
			lNextDepartures.add(lDepartureTime + " " + lLineName);
		}
		return lNextDepartures;
	}
	
	/**
	 * @return the date format used by the API
	 */
	public static SimpleDateFormat getApiDateFormat() {
		return sApiDateFormat;
	}

	/**
	 * List the next departures for given station
	 * @param iStationCode
	 * @return a map with departure time as a key and train name as values
	 */
	public static List<String> getNextDepartures(String iStationCode) {
		return getNextDeparturesForDate(
				iStationCode, sApiDateFormat.format(Calendar.getInstance().getTime()));
	}

	/**
	 * For testing purposes only
	 */
	public static void main(String[] iArgs) {
		/*
		 * For demo purposes, "OCE:SA:87391003" is code for Gare Montparnasse.
		 * We could add a station dictionary.
		 */
		List<String> lNextDepartures = getNextDepartures("OCE:SA:87391003");
		for (String lNextDep : lNextDepartures) {
			System.out.println(lNextDep);
		}
	}
}
