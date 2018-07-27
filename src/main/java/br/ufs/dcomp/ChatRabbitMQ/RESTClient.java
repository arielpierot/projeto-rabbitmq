package br.ufs.dcomp.ChatRabbitMQ;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
 
public class RESTClient extends Thread
{
    public void run()
    {
        try {
            
            // JAVA 8 como pré-requisito (ver README.md)
            
            String username = "mqadmin";
            String password = "mqadmin";
     
            String usernameAndPassword = username + ":" + password;
            String authorizationHeaderName = "Authorization";
            String authorizationHeaderValue = "Basic " + java.util.Base64.getEncoder().encodeToString( usernameAndPassword.getBytes() );
     
            // Perform a request
            String restResource = "https://34.218.190.30:15672";
            Client client = ClientBuilder.newClient();
            Response resposta = client.target( restResource )
            	//.path("/api/exchanges/iagffzqu/ufs/bindings/source") // lista todos os binds que tem "ufs" como source	
                .path("/api/exchanges")
            	.request(MediaType.APPLICATION_JSON)
                .header( authorizationHeaderName, authorizationHeaderValue ) // The basic authentication header goes here
                .get();     // Perform a post with the form values
           
            if (resposta.getStatus() == 200) {
            	String json = resposta.readEntity(String.class);
                System.out.println(json);
            }    
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
}