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
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by rcouturi on 15/11/2015.
 */
public class AndroidProvider extends ContentProvider {

    // URI du content provider
    public static final String AUTHORITY = "fr.free.couturier_remi_hd.huemyhouse.provider";
    public static final Uri    CONTENT_URI = Uri.parse("content://" + AUTHORITY);
    public static final Uri    CONTENT_URI_BRIDGE            = Uri.parse("content://fr.free.couturier_remi_hd.huemyhouse.provider/bridge");
    public static final Uri    CONTENT_URI_LIGHT             = Uri.parse("content://fr.free.couturier_remi_hd.huemyhouse.provider/light");

    public static final String CONTENT_PROVIDER_DB_NAME      = "HueMyHouse.db";                // Nom de la base de données
    public static final int    CONTENT_PROVIDER_DB_VERSION   = 1;                              // Version de la base de données
    public static final String CONTENT_PROVIDER_TABLE_BRIDGE = "hueBridge";                    // Nom de la table des ponts philips
    public static final String CONTENT_PROVIDER_TABLE_LIGHT  = "hueLight";                     // Nom de la table des ampoules

    public static final int    URI_BRIDGE                    = 1;
    public static final int    URI_LIGHT                     = 2;

    public static final String      TAG                         = "[HueMyHouse][Provider]";
    private DatabaseHelper          dbHelper;
    private static final UriMatcher uriMatcher;

    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(AUTHORITY, "bridge", 1);
        uriMatcher.addURI(AUTHORITY, "light",  2);
    }

    @Override
    public boolean onCreate() {
        dbHelper = new DatabaseHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        long id = getId(uri);
        Cursor cursor = null;
        switch (uriMatcher.match(uri)){
            case URI_BRIDGE:
                if (id < 0) {
                    cursor = db.query(CONTENT_PROVIDER_TABLE_BRIDGE, projection, selection, selectionArgs, null, null, sortOrder);
                } else {
                    cursor = db.query(CONTENT_PROVIDER_TABLE_BRIDGE, projection, SharedInformation.hueBridge.HUE_ID + "=" + id, null, null, null, null);
                }
                break;
            case URI_LIGHT:
                if (id < 0) {
                    cursor = db.query(CONTENT_PROVIDER_TABLE_LIGHT, projection, selection, selectionArgs, null, null, sortOrder);
                } else {
                    cursor = db.query(CONTENT_PROVIDER_TABLE_LIGHT, projection, SharedInformation.hueLight.LIGHT_ID + "=" + id, null, null, null, null);
                }
                break;
        }
        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long id;
        switch (uriMatcher.match(uri)){
            case URI_BRIDGE:
                id = db.insertOrThrow(CONTENT_PROVIDER_TABLE_BRIDGE, null, values);
                Log.d(TAG, "Ajout d'un pont en base");
                break;
            case URI_LIGHT:
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
        long id = getId(uri);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try {
            if (id < 0)
                return db.delete(CONTENT_PROVIDER_TABLE_BRIDGE, selection, selectionArgs);
            else
                return db.delete(CONTENT_PROVIDER_TABLE_BRIDGE, SharedInformation.hueBridge.HUE_ID + "=" + id, selectionArgs);
        } finally {
            db.close();
        }
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        long id = getId(uri);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        try {
            if (id < 0)
                return db.update(CONTENT_PROVIDER_TABLE_BRIDGE,values, selection, selectionArgs);
            else
                return db.update(CONTENT_PROVIDER_TABLE_BRIDGE,
                        values, SharedInformation.hueBridge.HUE_ID + "=" + id, null);
        } finally {
            db.close();
        }
    }

    /**
     * Récuperation de l'identifiant d'un pont Hue
     * @param uri
     * @return
     */
    private long getId(Uri uri) {
        String lastPathSegment = uri.getLastPathSegment();
        if (lastPathSegment != null) {
            try {
                return Long.parseLong(lastPathSegment);
            } catch (NumberFormatException e) {
                Log.e(TAG, "Number Format Exception : " + e);
            }
        }
        return -1;
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
