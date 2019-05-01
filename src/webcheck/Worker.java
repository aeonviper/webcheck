package webcheck;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public class Worker extends Thread {

	public static final String HTTP_METHOD_GET = "GET";
	public static final String HTTP_METHOD_POST = "POST";

	private AtomicInteger responseCode = new AtomicInteger(-1);
	private List<String> lines = new ArrayList<String>();

	private CountDownLatch latch = null;
	private String inputUrl = null;
	private int verboseLevel = 0;
	private String httpRequestMethod = HTTP_METHOD_GET;

	public Worker(CountDownLatch latch, String inputUrl, int verboseLevel, String httpRequestMethod) {
		this.latch = latch;
		this.inputUrl = inputUrl;
		this.verboseLevel = verboseLevel;
		if (HTTP_METHOD_POST.equals(httpRequestMethod) || HTTP_METHOD_GET.equals(httpRequestMethod)) {
			this.httpRequestMethod = httpRequestMethod;
		}
	}

	public void run() {
		BufferedReader reader = null;
		HttpURLConnection httpUrlConnection = null;
		try {
			URL url = new URL(inputUrl);
			httpUrlConnection = (HttpURLConnection) url.openConnection();
			httpUrlConnection.setRequestMethod(httpRequestMethod);
			reader = new BufferedReader(new InputStreamReader(httpUrlConnection.getInputStream()));
			String line;

			if (verboseLevel >= Check.VERBOSE_LEVEL_2) {
				System.out.println("> -== start ==-");
			}
			while ((line = reader.readLine()) != null) {
				lines.add(line);
				if (verboseLevel >= Check.VERBOSE_LEVEL_2) {
					System.out.println(line);
				}
			}
			if (verboseLevel >= Check.VERBOSE_LEVEL_2) {
				System.out.println("> -== end ==-");
			}

			responseCode.set(httpUrlConnection.getResponseCode());
			if (verboseLevel >= Check.VERBOSE_LEVEL_1) {
				System.out.println("> Http Response Code: " + responseCode.get());
			}

		} catch (IOException eio) {
			eio.printStackTrace();
			responseCode.set(-2);
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (httpUrlConnection != null) {
				httpUrlConnection.disconnect();
			}
		}

		latch.countDown();
	}

	public AtomicInteger getResponseCode() {
		return responseCode;
	}

	public List<String> getLines() {
		return lines;
	}
}
