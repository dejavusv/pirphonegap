package com.ecmxpert.pirphonegap;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.os.Environment;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import ioio.lib.api.DigitalInput;
import ioio.lib.api.DigitalOutput;
import ioio.lib.api.IOIO;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.BaseIOIOLooper;
import ioio.lib.util.IOIOLooper;
import ioio.lib.util.android.IOIOService;

public class PIRMotionService extends IOIOService {
	public int LedStatus = 0;
	public int DulationTime = 2;
	public int IntervalTime = 5;
	public int startupDulationTime = 0;
	public int IsDetect = 0;
	//sqliteHandler sqlite = new sqliteHandler(this);
	MediaPlayer mp;
	private Intent broadcastIntent = new Intent("returnIOIOdata");
	public int timetoggle = 0;

	@Override
	protected IOIOLooper createIOIOLooper() {

		return new BaseIOIOLooper() {
			// Declare digital input instance
			DigitalInput Di;
			//int lengthDetect = 0;
			//int lengthNoNDetect = 0;
			int state = 0;
			private DigitalOutput led_;
			int[] output = new int[100];
			int LoopCount = 0;
			int TimeCount = 0;
			int NoNDetectCount =0;
			int sensitive =90;

			@Override
			protected void setup() throws ConnectionLostException,
					InterruptedException {
				System.out.println("service setup");

				Di = ioio_.openDigitalInput(38);

			}

			@Override
			public void loop() throws ConnectionLostException,
					InterruptedException {

				if (LoopCount < (DulationTime * 10)) {
					output[LoopCount] = checkIO(Di.read());
					LoopCount++;
				} else {
					LoopCount = 0;
				}
				if(Di.read()){state = 1;}
				if(state == 1){
					TimeCount++;
				}
				System.out.println("TimeCount :"+TimeCount);
				if (IsDetect(output, DulationTime * 10, sensitive)) {
					if (TimeCount >= DulationTime * 10) {
						IsDetect = 1;
						broadcastVars();
						if (state == 1) {
							System.out.println("write file detect human");
					//		writeToFile("Detect Human Date :"
					//				+ getCurrentDate() + "Time :"
					//				+ getCurrentTime());
							state = 2;
							
						}

						if (!mp.isPlaying()) {
							mp.start();
						}
					}

				} else {
					//System.out.println("NoNDetectCount :" +NoNDetectCount);
					
					if(state == 2){
						NoNDetectCount++;
						TimeCount = 0;
					}
					if (NoNDetectCount >= IntervalTime * 10) {
						IsDetect = 0;
						broadcastVars();
						if (state == 2) {
							System.out.println("write file not detect human");
						//	writeToFile("not detect human :" + getCurrentDate()
						//			+ "Time :" + getCurrentTime());
							state = 0;
							
							NoNDetectCount =0;
							for(int i=0;i<output.length;i++){
								output[i] =0;
							}
						}
					}

				}
				

				/*
				  //detect motion human if (Di.read()) {
				  
				  lengthNoNDetect =0; lengthDetect += 1; if(lengthDetect >
				  (DulationTime * 10)){ IsDetect = 1; broadcastVars(); if(state
				  == 0){ System.out.println("write file detect human");
				  writeToFile("Detect Human Date :"+getCurrentDate()
				  +"Time :"+getCurrentTime()); state=1; }
				  
				  if(!mp.isPlaying()){ mp.start(); } }
				  
				  } else if (!Di.read()) {// not detect if(lengthDetect != 0){
				  System.out.println("detect : "+lengthDetect); lengthDetect
				  =0; IsDetect = 0; broadcastVars(); } if(state == 1){
				  lengthNoNDetect+=1; if(lengthNoNDetect > (IntervalTime*10)){
				  System.out.println("write file not detect human");
				  writeToFile("Human gone Date :"+getCurrentDate()
				 * +"Time :"+getCurrentTime()); state =0; } }
				 * 
				 * }
				 */
				// Delay time 100 milliseconds
				Thread.sleep(100);
				// System.out.println("broadcastVars ");

			}
		};
	}

	public boolean IsDetect(int[] data, int length, int sensitive) {
		int count = 0;
		for (int i = 0; i < length; i++) {
			if (data[i] == 1) {
				count++;
			}
		}
		System.out.println("count detect :" +count);
		if (count == 0) {
			return false;
		}
	//	System.out.println(length +","+sensitive);
	//	System.out.println( (float)length * ((100.0 - (float)sensitive) / 100.0));
		if (count > ((float)length * ((100.0 - (float)sensitive) / 100.0))) {
			return true;
		} else {
			return false;
		}

	}

