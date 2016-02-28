package com.vomtom.refugeebuddy.contracts;

import android.provider.BaseColumns;

/**
 * Created by Thomas on 20.02.2016.
 */
public class LocationContract {
	private static final String TYPE_TEXT     = " TEXT";
	private static final String TYPE_DATETIME = " DATETIME";
	private static final String TYPE_INTEGER  = " INTEGER";
	private static final String COMMA_SEP     = ",";

	// To prevent someone from accidentally instantiating the contract class,
	// give it an empty constructor.
	public LocationContract() {
	}

	/* Inner class that defines the table contents */
	public static abstract class LocationEntry implements BaseColumns {
		public static final String TABLE_NAME              = "locations";
		public static final String COLUMN_NAME_LNG         = "lng";
		public static final String COLUMN_NAME_LAT         = "lat";
		public static final String COLUMN_NAME_ACCURACY    = "accuracy";
		public static final String COLUMN_NAME_ALTITUDE    = "altitude";
		public static final String COLUMN_NAME_BEARING     = "bearing";
		public static final String COLUMN_NAME_SPEED       = "speed";
		public static final String COLUMN_NAME_TIME        = "time";
		public static final String COLUMN_NAME_ADDRESS     = "address";
		public static final String COLUMN_NAME_TRANSFERRED = "is_transferred";
		public static final String COLUMN_NAME_SERVERID    = "serverid_idfk";
		public static final String COLUMN_NAME_CREATED     = "created";


		public static final String SQL_CREATE =
				"CREATE TABLE " + LocationEntry.TABLE_NAME + " (" +
				LocationEntry._ID + TYPE_INTEGER + " PRIMARY KEY" + COMMA_SEP +
				LocationEntry.COLUMN_NAME_LAT + TYPE_TEXT + COMMA_SEP +
				LocationEntry.COLUMN_NAME_LNG + TYPE_TEXT + COMMA_SEP +
				LocationEntry.COLUMN_NAME_ACCURACY + TYPE_TEXT + COMMA_SEP +
				LocationEntry.COLUMN_NAME_ALTITUDE + TYPE_TEXT + COMMA_SEP +
				LocationEntry.COLUMN_NAME_BEARING + TYPE_TEXT + COMMA_SEP +
				LocationEntry.COLUMN_NAME_SPEED + TYPE_TEXT + COMMA_SEP +
				LocationEntry.COLUMN_NAME_TIME + TYPE_TEXT + COMMA_SEP +
				LocationEntry.COLUMN_NAME_ADDRESS + TYPE_TEXT + COMMA_SEP +
				LocationEntry.COLUMN_NAME_TRANSFERRED + TYPE_INTEGER + COMMA_SEP +
				LocationEntry.COLUMN_NAME_SERVERID + TYPE_INTEGER + COMMA_SEP +
				LocationEntry.COLUMN_NAME_CREATED + TYPE_DATETIME + " DEFAULT CURRENT_TIMESTAMP " + COMMA_SEP +
				" FOREIGN KEY (" + LocationEntry.COLUMN_NAME_SERVERID + ") REFERENCES " + ServeridEntry.TABLE_NAME + "(" + ServeridEntry._ID + ")" +
				" )";

		public static final String SQL_DELETE =
				"DROP TABLE IF EXISTS " + LocationEntry.TABLE_NAME;


	}

	/* Inner class that defines the table contents */
	public static abstract class ServeridEntry implements BaseColumns {
		public static final String TABLE_NAME            = "serverid";
		public static final String COLUMN_NAME_SERVERID  = "serverid_id";
		public static final String COLUMN_NAME_TIME      = "time";
		public static final String COLUMN_NAME_SERVERKEY = "key";

		public static final String SQL_CREATE =
				"CREATE TABLE " + LocationContract.ServeridEntry.TABLE_NAME + " (" +
				LocationContract.ServeridEntry._ID + TYPE_INTEGER + " PRIMARY KEY" + COMMA_SEP +
				LocationContract.ServeridEntry.COLUMN_NAME_SERVERID + TYPE_TEXT + COMMA_SEP +
				LocationContract.ServeridEntry.COLUMN_NAME_SERVERKEY + TYPE_TEXT + COMMA_SEP +
				LocationContract.ServeridEntry.COLUMN_NAME_TIME + TYPE_DATETIME +
				" )";
		public static final String SQL_DELETE =
				"DROP TABLE IF EXISTS " + LocationContract.ServeridEntry.TABLE_NAME;
	}


}
