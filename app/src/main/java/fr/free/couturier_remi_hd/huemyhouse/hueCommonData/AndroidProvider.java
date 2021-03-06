package fr.free.couturier_remi_hd.huemyhouse.hueCommonData;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.util.Log;

/**
 * Created by rcouturi on 15/11/2015.
 */
public class AndroidProvider extends ContentProvider {

    // URI du content provider
    public static final String AUTHORITY = "fr.free.couturier_remi_hd.huemyhouse.provider";
    public static final Uri    CONTENT_URI_BRIDGE            = Uri.parse("content://" + AUTHORITY + "/bridge");
    public static final Uri    CONTENT_URI_LIGHT             = Uri.parse("content://" + AUTHORITY + "/light");

    public static final String CONTENT_PROVIDER_DB_NAME      = "HueMyHouse.db";                // Nom de la base de données
    public static final int    CONTENT_PROVIDER_DB_VERSION   = 1;                              // Version de la base de données
    public static final String CONTENT_PROVIDER_TABLE_BRIDGE = "hueBridge";                    // Nom de la table des ponts philips
    public static final String CONTENT_PROVIDER_TABLE_LIGHT  = "hueLight";                     // Nom de la table des ampoules

    public static final int    BRIDGE_LIST                   = 1;                              // For UriMatcher
    public static final int    BRIDGE_ID                     = 2;
    public static final int    LIGHT_LIST                    = 3;
    public static final int    LIGHT_ID                      = 4;

    public static final String      TAG                      = "[HueMyHouse][Provider]";
    private DatabaseHelper          dbHelper;
    private static final UriMatcher uriMatcher;

    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(AUTHORITY, "bridge",   BRIDGE_LIST);
        uriMatcher.addURI(AUTHORITY, "bridge/#", BRIDGE_ID);
        uriMatcher.addURI(AUTHORITY, "light",    LIGHT_LIST);
        uriMatcher.addURI(AUTHORITY, "light/#",  LIGHT_ID);
    }

    @Override
    public boolean onCreate() {
        dbHelper = new DatabaseHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;
        switch (uriMatcher.match(uri)){
            case BRIDGE_LIST:
                cursor = db.query(CONTENT_PROVIDER_TABLE_BRIDGE, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case LIGHT_LIST:
                cursor = db.query(CONTENT_PROVIDER_TABLE_LIGHT, projection, selection, selectionArgs, null, null, sortOrder);
                break;
        }
        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        switch (uriMatcher.match(uri)) {
            case BRIDGE_LIST:
                return String.valueOf(CONTENT_URI_BRIDGE);
            case BRIDGE_ID:
                return String.valueOf(CONTENT_URI_BRIDGE);
            case LIGHT_LIST:
                return String.valueOf(CONTENT_URI_LIGHT);
            case LIGHT_ID:
                return String.valueOf(CONTENT_URI_LIGHT);
             default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long id;
        switch (uriMatcher.match(uri)){
            case BRIDGE_LIST:
                id = db.insertOrThrow(CONTENT_PROVIDER_TABLE_BRIDGE, null, values);
                Log.d(TAG, "Ajout d'un pont en base");
                break;
            case LIGHT_LIST:
                id = db.insertOrThrow(CONTENT_PROVIDER_TABLE_LIGHT, null, values);
                Log.d(TAG, "Ajout d'une ampoule en base.");
                break;
            default :
                id = -1;
        }

        try {
            if (id == -1) {
                throw new RuntimeException(String.format(
                        "%s : Failed to insert [%s] for unknown reasons.",TAG, values, uri));
            } else {
                return ContentUris.withAppendedId(uri, id);
            }
        } catch (SQLiteException e) {
           Log.e(TAG, e.getMessage());
            return null;
        } finally {
            db.close();
        }
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int count = 0;
        switch (uriMatcher.match(uri)){
            case BRIDGE_LIST:
                count = db.delete(CONTENT_PROVIDER_TABLE_BRIDGE, selection, selectionArgs);
                break;
            case LIGHT_LIST:
                count = db.delete(CONTENT_PROVIDER_TABLE_LIGHT, selection, selectionArgs);
                break;
        }
        return count;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int count = 0;
        switch (uriMatcher.match(uri)){
            case BRIDGE_LIST:
                count = db.update(CONTENT_PROVIDER_TABLE_BRIDGE, values, selection, selectionArgs);
                break;
            case LIGHT_LIST:
                count = db.update(CONTENT_PROVIDER_TABLE_LIGHT, values, selection, selectionArgs);
                break;
        }
        return count;
    }

     // DatabaseHelper
    private static class DatabaseHelper extends SQLiteOpenHelper {

        // Création de la base et du numéro de version
        DatabaseHelper(Context context) {
            super(context, CONTENT_PROVIDER_DB_NAME, null, CONTENT_PROVIDER_DB_VERSION);
        }

        // Création des tables
        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE " + CONTENT_PROVIDER_TABLE_BRIDGE + " ("
                    + SharedInformation.hueBridge.HUE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + SharedInformation.hueBridge.HUE_BRIDGE_ID + " VARCHAR(255) UNIQUE,"
                    + SharedInformation.hueBridge.HUE_IP + " VARCHAR(255),"
                    + SharedInformation.hueBridge.HUE_MAC_ADRESS + " VARCHAR(255),"
                    + SharedInformation.hueBridge.HUE_WIFI_NAME + " VARCHAR(255),"
                    + SharedInformation.hueBridge.HUE_USERNAME + " VARCHAR(255),"
                    + SharedInformation.hueBridge.HUE_TOKEN + ");");

            db.execSQL("CREATE TABLE " + CONTENT_PROVIDER_TABLE_LIGHT + " ("
                    + SharedInformation.hueLight.LIGHT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + SharedInformation.hueLight.HUE_ID + " INTEGER,"
                    + SharedInformation.hueLight.HUE_LIGHT_ID + " VARCHAR(255) UNIQUE,"
                    + SharedInformation.hueLight.LIGHT_MODEL + " VARCHAR(255),"
                    + SharedInformation.hueLight.LIGHT_TYPE + " VARCHAR(255),"
                    + SharedInformation.hueLight.LIGHT_NAME + " VARCHAR(255),"
                    + "FOREIGN KEY(" + SharedInformation.hueLight.HUE_ID  + ")REFERENCES " + CONTENT_PROVIDER_TABLE_BRIDGE + "(" + SharedInformation.hueBridge.HUE_ID + "));");
        }

        // Cette méthode sert à gérer la montée de version
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + CONTENT_PROVIDER_TABLE_BRIDGE);
            db.execSQL("DROP TABLE IF EXISTS " + CONTENT_PROVIDER_TABLE_LIGHT);
            onCreate(db);
        }

    }

}
