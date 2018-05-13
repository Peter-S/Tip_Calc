package com.murach.tipcalculator;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.security.PublicKey;
import java.util.ArrayList;

public class TipCalcDB {

    public static final String  DB_NAME = "tipcalc.db";
    public static final int     DB_VERSION = 1;

    public static final String  TC_TABLE = "tc";

    public static final String  TC_ID = "_id";
    public static final int     TC_ID_COL = 0;

    public static final String  TC_BILLDATE = "bill_date";
    public static final int     TC_BILLDATE_COL = 1;

    public static final String  TC_BILLAMOUNT = "bill_amount";
    public static final int     TC_BILLAMOUNT_COL = 2;

    public static final String  TC_TIPPERCENT = "tip_percent";
    public static final int     TC_TIPPERCENT_COL = 3;

    public static final String  CREATE_TC_TABLE =
            "CREATE TABLE " + TC_TABLE + " (" +
                    TC_ID           + " INTEGER NOT NULL, " +
                    TC_BILLDATE     + " INTEGER NOT NULL, " +
                    TC_BILLAMOUNT   + " REAL, " +
                    TC_TIPPERCENT   + " REAL);";

    public static final String  DROP_TC_TABLE =
            "DROP TABLE IF EXISTS " + TC_TABLE;

    public static class DBHelper extends SQLiteOpenHelper {

        public DBHelper (Context context, String name,
                         SQLiteDatabase.CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(CREATE_TC_TABLE);

            db.execSQL("INSERT INTO tc VALUES (1, 20180101, 22.55, 20.0)");
            db.execSQL("INSERT INTO tc VALUES (2, 20180202, 10.00, 10.0)");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.d("TC ",  "Upgrading db from version " +
            oldVersion + "to " + newVersion);

            db.execSQL(TipCalcDB.DROP_TC_TABLE);
            onCreate(db);
        }
    }

    private SQLiteDatabase db;
    private DBHelper dbHelper;

    public TipCalcDB (Context context) {
        dbHelper = new DBHelper(context, DB_NAME, null, DB_VERSION);
    }

    private void openReadableDB() {
        db = dbHelper.getReadableDatabase();
    }

    private void openWriteableDB() {
        db = dbHelper.getWritableDatabase();
    }

    private void closeDB() {
        if (db != null)
            db.close();
    }

    public ArrayList<Tip> getTips() {
        ArrayList<Tip> tips = new ArrayList<>();
        openReadableDB();
        Cursor cursor = db.query(TC_TABLE, null, null,
                null, null, null, null);
        while (cursor.moveToNext()) {
            Tip tip = new Tip();
            tip.setId(cursor.getInt(TC_ID_COL));

            tips.add(tip);
        }

        if (cursor != null)
            cursor.close();
        closeDB();

        return tips;
    }

    public Tip getTip (String id) {
        String where = TC_ID + "= ?";
        String[] whereArgs = { id };

        openReadableDB();
        Cursor cursor = db.query(TC_TABLE, null, where, whereArgs,
                null, null, null);
        Tip tip = null;
        cursor.moveToFirst();
        tip = new Tip(cursor.getInt(TC_ID_COL), cursor.getInt(TC_BILLDATE_COL),
                cursor.getInt(TC_BILLAMOUNT_COL), cursor.getInt(TC_TIPPERCENT_COL));
        if (cursor != null)
            cursor.close();
        this.closeDB();

        return tip;
    }

    public ArrayList<Tip> getTips(String tipName) {
        String where = TC_ID + "= ?";
        int TCID = (int) getTip(tipName).getId();
        String[] whereArgs = { Integer.toString(TCID)};

        this.openReadableDB();
        Cursor cursor = db.query(TC_TABLE, null, where, whereArgs,
                null, null, null);
        ArrayList<Tip> tips = new ArrayList<>();
        while (cursor.moveToNext()) {
            tips.add(getTaskFromCursor(cursor));
        }

        if (cursor != null)
            cursor.close();
        this.closeDB();

        return tips;
    }

    public Tip getTip (int id) {
        String where = TC_ID + "= ?";
        String[] whereArgs = { Integer.toString(id)};

        this.openReadableDB();
        Cursor cursor = db.query(TC_TABLE, null, where, whereArgs,
                null, null, null);
        cursor.moveToFirst();
        Tip tip = getTaskFromCursor(cursor);
        if (cursor != null)
            cursor.close();
        this.closeDB();

        return tip;
    }

    private static Tip getTaskFromCursor(Cursor cursor) {
        if (cursor == null || cursor.getCount() == 0) {
            return null;
        } else {
            try {
                Tip tip = new Tip(
                        cursor.getInt(TC_ID_COL),
                        cursor.getInt(TC_BILLDATE_COL),
                        cursor.getInt(TC_BILLAMOUNT_COL),
                        cursor.getInt(TC_TIPPERCENT_COL));
                return tip;
            } catch (Exception e) {
                return null;
            }
        }
    }

    public long insertTip(Tip tip) {
        ContentValues cv = new ContentValues();
        cv.put(TC_ID, tip.getId());
        cv.put(TC_BILLDATE, tip.getDateStringFormatted());
        cv.put(TC_BILLAMOUNT, tip.getBillAmountFormatted());
        cv.put(TC_TIPPERCENT, tip.getGetTipPercentFormatted());

        this.openWriteableDB();
        long rowID = db.insert(TC_TABLE, null, cv);
        this.closeDB();

        return rowID;
    }




}
