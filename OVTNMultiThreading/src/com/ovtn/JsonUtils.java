package com.ovtn;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.Base64;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.ovtn.JsonUtils.RequestType;

public class JsonUtils {

	/* EOL character is system dependent */
	private static final String sEndOfLine = "\n";
	/* HTTP return codes considered as successful */
	private static final Integer[] sValidCodes = new Integer[] {
		200, 201
	};

	/**
	 * How to retrieve info?
	 * @author Jean-Baptiste
	 */
	public enum RequestType {
		GET, POST
	}

	private static final String sLoginParam = "Authorization";

	/**
	 * 
	 * @param iUrl The url to use for retrieving data
	 * @param iApiKey Optional - api key for authentication purposes
	 * @param iRequestType Specify query type (get or post)
	 * @return The Json resulting from the query
	 */
	public static JSONObject getJsonFromUrl(
			String iUrl, String iApiKey, RequestType iRequestType) {
		JSONObject lJson = null;
		HttpURLConnection lConnection = null;
		try {
			lConnection = (HttpURLConnection) (new URL(iUrl)).openConnection();
			/* Login is not mandatory */
			if (iApiKey != null) {
				lConnection.setRequestProperty(sLoginParam, encodeKey(iApiKey));
			}
			lConnection.setRequestMethod(iRequestType.name());
			lConnection.setRequestProperty("Content-length", "0");
			lConnection.setUseCaches(false);
			lConnection.setAllowUserInteraction(false);
			lConnection.connect();
			int lResponseCode = lConnection.getResponseCode();
			String lFullJson = "";
			if (!Arrays.asList(sValidCodes).contains(lResponseCode)) {
				System.err.println("Invalid HTTP Code " + lResponseCode);
				return lJson;
			}
			/*
			 * try with resource : Bufferedreader will automatically be closed
			 * at the end of the try block
			 */
			try (BufferedReader lReader = new BufferedReader(
					new InputStreamReader(lConnection.getInputStream()))) {
				StringBuilder lStringBuilder = new StringBuilder();
				String line;
				while ((line = lReader.readLine()) != null) {
					// Use the factory pattern that is available
					lStringBuilder.append(line).append(sEndOfLine);
				}
				lFullJson = lStringBuilder.toString();
				lJson = (JSONObject)(new JSONParser()).parse(lFullJson);
			}
		} catch (ParseException | IOException iException) {
			System.err.println(iException);
		} finally {
			/*
			 * Old fashioned try with resource
			 */
			if (lConnection != null) {
				lConnection.disconnect();
			}
		}
		return lJson;
	}

	/**
	 * Properly encode the key for authentication in HTTP request
	 * @param iKey the key to be encoded
	 * @return
	 */
	private static String encodeKey(String iKey) {
		return "Basic " + (Base64.getUrlEncoder().encodeToString(iKey.getBytes()));
	}

	/**
	 * Retrieve an object in the json from its path. It's not made for JSONArray.
	 * @param iObject
	 * @param iPath use / separator
	 * @return
	 */
	public static String getJsonObject(JSONObject iObject, String iPath) throws NullPointerException {
		String[] lPath = iPath.split("/");
		JSONObject lObject = iObject;
		for (String lSubPath : lPath) {
			Object lCurrentObject = lObject.get(lSubPath);
			if (lCurrentObject instanceof JSONObject) {
				lObject = (JSONObject) lCurrentObject;
			} else if (lPath[lPath.length - 1].equals(lSubPath)) {
				return lCurrentObject.toString();
			} else {
				System.err.println("Error in Json Path : "
						+ iPath + " at level " + lSubPath);
				throw new NullPointerException();
			}
		}
		return lObject.toString();
	}
}
