package com.example.gestioncontactos;

import java.io.Serializable;

public class Contacto implements Serializable {
    private int id;
    private String pais;
    private String nombre;
    private String telefono;
    private String nota;
    private byte[] imagen;

    public Contacto(int id, String pais, String nombre, String telefono, String nota, byte[] imagen) {
        this.id = id;
        this.pais = pais;
        this.nombre = nombre;
        this.telefono = telefono;
        this.nota = nota;
        this.imagen = imagen;
    }

    public int getId() { return id; }
    public String getPais() { return pais; }
    public String getNombre() { return nombre; }
    public String getTelefono() { return telefono; }
    public String getNota() { return nota; }
    public byte[] getImagen() { return imagen; }
}
