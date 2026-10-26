package com.example.gestioncontactos;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.List;

public class ContactoAdapter extends ArrayAdapter<Contacto> {
    private Context context;
    private List<Contacto> listaContactos;

    public ContactoAdapter(Context context, List<Contacto> listaContactos) {
        super(context, 0, listaContactos);
        this.context = context;
        this.listaContactos = listaContactos;
    }

    @Override
    public int getCount() {
        return listaContactos.size();
    }

    @Override
    public Contacto getItem(int position) {
        return listaContactos.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            // Layout nativo de Android con soporte automático para Texto e Icono/Imagen juntos
            convertView = LayoutInflater.from(context).inflate(android.R.layout.activity_list_item, parent, false);
        }

        TextView txtInformacion = convertView.findViewById(android.R.id.text1);
        ImageView imgIcono = convertView.findViewById(android.R.id.icon);

        Contacto contacto = listaContactos.get(position);

        String codigoArea = "";
        if (contacto.getPais() != null && contacto.getPais().contains("(+")) {
            codigoArea = " (" + contacto.getPais().substring(contacto.getPais().indexOf("(+") + 1, contacto.getPais().indexOf(")")) + ") ";
        } else if (contacto.getPais() != null) {
            codigoArea = " (" + contacto.getPais() + ") ";
        }

        if (txtInformacion != null) {
            txtInformacion.setText(contacto.getNombre() + codigoArea + contacto.getTelefono());
        }

        // CRITERIO OBLIGATORIO: Ver imagen guardada desde SQLite
        if (imgIcono != null) {
            if (contacto.getImagen() != null && contacto.getImagen().length > 0) {
                imgIcono.setImageBitmap(BitmapFactory.decodeByteArray(contacto.getImagen(), 0, contacto.getImagen().length));
            } else {
                imgIcono.setImageResource(android.R.drawable.ic_menu_report_image);
            }
        }

        return convertView;
    }
}
