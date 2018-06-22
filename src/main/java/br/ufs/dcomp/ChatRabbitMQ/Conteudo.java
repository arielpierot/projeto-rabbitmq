package br.ufs.dcomp.ChatRabbitMQ;

import com.google.protobuf.ByteString;

public class Conteudo {

    private String tipo;
    private String corpo;
    private String nome;
    
    public Conteudo(String tipo, byte[] corpo, String nome){
        this.tipo = tipo;
        this.corpo = new String(corpo);
        this.nome = nome;
    }
    
    public Conteudo(String tipo, String corpo, String nome){
        this.tipo = tipo;
        this.corpo = corpo;
        this.nome = nome;
    }
    
    public String getTipo()
    {
        return tipo;
    }
    
    public void setTipo(String tipo)
    {
        this.tipo = tipo;
    }
    
    public String getCorpo()
    {
        return corpo;
    }
    
    public void setCorpo(String corpo)
    {
        this.corpo = corpo;
    }
    
    public String getNome()
    {
        return nome;
    }
    
    public void setNome(String nome)
    {
        this.nome = nome;
    }
  
}