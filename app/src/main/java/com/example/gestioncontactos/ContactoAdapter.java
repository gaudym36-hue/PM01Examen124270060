package com.example.gestioncontactos;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ContactoAdapter extends RecyclerView.Adapter<ContactoAdapter.ViewHolder> {

    private List<Contacto> lista;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Contacto contacto);
    }

    public ContactoAdapter(List<Contacto> lista, OnItemClickListener listener) {
        this.lista = lista;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Contacto c = lista.get(position); // Corregido: se usa .get() en lugar de []
        holder.textView.setText(c.getNombre() + " (" + c.getPais() + " " + c.getTelefono() + ")");
        holder.itemView.setOnClickListener(v -> listener.onItemClick(c));
    }

    @Override
    public int getItemCount() { return lista.size(); }

    public void actualizarLista(List<Contacto> nuevaLista) {
        this.lista = nuevaLista;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textView;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(android.R.id.text1);
        }
    }
}
