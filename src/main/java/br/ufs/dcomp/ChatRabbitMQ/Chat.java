package br.ufs.dcomp.ChatRabbitMQ;

import com.rabbitmq.client.*;
import java.io.IOException;
import java.util.*;
import java.text.*;
import com.google.protobuf.util.JsonFormat;
import com.google.protobuf.ByteString;

public class Chat {

  public static void main(String[] argv) throws Exception {
    
    Scanner scanner = new Scanner(System.in);
    
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost("54.212.207.13");
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
            
        MensagemProto.Mensagem mensagemRecebida = MensagemProto.Mensagem.parseFrom(body);
        String emissor = mensagemRecebida.getEmissor();
        String data = mensagemRecebida.getData();
        String hora = mensagemRecebida.getHora();
        String grupo = mensagemRecebida.getGrupo();
        
        MensagemProto.Conteudo conteudoRecebido = mensagemRecebida.getConteudo();
        String tipo = conteudoRecebido.getTipo();
        String corpo = new String(conteudoRecebido.getCorpo().toByteArray());
        String nome = conteudoRecebido.getNome();
        
        System.out.println("("+ data + " às " + hora +") " + emissor + " diz: " + corpo);
        
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
    
    
      
      
      if(mensagem.length()> 0 && mensagem.substring(0, 1).matches("[@]"))
      {
        int tamanhoMensagem = mensagem.length();
        usuarioReceptor = mensagem.substring(1,tamanhoMensagem);
      }
      else if(mensagem.length()> 0 && mensagem.substring(0, 1).matches("[!]"))
      {
        if(mensagem.contains("!addGroup"))
        {
           String[] comando = mensagem.split(" ");
           channel.exchangeDeclare(comando[1], "fanout");
             
        }
        else if (mensagem.contains("!addUser"))
        {
          String[] comando = mensagem.split(" ");
          channel.queueBind(comando[1], comando[2], "");
        }
        else if (mensagem.contains("!removeGroup"))
        {
          String[] comando = mensagem.split(" ");
          channel.exchangeDelete(comando[1], false);
        }
        else if (mensagem.contains("!delFromGroup"))
        {
          String[] comando = mensagem.split(" ");
          channel.queueUnbind(comando[1], comando[2], "");
        }
      }
      else
      {
        
        MensagemProto.Mensagem.Builder msgBuilder = MensagemProto.Mensagem.newBuilder();
        msgBuilder.setEmissor(usuario);
        msgBuilder.setData(new SimpleDateFormat("dd/MM/yyyy").format(new Date()));
        msgBuilder.setHora(new SimpleDateFormat("HH:mm").format(new Date()));
        
        MensagemProto.Conteudo.Builder conteudoBuilder = MensagemProto.Conteudo.newBuilder();
        conteudoBuilder.setTipo("text/plain");
        
        conteudoBuilder.setCorpo(ByteString.copyFrom(mensagem.getBytes("UTF-8")));
        
        conteudoBuilder.setNome("Nova Mensagem");
        
        msgBuilder.setConteudo(conteudoBuilder);
        
        MensagemProto.Mensagem mensagemBuilded = msgBuilder.build();
    
        byte[] mensagemBytes = mensagemBuilded.toByteArray();
        
        channel.basicPublish("", usuarioReceptor, null, mensagemBytes);
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