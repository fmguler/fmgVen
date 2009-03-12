/*
 * SorguEslestirici.java
 *
 * Created on December 18, 2006, 10:40 AM
 * (4.12.06 tarihli SpringDaoDeneme çalýþmasýndan derlenmiþtir)
 *
 * Ven - Ayar Yerine Gelenek veritabaný eriþim nesnesi
 */

package net.fmg.ven;

import java.beans.PropertyDescriptor;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.fmg.ven.arac.Cevir;
import net.fmg.ven.arac.SinifBildirenLinkedList;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

/**
 * 'Ayar yerine gelenek' yaklaþýmýnda üretilmiþ sorgunun sonucunu belirtilen nesneye eþler
 * @author Fatih Mehmet Güler
 */
public class SorguEslestirici{
    private NamedParameterJdbcTemplate sablon;
    private Set vtSiniflari;
    private Set nesnePaketleri;
    private boolean hataAyiklama = false;
    private Map saklanmisSiniflar;
    
    /**
     * Creates a new instance of SorguEslestirici
     */
    public SorguEslestirici(){
        this.vtSiniflari = new HashSet();
        this.nesnePaketleri = new HashSet();
        this.saklanmisSiniflar = new HashMap();
        this.vtSiniflari.add(Integer.class);
        this.vtSiniflari.add(String.class);
        this.vtSiniflari.add(Date.class);
        this.vtSiniflari.add(Double.class);
        this.vtSiniflari.add(Boolean.class);
    }
    
    public List listele(String sorgu, Map parametreler, final Class nesneSinifi){
        long t1 = System.currentTimeMillis();
        final List sonuclar = new LinkedList();
        final String tabloAdi = Cevir.vt(nesneSinifi.getSimpleName());
        final Set sutunlar = new HashSet();
        
        sablon.query(sorgu,parametreler,new RowCallbackHandler(){
            public void processRow(ResultSet rs) throws SQLException {
                sutunAdlariniAl(sutunlar,rs);
                esle(rs,sutunlar,tabloAdi,nesneSinifi,sonuclar);
            }
        }
        );
        System.out.println("Listeleme zamaný="+(System.currentTimeMillis()-t1));        
        return sonuclar;
    }
    
    private void esle(ResultSet rs, Set sutunlar, String tabloAdi, Class nesneSinifi, List ustListe){
        try{
            if (!sutunlar.contains(tabloAdi+"_no")) return; //bu nesne sütunlar arasýnda hiç yok
            Object no = rs.getObject(tabloAdi+"_no");
            if (no==null) return; //bu nesne sütunlar arasýnda var ama null, muhtemelen left join den dolayý
            BeanWrapper wr=new BeanWrapperImpl(nesneSinifi); //Zaten class introspectionunu saklýyor (CachedIntrospectionResults.forClass())
            wr.setPropertyValue("no",no);
            Object nesne = wr.getWrappedInstance();
            boolean esle = true;
            for (Iterator it = ustListe.iterator(); it.hasNext();) { //listenin içinde indexOf ve get(i) ile birkaç kez dolaþmak yerinde bir kez dolaþmýþ olalým, onlar da ayný þeyi yapýyor çünkü.
                Object listedekiNesne = (Object) it.next();
                if (nesne.equals(listedekiNesne)){ //NOT: bunu no'larý karþýlaþtýrarak da yapabiliriz
                    wr.setWrappedInstance(listedekiNesne); //listede zaten var onu kullanmalýyýz
                    esle = false; // ve tekrar eþleme yapmamalýyýz
                    break;
                }
            }
            if (esle) ustListe.add(nesne); //bulamadýk, yani listede yok bunu ekliyoruz
            PropertyDescriptor[] pdArr = wr.getPropertyDescriptors();
            for (int i = 0; i < pdArr.length; i++) {
                PropertyDescriptor pd = pdArr[i];
                String alanAdi = Cevir.vt(pd.getName());
                Class alanSinifi = pd.getPropertyType();
                String sutun = tabloAdi+"_"+alanAdi;
                if (esle && vtSiniflari.contains(alanSinifi)){ //veritabaný nesneleri
                    if(sutunlar.contains(sutun)){
                        if(hataAyiklama) System.out.println(">>alan bulundu "+sutun);
                        wr.setPropertyValue(pd.getName(),rs.getObject(sutun));
                    }else{
                        if(hataAyiklama) System.out.println("--alan bulunamadý: "+sutun);
                    }
                }
                if (esle && alanSinifi.getPackage()!=null && getNesnePaketleri().contains(alanSinifi.getPackage().getName())){ //bire bir nesneler
                    if(sutunlar.contains(sutun+"_no")){
                        if(hataAyiklama) System.out.println(">>nesne bulundu "+sutun);
                        List list = new ArrayList(1); //tek sonuç olacaðýný biliyoruz
                        esle(rs,sutunlar,sutun,alanSinifi,list);
                        if(list.size()>0)wr.setPropertyValue(pd.getName(),list.get(0));
                    }else{
                        if(hataAyiklama) System.out.println("--nesne bulunamadý: "+sutun);
                    }
                }
                if ((SinifBildirenLinkedList) wr.getPropertyValue(pd.getName())  instanceof SinifBildirenLinkedList){ //çoklu nesneler
                    if(sutunlar.contains(sutun+"_no")){
                        if(hataAyiklama) System.out.println(">>liste bulundu "+sutun);
                        Class bagNesneSinifi = (Class)wr.getPropertyValue(pdArr[i].getName()+".nesneSinifi");
                        esle(rs,sutunlar,sutun,bagNesneSinifi,(List)wr.getPropertyValue(pd.getName()));
                    }else{
                        if(hataAyiklama) System.out.println("--liste bulunamadý: "+sutun);
                    }
                }
                
            }
        } catch(SQLException ex){
            ex.printStackTrace();
        }
    }
    
    private Set sutunAdlariniAl(Set sutunAdlari, ResultSet rs) throws SQLException{
        if (!sutunAdlari.isEmpty()) return sutunAdlari;
        for (int i=1; i<rs.getMetaData().getColumnCount()+1; i++) {
            sutunAdlari.add(rs.getMetaData().getColumnName(i));
        }
        return sutunAdlari;
    }
    
    public void setHataAyiklama(boolean hataAyiklama) {
        this.hataAyiklama = hataAyiklama;
    }
    
    public Set getNesnePaketleri() {
        return nesnePaketleri;
    }
    
    public void setNesnePaketleri(Set nesnePaketleri) {
        this.nesnePaketleri = nesnePaketleri;
    }
    
    public void setSablon(NamedParameterJdbcTemplate sablon) {
        this.sablon = sablon;
    }
}
