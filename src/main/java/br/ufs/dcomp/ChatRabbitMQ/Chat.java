package br.ufs.dcomp.ChatRabbitMQ;

import com.rabbitmq.client.*;
import java.io.IOException;
import java.util.*;
import java.text.*;

public class Chat {

  public static void main(String[] argv) throws Exception {
    
    Scanner scanner = new Scanner(System.in);
    
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost("34.219.46.92");
    factory.setUsername("admin");
    factory.setPassword("admin");
    factory.setVirtualHost("/");
    Connection connection = factory.newConnection();
    Channel channel = connection.createChannel();
    
    System.out.print("User: ");
    String usuario = scanner.nextLine();
    
    channel.queueDeclare(usuario, false, false, false, null);
    
    String usuarioReceptor = "";
    
    Consumer consumer = new DefaultConsumer(channel) {
      public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body)
          throws IOException {
        String message = new String(body, "UTF-8");
      
        System.out.println();
        System.out.println(message);
        
        String emissor = message.substring(message.indexOf("@") + 1, message.indexOf(" diz"));
        System.out.print("@" + emissor + " >> ");
        
      }
    };
    
    channel.basicConsume(usuario, true, consumer);
    
    while(true)
    {
      String mensagem = "";
      
      if(usuarioReceptor.length() == 0)
      {
        System.out.print(">> ");
        mensagem = scanner.nextLine(); 
      }
      else
      {
        System.out.print("@" + usuarioReceptor + " >> ");
        mensagem = scanner.nextLine();
      }
    
      if(mensagem.contains("@"))
      {
        int tamanhoMensagem = mensagem.length();
        usuarioReceptor = mensagem.substring(1,tamanhoMensagem);
      }
      else
      {
        
        String data = new SimpleDateFormat("dd/MM/yyyy").format(new Date());
        String horario = new SimpleDateFormat("HH:mm").format(new Date());
        
        mensagem = "("+ data +" às " + horario + ") @" + usuario + " diz: " + mensagem;
        channel.basicPublish("", usuarioReceptor, null, mensagem.getBytes("UTF-8"));
      }
      
      if(mensagem.contains("exit"))
      {
        channel.close();
        connection.close();
        break;
      }
        
    }
  }
}