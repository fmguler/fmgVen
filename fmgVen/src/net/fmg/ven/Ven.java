/*
 * Ven.java
 *
 * Created on December 18, 2006, 5:58 PM
 * (4.12.06 tarihli SpringDaoDeneme çalışmasından derlenmiştir)
 *
 * Ven - Ayar Yerine Gelenek veritabanı erişim nesnesi
 */

package net.fmg.ven;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.sql.DataSource;
import net.fmg.ven.arac.Cevir;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

/**
 * Veritabanı erişim nesnesi temel sınıf
 * @author Fatih Mehmet Güler
 */
public class Ven {
    private SorguUretici uretici;
    private SorguEslestirici eslestirici;
    private NamedParameterJdbcTemplate sablon;
    private Map kullanimlar;
    private boolean hataAyiklama = false;
    
    /** Creates a new instance of Ven */
    public Ven() {
        uretici = new SorguUretici();
        eslestirici = new SorguEslestirici();
        kullanimlar = new HashMap();
    }
    
    /**
     * Kullanım No: O kullanıma göre otomatik tespit edeceği bağları numaralandırır
     * <p> ÖNEMLİ: daha bitmedi :)
     */
    public List nesneleriAl(Class nesneSinifi, Integer kullanimNo){
        Set baglar = new HashSet();
        kullanimlar.put(kullanimNo,baglar);
        baglar.add(nesneSinifi.getSimpleName());
        
        String sorgu = uretici.secmeSorgusuUret(baglar, nesneSinifi);
        if(hataAyiklama) System.out.println("SQL: "+sorgu);
        
        List sonuc = eslestirici.listele(sorgu,new HashMap(),nesneSinifi);
        return sonuc;
    }
    
    /**
     * Gönderilen bağlara göre nesneyi ilişkileriyle birlikte veritabanından getirir
     */
    public List nesneleriAl(Class nesneSinifi, Set baglar){
        String sorgu = uretici.secmeSorgusuUret(baglar, nesneSinifi);
        if(hataAyiklama) System.out.println("SQL: "+sorgu);
        
        List sonuc = eslestirici.listele(sorgu,new HashMap(),nesneSinifi);
        return sonuc;
    }
    
    /**
     * Gönderilen bağlara göre nesneyi ilişkileriyle birlikte verilen ölçüte göre veritabanından getirir
     * Verilen ölçütlere göre
     */
    public List nesneleriAl(Class nesneSinifi, Set baglar, Olcut olcut){
        String sorgu = uretici.secmeSorgusuUret(baglar, nesneSinifi);
        sorgu += " where 1=1"+olcut.olcutleriAl();
        if(hataAyiklama) System.out.println("SQL: "+sorgu);
        
        List sonuc = eslestirici.listele(sorgu,olcut.parametreler(),nesneSinifi);
        return sonuc;
        
    }
    
    /**
     * Gönderilen bağlara göre nesneyi ilişkileriyle birlikte verilen ölçüte göre veritabanından kaç satır olacağını sayar
     * Verilen ölçütlere göre
     */
    public int nesneleriSay(Class nesneSinifi, Set baglar){
        String sorgu = uretici.saymaSorgusuUret(baglar, nesneSinifi);        
        if(hataAyiklama) System.out.println("SQL: "+sorgu);
        int sonuc = sablon.queryForInt(sorgu,new HashMap());
        return sonuc;
    }
    
    /**
     * Gönderilen bağlara göre nesneyi ilişkileriyle birlikte verilen ölçüte göre veritabanından kaç satır olacağını sayar
     * Verilen ölçütlere göre
     */
    public int nesneleriSay(Class nesneSinifi, Set baglar, Olcut olcut){
        String sorgu = uretici.saymaSorgusuUret(baglar, nesneSinifi);
        sorgu += " where 1=1"+olcut.olcutleriAl();
        if(hataAyiklama) System.out.println("SQL: "+sorgu);
        int sonuc = sablon.queryForInt(sorgu,olcut.parametreler());
        return sonuc;
    }
    
