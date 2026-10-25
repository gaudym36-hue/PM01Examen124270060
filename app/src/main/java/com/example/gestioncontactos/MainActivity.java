package com.example.gestioncontactos;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
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
    private byte[] imageBytes = null;
    private DatabaseHelper dbHelper;
    private final String[] paisesCodigos = {"Honduras (+504)", "El Salvador (+503)", "Guatemala (+502)", "USA (+1)"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new DatabaseHelper(this);

        // 1. Intentar buscar por los IDs por defecto
        txtNombre = findViewById(getResources().getIdentifier("txtNombre", "id", getPackageName()));
        txtTelefono = findViewById(getResources().getIdentifier("txtTelefono", "id", getPackageName()));
        txtNota = findViewById(getResources().getIdentifier("txtNota", "id", getPackageName()));
        spnPais = findViewById(getResources().getIdentifier("spnPais", "id", getPackageName()));
        imgContacto = findViewById(getResources().getIdentifier("imgContacto", "id", getPackageName()));
        Button btnGuardar = findViewById(getResources().getIdentifier("btnGuardar", "id", getPackageName()));
        Button btnVerLista = findViewById(getResources().getIdentifier("btnVerLista", "id", getPackageName()));

        // 2. Si fallan los IDs, buscar dinámicamente por tipo de componente en la pantalla
        ViewGroup viewGroup = findViewById(android.R.id.content);
        ArrayList<EditText> editTexts = new ArrayList<>();
        ArrayList<Button> buttons = new ArrayList<>();
        buscarComponentes(viewGroup, editTexts, buttons);

        if (txtNombre == null && editTexts.size() >= 1) txtNombre = editTexts.get(0);
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

        if (btnGuardar == null && buttons.size() >= 1) btnGuardar = buttons.get(0);
        if (btnVerLista == null && buttons.size() >= 2) btnVerLista = buttons.get(1);

        // 3. Asignar adaptadores y eventos de forma segura
        if (spnPais != null) {
            spnPais.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, paisesCodigos));
        }

        if (imgContacto != null) {
            imgContacto.setOnClickListener(v -> {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                cameraLauncher.launch(intent);
            });
        }

        if (btnGuardar != null) {
            btnGuardar.setOnClickListener(v -> guardarContacto());
        }

        if (btnVerLista != null) {
            btnVerLista.setOnClickListener(v -> startActivity(new Intent(this, ListaContactosActivity.class)));
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
                    Bitmap bitmap = (Bitmap) result.getData().getExtras().get("data");
                    imgContacto.setImageBitmap(bitmap);
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                    imageBytes = stream.toByteArray();
                }
            }
    );

    private void guardarContacto() {
        if (txtNombre == null || txtTelefono == null || txtNota == null) {
            Toast.makeText(this, "Error de interfaz: No se hallaron los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        String nombre = txtNombre.getText().toString().trim();
        String telefono = txtTelefono.getText().toString().trim();
        String nota = txtNota.getText().toString().trim();
        String codigoArea = "+504";

        if (spnPais != null && spnPais.getSelectedItem() != null) {
            String item = spnPais.getSelectedItem().toString();
            codigoArea = item.substring(item.indexOf("(") + 1, item.indexOf(")"));
        }

        if (nombre.isEmpty()) { txtNombre.setError("Falta el nombre"); return; }
        if (telefono.isEmpty()) { txtTelefono.setError("Falta el teléfono"); return; }
        if (nota.isEmpty()) { txtNota.setError("Falta la nota"); return; }

        if (!nombre.matches("^[a-zA-Z ]+$")) { txtNombre.setError("Solo letras"); return; }
        if (!telefono.matches("^[0-9]+$")) { txtTelefono.setError("Solo números"); return; }

        long res = dbHelper.addContacto(new Contacto(0, codigoArea, nombre, telefono, nota, imageBytes));
        if (res > 0) {
            Toast.makeText(this, "Guardado con éxito en SQLite", Toast.LENGTH_SHORT).show();
            txtNombre.setText(""); txtTelefono.setText(""); txtNota.setText("");
            if (imgContacto != null) imgContacto.setImageResource(android.R.drawable.ic_menu_camera);
            imageBytes = null;
        }
    }
}
