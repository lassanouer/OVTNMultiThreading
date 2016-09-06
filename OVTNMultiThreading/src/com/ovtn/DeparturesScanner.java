package com.ovtn;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.io.FileUtils;

public class DeparturesScanner {
	private static boolean		sCancel			= false;
	private static final int        sMaxFolderSize	        = 500;
	private static final String	sLiveFolder		= "./live/";
	private static final String	sArchFolder		= "./arch";
	private Lock			mLock			= new ReentrantLock();
	private String[]		mStationsToScan;

	public DeparturesScanner(String... iStationsToScan) {
		mStationsToScan = new String[iStationsToScan.length];
		mStationsToScan = iStationsToScan;
		File lLiveFolder = new File(sLiveFolder);
		if (!lLiveFolder.exists() && lLiveFolder.isDirectory()) {
			lLiveFolder.mkdir();
		}
	}

	/**
	 * Démarre l'aspiration des données ainsi que le thread de gestion des
	 * répertoires.
	 */
	public void start() {
		for (String tmpStation : mStationsToScan) {
			Thread lWriterThread = new Thread(createScanThread(tmpStation));
			lWriterThread.start();
		}
		startDaemonThread();
	}

	/**
	 * Créer un thread qui va aspirer les données pour la station fournie et les
	 * stocker dans le folder live.
	 * 
	 * @param iStation
	 * @return
	 */
	private Thread createScanThread(final String iStation) {
		final String lPath = sLiveFolder + iStation.replaceAll(":", "") + ".txt";
		return new Thread(new Runnable() {
			@Override
			public void run() {
				while (!sCancel) {
					File lLiveFolder = new File(lPath);
					mLock.lock();
					try {
						FileUtils.writeLines(lLiveFolder, SncfApiReader.getNextDepartures(iStation));
					} catch (IOException e) {
						e.printStackTrace();
					} finally {
						mLock.unlock();
					}
					try {
						Thread.sleep(60000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		});
	}

	/**
	 * Démarre un daemon qui va périodiquement scruter le folder live et le
	 * déplacer s'il devient trop gros.
	 */
	private void startDaemonThread() {
		Thread lDaemonThread = new Thread(new Runnable() {
			@Override
			public void run() {
				File lLiveFolder = new File(sLiveFolder);
				while (!sCancel) {
					if (lLiveFolder.length() > sMaxFolderSize) {
						mLock.lock();
						File lArchFolder = new File(sArchFolder + new SimpleDateFormat("yyyyMMddhhmm'.txt'").format(new Date()));
						System.out.println("La taille de fichier " + sLiveFolder + "a dépassé le max");
						try {
							FileUtils.copyFile(lLiveFolder, lArchFolder);
							FileUtils.deleteDirectory(lLiveFolder);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} finally {
							mLock.unlock();
						}
						try {
							// 5 minutes pour redémarer
							Thread.sleep(300000);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			}
		});
		lDaemonThread.start();
	}

	/**
	 * Arrete l'application et tous ses threads.
	 */
	public static void cancel() {
		sCancel = true;
	}

	public static void main(String[] args) {
		DeparturesScanner lScanner = new DeparturesScanner("OCE:SA:87391003", "OCE:SA:87723197");
		lScanner.start();
	}
}
