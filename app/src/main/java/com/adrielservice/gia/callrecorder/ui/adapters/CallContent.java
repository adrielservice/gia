package com.adrielservice.gia.callrecorder.ui.adapters;

import com.adrielservice.gia.callrecorder.services.RecordService;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by dbeilis on 1/6/15.
 */
public class CallContent {

    /**
     * An array of sample (dummy) items.
     */
    public static List<Call> ITEMS = new ArrayList<Call>();

    /**
     * A map of sample (dummy) items, by ID.
     */
    public static Map<String, Call> ITEM_MAP = new HashMap<String, Call>();

    static {
        loadRecordingsFromDir();
    }

    public static void loadRecordingsFromDir() {
        ITEMS.clear();
        ITEM_MAP.clear();
        File dir = new File(RecordService.DEFAULT_STORAGE_LOCATION);
        String[] dList = dir.list();

        if (dList != null) {
            for (int i = 0; i < dList.length; i++) {
                addItem(new Call("" + i, dList[i]));
            }
        }
    }

    private static void addItem(Call item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.id, item);
    }

    /**
     * A dummy item representing a piece of content.
     */
    public static class Call {
        public String id;
        public String content;

        public Call(String id, String content) {
            this.id = id;
            this.content = content;
        }

        @Override
        public String toString() {
            return content;
        }
    }
}
