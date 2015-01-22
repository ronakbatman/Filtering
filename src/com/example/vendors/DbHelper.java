package com.example.vendors;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * @author Ronak
 * 
 */
public class DbHelper extends SQLiteOpenHelper {

	private static final String LOG = DbHelper.class.toString();
	private static final String DATABASE_NAME = "goods_service";
	private static final int DATABASE_VERSION = 1;
	public static final String TABLE_NAME_VENDORS = "vendors";
	private Context context;
	private SQLiteDatabase myDataBase;
	// The Android's default system path of your application database.
	String DB_PATH = null;

	public DbHelper(Context context) {

		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		this.context = context;
//		DB_PATH = "/data/data/" + context.getPackageName() + "/" + "databases/";
	}

	private static final String CREATE_VENDOR_TABLE = "CREATE TABLE IF NOT EXISTS "
			+ TABLE_NAME_VENDORS
			+ "("
			+ "_id INTEGER PRIMARY KEY AUTOINCREMENT,"
			+ "name TEXT NOT NULL,"
			+ "locality TEXT NOT NULL," + "category TEXT NOT NULL" + ");";

	@Override
	public void onCreate(SQLiteDatabase db) {
		try {
			db.execSQL(CREATE_VENDOR_TABLE);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

		Log.w(LOG, "Upgrading database from version " + oldVersion + " to "
				+ newVersion);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_VENDORS);
		onCreate(db);
	}

	public static void addRowInVendortable(SQLiteDatabase db, String name,
			String locality, String category) {

		ContentValues values = new ContentValues();
		values.put("name", name);
		values.put("locality", locality);
		values.put("category", category);
		try {
			db.insert(TABLE_NAME_VENDORS, null, values);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static Cursor retrieveAllEntries(SQLiteDatabase db) {
		try {

			String[] columns = { "_id", "name", "locality", "category" };

			Cursor cursor = db.query(TABLE_NAME_VENDORS, columns, null, null,
					null, null, null);
			if (cursor != null) {
				cursor.moveToFirst();
				return cursor;
			} else {
				return null;
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static Cursor retrieveEntriesByName(SQLiteDatabase db, String input) {

		try {
			String query = "select _id, name, locality, category from "
					+ TABLE_NAME_VENDORS + " where ( " + "lower(name) like "
					+ "lower('%" + input + "%') OR " + "lower(locality) like "
					+ "lower('%" + input + "%') OR " + "lower(category) like "
					+ "lower('%" + input + "%') )";

			Cursor cursor = db.rawQuery(query, null);
			if (cursor != null) {
				cursor.moveToFirst();
			} else {
				Log.d(LOG, "Cursor NuLL");
			}
			return cursor;

		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Check if the database already exist to avoid re-copying the file each
	 * time you open the application.
	 * 
	 * @return true if it exists, false if it doesn't
	 */
	private boolean checkDataBase() {

		SQLiteDatabase checkDB = null;

		try {
			String myPath = DB_PATH + DATABASE_NAME;
			checkDB = SQLiteDatabase.openDatabase(myPath, null,
					SQLiteDatabase.OPEN_READONLY);

		} catch (SQLiteException e) {

			// database does't exist yet.

		}

		if (checkDB != null) {

			checkDB.close();

		}

		return checkDB != null ? true : false;
	}

	/**
	 * Creates a empty database on the system and rewrites it with your own
	 * database.
	 * */
	public void createDataBase() throws IOException {

		boolean dbExist = checkDataBase();

		if (dbExist) {
			// do nothing - database already exist
		} else {

			// By calling this method and empty database will be created into
			// the default system path
			// of your application so we are gonna be able to overwrite that
			// database with our database.
			this.getReadableDatabase();

			try {
				copyDataBase();

			} catch (IOException e) {

				throw new Error("Error copying database");

			}
		}

	}

	/**
	 * Copies your database from your local assets-folder to the just created
	 * empty database in the system folder, from where it can be accessed and
	 * handled. This is done by transfering bytestream.
	 * */
	private void copyDataBase() throws IOException {

		// Open your local db as the input stream
		InputStream myInput = context.getAssets().open(DATABASE_NAME);

		// Path to the just created empty db
		String outFileName = DB_PATH + DATABASE_NAME;

		// Open the empty db as the output stream
		OutputStream myOutput = new FileOutputStream(outFileName);

		// transfer bytes from the inputfile to the outputfile
		byte[] buffer = new byte[1024];
		int length;
		while ((length = myInput.read(buffer)) > 0) {
			myOutput.write(buffer, 0, length);
		}

		// Close the streams
		myOutput.flush();
		myOutput.close();
		myInput.close();

	}

	public void openDataBase() throws SQLException {

		// Open the database
		String myPath = DB_PATH + DATABASE_NAME;
		myDataBase = SQLiteDatabase.openDatabase(myPath, null,
				SQLiteDatabase.OPEN_READONLY);

	}

	@Override
	public synchronized void close() {

		if (myDataBase != null)
			myDataBase.close();

		super.close();

	}

}
