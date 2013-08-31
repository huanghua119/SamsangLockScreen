
package com.huanghua.samsanglockscreen;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.provider.CallLog.Calls;
import android.provider.ContactsContract;

import java.util.ArrayList;

public class MissCallUnReadObserver {

    private static final String TAG = "MissCallUnReadObserver";

    public static final Uri MISS_CALL_URI = Calls.CONTENT_URI;
    private static final String[] MISS_CALL_PROJECTION = new String[] {
            Calls._ID, Calls.NEW, Calls.DATE, Calls.NUMBER
    };
    private static final String MISS_CALL_SELECTION = "(" + Calls.NEW + " = ? AND " +
            Calls.TYPE + " = ? AND " + Calls.IS_READ + " = ? AND " + Calls.DATE + " >= ";
    private static final String[] MISS_CALL_SELECTION_ARGS = new String[] {
            "1", Integer.toString(Calls.MISSED_TYPE), Integer.toString(0)
    };

    public Context mContext;

    private ArrayList<String> mNumberOrNames;

    public MissCallUnReadObserver(Handler handler,
            long createTime) {
    }

    public void refreshUnReadNumber() {
        new AsyncTask<Void, Void, Integer>() {
            @Override
            public Integer doInBackground(Void... params) {
                Cursor cursor = mContext
                        .getContentResolver()
                        .query(MISS_CALL_URI, MISS_CALL_PROJECTION,
                                MISS_CALL_SELECTION + "" + " )", MISS_CALL_SELECTION_ARGS,
                                null);
                int count = 0;
                if (cursor != null) {
                    try {
                        count = cursor.getCount();
                        String number = cursor.getString(cursor.getColumnIndex(Calls.NUMBER));
                    } finally {
                        cursor.close();
                    }
                }
                return count;
            }

            @Override
            public void onPostExecute(Integer result) {
            }
        }.execute(null, null, null);
    }

    public String getContactPeople(String incomingNumber) {
        String result = incomingNumber;
        if (null != incomingNumber) {
            ContentResolver contentResolver = mContext.getContentResolver();
            Cursor cursor = null;
            String[] projection = new String[] {
                    ContactsContract.Contacts._ID, ContactsContract.Contacts.DISPLAY_NAME,
                    ContactsContract.CommonDataKinds.Phone.NUMBER

            };
            cursor = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    projection, ContactsContract.CommonDataKinds.Phone.NUMBER + "=?", new String[] {
                        incomingNumber
                    }, "");
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                result = cursor.getString(1);
            }
            if (cursor != null) {
                cursor.close();
            }
        }
        return result;
    }
}
