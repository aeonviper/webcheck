package webcheck;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.rrd4j.core.RrdDb;
import org.rrd4j.core.Sample;
import org.rrd4j.core.Util;

public class Check {

	public static final int VERBOSE_LEVEL_1 = 1;
	public static final int VERBOSE_LEVEL_2 = 2;

	public static void main(String[] args) throws IOException {
		String url = null;
		Integer timeout = 15;
		Integer wait = 60;
		Integer verboseLevel = 0;
		String verifyFilename = null;
		String verifyKeyword = null;
		String writeVerifyFilename = null;
		String rrdFilename = null;
		boolean hideFormData = false;
		String httpRequestMethod = Worker.HTTP_METHOD_GET;		

		if (args.length <= 0) {
			System.out.println("Usage:");
			System.out.println("\tjava webcheck.Check [parameters] url");
			System.out.println("\nwhere parameters are:");
			System.out.println("\t--get");
			System.out.println("\t--post");
			System.out.println("\t--timeout <seconds>");
			System.out.println("\t--wait <seconds>");
			System.out.println("\t--verbose");
			System.out.println("\t--very-verbose");
			System.out.println("\t--hide-form-data");
			System.out.println("\t--verify-file <filename>");
			System.out.println("\t--verify-keyword <keyword>");
			System.out.println("\t--write-verify <filename>");
			System.out.println("\t--rrd-file <filename>");
			System.out.println("e.g.");
			System.out.println("\tjava webcheck.Check --verbose --write-verify das_login.html https://rmg-dev:21002/das/login.jsp");
			System.out.println("\tjava webcheck.Check --verbose --timeout 5 --verify-file das_login.html https://rmg-dev:21002/das/login.jsp");
			System.exit(-1);
		}

		int i = 0;
		String arg = null;
		for (i = 0; i < args.length; i++) {
			arg = args[i].trim();
			if ("--timeout".equals(arg)) {
				if (((i + 1) < args.length)) {
					try {
						timeout = Integer.parseInt(args[i + 1]);
					} catch (NumberFormatException nfe) {
						System.out.println("> Unable to parse timeout value: " + args[i + 1]);
						System.exit(1);
					}
					i++;
				} else {
					System.out.println("> Please specify a timeout value");
					System.exit(1);
				}
			} else if ("--wait".equals(arg)) {
				if (((i + 1) < args.length)) {
					try {
						wait = Integer.parseInt(args[i + 1]);
					} catch (NumberFormatException nfe) {
						System.out.println("> Unable to parse wait value: " + args[i + 1]);
						System.exit(1);
					}
					i++;
				} else {
					System.out.println("> Please specify a wait value");
					System.exit(1);
				}
			} else if ("--verbose".equals(arg)) {
				if (verboseLevel <= VERBOSE_LEVEL_1) {
					verboseLevel = VERBOSE_LEVEL_1;
				}
			} else if ("--very-verbose".equals(arg)) {
				if (verboseLevel <= VERBOSE_LEVEL_2) {
					verboseLevel = VERBOSE_LEVEL_2;
				}
			} else if ("--hide-form-data".equals(arg)) {
				hideFormData = true;
			} else if ("--get".equals(arg)) {
				httpRequestMethod = Worker.HTTP_METHOD_GET;
			} else if ("--post".equals(arg)) {
				httpRequestMethod = Worker.HTTP_METHOD_POST;
			} else if ("--verify-file".equals(arg)) {
				if (((i + 1) < args.length)) {
					verifyFilename = args[i + 1];
					i++;
				} else {
					System.out.println("> Please specify a filename for verification");
					System.exit(1);
				}
			} else if ("--verify-keyword".equals(arg)) {
				if (((i + 1) < args.length)) {
					verifyKeyword = args[i + 1];
					i++;
				} else {
					System.out.println("> Please specify a keyword for verification");
					System.exit(1);
				}
			} else if ("--write-verify".equals(arg)) {
				if (((i + 1) < args.length)) {
					writeVerifyFilename = args[i + 1];
					i++;
				} else {
					System.out.println("> Please specify a filename for saving verification");
					System.exit(1);
				}
			} else if ("--rrd-file".equals(arg)) {
				if (((i + 1) < args.length)) {
					rrdFilename = args[i + 1];
					i++;
				} else {
					System.out.println("> Please specify a rrd filename");
					System.exit(1);
				}
			} else if (arg.startsWith("-")) {
				System.out.println("> Unknown parameter: " + arg);
				System.exit(1);
			} else {
				url = arg;
			}
		}

		if (!(wait >= timeout)) {
			System.out.println("> Wait time must be greater or equal than timeout");
			System.exit(1);
		}

		if (url == null || url.length() == 0) {
			System.out.println("> Unable to determine URL to check");
			System.exit(1);
		} else {
			String displayUrl = url;
			if (hideFormData) {
				int dataBeginIndex = url.indexOf("?");
				if (dataBeginIndex != -1) {
					displayUrl = url.substring(0, dataBeginIndex);
				}
			}
			System.out.println("> URL: " + displayUrl);
			if (url.startsWith("http")) {
			} else {
				System.out.println("> URL must start with http or https");
				System.exit(1);
			}
		}

		if (verboseLevel >= VERBOSE_LEVEL_1) {
			System.out.println("> Timeout: " + timeout + " sec(s)");
			System.out.println("> Wait: " + wait + " sec(s)");
			System.out.println("> Request Method: " + httpRequestMethod);
			System.out.println("> Verbose Level: " + verboseLevel);
			if (verifyFilename != null) {
				System.out.println("> Verify Filename: " + verifyFilename);
			}
			if (writeVerifyFilename != null) {
				System.out.println("> Write Verify Filename: " + writeVerifyFilename);
			}
		}

		File verifyFile = null;
		if (verifyFilename != null) {
			verifyFile = new File(verifyFilename);
			if (verifyFile.canRead()) {

			} else {
				System.out.println("> Cannot read " + verifyFile);
				System.exit(1);
			}
		}

		disableSSLValidation();
		new Check().doCheck(url, timeout, wait, verifyFile, verifyKeyword, writeVerifyFilename, rrdFilename, verboseLevel, httpRequestMethod);
	}

