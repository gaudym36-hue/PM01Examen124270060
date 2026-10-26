package com.example.gestioncontactos;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import java.io.ByteArrayOutputStream;

public class MainActivity extends AppCompatActivity {

    private EditText txtNombre, txtTelefono, txtNota;
    private Spinner spnPais;
    private ImageView imgContacto;
    private Button btnGuardar;
    private byte[] imageBytes = null;
    private DatabaseHelper dbHelper;
    private final String[] paisesCodigos = {"Honduras (+504)", "El Salvador (+503)", "Guatemala (+502)", "USA (+1)"};
    private Contacto contactoEdicion = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new DatabaseHelper(this);

        // ENLACE DIRECTO SEGURO: Garantiza que cada variable tome el campo correcto del XML
        txtNombre = findViewById(R.id.txtNombre);
        txtTelefono = findViewById(R.id.txtTelefono);
        txtNota = findViewById(R.id.txtNota);
        spnPais = findViewById(R.id.spnPais);
        imgContacto = findViewById(R.id.imgContacto);
        btnGuardar = findViewById(R.id.btnGuardar);
        Button btnVerLista = findViewById(R.id.btnVerLista);

        if (spnPais != null) {
            spnPais.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, paisesCodigos));
        }

        if (imgContacto != null) {
            imgContacto.setOnClickListener(v -> {
                try {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    cameraLauncher.launch(intent);
                } catch (Exception e) {
                    Toast.makeText(this, "Error al abrir la cámara", Toast.LENGTH_SHORT).show();
                }
            });
        }

        if (btnGuardar != null) {
            btnGuardar.setOnClickListener(v -> guardarContacto());
        }

        if (btnVerLista != null) {
            btnVerLista.setOnClickListener(v -> startActivity(new Intent(this, ListaContactosActivity.class)));
        }

        // CONTROL DE EDICIÓN: Si viene un contacto de la lista para ser editado
        if (getIntent().hasExtra("CONTACTO_OBJETO")) {
            contactoEdicion = (Contacto) getIntent().getSerializableExtra("CONTACTO_OBJETO");
            if (contactoEdicion != null) {
                if (txtNombre != null) txtNombre.setText(contactoEdicion.getNombre());
                if (txtTelefono != null) txtTelefono.setText(contactoEdicion.getTelefono());
                if (txtNota != null) txtNota.setText(contactoEdicion.getNota());
                if (btnGuardar != null) btnGuardar.setText("ACTUALIZAR CONTACTO");

                if (spnPais != null) {
                    for (int i = 0; i < paisesCodigos.length; i++) {
                        if (paisesCodigos[i].contains(contactoEdicion.getPais())) {
                            spnPais.setSelection(i);
                            break;
                        }
                    }
                }

                if (contactoEdicion.getImagen() != null && contactoEdicion.getImagen().length > 0 && imgContacto != null) {
                    imageBytes = contactoEdicion.getImagen();
                    Bitmap bmp = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                    imgContacto.setImageBitmap(bmp);
                }
            }
        }
    }

    private final ActivityResultLauncher<Intent> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null && imgContacto != null) {
                    try {
                        Bundle extras = result.getData().getExtras();
                        if (extras != null) {
                            Bitmap bitmap = (Bitmap) extras.get("data");
                            if (bitmap != null) {
                                imgContacto.setImageBitmap(bitmap);
                                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                                imageBytes = stream.toByteArray();
                            }
                        }
                    } catch (Exception e) {
                        Toast.makeText(this, "Error al recuperar la foto", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    private void guardarContacto() {
        if (txtNombre == null || txtTelefono == null || txtNota == null || spnPais == null) {
            Toast.makeText(this, "Error: No se enlazaron los componentes del XML", Toast.LENGTH_LONG).show();
            return;
        }

        String nombre = txtNombre.getText().toString().trim();
        String telefono = txtTelefono.getText().toString().trim();
        String nota = txtNota.getText().toString().trim();
        String pais = spnPais.getSelectedItem() != null ? spnPais.getSelectedItem().toString() : "";

        // VALIDACIÓN CRÍTICA: Bloquea el flujo si el usuario no escribe datos obligatorios
        if (nombre.isEmpty()) {
            Toast.makeText(this, "Por favor, escribe un nombre", Toast.LENGTH_SHORT).show();
            txtNombre.requestFocus();
            return;
        }

        if (telefono.isEmpty()) {
            Toast.makeText(this, "Por favor, escribe un teléfono", Toast.LENGTH_SHORT).show();
            txtTelefono.requestFocus();
            return;
        }

        // PROCESADO DE IMAGEN: Si no usó la cámara, guarda la foto por defecto del ImageView
        if (imageBytes == null && imgContacto != null) {
            try {
                Drawable drawable = imgContacto.getDrawable();
                if (drawable instanceof BitmapDrawable) {
                    Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                    imageBytes = stream.toByteArray();
                }
            } catch (Exception e) {
                imageBytes = new byte[0]; // Arreglo vacío de bytes en caso de error
            }
        }

        if (contactoEdicion == null) {
            // OPERACIÓN: NUEVO CONTACTO
            Contacto nuevoContacto = new Contacto(0, pais, nombre, telefono, nota, imageBytes);
            long resultado = dbHelper.addContacto(nuevoContacto);

            if (resultado != -1) {
                Toast.makeText(this, "¡Contacto " + nombre + " guardado con éxito!", Toast.LENGTH_SHORT).show();
                limpiarCampos();
            } else {
                Toast.makeText(this, "Error al insertar en SQLite", Toast.LENGTH_SHORT).show();
            }
        } else {
            // OPERACIÓN: ACTUALIZAR EXISTENTE
            contactoEdicion.setPais(pais);
            contactoEdicion.setNombre(nombre);
            contactoEdicion.setTelefono(telefono);
            contactoEdicion.setNota(nota);
            contactoEdicion.setImagen(imageBytes);

            int filasActualizadas = dbHelper.actualizarContacto(contactoEdicion);
            if (filasActualizadas > 0) {
                Toast.makeText(this, "Contacto actualizado con éxito", Toast.LENGTH_SHORT).show();
                finish(); // Cierra y vuelve automáticamente a la lista
            } else {
                Toast.makeText(this, "Error al actualizar en SQLite", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void limpiarCampos() {
        if (txtNombre != null) txtNombre.setText("");
        if (txtTelefono != null) txtTelefono.setText("");
        if (txtNota != null) txtNota.setText("");
        if (spnPais != null && spnPais.getCount() > 0) spnPais.setSelection(0);
        if (imgContacto != null) imgContacto.setImageResource(android.R.drawable.ic_menu_gallery);
        imageBytes = null;
    }
}
