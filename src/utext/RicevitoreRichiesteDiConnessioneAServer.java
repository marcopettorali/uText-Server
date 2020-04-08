package utext;

import java.io.*;
import java.net.*;

public class RicevitoreRichiesteDiConnessioneAServer {
    private final static int porta = 7799;
    private static ServerSocket socketDiAscolto;
    
    public static void main(String[] args) {
        System.out.println("uText Server: Sto avviando l'applicazione:");
        try{
            socketDiAscolto = new ServerSocket(porta, 10);
        }catch(IOException ioe){
            System.err.println(ioe.getMessage());
        }
        System.out.println("Pronto.");
        while(true){//01
            try{
                Socket socket = socketDiAscolto.accept();
                RicevitoreMessaggiDaClient connessione = new RicevitoreMessaggiDaClient(socket);
                ElencoConnessioniAttive.aggiungiConnessione(connessione);
                connessione.setDaemon(true);//01.a
                connessione.start();
                System.out.println("Nuova connessione accettata");
            }catch(IOException ioe){
                System.out.println(ioe.getMessage());
            }
        }
    }  
}

/*

NOTE:
(01) Continuamente si cercano nuove richieste di connessione al server. Per ogni richiesta effettuata dai client, il server la accetta,
     crea un nuovo oggetto RicevitoreMessaggiDaClient e gli delega la gestione della connessione con un particolare client.
     (01.a) Il nuovo oggetto RicevitoreMessaggiDaClient viene attivato come daemon per far si che, alla chiusura del server, tutte le
            connessioni con gli utenti vengano abortite. Si presuppone, infatti, che se si vuole chiudere il server ci si aspetti che
            il server si chiuda immediatamente, e non quando tutte le connessioni con i client vengono chiuse.
     

*/