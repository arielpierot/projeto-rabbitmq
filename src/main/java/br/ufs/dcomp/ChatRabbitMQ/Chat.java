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
    
    Boolean msgGrupo = false;
    
    channel.queueDeclare(usuario, false, false, false, null);
    
    String usuarioReceptor = "";
    String grupoNome = "";
    String mensagem = "";
    
    
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
        
        System.out.println("");
        
        if(grupo.length() == 0)
          System.out.println("("+ data + " às " + hora +") " + emissor + " diz: " + corpo);
        else
          System.out.println("("+ data + " às " + hora +") " + emissor + "#" + grupo + " diz: " + corpo);
          
      }
    };
    
    channel.basicConsume(usuario, true, consumer);
    
    System.out.print(">> ");
    mensagem = scanner.nextLine();
    
    while(true && usuario.length() > 0)
    {
      
      Boolean comandoAtivo = false;
      Boolean mensagemEnviada = false;
      
      int tamanhoMensagem = mensagem.length();
      
      if(tamanhoMensagem == 0)
        break;
      
      if(mensagem.substring(0, 1).equals("@"))
      {
        grupoNome = "";
        comandoAtivo = true;
        usuarioReceptor = mensagem.substring(1,tamanhoMensagem);
        msgGrupo = false;
        
        System.out.print("@" + usuarioReceptor + " >> ");
      }
      else if(mensagem.substring(0, 1).equals("!"))
      {
        comandoAtivo = true;
        String[] comando = mensagem.split(" ");
  
        if(mensagem.contains("!addGroup"))
           channel.exchangeDeclare(comando[1], "fanout");
        else if (mensagem.contains("!addUser"))
          channel.queueBind(comando[1], comando[2], "");
        else if (mensagem.contains("!removeGroup"))
          channel.exchangeDelete(comando[1], false);
        else if (mensagem.contains("!delFromGroup"))
          channel.queueUnbind(comando[1], comando[2], "");
          
        if(usuarioReceptor.length() > 0)
          System.out.print("@" + usuarioReceptor + " >> ");
        else if(grupoNome.length() > 0)
          System.out.print("#" + grupoNome + " >> ");
      }
      else if(mensagem.substring(0, 1).equals("#"))
      {
        usuarioReceptor = "";
        comandoAtivo = true;
        grupoNome = mensagem.substring(1, tamanhoMensagem);
        msgGrupo = true;
        
        System.out.print("#" + grupoNome + " >> ");
      }
      
      if(msgGrupo && !comandoAtivo)
      {
        System.out.print("#" + grupoNome + " >> ");
        enviar_mensagem_grupo(mensagem, usuario, grupoNome, channel);
        mensagem = scanner.nextLine();
      }
      else if(!comandoAtivo)
      {
        System.out.print("@" + usuarioReceptor + " >> ");
        enviar_mensagem(mensagem, usuario, usuarioReceptor, channel);
        mensagem = scanner.nextLine();
      }
      else
        mensagem = scanner.nextLine();
      
      
    }
    
    channel.close();
    connection.close();
    
  }
  
  public static void enviar_mensagem(String mensagem, String usuario, String usuarioReceptor, Channel channel) throws IOException
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
  
  public static void enviar_mensagem_grupo(String mensagem, String usuario, String grupoNome, Channel channel) throws IOException
  {
    MensagemProto.Mensagem.Builder msgBuilder = MensagemProto.Mensagem.newBuilder();
    msgBuilder.setEmissor(usuario);
    msgBuilder.setGrupo(grupoNome);
    msgBuilder.setData(new SimpleDateFormat("dd/MM/yyyy").format(new Date()));
    msgBuilder.setHora(new SimpleDateFormat("HH:mm").format(new Date()));
    

    MensagemProto.Conteudo.Builder conteudoBuilder = MensagemProto.Conteudo.newBuilder();
    conteudoBuilder.setTipo("text/plain");
    conteudoBuilder.setNome("Nova msg");
    conteudoBuilder.setCorpo(ByteString.copyFrom(mensagem.getBytes("UTF-8")));
    
    msgBuilder.setConteudo(conteudoBuilder);
  
    MensagemProto.Mensagem mensagemBuilded = msgBuilder.build();
    
    byte[] mensagemBytes = mensagemBuilded.toByteArray();
    
    channel.basicPublish(grupoNome, "", null, mensagemBytes);
  }
}