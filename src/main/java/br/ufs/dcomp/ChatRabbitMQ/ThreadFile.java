package br.ufs.dcomp.ChatRabbitMQ;

import com.rabbitmq.client.*;
import java.io.IOException;
import java.util.*;
import java.text.*;
import com.google.protobuf.util.JsonFormat;
import com.google.protobuf.ByteString;

public class ThreadFile extends Thread {

    String arquivoUpload;
    String usuario;
    String usuarioReceptor;
    String grupoNome;
    Channel channel;
    
    public ThreadFile(String arquivoUpload, String usuario, String usuarioReceptor, String grupoNome, Channel channel){
        this.arquivoUpload = arquivoUpload;
        this.usuario = usuario;
        this.usuarioReceptor = usuarioReceptor;
        this.grupoNome = grupoNome;
        this.channel = channel;
    }
    
    public void run(){
        try{
        
            MensagemProto.Mensagem.Builder msgBuilder = MensagemProto.Mensagem.newBuilder();
            msgBuilder.setEmissor(usuario);
            msgBuilder.setData(new SimpleDateFormat("dd/MM/yyyy").format(new Date()));
            msgBuilder.setHora(new SimpleDateFormat("HH:mm").format(new Date()));
        
            MensagemProto.Conteudo.Builder conteudoBuilder = MensagemProto.Conteudo.newBuilder();
            conteudoBuilder.setTipo("pdf");
            
            conteudoBuilder.setCorpo(ByteString.copyFrom(arquivoUpload.getBytes("UTF-8")));
            
            conteudoBuilder.setNome("Novo arquivo");
            
            msgBuilder.setConteudo(conteudoBuilder);
          
            MensagemProto.Mensagem arquivoBuilded = msgBuilder.build();
            
            byte[] arquivoBytes = arquivoBuilded.toByteArray();
            
            if(grupoNome.length() == 0)
                channel.basicPublish("", usuarioReceptor, null, arquivoBytes);
            else
                channel.basicPublish(grupoNome, "", null, arquivoBytes);
                
            channel.close();
        
        
        } catch (Exception e){
            
        }
    }
    
  
}