package utext;

import java.io.*;
import java.net.*;
import java.util.*;

public class RicevitoreMessaggiDaClient extends Thread{//01
    private String username;
    private final Socket socket;
    private ObjectInputStream ois;
    private ObjectOutputStream oos;
    private DataInputStream dis;
    private DataOutputStream dos;
    
    public RicevitoreMessaggiDaClient(Socket sock){
        socket = sock;
    }
    
    public String getUsername(){
        return username;
    }
    
    public Socket getSocket(){
        return socket;
    }
    
    private void loginUtente(){//02
        try {
            String user = dis.readUTF();
            username = user;                
        } catch (IOException ex) {
            System.err.println(ex.getMessage());
        }
        notificaStatoOnlineUtenteInBroadcast(true);//02.a
        System.out.println(username + " ha fatto login");
        
        List<Messaggio> listaMessaggiBufferizzati = ArchiviazioneMessaggiBufferizzati.caricaMessaggiUtente(username);//02.b
        
        if(listaMessaggiBufferizzati == null){//02.c
            return;
        }
        
        for(Messaggio messaggioBufferizzato : listaMessaggiBufferizzati){//02.d
            invia("MESSAGE", messaggioBufferizzato);
        }
        ArchiviazioneMessaggiBufferizzati.eliminaMessaggiBufferizzati(username);//02.e
    }
    
    private void logoutUtente(){
        notificaStatoOnlineUtenteInBroadcast(false);
        ElencoConnessioniAttive.rimuoviConnessione(this);
        try{
            dos.close();
            oos.close();
            ois.close();
            dis.close();
            socket.close();
        }catch(IOException ioe){
            System.err.println(ioe.getMessage());
        }
        System.out.println(username + " ha fatto logout");
    }
    
    private void inoltraMessaggio(){//03
        try {
            Messaggio messaggio  = (Messaggio) ois.readObject();
           
            RicevitoreMessaggiDaClient connessioneAmico = ElencoConnessioniAttive.cercaConnessione(messaggio.getDestinatario());//03.a
            if(connessioneAmico == null){//03.b
                ArchiviazioneMessaggiBufferizzati.salvaMessaggio(messaggio.getTimestampInvio(), messaggio.getMittente(), messaggio.getDestinatario(), messaggio.getTesto());
            }else{
                connessioneAmico.invia("MESSAGE", messaggio);
               
            }
            System.out.println("Ricevuto messaggio da " + messaggio.getMittente() + " per " + messaggio.getDestinatario());
        } catch (IOException | ClassNotFoundException ex) {
            System.err.println(ex.getMessage());
        }
        
    }
    
    private void comunicaAmicoOnline(){//04
        try {
            String amico = dis.readUTF();
            
            ComunicazioneAmicoOnline comunicazione;
            
            RicevitoreMessaggiDaClient connessioneAmico = ElencoConnessioniAttive.cercaConnessione(amico);
            if(connessioneAmico == null){
                comunicazione = new ComunicazioneAmicoOnline(amico, false);
            }else{
                comunicazione = new ComunicazioneAmicoOnline(amico, true);
            }
            
            invia("ONLINE ANSWER", comunicazione);
            
            System.out.println("Inviato stato online di " + amico + " a " + username);
            
        } catch (IOException ex) {
            System.err.println(ex.getMessage());
        }

    }
    
    private void notificaStatoOnlineUtenteInBroadcast(boolean online){//05
        List<RicevitoreMessaggiDaClient> listaConnessioni = ElencoConnessioniAttive.getListaConnessioni();
        ComunicazioneAmicoOnline comunicazione = new ComunicazioneAmicoOnline(username, online);
        
        for(RicevitoreMessaggiDaClient connessione : listaConnessioni){
            if(connessione.getUsername().equals(username) && !online){
                continue;
            }
            connessione.invia("ONLINE ANSWER", comunicazione);
        }
    }
    
    public void invia(String comando, Serializable messaggio){//06
        synchronized(socket){
            try{
                dos.writeUTF(comando);
                oos.writeObject(messaggio);
            }catch(IOException ioe){
                System.err.println(ioe.getMessage());
            }
        }
    }
    
    @Override
    public void run(){
        try{
           ois = new ObjectInputStream(socket.getInputStream());
           oos = new ObjectOutputStream(socket.getOutputStream());
           dis = new DataInputStream(socket.getInputStream());
           dos = new DataOutputStream(socket.getOutputStream());
        }catch(IOException ioe){
            System.err.println(ioe.getMessage());
        }
        
        while(true){//07
            try {
                String comando = dis.readUTF();
                System.out.println("Ricevuto comando " + comando);
                switch(comando){
                    case "LOGIN":
                        loginUtente();
                        break;
                    case "ONLINE REQUEST":
                        comunicaAmicoOnline();
                        break;
                    case "MESSAGE":
                        inoltraMessaggio();
                        break;
                    case "LOGOUT":
                        logoutUtente();
                        return;
                                              
                }
            } catch (IOException ex) {
                System.err.println(ex.getMessage());
            }
        }
    }
}

/*

NOTE:
(01) Classe che implementa la gestione della connessione lato server con un singolo client, secondo l'approccio server multithreaded.

(02) Metodo invocato quando un client decide di fare login:
     (02.a) Dopo aver recuperato lo username scelto dall'utente, si comunica a tutti gli altri client attualmente connessi che l'utente
            gestito da questa istanza di RicevitoreMessaggiDaClient e' raggiungibile tramite il suo username
     (02.b) Al momento del login, vengono caricati gli eventuali messaggi bufferizzati destinati all'utente mentre questi era offline 
     (02.c) Se non ci sono messaggi bufferizzati per questo utente, il metodo non fa altro
     (02.d) Altrimenti, se ci fossero uno o piu' messaggi bufferizzati destinati all'utente, questi gli vengono recapitati nell'ordine
            in cui sono stati inviati
     (02.e) Dopo l'invio dei messaggi, si procede alla loro eliminazione dall'archivio dei messaggi bufferizzati

(03) Metodo invocato quando il client chiede al server di recapitare un messaggio ad un altro utente:
     (03.a) Si cerca lo username destinatario tra gli utenti attualmente attivi.
     (03.b) Se l'utente non e' tra gli utenti attivi, e' offline, si bufferizza il messaggio, altrimenti glielo si recapita sfruttando
            il socket che il client destinatario condivide con un'altra istanza di RicevitoreMessaggiDaClient

(04) Metodo invocato quando il client chiede al server di informarlo sullo stato online/offline di un utente richiesto dal client stesso.
     Ricerca lo username dell'utente richiesto dal client, successivamente crea un oggetto ComunicazioneAmicoOnline in cui scrive l'esito della ricerca
     e lo invia al client.

(05) Metodo invocato dopo che un utente ha fatto login oppure logout. Si recupera l'elenco degli utenti attivi in un determinato momento, e per ognuno
     si invia un oggetto ComunicazioneAmicoOnline contenente il nome dello username che ha fatto login/logout e il suo stato online/offline attuale.

(06) Metodo synchronized sul socket. La mutua esclusione è necessaria per evitare che due ricevitori scrivano contemporaneamente sullo stesso socket,
     ad esempio se si sta inoltrando un messaggio verso un certo client e contemporaneamente un utente effettua il login, provocando la corrispondente
     comunicazione a tutti gli altri utenti, si avrebbe certamente una collisione sul socket e le due comunicazioni andrebbero perdute.

(07) Continuamente si cercano nuovi messaggi dal client, per poi invocare il metodo corretto per il comando ricevuto.

*/