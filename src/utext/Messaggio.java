package utext;

import java.io.*;
import java.sql.*;

public class Messaggio implements Serializable{
    final private Timestamp timestampInvio;
    final private String mittente;
    final private String destinatario;
    final private String testo;
    
    public Messaggio(String mit, String dest, String txt){//01
        timestampInvio = new Timestamp(System.currentTimeMillis());
        mittente = mit;
        destinatario = dest;
        testo = txt;
    }
    
    public Messaggio(Timestamp timestamp, String mit, String dest, String txt){//01
        timestampInvio = timestamp;
        mittente = mit;
        destinatario = dest;
        testo = txt;
    }
    
    public Timestamp getTimestampInvio(){
        return timestampInvio;
    }
    
    public String getMittente(){
        return mittente;
    }
    
    public String getDestinatario(){
        return destinatario;
    }
    
    public String getTesto(){
        return testo;
    }
    
}

/*

NOTE:
(01) Questa classe contiene due costruttori simili, che differiscono soltanto dalla presenza o meno del parametro timestampInvio. Questo e' necessario, poiche' si vuole
     che al momento dell'invio il messaggio contenga il timestamp di invio effettivo, mentre in fase di recupero da database si deve prendere il timestamp contenuto
     dentro il messaggio.

*/