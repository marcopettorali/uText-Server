
package utext;

import java.sql.*;
import java.util.*;

public abstract class ArchiviazioneMessaggiBufferizzati {//01
    private static Connection connessioneDB;
    static{
        try{
            connessioneDB = DriverManager.getConnection("jdbc:mysql://localhost:3306/utextServer","root","");
        }catch(SQLException sqle){
            System.out.println(sqle.getMessage());
        }
    }
    
    public static List<Messaggio> caricaMessaggiUtente(String username){//02
        List<Messaggio> listaMessaggi = null;
        try{
            PreparedStatement caricaMessaggi = connessioneDB.prepareStatement("SELECT timestampInvio, mittente, destinatario, testo " 
                                                                            + "FROM Messaggio "
                                                                            + "WHERE destinatario = ?" 
                                                                            + "ORDER BY idMessaggio");
            
            caricaMessaggi.setString(1, username);
                  
            ResultSet rs = caricaMessaggi.executeQuery();
            
            listaMessaggi = new ArrayList();
            
            while(rs.next()){
                listaMessaggi.add(new Messaggio(rs.getTimestamp(1), rs.getString(2), rs.getString(3), rs.getString(4)));
            }
            
        }catch(SQLException sqle){
            System.err.println(sqle.getMessage());
        }
        
        return listaMessaggi;
    }
    
    public static void salvaMessaggio(Timestamp timestampInvio, String mittente, String destinatario, String testo){//03
        try{
            PreparedStatement salvaMessaggio = connessioneDB.prepareStatement("INSERT INTO Messaggio(timestampInvio, mittente, destinatario, testo) VALUES(?,?,?,?)");
        
            salvaMessaggio.setTimestamp(1, timestampInvio);
            salvaMessaggio.setString(2, mittente);
            salvaMessaggio.setString(3, destinatario);
            salvaMessaggio.setString(4, testo);
            
            salvaMessaggio.executeUpdate();
            
        }catch(SQLException sqle){
            System.err.println(sqle.getMessage());
        }
    }
    
    public static void eliminaMessaggiBufferizzati(String username){//04
        try{
            PreparedStatement salvaMessaggio = connessioneDB.prepareStatement("DELETE FROM Messaggio WHERE destinatario = ?");
        
            salvaMessaggio.setString(1, username);
            
            salvaMessaggio.executeUpdate();
            
        }catch(SQLException sqle){
            System.err.println(sqle.getMessage());
        }
    }
}

/*

NOTE:
(01) Classe astratta che racchiude metodi statici per l'archiviazione e il recupero dei messaggi bufferizzati,
     ossia destinati a utenti che al momento dell'invio del messaggio erano offline.

(02) Metodo statico che carica tutti i messaggi contenuti nell'archivio di buffer destinati all'utente passato per parametro

(03) Metodo statico che archivia tra i messaggi bufferizzati un messaggio i cui parametri sono passati alla funzione

(04) Metodo statico che elimina tutti i messaggi bufferizzati destinati ad uno specifico utente. Questo metodo viene invocato
     dopo il ripristino dei suddetti messaggi per non occupare spazio inutile, dato che una volta consegnati al destinatario
     non e' piu' necessario mantenerne una copia nell'archivio buffer.
     

*/