package com.example.gestioncontactos;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.ViewGroup;
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
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private EditText txtNombre, txtTelefono, txtNota;
    private Spinner spnPais;
    private ImageView imgContacto;
    private Button btnGuardar;
    private byte[] imageBytes = null;
    private DatabaseHelper dbHelper;
    private final String[] paisesCodigos = {"Honduras (+504)", "El Salvador (+503)", "Guatemala (+502)", "USA (+1)"};
    private Contacto contactoEdicion = null;

    @SuppressWarnings({"DiscouragedApi", "deprecation"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new DatabaseHelper(this);

        txtNombre = findViewById(getResources().getIdentifier("txtNombre", "id", getPackageName()));
        txtTelefono = findViewById(getResources().getIdentifier("txtTelefono", "id", getPackageName()));
        txtNota = findViewById(getResources().getIdentifier("txtNota", "id", getPackageName()));
        spnPais = findViewById(getResources().getIdentifier("spnPais", "id", getPackageName()));
        imgContacto = findViewById(getResources().getIdentifier("imgContacto", "id", getPackageName()));
        btnGuardar = findViewById(getResources().getIdentifier("btnGuardar", "id", getPackageName()));
        Button btnVerLista = findViewById(getResources().getIdentifier("btnVerLista", "id", getPackageName()));

        ViewGroup viewGroup = findViewById(android.R.id.content);
        ArrayList<EditText> editTexts = new ArrayList<>();
        ArrayList<Button> buttons = new ArrayList<>();
        buscarComponentes(viewGroup, editTexts, buttons);

        if (txtNombre == null && !editTexts.isEmpty()) txtNombre = editTexts.get(0);
        if (txtTelefono == null && editTexts.size() >= 2) txtTelefono = editTexts.get(1);
        if (txtNota == null && editTexts.size() >= 3) txtNota = editTexts.get(2);

        if (spnPais == null) {
            ArrayList<Spinner> spinners = new ArrayList<>();
            buscarSpinners(viewGroup, spinners);
            if (!spinners.isEmpty()) spnPais = spinners.get(0);
        }

        if (imgContacto == null) {
            ArrayList<ImageView> imageViews = new ArrayList<>();
            buscarImageViews(viewGroup, imageViews);
            if (!imageViews.isEmpty()) imgContacto = imageViews.get(0);
        }

        if (btnGuardar == null && !buttons.isEmpty()) btnGuardar = buttons.get(0);
        if (btnVerLista == null && buttons.size() >= 2) btnVerLista = buttons.get(1);

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

    private void buscarComponentes(ViewGroup root, ArrayList<EditText> eds, ArrayList<Button> btns) {
        for (int i = 0; i < root.getChildCount(); i++) {
            View child = root.getChildAt(i);
            if (child instanceof EditText) {
                eds.add((EditText) child);
            } else if (child instanceof Button) {
                btns.add((Button) child);
            } else if (child instanceof ViewGroup) {
                buscarComponentes((ViewGroup) child, eds, btns);
            }
        }
    }

    private void buscarSpinners(ViewGroup root, ArrayList<Spinner> spins) {
        for (int i = 0; i < root.getChildCount(); i++) {
            View child = root.getChildAt(i);
            if (child instanceof Spinner) {
                spins.add((Spinner) child);
            } else if (child instanceof ViewGroup) {
                buscarSpinners((ViewGroup) child, spins);
            }
        }
    }

    private void buscarImageViews(ViewGroup root, ArrayList<ImageView> imgs) {
        for (int i = 0; i < root.getChildCount(); i++) {
            View child = root.getChildAt(i);
            if (child instanceof ImageView) {
                imgs.add((ImageView) child);
            } else if (child instanceof ViewGroup) {
                buscarImageViews((ViewGroup) child, imgs);
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
            Toast.makeText(this, "Error de interfaz: No se hallaron los campos", Toast.LENGTH_LONG).show();
            return;
        }

        String nombre = txtNombre.getText().toString().trim();
        String telefono = txtTelefono.getText().toString().trim();
        String nota = txtNota.getText().toString().trim();
        String pais = spnPais.getSelectedItem() != null ? spnPais.getSelectedItem().toString() : "";

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
                imageBytes = new byte[0];
            }
        }

        if (contactoEdicion == null) {
            Contacto nuevoContacto = new Contacto(0, pais, nombre, telefono, nota, imageBytes);
            long resultado = dbHelper.addContacto(nuevoContacto); // CORREGIDO: Guarda el nuevo objeto con datos reales

            if (resultado != -1) {
                Toast.makeText(this, "¡Contacto " + nombre + " guardado con éxito!", Toast.LENGTH_SHORT).show();
                txtNombre.setText("");
                txtTelefono.setText("");
                txtNota.setText("");
                imageBytes = null;

            }
        }
    }
}

