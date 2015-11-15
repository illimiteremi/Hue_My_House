package fr.free.couturier_remi_hd.huemyhouse.hueCommonData;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
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
    public static final Uri    CONTENT_URI                 = Uri.parse("content://fr.free.couturier_remi_hd.huemyhouse.provider");

    public static final String CONTENT_PROVIDER_DB_NAME    = "HueMyHouse.db";                // Nom de notre base de données
    public static final int    CONTENT_PROVIDER_DB_VERSION = 1;                              // Version de notre base de données
    public static final String CONTENT_PROVIDER_TABLE_NAME = "hueBridge";                    // Nom de la table de notre base

    public static final String TAG                         = "[HueMyHouse]";

    private DatabaseHelper     dbHelper;

    // Mime du content provider
    public static final String CONTENT_PROVIDER_MIME = "vnd.android.cursor.item/vnd.android.content.provider.huebridge";

    @Override
    public boolean onCreate() {
        dbHelper = new DatabaseHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

            long id = getId(uri);
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            if (id < 0) {
                return db.query(CONTENT_PROVIDER_TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
            } else {
                return db.query(CONTENT_PROVIDER_TABLE_NAME, projection, SharedInformation.hueBridge.HUE_ID + "=" + id, null, null, null, null);
            }
    }

    @Override
    public String getType(Uri uri) {
        return CONTENT_PROVIDER_MIME;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try {
            long id = db.insertOrThrow(CONTENT_PROVIDER_TABLE_NAME, null, values);
            if (id == -1) {
                throw new RuntimeException(String.format(
                        "%s : Failed to insert [%s] for unknown reasons.","TutosAndroidProvider", values, uri));
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
                return db.delete(CONTENT_PROVIDER_TABLE_NAME, selection, selectionArgs);
            else
                return db.delete(CONTENT_PROVIDER_TABLE_NAME, SharedInformation.hueBridge.HUE_ID + "=" + id, selectionArgs);
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
                return db.update(CONTENT_PROVIDER_TABLE_NAME,values, selection, selectionArgs);
            else
                return db.update(CONTENT_PROVIDER_TABLE_NAME,
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
            db.execSQL("CREATE TABLE " + CONTENT_PROVIDER_TABLE_NAME + " ("
                    + SharedInformation.hueBridge.HUE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + SharedInformation.hueBridge.HUE_BRIDGE_ID + " VARCHAR(255) UNIQUE,"
                    + SharedInformation.hueBridge.HUE_IP + " VARCHAR(255),"
                    + SharedInformation.hueBridge.HUE_MAC_ADRESS + " VARCHAR(255),"
                    + SharedInformation.hueBridge.HUE_WIFI_NAME + " VARCHAR(255),"
                    + SharedInformation.hueBridge.HUE_USERNAME + " VARCHAR(255),"
                    + SharedInformation.hueBridge.HUE_TOKEN + ");");
        }

        // Cette méthode sert à gérer la montée
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + CONTENT_PROVIDER_TABLE_NAME);
            onCreate(db);
        }

    }

}
