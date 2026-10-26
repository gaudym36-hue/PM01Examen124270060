package com.example.gestioncontactos;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;

public class DatabaseHelper extends SQLiteOpenHelper {

    // CORRECCIÓN: Cambiado el número de versión a 2 para limpiar el caché corrupto del teléfono
    public DatabaseHelper(Context context) {
        super(context, "ContactosDB", null, 2);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE contactos (id INTEGER PRIMARY KEY AUTOINCREMENT, pais TEXT, nombre TEXT, telefono TEXT, nota TEXT, imagen BLOB)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS contactos");
        onCreate(db);
    }

    public long addContacto(Contacto c) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("pais", c.getPais());
        values.put("nombre", c.getNombre());
        values.put("telefono", c.getTelefono());
        values.put("nota", c.getNota());
        values.put("imagen", c.getImagen());
        long res = db.insert("contactos", null, values);
        db.close();
        return res;
    }

    public ArrayList<Contacto> getAllContactos() {
        ArrayList<Contacto> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM contactos", null);
        if (cursor.moveToFirst()) {
            do {
                list.add(new Contacto(
                        cursor.getInt(0), cursor.getString(1),
                        cursor.getString(2), cursor.getString(3),
                        cursor.getString(4), cursor.getBlob(5)
                ));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return list;
    }

    public int deleteContacto(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int res = db.delete("contactos", "id=?", new String[]{String.valueOf(id)});
        db.close();
        return res;
    }

    public int actualizarContacto(Contacto contacto) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("pais", contacto.getPais());
        values.put("nombre", contacto.getNombre());
        values.put("telefono", contacto.getTelefono());
        values.put("nota", contacto.getNota());
        values.put("imagen", contacto.getImagen());

        int res = db.update("contactos", values, "id=?", new String[]{String.valueOf(contacto.getId())});
        db.close();
        return res;
    }
}