	public int checkIO(boolean input) {
		if (input)
			return 1;
		else
			return 0;
	}

	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		LedStatus = intent.getIntExtra("ledstatus", -1);
		// System.out.println("LedStatus :" +LedStatus);
		// setup folder keep log file
		File folder = new File(Environment.getExternalStorageDirectory()
				+ "/PIRDetect");
		boolean success = true;
		if (!folder.exists()) {
			success = folder.mkdir();
		}

		// setup config in database
	//	if (sqlite.getRowdata() == 0) {
	//		sqlite.InsertData(2, 5);
	//	}

		// Create a media player for warning sound
		mp = MediaPlayer.create(getApplicationContext(), R.raw.ring);
		// Setup a method to receive messages broadcast from the IOIO plugin
		LocalBroadcastManager.getInstance(this).registerReceiver(mIOIOReceiver,
				new IntentFilter("msgIOIO"));

		//int[] configDetail = sqlite.SelectData();
		//DulationTime = configDetail[0];
		//IntervalTime = configDetail[1];

		broadcastIntent.putExtra("setinterval", String.valueOf(IntervalTime));
		broadcastIntent.putExtra("setdulation", String.valueOf(DulationTime));
		LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent);

		NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		if (intent != null && intent.getAction() != null
				&& intent.getAction().equals("stop")) {
			// User clicked the notification. Need to stop the service.
			nm.cancel(0);
			stopSelf();
		} else {
			// Service starting. Create a notification.
			Notification notification = new Notification(
					2, "IOIO service running",
					System.currentTimeMillis());
			notification
					.setLatestEventInfo(this, "IOIO Service", "Click to stop",
							PendingIntent.getService(this, 0, new Intent(
									"stop", null, this, this.getClass()), 0));
			notification.flags |= Notification.FLAG_ONGOING_EVENT;
			nm.notify(0, notification);
		}
	}

	// Receive message from the phonegap plugin
	private BroadcastReceiver mIOIOReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			// Received a message
			int tempinterval = IntervalTime;
			int tempDulation = DulationTime;
			System.out.println("service Received a message");
			System.out.println(intent.getStringExtra("dulation"));
			System.out.println(intent.getStringExtra("interval"));
			try {
				String command = intent.getStringExtra("command");
				int dulation = Integer.parseInt(intent
						.getStringExtra("dulation"));
				int interval = Integer.parseInt(intent
						.getStringExtra("interval"));
				if (command.equalsIgnoreCase("config")) {
					// set DulationTime
					System.out.println("set DulationTime : " + dulation);
					DulationTime = dulation;
					// set IntervalTime
					System.out.println("set IntervalTime : " + interval);
					IntervalTime = interval;

				//	long result = sqlite.updateConfig(dulation, interval);
				}
			} catch (Exception e) {
				System.out.println("setup error");
				IntervalTime = tempinterval;
				DulationTime = tempDulation;
			}

		}
	};

	// get present Date format : DD/MM/YYYY
	public String getCurrentDate() {
		Date d = new Date();
		String date = String.valueOf(d.getDate());
		String month = String.valueOf(d.getMonth() + 1);
		String year = String.valueOf(d.getYear() + 1900);
		String hour = String.valueOf(d.getHours());
		String minute = String.valueOf(d.getMinutes());
		if (month.length() < 2) {
			month = "0" + month;
		}
		if (date.length() < 2) {
			date = "0" + date;
		}
		if (hour.length() < 2) {
			hour = "0" + hour;
		}
		if (minute.length() < 2) {
			minute = "0" + minute;
		}
		return date + "/" + month + "/" + year;
	}

	// get present Time format : HH/MM/SS
	public String getCurrentTime() {
		Date d = new Date();
		String hour = String.valueOf(d.getHours());
		String minute = String.valueOf(d.getMinutes());
		String sec = String.valueOf(d.getSeconds());
		if (hour.length() == 1) {
			hour = "0" + hour;
		}
		if (minute.length() == 1) {
			minute = "0" + minute;
		}
		if (sec.length() == 1) {
			sec = "0" + sec;
		}
		return hour + ":" + minute + ":" + sec;
	}

	// write Log file detect motion human
	private void writeToFile(String data) {
		try {
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

	// Broadcast a message to the IOIO plugin
	private void broadcastVars() {

		// write var to send
		broadcastIntent.putExtra("setinterval", "");
		broadcastIntent.putExtra("setdulation", "");
		broadcastIntent.putExtra("PIRDetect", IsDetect);

		LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent);

	}

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

}
