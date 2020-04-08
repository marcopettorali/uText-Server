package utext;

import java.io.*;

public class ComunicazioneAmicoOnline implements Serializable{
    private final String username;//01
    private final boolean online;
    
    public ComunicazioneAmicoOnline(String user, boolean statoOnline){
        username = user;
        online = statoOnline;
    }
    
    public String getUsername(){
        return username;
    }
    
    public boolean getOnline(){
        return online;
    }
}
/*

NOTE:
(01) Ho usato String invece che SimpleStringProperty perche' non ho bisogno della osservabilita' del parametro username.
*/