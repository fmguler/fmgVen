/*
 * Cevir.java
 *
 * Created on December 18, 2006, 10:51 AM
 * (4.12.06 tarihli SpringDaoDeneme çalışmasından derlenmiştir)
 *
 * Ven - Ayar Yerine Gelenek veritabanı erişim nesnesi
 */

package net.fmg.ven.arac;

import java.util.Locale;

/**
 * Deve harflerle yazılmış metni veritabanı şekline çevirir
 * @author Fatih Mehmet Güler
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

    public static String isim(String isim) {
        int i = isim.lastIndexOf(".");
        if (i < 0) return isim;
        return isim.substring(i + 1);
    }
    
}
