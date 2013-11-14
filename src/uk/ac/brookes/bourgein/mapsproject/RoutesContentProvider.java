package uk.ac.brookes.bourgein.mapsproject;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.net.Uri;

public class RoutesContentProvider extends ContentProvider {
	private static final String AUTHORITY = "uk.ac.brookes.bourgein.mapsproject.routes";
	public static final Uri CONTENT_URI = Uri.parse("content://"+AUTHORITY+"/routes");
	private SQLiteDatabase routesDB;
	private static final String ROUTES_TABLE = "routes";
	public static final String KEY_ID = "_id";
	public static final String KEY_USERID = "user_id";
	public static final String KEY_ROUTEDETAILS = "route_details";
	public static final int USERID_COLUMN = 1;
	public static final int ROUTEDETAILS_COLUMN = 2;
	
	private static final int ROUTES = 1;
	private static final int ROUTE_ID = 2;
	private static final UriMatcher uriMatcher;
	
	private static final String DATABASE_NAME = "routes.db";
	private static final int DATABASE_VERSION = 1;
	
	static {
		uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		uriMatcher.addURI(AUTHORITY, "routes", ROUTES);
		uriMatcher.addURI(AUTHORITY, "routes/#", ROUTE_ID);
	}
	
	@Override
	public int delete(Uri arg0, String arg1, String[] arg2) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getType(Uri uri) {
		switch (uriMatcher.match(uri)) {
		case ROUTES:
			return "vnd.android.cursor.dir/vnd.example.routes";
		case ROUTE_ID:
			return "vnd.android.cursor.item/vnd.example.routes";
		default:
			throw new IllegalArgumentException("Unsupported URI: " + uri);
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		long rowId = routesDB.insert(ROUTES_TABLE,null,values);
		if (rowId > 0){
			Uri newUri = ContentUris.withAppendedId(CONTENT_URI, rowId);
			getContext().getContentResolver().notifyChange(newUri, null);
			return uri;
		}
		else{
			throw new SQLException("Failed to insert row "+ rowId);
		}
	}

	@Override
	public boolean onCreate() {
		RoutesDatabaseHelper helper = new RoutesDatabaseHelper(this.getContext(), DATABASE_NAME, null, DATABASE_VERSION);
		this.routesDB = helper.getWritableDatabase();
		return (routesDB != null);
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		qb.setTables(ROUTES_TABLE);
		
		if (uriMatcher.match(uri) == ROUTE_ID){
			qb.appendWhere(KEY_ID + "=" + uri.getPathSegments().get(1));
		}
		
		Cursor c = qb.query(routesDB,projection,selection,selectionArgs,null,null,sortOrder);
		c.setNotificationUri(getContext().getContentResolver(), uri);
		
		return c;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	private static class RoutesDatabaseHelper extends SQLiteOpenHelper{
		public RoutesDatabaseHelper(Context context, String name,
				CursorFactory factory, int version) {
			super(context, name, factory, version);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL("CREATE TABLE " + ROUTES_TABLE + " (" + KEY_ID
					+ " INTEGER PRIMARY KEY AUTOINCREMENT, " + KEY_USERID
					+ " TEXT," + KEY_ROUTEDETAILS + " TEXT);");

		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			db.execSQL("DROP TABLE IF EXISTS " + ROUTES_TABLE);
			onCreate(db);
		}
	}

}