    /**
     * Gönderilen bağlara göre nesneyi ilişkileriyle birlikte veritabanından getirir
     * <p> Sadece Nesne
     */
    public Object nesneAl(Class nesneSinifi, Integer no, Set baglar){
        String sorgu = uretici.secmeSorgusuUret(baglar, nesneSinifi);
        Olcut olcut = new Olcut().ekle("and "+Cevir.vt(nesneSinifi.getSimpleName())+".no = :___no").ekle("___no",no);
        sorgu += " where 1=1"+olcut.olcutleriAl();
        if(hataAyiklama) System.out.println("SQL: "+sorgu);
        
        List sonuc = eslestirici.listele(sorgu,olcut.parametreler(),nesneSinifi);
        if (sonuc.size()==0) return null;
        if (sonuc.size()>1) System.out.println("**UYARI>> nesneAl birden fazla sonuç döndürüyor, haberin olsun");
        return sonuc.get(0);
    }
    
    /**
     * Gönderilen bağlara göre nesneyi ilişkileriyle birlikte verilen ölçüte göre veritabanından getirir
     * <p> Sadece Nesne, ölçütlere göre
     */
    public Object nesneAl(Class nesneSinifi, Integer no, Set baglar, Olcut olcut){
        String sorgu = uretici.secmeSorgusuUret(baglar, nesneSinifi);
        sorgu += " where 1=1 and "+Cevir.vt(nesneSinifi.getSimpleName())+".no = :___no "+olcut.olcutleriAl(); //No diğer ölçütlerden önce gelmeli order-limit için
        olcut.ekle("___no",no);
        if(hataAyiklama) System.out.println("SQL: "+sorgu);
        
        List sonuc = eslestirici.listele(sorgu,olcut.parametreler(),nesneSinifi);
        if (sonuc.size()==0) return null;
        if (sonuc.size()>1) System.out.println("**UYARI>> nesneAl birden fazla sonuç döndürüyor, haberin olsun");
        return sonuc.get(0);
        
    }
    
    public void nesneSakla(Object nesne){        
        String sorgu = uretici.guncellemeSorgusuUret(nesne);
        SqlParameterSource parametreKaynagi = new BeanPropertySqlParameterSource(nesne);
        sablon.update(sorgu,parametreKaynagi);
        //YAP: yeni eklenenin nosunu almak için daha etkin bir yöntem bulunabilir (üreticinin içinde deki beanwrapper kullanılabilir vs)
        BeanWrapper wr = new BeanWrapperImpl(nesne);
        if(wr.getPropertyValue("no")==null){            
            wr.setPropertyValue("no",new Integer(sablon.queryForInt("select currval('"+Cevir.vt(nesne.getClass().getSimpleName())+"_no')", new HashMap())));
        }
    }
    
    public void nesneSil(Integer no, Class nesneSinifi){
        String sorgu = "delete from "+Cevir.vt(nesneSinifi.getSimpleName())+" where no = :no ;";
        Map parametreler = new HashMap(2);
        parametreler.put("no",no);
        sablon.update(sorgu,parametreler);
    }
    
    //SETTERS--------------------------------------------
    public void setDataSource(DataSource dataSource){
        if (dataSource==null) throw new RuntimeException("DataSource null olamaz!!! Bu koşullar altında daha fazla çalışamam :)");
        this.sablon = new NamedParameterJdbcTemplate(dataSource);
        this.eslestirici.setSablon(sablon);
    }
    
    public void setHataAyiklama(boolean hataAyiklama) {
        this.hataAyiklama = hataAyiklama;
        eslestirici.setHataAyiklama(hataAyiklama);
    }
    
    public void setNesnePaketleri(Set nesnePaketleri) {
        uretici.getNesnePaketleri().addAll(nesnePaketleri);
        eslestirici.getNesnePaketleri().addAll(nesnePaketleri);
    }
    
    public NamedParameterJdbcTemplate getSablon() {
        return sablon;
    }
}
