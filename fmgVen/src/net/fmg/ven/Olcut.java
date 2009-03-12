/*
 * Olcut.java
 *
 * Created on December 20, 2006, 1:45 PM
 * (4.12.06 tarihli SpringDaoDeneme �al��mas�ndan derlenmi�tir)
 *
 * Ven - Ayar Yerine Gelenek veritaban� eri�im nesnesi
 */

package net.fmg.ven;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.fmg.ven.arac.Cevir;

/**
 * Ven i�in �l��tlerin belirlenebilece�i s�n�f
 * @author Fatih Mehmet G�ler
 */
public class Olcut {
    private String metin;
    private Map parametreler;
    private Set baglaclar;
    
    /** 
     * Yeni �l��t
     */
    public Olcut() {
        this.metin = "";
        this.parametreler = new HashMap();
        this.baglaclar =  new HashSet();
        //{"and", "or", "=", "<>", "<", ">"}
    }
    
    /**
     * YAPILMADI
     */
    public Olcut ekle(Olcut olcut){        
        return null;
    }
    
    /**
     * YAPILMADI
     */
    public Olcut ekle(List olcutler){        
        return null;
    }
    
    /**
     * YAPILMADI
     */
    public Olcut ve(){
        return null;
    }
    
    /**
     * ko�ul ekle
     */
    public Olcut ekle(String kosul){
        this.metin +=" "+kosul;
        return this;
    }
    
    /**
     * ko�ullarda kullan�lan parametreleri ekle
     */
    public Olcut ekle(String parametre, Object nesne){
        this.parametreler.put(parametre,nesne);
        return this;
    }
    
    /**
     * ko�ullarda kullan�lan parametreleri t�mden ekle
     */
    public Olcut ekle(Map parametreler){
        this.parametreler.putAll(parametreler);
        return this;
    }
    
    public String olcutleriAl(){
        //((1=1) and ((1=1) and (musteri_numuneler_numune_sahibi.rapor_tarihi is null) and (musteri.no = 4)))
        //Musteri.numuneler.deneyler.deneyTip.ad like :p1
        String sonuc = "";
        metin = metin.replace("(","( ");
        metin = metin.replace(")"," )");
        String[] parcalar = metin.split(" ");
        for (int i = 0; i < parcalar.length; i++) {
            if (!parcalar[i].startsWith(":") && parcalar[i].contains(".")){
                int sonNokta = parcalar[i].lastIndexOf('.');                
                sonuc += " ";
                int u = parcalar[i].length();                
                sonuc +=Cevir.vt(parcalar[i].substring(0,sonNokta).replace('.','_'));
                sonuc +=Cevir.vt(parcalar[i].substring(sonNokta,u));
            }else{
                sonuc += " "+parcalar[i];
            }
        }
        return sonuc;
    }
    
    public Map parametreler(){
        return this.parametreler;
    }

    public String toString() {
        return olcutleriAl()+" "+this.parametreler;
    }
    
}
