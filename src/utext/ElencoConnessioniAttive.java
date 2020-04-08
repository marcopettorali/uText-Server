package utext;

import java.util.*;

public abstract class ElencoConnessioniAttive {//01
    private static List<RicevitoreMessaggiDaClient> listaConnessioni;
    
    static{
        listaConnessioni = new ArrayList<>();
    }
    
    public static List<RicevitoreMessaggiDaClient> getListaConnessioni(){
        return listaConnessioni;
    }
    
    public static void aggiungiConnessione(RicevitoreMessaggiDaClient connessione){
        listaConnessioni.add(connessione);
    }
    
    public static void rimuoviConnessione(RicevitoreMessaggiDaClient connessione){
        listaConnessioni.remove(connessione);
    }
    
    public static RicevitoreMessaggiDaClient cercaConnessione(String username){
        for (RicevitoreMessaggiDaClient connessione : listaConnessioni) {
            if (connessione.getUsername().equals(username)) {
                return connessione;
            }
        }
        return null;
    }
}

/*

NOTE:
(01) Classe astratta che raccoglie metodi statici per mantenere la lista di tutte le connessioni attive gestite dal server
     in un determinato momento. Questo e' necessario, perche' si deve aver la possibilita' di controllare se un certo utente
     e' attualmente online per recapitargli subito un messaggio, oppure e' offline, e allora si deve procedere con l'archiviazione
     del messaggio sull'archivio dei messaggi bufferizzati.

*/