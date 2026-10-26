package com.example.gestioncontactos;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import java.util.ArrayList;
import java.util.List;

public class ListaContactosActivity extends AppCompatActivity {

    private ListView listView;
    private EditText etBuscador;
    private DatabaseHelper dbHelper;
    private List<Contacto> listaOriginal;
    private List<Contacto> listaFiltrada;
    private ContactoAdapter adaptador;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_contactos);

        dbHelper = new DatabaseHelper(this);

        // BÚSQUEDA DINÁMICA: Encuentra la lista y el buscador sin importar sus IDs en el XML
        ViewGroup viewGroup = findViewById(android.R.id.content);

        ArrayList<ListView> listViews = new ArrayList<>();
        buscarListViews(viewGroup, listViews);
        if (!listViews.isEmpty()) {
            listView = listViews.get(0);
        }

        ArrayList<EditText> editTexts = new ArrayList<>();
        buscarComponentesEditText(viewGroup, editTexts);
        if (!editTexts.isEmpty()) {
            etBuscador = editTexts.get(0);
        }

        if (etBuscador != null) {
            etBuscador.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    filtrarContactosPorNombre(s.toString());
                }
                @Override
                public void afterTextChanged(Editable s) {}
            });
        }

        if (listView != null) {
            listView.setOnItemClickListener((parent, view, position, id) -> {
                if (listaFiltrada != null && position < listaFiltrada.size()) {
                    Contacto contactoSeleccionado = listaFiltrada.get(position);
                    mostrarMenuOpciones(contactoSeleccionado);
                }
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        refrescarYMostrarLista();
    }

    private void buscarListViews(ViewGroup root, ArrayList<ListView> lists) {
        for (int i = 0; i < root.getChildCount(); i++) {
            View child = root.getChildAt(i);
            if (child instanceof ListView) {
                lists.add((ListView) child);
            } else if (child instanceof ViewGroup) {
                buscarListViews((ViewGroup) child, lists);
            }
        }
    }

    private void buscarComponentesEditText(ViewGroup root, ArrayList<EditText> eds) {
        for (int i = 0; i < root.getChildCount(); i++) {
            View child = root.getChildAt(i);
            if (child instanceof EditText) {
                eds.add((EditText) child);
            } else if (child instanceof ViewGroup) {
                buscarComponentesEditText((ViewGroup) child, eds);
            }
        }
    }

    private void refrescarYMostrarLista() {
        if (dbHelper == null) return;
        listaOriginal = dbHelper.getAllContactos();
        if (listaOriginal == null) {
            listaOriginal = new ArrayList<>();
        }
        listaFiltrada = new ArrayList<>(listaOriginal);
        adaptador = new ContactoAdapter(this, listaFiltrada);

        if (listView != null) {
            listView.setAdapter(adaptador);
            adaptador.notifyDataSetChanged();
        }
    }

    private void filtrarContactosPorNombre(String texto) {
        if (listaFiltrada == null || listaOriginal == null || adaptador == null) return;
        listaFiltrada.clear();
        for (Contacto c : listaOriginal) {
            if (c.getNombre().toLowerCase().contains(texto.toLowerCase())) {
                listaFiltrada.add(c);
            }
        }
        adaptador.notifyDataSetChanged();
    }

    private void mostrarMenuOpciones(Contacto contacto) {
        String[] opcionesMenu = {"Llamar / Ver Detalles", "Compartir", "Editar", "Eliminar"};

        new AlertDialog.Builder(this)
                .setTitle(contacto.getNombre())
                .setItems(opcionesMenu, (dialog, itemPulsado) -> {
                    switch (itemPulsado) {
                        case 0:
                            mostrarConfirmacionDeLlamada(contacto);
                            break;
                        case 1:
                            compartirContactoIntent(contacto);
                            break;
                        case 2:
                            Intent intent = new Intent(this, MainActivity.class);
                            intent.putExtra("CONTACTO_OBJETO", contacto);
                            startActivity(intent);
                            break;
                        case 3:
                            eliminarContactoDeBaseDatos(contacto);
                            break;
                    }
                }).show();
    }

    private void mostrarConfirmacionDeLlamada(Contacto contacto) {
        new AlertDialog.Builder(this)
                .setTitle("Confirmar llamada")
                .setMessage("¿Desea llamar a " + contacto.getNombre() + "?")
                .setPositiveButton("Sí", (dialog, which) -> ejecutarLlamadaTelefonica(contacto))
                .setNegativeButton("No", null)
                .show();
    }

    private void ejecutarLlamadaTelefonica(Contacto contacto) {
        String codigoArea = "";
        if (contacto.getPais() != null && contacto.getPais().contains("(+")) {
            codigoArea = contacto.getPais().substring(contacto.getPais().indexOf("(+") + 1, contacto.getPais().indexOf(")"));
        } else if (contacto.getPais() != null) {
            codigoArea = contacto.getPais();
        }

        Uri uriLlamada = Uri.parse("tel:" + codigoArea + contacto.getTelefono());

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
            Intent callIntent = new Intent(Intent.ACTION_CALL, uriLlamada);
            startActivity(callIntent);
        } else {
            requestPermissions(new String[]{Manifest.permission.CALL_PHONE}, 123);
        }
    }

    private void compartirContactoIntent(Contacto contacto) {
        String datosCompartir = "Contacto: " + contacto.getNombre() + "\nTeléfono: " + contacto.getTelefono();
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, datosCompartir);
        startActivity(Intent.createChooser(intent, "Compartir usando"));
    }

    private void eliminarContactoDeBaseDatos(Contacto contacto) {
        dbHelper.deleteContacto(contacto.getId());
        refrescarYMostrarLista();
        Toast.makeText(this, "Contacto eliminado con éxito", Toast.LENGTH_SHORT).show();
    }
}
