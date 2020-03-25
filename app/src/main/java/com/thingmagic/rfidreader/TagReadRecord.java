package com.thingmagic.rfidreader;

import com.thingmagic.Reader;
import com.thingmagic.ReaderUtil;
import com.thingmagic.TagProtocol;
import com.thingmagic.TagReadData;
import com.thingmagic.util.LoggerUtil;

import java.text.SimpleDateFormat;

public class TagReadRecord {
	private static final String TAG = "TagReadRecord";
	private int serialNo = 0;
//	String epcString;
//	Date timeStamp;
//	int antenna = 0;
	private int tagReadCount = 0;
//	int rssi = 0;
//	int frequency = 0;
//	int phase = 0;
//	Reader.GpioPin[] gpio = null;
//	TagProtocol readProtocol;
//	byte[] data;

	TagReadData rawRead = null;

	public TagReadRecord(TagReadData newRead) {
		rawRead = newRead;
		tagReadCount = newRead.getReadCount();
	}

	public int getSerialNo() {
		return serialNo;
	}

	public void setSerialNo(int serialNo) {
		this.serialNo = serialNo;
	}

	public String getEpcString() {
		return rawRead.epcString();
	}

	public long getTime() {
		return rawRead.getTime();
	}

	public String getTimeStamp()
	{
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		return formatter.format(rawRead.getTime());
	}

	public int getAntenna() {
		return rawRead.getAntenna();
	}

	public int getTagReadCount() {
		return tagReadCount;
	}

	public int getRssi() {
		return rawRead.getRssi();
	}

	public int getFrequency() {
		return rawRead.getFrequency();
	}

	public int getPhase() {
		return rawRead.getPhase();
	}

	public Reader.GpioPin[] getGpio() {
		return rawRead.getGpio();
	}

	public TagProtocol getReadProtocol() {
		return rawRead.getTag().getProtocol();
	}

	public String getData() {
		return ReaderUtil.byteArrayToHexString(rawRead.getData());
	}

	public TagReadData getRawRead() {
		return rawRead;
	}

	public void Update(TagReadData mergeData) {
		long timediff = System.currentTimeMillis() - mergeData.getTime();
		if(timediff > 0)
		{
			rawRead = mergeData;
		}
		else
		{
			tagReadCount += mergeData.getReadCount();
		}
	}
}