	public void doCheck(String url, int timeout, int wait, File verifyFile, String verifyKeyword, String writeVerifyFilename, String rrdFilename, int verboseLevel, String httpRequestMethod) {
		int returnCode = -1;
		int responseCode = -1;
		Worker worker;
		long start = System.currentTimeMillis();
		final CountDownLatch latch = new CountDownLatch(1);

		(worker = new Worker(latch, url, verboseLevel, httpRequestMethod)).start();

		try {
			latch.await(wait, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		long delta = System.currentTimeMillis() - start;

		if (delta / 1000 > timeout) {
			returnCode = 2;
			if (verboseLevel >= VERBOSE_LEVEL_1) {
				System.out.println("> Soft timed out!");
			}
		} else {
			responseCode = worker.getResponseCode().get();
			if (responseCode == -1) {
				returnCode = 2;
				if (verboseLevel >= VERBOSE_LEVEL_1) {
					System.out.println("> Timed out!");
				}

			} else if (responseCode == -2) {
				returnCode = 3;
				if (verboseLevel >= VERBOSE_LEVEL_1) {
					System.out.println("> Java exception occurred");
				}

			} else if (responseCode == HttpURLConnection.HTTP_OK) {
				if (verboseLevel >= VERBOSE_LEVEL_1) {
					System.out.println("> Check successful");
					System.out.println("> Processing time: " + delta + " ms");
				}

				returnCode = 0;

				if (verifyKeyword != null) {
					returnCode = 6;

					boolean found = false;

					for (String s : worker.getLines()) {
						if (s.contains(verifyKeyword)) {
							found = true;
							break;
						}
					}

					if (found) {
						returnCode = 0;
						if (verboseLevel >= VERBOSE_LEVEL_1) {
							System.out.println("> Keyword found");
						}
					} else {
						System.out.println("> Keyword: " + verifyKeyword + " not found");
					}
				}

				// this means verify file takes precedence than verify keyword
				// i.e. if user supplies both and keyword is not found but
				// verify file matches, it will return success
				if (verifyFile != null) {
					returnCode = 5;

					List<String> fileLines = readFile(verifyFile);
					List<String> urlLines = worker.getLines();
					int i;

					if (urlLines.size() == fileLines.size()) {
						boolean identical = true;
						for (i = 0; i < urlLines.size(); i++) {
							if (urlLines.get(i).equals(fileLines.get(i))) {
							} else {
								identical = false;
								System.out.println("> Line: " + i + " differs");
								break;
							}
						}
						if (identical) {
							returnCode = 0;
							if (verboseLevel >= VERBOSE_LEVEL_1) {
								System.out.println("> Verified");
							}
						}
					} else {
						System.out.println("> Lines from URL: " + urlLines.size() + " != lines from file: " + fileLines.size());
					}
				}

				if (writeVerifyFilename != null) {
					File writeVerifyFile = new File(writeVerifyFilename);
					try {
						writeVerifyFile.createNewFile();
					} catch (IOException eio) {
						eio.printStackTrace();
					}
					if (writeVerifyFile.exists() && writeVerifyFile.canWrite()) {
						boolean status = writeFile(writeVerifyFile, worker.getLines());
						if (verboseLevel >= VERBOSE_LEVEL_1) {
							if (status) {
								System.out.println("> Verification written to " + writeVerifyFilename);
							} else {
								System.out.println("> Error in writing verification to " + writeVerifyFilename);
							}
						}
					} else {
						System.out.println("> Cannot write to " + writeVerifyFilename);
					}
				}

			} else {
				returnCode = 4;
				if (verboseLevel >= VERBOSE_LEVEL_1) {
					System.out.println("> Unexpected Http Response Code: " + responseCode);
				}
			}
		}

		if (rrdFilename != null) {
			RrdDb rrdDb = null;
			try {
				rrdDb = new RrdDb(rrdFilename);
				Sample sample = rrdDb.createSample();
				sample.setTime(Util.getTimestamp());
				sample.setValue("responsetime", delta);
				sample.setValue("responsecode", responseCode);

				System.out.println("> RRD Db: " + rrdDb.getPath() + " " + sample.getTime() + " responsetime: " + sample.getValues()[0] + " responsecode: " + sample.getValues()[1]);

				sample.update();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (rrdDb != null) {
					try {
						rrdDb.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}

		}

		System.exit(returnCode);
	}

	private static List<String> readFile(File file) {
		FileReader fileReader = null;
		BufferedReader reader = null;
		List<String> lines = new ArrayList<String>();
		try {
			String line = null;
			reader = new BufferedReader(fileReader = new FileReader(file));
			while ((line = reader.readLine()) != null) {
				lines.add(line);
			}
		} catch (IOException eio) {
			eio.printStackTrace();
		} finally {
			try {
				if (fileReader != null) {
					fileReader.close();
				}
				if (reader != null) {
					reader.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
		return lines;
	}

	private static boolean writeFile(File file, List<String> lines) {
		FileWriter fileWriter = null;
		PrintWriter outputStream = null;
		boolean success = false;
		try {
			outputStream = new PrintWriter(fileWriter = new FileWriter(file));
			for (String line : lines) {
				outputStream.println(line);
			}
			success = true;
		} catch (IOException eio) {
			eio.printStackTrace();
		} finally {
			try {
				if (fileWriter != null) {
					fileWriter.close();
				}
				if (outputStream != null) {
					outputStream.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return success;
	}

	private static void disableSSLValidation() {
		try {
			TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
				public java.security.cert.X509Certificate[] getAcceptedIssuers() {
					return null;
				}

				public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
				}

				public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
				}
			} };

			SSLContext sc = SSLContext.getInstance("SSL");
			sc.init(null, trustAllCerts, new java.security.SecureRandom());
			HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
			HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
				public boolean verify(String urlHostName, SSLSession session) {
					return true;
				}
			});
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (KeyManagementException e) {
			e.printStackTrace();
		}
	}

}
