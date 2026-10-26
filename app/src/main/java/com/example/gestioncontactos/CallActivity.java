package com.example.gestioncontactos;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

public class CallActivity extends AppCompatActivity {

    private Contacto contacto;
    private static final int REQUEST_CALL_PERM = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Diseñamos el contenedor principal de la pantalla dinámicamente
        LinearLayout layoutPrincipal = new LinearLayout(this);
        layoutPrincipal.setOrientation(LinearLayout.VERTICAL);
        layoutPrincipal.setGravity(Gravity.CENTER_HORIZONTAL);
        layoutPrincipal.setPadding(60, 60, 60, 60);
        layoutPrincipal.setBackgroundColor(Color.parseColor("#FAFAFA"));

        // Recuperamos el contacto enviado desde la lista
        if (getIntent() != null && getIntent().getSerializableExtra("CONTACTO") != null) {
            contacto = (Contacto) getIntent().getSerializableExtra("CONTACTO");
        }

        // 1. Imagen del contacto
        ImageView imgDisplayCall = new ImageView(this);
        LinearLayout.LayoutParams paramsImg = new LinearLayout.LayoutParams(450, 450);
        paramsImg.setMargins(0, 40, 0, 0);
        imgDisplayCall.setLayoutParams(paramsImg);
        imgDisplayCall.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imgDisplayCall.setBackgroundColor(Color.parseColor("#E0E0E0"));
        imgDisplayCall.setImageResource(android.R.drawable.ic_menu_report_image);

        // 2. Etiqueta de Nombre
        TextView lblNombreCall = new TextView(this);
        lblNombreCall.setTextSize(26);
        lblNombreCall.setTypeface(null, Typeface.BOLD);
        lblNombreCall.setTextColor(Color.parseColor("#212121"));
        LinearLayout.LayoutParams paramsTxt = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        paramsTxt.setMargins(0, 50, 0, 0);
        lblNombreCall.setLayoutParams(paramsTxt);

        // 3. Etiqueta de Teléfono
        TextView lblTelefonoCall = new TextView(this);
        lblTelefonoCall.setTextSize(20);
        lblTelefonoCall.setTextColor(Color.parseColor("#666666"));
        lblTelefonoCall.setLayoutParams(paramsTxt);

        // 4. Etiqueta de Nota
        TextView lblNotaCall = new TextView(this);
        lblNotaCall.setTextSize(16);
        lblNotaCall.setTextColor(Color.parseColor("#757575"));
        lblNotaCall.setGravity(Gravity.CENTER);
        lblNotaCall.setLayoutParams(paramsTxt);

        // 5. Botón para Iniciar Llamada
        Button btnIniciarLlamada = new Button(this);
        btnIniciarLlamada.setText("Llamar");
        btnIniciarLlamada.setTextSize(18);
        btnIniciarLlamada.setBackgroundColor(Color.parseColor("#4CAF50"));
        btnIniciarLlamada.setTextColor(Color.WHITE);
        LinearLayout.LayoutParams paramsBtn = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        paramsBtn.setMargins(0, 80, 0, 0);
        btnIniciarLlamada.setLayoutParams(paramsBtn);

        // Asignamos los datos del contacto si existen
        if (contacto != null) {
            lblNombreCall.setText(contacto.getNombre());
            lblTelefonoCall.setText("+" + contacto.getPais() + " " + contacto.getTelefono());
            lblNotaCall.setText(contacto.getNota());

            if (contacto.getImagen() != null) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(contacto.getImagen(), 0, contacto.getImagen().length);
                imgDisplayCall.setImageBitmap(bitmap);
            }
        }

        // Añadimos los elementos en orden al contenedor principal
        layoutPrincipal.addView(imgDisplayCall);
        layoutPrincipal.addView(lblNombreCall);
        layoutPrincipal.addView(lblTelefonoCall);
        layoutPrincipal.addView(lblNotaCall);
        layoutPrincipal.addView(btnIniciarLlamada);

        // Le indicamos a la Actividad que use esta interfaz nativa
        setContentView(layoutPrincipal);

        // Lógica para realizar la llamada telefónica con confirmación previa
        btnIniciarLlamada.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Confirmar Llamada")
                    .setMessage("¿Desea llamar a " + (contacto != null ? contacto.getNombre() : "") + "?")
                    .setPositiveButton("Sí", (dialog, which) -> verificarPermisoYLLamar())
                    .setNegativeButton("No", null)
                    .show();
        });
    }

    private void verificarPermisoYLLamar() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE}, REQUEST_CALL_PERM);
        } else {
            ejecutarLlamada();
        }
    }

    private void ejecutarLlamada() {
        if (contacto != null) {
            Intent callIntent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:+" + contacto.getPais() + contacto.getTelefono()));
            startActivity(callIntent);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CALL_PERM && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            ejecutarLlamada();
        } else {
            Toast.makeText(this, "Permiso de llamada denegado", Toast.LENGTH_SHORT).show();
        }
    }
}
