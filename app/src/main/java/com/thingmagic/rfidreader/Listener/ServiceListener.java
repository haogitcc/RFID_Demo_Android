package com.thingmagic.rfidreader.Listener;

import android.content.Context;
import android.os.AsyncTask;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.thingmagic.ReadExceptionListener;
import com.thingmagic.ReadListener;
import com.thingmagic.Reader;
import com.thingmagic.ReaderException;
import com.thingmagic.SimpleReadPlan;
import com.thingmagic.TMConstants;
import com.thingmagic.TagProtocol;
import com.thingmagic.TagReadData;

import com.thingmagic.R;
import com.thingmagic.rfidreader.activities.ReaderActivity;
import com.thingmagic.rfidreader.model.TagDataBase;
import com.thingmagic.rfidreader.model.TagReadRecord;
import com.thingmagic.rfidreader.services.SettingsService;
import com.thingmagic.util.LoggerUtil;
import com.thingmagic.util.Utilities;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class ServiceListener implements View.OnClickListener {

	private static String TAG = "ServiceListener";
	private static EditText ntReaderField;
	private static EditText customReaderField;
	private static Spinner serialList = null;
	private static LinearLayout servicelayout;
	private static RadioGroup readerRadioGroup = null;
	private static RadioButton serialReaderRadioButton = null;
	private static RadioButton networkReaderRadioButton = null;
	private static RadioButton customReaderRadioButton = null;
	private static RadioButton syncReadRadioButton = null;
	private static RadioButton asyncReadSearchRadioButton = null;
	private static Button readButton = null;
	private static Button connectButton = null;
	private static TextView searchResultCount = null;
	private static TextView readTimeView = null;
	private static TextView totalTagCountView = null;

	private static TextView unique_tag_count = null;
	private static TextView total_tag_count = null;
	private static TextView time_taken = null;

	private static TextView rowNumberLabelView = null;
	private static TextView epcEpcLabelView = null;
	private static TextView epcAntLabelView = null;
	private static TextView epcCountLabelView = null;
	private static TextView epcDataLabelView = null;
	private static TextView epcTimeLabelView = null;

	private static LinearLayout antLayout = null;
	private static CheckBox ant1CheckBox = null;
	private static CheckBox ant2CheckBox = null;
	private static CheckBox ant3CheckBox = null;
	private static CheckBox ant4CheckBox = null;

	private static ProgressBar progressBar = null;
	private static int redColor = 0xffff0000;
	private static int textColor = 0xff000000;
	private static ReadThread readThread;
	private static TableLayout table;
	private static LayoutInflater inflater;
	static TagDataBase tagdb = new TagDataBase();
	private static ArrayList<String> addedEPCRecords = new ArrayList<String>();

	private static long queryStartTime = 0;
	private static long queryStopTime = 0;

	private static ReaderActivity mReaderActivity;
	private static SettingsService mSettingsService;
	private static Timer timer = new Timer();

	public ServiceListener(ReaderActivity readerActivity) {
		mReaderActivity = readerActivity;
		mSettingsService = new SettingsService(mReaderActivity);
		findAllViewsById();
		initData();
	}

	private void findAllViewsById() {
		syncReadRadioButton = (RadioButton) mReaderActivity.findViewById(R.id.SyncRead_radio_button);
		asyncReadSearchRadioButton = (RadioButton) mReaderActivity.findViewById(R.id.AsyncRead_radio_button);
		readButton = (Button) mReaderActivity.findViewById(R.id.Read_button);
		connectButton = (Button) mReaderActivity.findViewById(R.id.Connect_button);
		searchResultCount = (TextView) mReaderActivity.findViewById(R.id.search_result_view);
		totalTagCountView = (TextView) mReaderActivity.findViewById(R.id.totalTagCount_view);
		progressBar = (ProgressBar) mReaderActivity.findViewById(R.id.progressbar);
		textColor = searchResultCount.getTextColors().getDefaultColor();
		table = (TableLayout) mReaderActivity.findViewById(R.id.tablelayout);
		inflater = (LayoutInflater) mReaderActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		ntReaderField = (EditText) mReaderActivity.findViewById(R.id.search_edit_text);
		customReaderField = (EditText) mReaderActivity.findViewById(R.id.custom_reader_field);
		serialList = (Spinner) mReaderActivity.findViewById(R.id.SerialList);
		serialReaderRadioButton = (RadioButton) mReaderActivity.findViewById(R.id.SerialReader_radio_button);
		networkReaderRadioButton = (RadioButton) mReaderActivity.findViewById(R.id.NetworkReader_radio_button);
		customReaderRadioButton = (RadioButton) mReaderActivity.findViewById(R.id.CustomReader_radio_button);
		servicelayout = (LinearLayout) mReaderActivity.findViewById(R.id.ServiceLayout);
		readerRadioGroup = (RadioGroup) mReaderActivity.findViewById(R.id.Reader_radio_group);
		readTimeView = (TextView) mReaderActivity.readOptions.findViewById(R.id.read_time_value);
		unique_tag_count = (TextView) mReaderActivity.performance_metrics.findViewById(R.id.unique_tag_count);
		total_tag_count = (TextView) mReaderActivity.performance_metrics.findViewById(R.id.total_tag_count);
		time_taken = (TextView) mReaderActivity.performance_metrics.findViewById(R.id.time_taken);
		rowNumberLabelView = (TextView) mReaderActivity.findViewById(R.id.NumberLabel);
		epcEpcLabelView = (TextView) mReaderActivity.findViewById(R.id.EPCLabel);
		epcAntLabelView = (TextView) mReaderActivity.findViewById(R.id.AntennaLabel);
		epcCountLabelView = (TextView) mReaderActivity.findViewById(R.id.ReadConutLabel);
		epcDataLabelView = (TextView) mReaderActivity.findViewById(R.id.DataLabel);
		epcTimeLabelView = (TextView) mReaderActivity.findViewById(R.id.TimeStampLabel);

		antLayout = (LinearLayout) mReaderActivity.findViewById(R.id.ant_layout);
		ant1CheckBox = (CheckBox) mReaderActivity.findViewById(R.id.ant1_cbx);
		ant2CheckBox = (CheckBox) mReaderActivity.findViewById(R.id.ant2_cbx);
		ant3CheckBox = (CheckBox) mReaderActivity.findViewById(R.id.ant3_cbx);
		ant4CheckBox = (CheckBox) mReaderActivity.findViewById(R.id.ant4_cbx);
	}

	private void initData() {

	}

	@Override
	public void onClick(View arg0) {
		try {
			String operation = "";
			if (syncReadRadioButton.isChecked()) {
				operation = "syncRead";
				String readTimout = readTimeView.getText().toString();
				if (!Utilities.validateReadTimeout(searchResultCount, readTimout)) {
					return;
				}
				readButton.setText("Reading");
				readButton.setClickable(false);
			}
			else if (asyncReadSearchRadioButton.isChecked()) {
				operation = "asyncRead";
			}
			
			if (readButton.getText().equals("Stop Reading")) {
				readThread.setReading(false);
				readButton.setText("Stopping...");
				readButton.setClickable(false);
			} else if (readButton.getText().equals("Start Reading") || readButton.getText().equals("Reading")) {
				if (readButton.getText().equals("Start Reading")) {
					readButton.setText("Stop Reading");
				}
				clearTagRecords();

				//设置天线 针对手持设备的
				String readerModel=(String) mReaderActivity.reader.paramGet("/reader/version/model");
				if(readerModel.equalsIgnoreCase("M6e Micro") || readerModel.equalsIgnoreCase("sargas")) {
					SimpleReadPlan  simplePlan = new SimpleReadPlan(new int[] {1,2}, TagProtocol.GEN2);
					mReaderActivity.reader.paramSet("/reader/read/plan", simplePlan);
				}
				else if(readerModel.equalsIgnoreCase("M6e Nano")) {
					SimpleReadPlan  simplePlan = new SimpleReadPlan(new int[] {1}, TagProtocol.GEN2);
					mReaderActivity.reader.paramSet("/reader/read/plan", simplePlan);
				}
				else { //其他设备
					if(antLayout.getVisibility() == View.VISIBLE)
					{
						ArrayList<Integer> ant = GetSelectedAntennaList();
						int[] antennaList = new int[ant.size()];
						for(int i = 0;i<ant.size();i++){
							antennaList[i] = ant.get(i);
							Log.d(TAG, "ant="+antennaList[i]);
						}
						SimpleReadPlan plan = new SimpleReadPlan(antennaList, TagProtocol.GEN2, null, null, 1000);
						mReaderActivity.reader.paramSet(TMConstants.TMR_PARAM_READ_PLAN, plan);
						LoggerUtil.debug(TAG, "set read plan 3 success");
					}
				}

				readThread = new ReadThread(mReaderActivity.reader, operation);
				readThread.execute();
			}
		} catch (Exception ex) {
			LoggerUtil.error(TAG, "Exception", ex);
		}
	}

	private ArrayList<Integer> GetSelectedAntennaList() {
		ArrayList<Integer> ant = new ArrayList<Integer>();
		CheckBox cbs[] = new CheckBox[] { ant1CheckBox, ant2CheckBox, ant3CheckBox, ant4CheckBox };
		for (int antIdx = 0; antIdx < cbs.length; antIdx++)
		{
			CheckBox cb = cbs[antIdx];
			if(cb.isEnabled() && cb.isChecked())
			{
				int antNum = antIdx + 1;
				ant.add(antNum);
			}
		}
		return ant;
	}

	public OnClickListener clearListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			clearTagRecords();
		}
	};

	public static void clearTagRecords() {
		tagdb.Clear();
		addedEPCRecords.clear();
		table.removeAllViews();
		searchResultCount.setText("");
		totalTagCountView.setText("");
		queryStartTime = System.currentTimeMillis();
	}

	public static class ReadThread extends AsyncTask<Void, Integer, TagDataBase> {

		private String operation;
		private static boolean exceptionOccur = false;
		private static String exception = "";
		private static boolean reading = true;
		private static Reader mReader;
		private static TableRow fullRow = null;
		private static TextView epcNumbuer = null;
		private static TextView epcEPC = null;
		private static TextView epcData = null;
		private static TextView epcAntenna = null;
		private static TextView epcTime = null;
		private static TextView epcReadCount = null;

		private static boolean isEmbeddedRead = false;
		private static double timeTaken;
		private static long startTime;
		static ReadExceptionListener exceptionListener = new TagReadExceptionReceiver();
		static ReadListener readListener = new PrintListener();

		public ReadThread(Reader reader, String operation) {
			this.operation = operation;
			mReader = reader;
		}

		@Override
		protected void onPreExecute() {
			startTime = System.currentTimeMillis();
			clearTagRecords();
			syncReadRadioButton.setClickable(false);
			asyncReadSearchRadioButton.setClickable(false);
			connectButton.setEnabled(false);
			connectButton.setClickable(false);
			searchResultCount.setTextColor(textColor);
			searchResultCount.setText("Reading Tags....");
			progressBar.setVisibility(View.VISIBLE);
			tagdb = new TagDataBase();
			exceptionOccur = false;
		}

		@Override
		protected TagDataBase doInBackground(
				Void... params) {
			try {
				//mSettingsService.loadReadPlan(mReader);
				if (operation.equalsIgnoreCase("syncRead")) {
					int timeOut = Integer.parseInt(readTimeView.getText().toString());
					TagReadData[] tagReads = mReader.read(timeOut);
					queryStopTime = System.currentTimeMillis();
					for (TagReadData tr : tagReads) {
						parseTag(tr);
					}
					publishProgress(0);
				}
				else { // Async Read
					setReading(true);
					mReader.addReadExceptionListener(exceptionListener);
					mReader.addReadListener(readListener);
					mReader.startReading();
					queryStartTime = System.currentTimeMillis();
					refreshReadRate();
					while (isReading()) {
						/* Waiting till stop reading button is pressed */
						Thread.sleep(5);
					}
					queryStopTime = System.currentTimeMillis();
					if(!exceptionOccur)
					{
						mReader.stopReading();
						mReader.removeReadListener(readListener);
						mReader.removeReadExceptionListener(exceptionListener);
					}
				}
			} catch (Exception ex) {
				exception = ex.getMessage();
				exceptionOccur = true;
				LoggerUtil.error(TAG, "Exception while reading :", ex);
			}
			return tagdb;
		}

		static class PrintListener implements ReadListener {
			public void tagRead(Reader r, final TagReadData tr) {
				readThread.parseTag(tr);
			}
		}

		private void parseTag(TagReadData tr) {
			tagdb.Add(tr);
		}

		// private static int connectionLostCount=0;
		static class TagReadExceptionReceiver implements ReadExceptionListener {
			public void tagReadException(Reader r, ReaderException re) {
				if (re.getMessage().contains("The module has detected high return loss")
						|| re.getMessage().contains("Tag ID buffer full") 
						|| re.getMessage().contains("No tags found")) {
					// exception = "No connected antennas found";
					/* Continue reading */
				}
				// else if(re.getMessage().equals("Connection Lost"))
				// {
				// if(connectionLostCount == 3){
				// connectionLostCount = 0;
				// try {
				// r.connect();
				// } catch (Exception e) {
				// // TODO Auto-generated catch block
				// e.printStackTrace();
				// exception=re.getMessage();
				// exceptionOccur = true;
				// readThread.setReading(false);
				// readThread.publishProgress(-1);
				// }
				// }
				// connectionLostCount++;
				// }
				else {
					Log.e(TAG, "Reader exception : ", re);
					exception = re.getMessage();
					exceptionOccur = true;
					readThread.setReading(false);
					readThread.publishProgress(-1);
				}
			}
		}

		private void refreshReadRate() {
			timer = new Timer();
			timer.schedule( new TimerTask() {
				@Override
				public void run() {
					publishProgress(0);
				}
			}, 100, 300);
		}

		// private static void calculateReadrate()
		// {
		// long readRatePerSec = 0;
		// long elapsedTime = (System.currentTimeMillis() - queryStartTime) ;
		// if(!isReading()){
		// elapsedTime = queryStopTime- queryStartTime;
		// }
		//
		// long tagReadTime = elapsedTime/ 1000;
		// if(tagReadTime == 0)
		// {
		// readRatePerSec = (long) ((totalTagCount) / ((double) elapsedTime /
		// 1000));
		// }
		// else
		// {
		// readRatePerSec = (long) ((totalTagCount) / (tagReadTime));
		// }
		// }

		@Override
		protected void onProgressUpdate(Integer... progress) {
			int progressToken = progress[0];
			if (progressToken == -1) {
				searchResultCount.setTextColor(redColor);
				searchResultCount.setText("ERROR :" + exception);
				totalTagCountView.setText("");
			} else {
				populateSearchResult(tagdb);
				if (!exceptionOccur && tagdb.getTotalTagCounts() > 0) {
					searchResultCount.setTextColor(textColor);
					searchResultCount.setText(Html.fromHtml("<b>Unique Tags :</b> " + tagdb.getUniqueTagCounts()));
					totalTagCountView.setText(Html.fromHtml("<b>Total Tags  :</b> " + tagdb.getTotalTagCounts()));
				}
			}
		}

		@Override
		protected void onPostExecute(TagDataBase tagdb) {
			timer.cancel();
			if (exceptionOccur) {
				searchResultCount.setTextColor(redColor);
				searchResultCount.setText("ERROR :" + exception);
				totalTagCountView.setText("");
				if (tagdb.getTotalTagCounts() > 0 && !operation.equalsIgnoreCase("syncRead")) {
					if(exception.length() > 20)
					{
						totalTagCountView.setText(Html.fromHtml("<br>"));
					}
					totalTagCountView.setText(Html.fromHtml("<b>Total Tags  :</b> " + tagdb.getTotalTagCounts()));
					}
			} else {
				populateSearchResult(tagdb);
				searchResultCount.setText(Html.fromHtml("<b>Unique Tags :</b> " + tagdb.getUniqueTagCounts()));
				totalTagCountView.setText(Html.fromHtml("<b>Total Tags  :</b> " + tagdb.getTotalTagCounts()));
//
				long elapsedTime = queryStopTime - queryStartTime;
				double timeTaken = (double) ((tagdb.getTotalTagCounts()) / ((double) elapsedTime / 1000));
				DecimalFormat df = new DecimalFormat("#.##");
				time_taken.setText(df.format(timeTaken) + " sec");
			}
			progressBar.setVisibility(View.INVISIBLE);
			readButton.setClickable(true);
			if (operation.equalsIgnoreCase("AsyncRead")) {
				readButton.setText("Start Reading");
			} else if (operation.equalsIgnoreCase("SyncRead")) {
				readButton.setText("Read");
			}
			readButton.setClickable(true);
			syncReadRadioButton.setClickable(true);
			asyncReadSearchRadioButton.setClickable(true);
			connectButton.setClickable(true);
			connectButton.setEnabled(true);
			if (exceptionOccur && !exception.equalsIgnoreCase("No connected antennas found")) {
				disconnectReader();
			}
		}

		private static void disconnectReader() {
			ntReaderField.setEnabled(true);
			serialList.setEnabled(true);
			customReaderField.setEnabled(true);
			serialReaderRadioButton.setClickable(true);
			networkReaderRadioButton.setClickable(true);
			customReaderRadioButton.setClickable(true);
			connectButton.setText("Connect");
			servicelayout.setVisibility(View.GONE);
			readerRadioGroup.setVisibility(View.VISIBLE);
			mReaderActivity.reader = null;
			if(!exceptionOccur)
			{
				searchResultCount.setText("");
				total_tag_count.setText("");
			}
		}

		private static void populateSearchResult(TagDataBase tagdb) {
//			LoggerUtil.debug(TAG, "populateSearchResult");
			try {
				for(int index : tagdb.getIndex().keySet())
				{
					String key = tagdb.getIndex().get(index);
					TagReadRecord value = tagdb.getEpcIndex().get(key);
					if(!addedEPCRecords.contains(key))
					{
						addedEPCRecords.add(key);
						AddRow(value);
					}
					else
					{
						UpdateRow(value);
					}
				}
			} catch (Exception ex) {
				LoggerUtil.error(TAG, "Exception while populating tags :", ex);
			}
		}

		private static void UpdateRow(TagReadRecord tagRecordData) {
			int row = addedEPCRecords.indexOf(tagRecordData.getEpcString());
			fullRow = (TableRow) table.getChildAt(row);
			if (fullRow != null) {
				//ToDo 具体参考row.xml
				int readCountColumn = 7;
				epcReadCount = (TextView) fullRow.getChildAt(readCountColumn);
				if (epcReadCount != null && Integer.valueOf(epcReadCount.getText().toString()).intValue() != tagRecordData.getTagReadCount()) {
					epcReadCount.setText(String.valueOf(tagRecordData.getTagReadCount()));
				}
			}
		}

		private static void AddRow(TagReadRecord tagRecordData) {
			if (inflater != null) {
				fullRow = (TableRow) inflater.inflate(R.layout.row, null, true);
				fullRow.setId(tagdb.getUniqueTagCounts());
				if (fullRow != null) {
					epcNumbuer = (TextView) fullRow.findViewById(R.id.Number);
					if (epcNumbuer != null) {
						epcNumbuer.setText(String.valueOf(tagRecordData.getSerialNo()));
						epcNumbuer.setWidth(rowNumberLabelView.getWidth());

						epcEPC = (TextView) fullRow.findViewById(R.id.EPC);
						if (epcEPC != null) {
							epcEPC.setText(tagRecordData.getEpcString());
							epcEPC.setWidth(epcEpcLabelView.getWidth());

							epcAntenna = (TextView) fullRow.findViewById(R.id.Antenna);
							if (epcAntenna != null) {
								epcAntenna.setText(String.valueOf(tagRecordData.getAntenna()));
								epcAntenna.setWidth(epcAntLabelView.getWidth());
							}

							epcTime = (TextView) fullRow.findViewById(R.id.Time);
							if (epcTime != null) {
								epcTime.setText(String.valueOf(tagRecordData.getTimeStamp()));
								epcTime.setWidth(epcTimeLabelView.getWidth());
							}

							epcReadCount = (TextView) fullRow.findViewById(R.id.ReadCount);
							if (epcReadCount != null) {
								epcReadCount.setText(String.valueOf(tagRecordData.getTagReadCount()));
								epcReadCount.setWidth(epcCountLabelView.getWidth());
							}

							if (isEmbeddedRead) {
								epcData = (TextView) fullRow.findViewById(R.id.Data);
								if (epcData != null) {
									epcData.setVisibility(View.VISIBLE);
									epcData.setText(String.valueOf(tagRecordData.getData()));
									epcData.setWidth(epcDataLabelView.getWidth());
								}
							}
							table.addView(fullRow);
						}
					}
				}
			}
		}

		public static boolean isReading() {
			return reading;
		}

		public void setReading(boolean reading) {
			ReadThread.reading = reading;
		}
	}
}
