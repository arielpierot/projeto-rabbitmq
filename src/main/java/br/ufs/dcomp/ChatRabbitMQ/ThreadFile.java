package br.ufs.dcomp.ChatRabbitMQ;

import com.rabbitmq.client.*;
import java.io.IOException;
import java.util.*;
import java.text.*;
import java.io.*;
import java.nio.file.*;
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
            
            File file_upload = new File(arquivoUpload);
            byte[] arquivoUploadBytes = getBytes(file_upload);
            conteudoBuilder.setCorpo(ByteString.copyFrom(arquivoUploadBytes));
            
            // MIME e Nome do arquivo
            Path source = Paths.get(arquivoUpload);
            String tipoMime = Files.probeContentType(source);
            conteudoBuilder.setNome(file_upload.getName());
            conteudoBuilder.setTipo(tipoMime);
            
            msgBuilder.setConteudo(conteudoBuilder);
          
            MensagemProto.Mensagem arquivoBuilded = msgBuilder.build();
            
            byte[] arquivoBytes = arquivoBuilded.toByteArray();
            
            if(grupoNome.length() == 0)
                channel.basicPublish("", usuarioReceptor, null, arquivoBytes);
            else
                channel.basicPublish(grupoNome, "", null, arquivoBytes);
                
            //channel.close();
        
        
        } catch (Exception e){
            
        }
    }
    
    public byte[] getBytes(File file) {
        
        int len = (int)file.length();  
        byte[] sendBuf = new byte[len];
        FileInputStream inFile  = null;
        try {
            inFile = new FileInputStream(file);         
            inFile.read(sendBuf, 0, len);  
        } catch (FileNotFoundException fnfex) {
        
        } catch (IOException ioex) {
            
        }
        
        return sendBuf;
    }
    
  
}