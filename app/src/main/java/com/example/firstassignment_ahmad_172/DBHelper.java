package com.example.firstassignment_ahmad_172;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DBHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "vet_vision.db";
    private static final int DB_VERSION = 1;

    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String query = "CREATE TABLE doctors (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT, " +
                "contact TEXT, " +
                "email TEXT, " +
                "address TEXT)";
        db.execSQL(query);

        // Dummy data
        insertDummyData(db);
    }

    private void insertDummyData(SQLiteDatabase db) {
        db.execSQL("INSERT INTO doctors (name, contact, email, address) " +
                "VALUES ('Dr. Ayesha Urooj', '+92 300 1234567', 'vetvision@clinic.com', '123 Vet Street, Animal Town')");
        db.execSQL("INSERT INTO doctors (name, contact, email, address) " +
                "VALUES ('Dr. Ali Khan', '+92 345 9876543', 'alikhan@clinic.com', '456 Care Avenue, Farmville')");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS doctors");
        onCreate(db);
    }

    public List<Doctor> getAllDoctors() {
        List<Doctor> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        try {
            cursor = db.rawQuery("SELECT * FROM doctors", null);

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    int id = cursor.getInt(cursor.getColumnIndex("id"));
                    String name = cursor.getString(cursor.getColumnIndex("name"));
                    String contact = cursor.getString(cursor.getColumnIndex("contact"));
                    String email = cursor.getString(cursor.getColumnIndex("email"));
                    String address = cursor.getString(cursor.getColumnIndex("address"));

                    list.add(new Doctor(id, name, contact, email, address));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();  // Log the exception (for debugging purposes)
        } finally {
            if (cursor != null) {
                cursor.close();  // Ensuring the cursor is closed
            }
        }
        return list;
    }
}
