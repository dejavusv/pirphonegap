package com.ecmxpert.pirphonegap;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import android.os.Environment;
import android.util.Log;

public class WriteFile {
	public void writeToFile(String data) {
		try {
			
			File folder = new File(Environment.getExternalStorageDirectory()
					+ "/PIRDetect");
			boolean success = true;
			if (!folder.exists()) {
				success = folder.mkdir();
			}
			
			// check folder if not found will create folder
			File logfile = new File(Environment.getExternalStorageDirectory()
					.getPath() + "/PIRDetect/LOG.txt");

			// write new data
			FileWriter out = new FileWriter(new File(Environment
					.getExternalStorageDirectory().getPath()
					+ "/PIRDetect/LOG.txt"), logfile.exists());
			out.write(data + "\r\n");
			out.write("");
			out.close();

		} catch (IOException e) {
			Log.e("Exception", "File write failed: " + e.toString());
		}
	}

}
