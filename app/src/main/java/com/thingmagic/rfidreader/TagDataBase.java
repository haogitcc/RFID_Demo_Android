package com.thingmagic.rfidreader;

import com.thingmagic.TagReadData;
import com.thingmagic.util.LoggerUtil;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class TagDataBase {
    private static final String TAG = "TagDataBase";
    private static ConcurrentHashMap<String, TagReadRecord> epcIndex = new ConcurrentHashMap<String, TagReadRecord>();
    private static ConcurrentHashMap<Integer, String> index = new ConcurrentHashMap<Integer, String>();
    private static int UniqueTagCounts = 0;
    private static int TotalTagCounts = 0;

    public void Clear() {
        epcIndex.clear();
        index.clear();
        UniqueTagCounts = 0;
        TotalTagCounts = 0;
    }

    public void Add(TagReadData addData) {
        String key = null;
        key = addData.getTag().epcString();

        UniqueTagCounts = 0;
        TotalTagCounts = 0;

        if (!getEpcIndex().containsKey(key))
        {
            TagReadRecord value = new TagReadRecord(addData);
            value.setSerialNo(epcIndex.size() + 1);
            index.put(epcIndex.size() + 1, key);
            epcIndex.put(key, value);
        }
        else
        {
            epcIndex.get(key).Update(addData);
        }

        //Call this method to calculate total tag reads and unique tag read counts
        UpdateTagCountTextBox(epcIndex);
    }

    private void UpdateTagCountTextBox(ConcurrentHashMap<String, TagReadRecord> epcIndex) {
        UniqueTagCounts += epcIndex.size();
        TotalTagCounts = 0;
        for (TagReadRecord tagReadRecord : epcIndex.values())
        {
            TotalTagCounts += tagReadRecord.getTagReadCount();
        }
    }

    public static ConcurrentHashMap<String, TagReadRecord> getEpcIndex() {
        return epcIndex;
    }

    public static ConcurrentHashMap<Integer, String> getIndex() {
        return index;
    }

    public static int getUniqueTagCounts() {
        return UniqueTagCounts;
    }

    public static int getTotalTagCounts() {
        return TotalTagCounts;
    }
}
