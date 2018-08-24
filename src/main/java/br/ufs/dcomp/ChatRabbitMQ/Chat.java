package br.ufs.dcomp.ChatRabbitMQ;

import com.rabbitmq.client.*;
import java.io.IOException;
import java.util.*;
import java.text.*;
import com.google.protobuf.util.JsonFormat;
import com.google.protobuf.ByteString;
import java.io.*;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;

public class Chat {
  
  static String usuario;
  static String usuarioReceptor;
  static String grupoReceptor;
  static boolean msgGrupo = false;

  public static void main(String[] argv) throws Exception {
    
    Scanner scanner = new Scanner(System.in);
    
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost("34.218.190.30");
    factory.setUsername("admin");
    factory.setPassword("admin");
    factory.setVirtualHost("/");
    Connection connection = factory.newConnection();
    Channel channel = connection.createChannel();
    
    Channel channelFile = connection.createChannel();
    
    System.out.print("User: ");
    usuario = scanner.nextLine();
    
    String usuarioUpload = usuario + "_upload";
    
    channel.queueDeclare(usuario, false, false, false, null);
    channelFile.queueDeclare(usuarioUpload, false, false, false, null);
    
    String grupoNome = "";
    String mensagem = "";
    
    RESTClient restClient = new RESTClient();
    
    
    // Consumidor fila usuário
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
        
        if(!tipo.equals("message"))
        {
          
          byte[] arquivoRecebidoBytes = conteudoRecebido.getCorpo().toByteArray();
          
          final String diretorio = "/home/ubuntu/workspace/sd/Chat/uploads/" + usuario;
          
          String arquivo = nome;
          
          System.out.println("("+ data + " às " + hora +")" + " Arquivo \"" + arquivo + "\" recebido de @" + emissor+ " !");
          File diretorioFile = new File(diretorio);
          
          if (!diretorioFile.exists() && !diretorioFile.isDirectory()) {
            diretorioFile.mkdir();
          }
          
          File file = new File(diretorio + "/"+ arquivo);
          BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
          bos.write(arquivoRecebidoBytes);
          bos.close();
          
          
          // resposta de arquivo recebido
          if(grupo.length() == 0 && usuario != null)
          {
            if(usuarioReceptor == null)
            {
              System.out.print(">> ");
            }
            else 
            {
              if(msgGrupo)
              {
                if(grupo.length() == 0)
                {
                  System.out.print("#" + grupoReceptor + " >> ");
                }
                else
                {
                  System.out.print("#" + grupo + " >> ");
                }
              }
              else
              {
                System.out.print("@" + usuarioReceptor + " >> "); 
              }
            }
          }
          else if(usuario != null)
          {
            if(msgGrupo)
            {
              if(grupo.length() == 0)
              {
                System.out.print("#" + grupoReceptor + " >> ");
              }
              else if(grupoReceptor.length() > 0)
              {
                System.out.print("#" + grupoReceptor + " >> ");
              }
              else
              {
                System.out.print("#" + grupo + " >> ");
              }
            }
            else {
              if(usuarioReceptor == null){
                System.out.print(">> ");
              }
              else {
                System.out.print("@" + usuarioReceptor + " >> ");
              }
            }
            
          }
          else
          {
            System.out.print(">> ");
          }
          
        // fim
        
        } else {
          if(grupo.length() == 0 && usuario != null)
          {
            if(usuarioReceptor == null)
            {
              System.out.println("("+ data + " às " + hora +") " + emissor + " diz: " + corpo);
              System.out.print(">> ");
            }
            else 
            {
              if(msgGrupo)
              {
                if(grupo.length() == 0)
                {
                  System.out.println("("+ data + " às " + hora +") " + emissor + " diz: " + corpo);
                  System.out.print("#" + grupoReceptor + " >> ");
                }
                else
                {
                  System.out.println("("+ data + " às " + hora +") " + emissor + " diz: " + corpo);
                  System.out.print("#" + grupo + " >> ");
                }
              }
              else
              {
                System.out.println("("+ data + " às " + hora +") " + emissor + " diz: " + corpo);
                System.out.print("@" + usuarioReceptor + " >> "); 
              }
            }
          }
          else if(usuario != null)
          {
            if(msgGrupo)
            {
              if(grupo.length() == 0)
              {
                System.out.println("("+ data + " às " + hora +") " + emissor + " diz: " + corpo);
                System.out.print("#" + grupoReceptor + " >> ");
              }
              else if(grupoReceptor.length() > 0)
              {
                System.out.println("("+ data + " às " + hora +") " + emissor + "#" + grupo + " diz: " + corpo);
                System.out.print("#" + grupoReceptor + " >> ");
              }
              else
              {
                System.out.println("("+ data + " às " + hora +") " + emissor + " diz: " + corpo);
                System.out.print("#" + grupo + " >> ");
              }
            }
            else {
              if(usuarioReceptor == null){
                System.out.println("("+ data + " às " + hora +") " + emissor + "#" + grupo + " diz: " + corpo);
                System.out.print(">> ");
              }
              else {
                System.out.println("("+ data + " às " + hora +") " + emissor + "#" + grupo + " diz: " + corpo);
                System.out.print("@" + usuarioReceptor + " >> ");
              }
            }
            
          }
          else
          {
            System.out.print(">> ");
          }
        }
        
      }
    };
    
    channel.basicConsume(usuario, true, consumer);
    channelFile.basicConsume(usuarioUpload, true, consumer);
    
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
        grupoReceptor = "";
        comandoAtivo = true;
        usuarioReceptor = mensagem.substring(1,tamanhoMensagem);
        msgGrupo = false;
        
        System.out.print("@" + usuarioReceptor + " >> ");
      }
      else if(mensagem.substring(0, 1).equals("#"))
      {
        usuarioReceptor = "";
        comandoAtivo = true;
        grupoNome = mensagem.substring(1, tamanhoMensagem);
        grupoReceptor = grupoNome;
        msgGrupo = true;
        
        System.out.print("#" + grupoNome + " >> ");
      }
      else if(mensagem.substring(0, 1).equals("!"))
      {
        comandoAtivo = true;
        String[] comando = mensagem.split(" ");
  
        if(mensagem.contains("!addGroup"))
           channel.exchangeDeclare(comando[1], "direct");
        else if (mensagem.contains("!addUser"))
        {
          channelFile.queueBind(comando[1] + "_upload", comando[2], "F");
          channel.queueBind(comando[1], comando[2], "T");
        }
        else if (mensagem.contains("!removeGroup"))
          channel.exchangeDelete(comando[1], false);
        else if (mensagem.contains("!delFromGroup"))
        {
         channelFile.queueUnbind(comando[1] + "_upload", comando[2], "F"); 
         channel.queueUnbind(comando[1], comando[2], "T"); 
        }
        else if (mensagem.contains("!upload"))
        {
          String arquivoUpload = comando[1];
          
          if(grupoReceptor.equals(""))
            System.out.println("Arquivo \"" + arquivoUpload +"\" foi enviado para @" + usuarioReceptor + "! ");
          else
            System.out.println("Arquivo \"" + arquivoUpload +"\" foi enviado para #" + grupoReceptor + "! ");
            
          ThreadFile arquivo = new ThreadFile(arquivoUpload, usuario, usuarioReceptor, grupoReceptor, channelFile);
          arquivo.start();
          
        }
        else if (mensagem.contains("!listGroups"))
        {
          String jsonStr = restClient.check("/api/exchanges");
          JSONArray arr = new JSONArray(jsonStr);
          
          String grupos = "";
          
          for (int i = 0; i < arr.length(); i++) {
            String USER_WHO_PERFORMED_ACTION = arr.getJSONObject(i).getString("user_who_performed_action");
            if(USER_WHO_PERFORMED_ACTION.equals("admin"))
            {
              grupos += arr.getJSONObject(i).getString("name") + ", ";
            }
            
          }
          
          System.out.println(grupos.substring(0, grupos.length() - 2)); // Pular linha no final
          
        }
        else if (mensagem.contains("!listUsers"))
        {
          
          if(comando.length > 1 && comando[1] != null)
          {
            String nomeGrupo = comando[1];
            
            String jsonStr =  restClient.check("/api/exchanges/%2F/" + nomeGrupo + "/bindings/source");
          
            JSONArray arr = new JSONArray(jsonStr);
            
            String usuarios = "";
            
            for (int i = 0; i < arr.length(); i++)
            {
              if(arr.getJSONObject(i).getString("routing_key").equals("T"))
                usuarios += arr.getJSONObject(i).getString("destination") + ", ";
            }
            
            if(usuarios.length() > 2)
              System.out.println(usuarios.substring(0, usuarios.length() - 2)); // Pular linha no final
            else
              System.out.println("Sem usuários.");
            
          }
          else
          {
            System.out.println("Digite corretamente: !listUsers [grupo]");
          }
          
        }
          
        if(usuarioReceptor != null && usuarioReceptor.length() > 0)
          System.out.print("@" + usuarioReceptor + " >> ");
        else if(grupoNome != null && grupoNome.length() > 0)
          System.out.print("#" + grupoNome + " >> ");
        else
          System.out.print(">> ");
      }
      
      if(msgGrupo && !comandoAtivo && usuario.length() > 0)
      {
        System.out.print("#" + grupoNome + " >> ");
        enviar_mensagem_grupo(mensagem, usuario, grupoNome, channel);
      }
      else if(!comandoAtivo && usuario.length() > 0)
      {
        System.out.print("@" + usuarioReceptor + " >> ");
        enviar_mensagem(mensagem, usuario, usuarioReceptor, channel);
      }
      else if(usuarioReceptor != null && usuarioReceptor.length() == 0)
        System.out.print(">> ");
        
       mensagem = scanner.nextLine();
    }
    
    channel.close();
    channelFile.close();
    connection.close();
    
  }
  
  public static void enviar_mensagem(String mensagem, String usuario, String usuarioReceptor, Channel channel) throws IOException
  {
    MensagemProto.Mensagem.Builder msgBuilder = MensagemProto.Mensagem.newBuilder();
    msgBuilder.setEmissor(usuario);
    msgBuilder.setData(new SimpleDateFormat("dd/MM/yyyy").format(new Date()));
    msgBuilder.setHora(new SimpleDateFormat("HH:mm").format(new Date()));

    MensagemProto.Conteudo.Builder conteudoBuilder = MensagemProto.Conteudo.newBuilder();
    conteudoBuilder.setTipo("message");
    
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
    conteudoBuilder.setTipo("message");
    conteudoBuilder.setNome("Nova Mensagem");
    conteudoBuilder.setCorpo(ByteString.copyFrom(mensagem.getBytes("UTF-8")));
    
    msgBuilder.setConteudo(conteudoBuilder);
  
    MensagemProto.Mensagem mensagemBuilded = msgBuilder.build();
    
    byte[] mensagemBytes = mensagemBuilded.toByteArray();
    
    channel.basicPublish(grupoNome, "T", null, mensagemBytes);
  }
}