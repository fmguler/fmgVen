/*
 * Cevir.java
 *
 * Created on December 18, 2006, 10:51 AM
 * (4.12.06 tarihli SpringDaoDeneme �al��mas�ndan derlenmi�tir)
 *
 * Ven - Ayar Yerine Gelenek veritaban� eri�im nesnesi
 */

package net.fmg.ven.arac;

import java.util.Locale;

/**
 * Deve harflerle yaz�lm�� metni veritaban� �ekline �evirir
 * @author Fatih Mehmet G�ler
 */
public class Cevir {
    
    /** Creates a new instance of Cevir */
    public Cevir() {
    }
    
    public static String vt(String deve){
        if (deve.equals("")) return "";
        StringBuffer sonuc = new StringBuffer();
        sonuc.append(deve.substring(0,1).toLowerCase(Locale.ENGLISH));
        for (int i = 1; i < deve.length(); i++) {
            String ki = deve.substring(i,i+1);
            String kiKucuk = ki.toLowerCase(Locale.ENGLISH);            
            if (!ki.equals(kiKucuk)){
                sonuc.append("_"+kiKucuk);
            }else sonuc.append(ki);                            
        }
        return sonuc.toString();
    }
    
}
