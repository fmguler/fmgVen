/*
 * SorguUretici.java
 *
 * Created on December 18, 2006, 9:32 AM
 * (4.12.06 tarihli SpringDaoDeneme çalýþmasýndan derlenmiþtir)
 *
 * Ven - Ayar Yerine Gelenek veritabaný eriþim nesnesi
 */

package net.fmg.ven;

import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import net.fmg.ven.arac.Cevir;
import net.fmg.ven.arac.SinifBildirenLinkedList;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

/**
 * Belirtilen sýnýfýn 'Ayar yerine gelenek' yaklaþýmýnda sorgusunu üretir
 * @author Fatih Mehmet Güler
 */
public class SorguUretici {
    private Set vtSiniflari;
    private Set nesnePaketleri;
    private Set baglar;
    private String selectCumlesi;
    private String fromCumlesi;
    
    /**
     * Creates a new instance of SorguUretici
     */
    public SorguUretici() {
        this.vtSiniflari = new HashSet();
        this.nesnePaketleri = new HashSet();
        this.vtSiniflari.add(Integer.class);
        this.vtSiniflari.add(String.class);
        this.vtSiniflari.add(Date.class);
        this.vtSiniflari.add(Double.class);
        this.vtSiniflari.add(Boolean.class);
    }
    
    private void tekrarlayanSorguUret(int seviye, String tabloAdi, String nesneYolu, Class nesneSinifi, Set baglar, StringBuffer selectCumlesi, StringBuffer fromCumlesi){
        BeanWrapper wr = new BeanWrapperImpl(nesneSinifi);
        PropertyDescriptor[] pdArr = wr.getPropertyDescriptors();
        for (int i = 0; i < pdArr.length; i++) {
            Class alanSinifi = pdArr[i].getPropertyType(); //alan sýnýfý
            String sutunAdi = Cevir.vt(pdArr[i].getName()); //sütun adý
            String alanAdi = pdArr[i].getName(); //alan adý
            if (vtSiniflari.contains(alanSinifi)){ //veritabaný direk alan (Integer,String,Date, vs)
                selectCumlesi.append(tabloAdi+"."+sutunAdi+" as "+tabloAdi+"_"+sutunAdi); //sütun
                selectCumlesi.append(", ");
            }
            if (alanSinifi.getPackage()!=null && nesnePaketleri.contains(alanSinifi.getPackage().getName()) && baglarKapsar(baglar,nesneYolu+"."+alanAdi)){ //domain nesnesi 1-1 join
                String bagTablosuDigerAd = tabloAdi+"_"+sutunAdi; // bað tablosu için diðer ad, çünkü ayný isimde birden fazla bað olabilir, karýþmasýn
                String bagTablosu = Cevir.vt(alanSinifi.getSimpleName());//gerçek bað tablosu
                fromCumlesi.append(" left join "+bagTablosu+" "+bagTablosuDigerAd);
                fromCumlesi.append(" on "+bagTablosuDigerAd+".no = "+tabloAdi+"."+sutunAdi+"_no");
                tekrarlayanSorguUret(++seviye,bagTablosuDigerAd,nesneYolu+"."+alanAdi,alanSinifi,baglar,selectCumlesi,fromCumlesi);
            }
            if (wr.getPropertyValue(alanAdi) instanceof SinifBildirenLinkedList && baglarKapsar(baglar,nesneYolu+"."+alanAdi)){
                Class cokluAlandakiNesneSinifi = (Class)wr.getPropertyValue(alanAdi+".nesneSinifi");
                String bagTablosuDigerAd = tabloAdi+"_"+sutunAdi; // bað tablosu için diðer ad, çünkü ayný isimde birden fazla bað olabilir, karýþmasýn
                String bagTablosu = Cevir.vt(cokluAlandakiNesneSinifi.getSimpleName());//gerçek bað tablosu
                String bagAlani = Cevir.vt((String)wr.getPropertyValue(alanAdi+".bagAlani")); //YAP: bunu vermeden de varsayýlan birþey yapsýn
                fromCumlesi.append(" left join "+bagTablosu+" "+bagTablosuDigerAd);
                fromCumlesi.append(" on "+bagTablosuDigerAd+"."+bagAlani+"_no = "+tabloAdi+".no");
                tekrarlayanSorguUret(++seviye,bagTablosuDigerAd,nesneYolu+"."+alanAdi,cokluAlandakiNesneSinifi,baglar,selectCumlesi,fromCumlesi);
            }
        }
    }
    
    private boolean baglarKapsar(Set baglar, String bag){
        Iterator it = baglar.iterator();
        while (it.hasNext()) {
            String str = (String) it.next();
            if (str.startsWith(bag)){
                if (str.length()==bag.length()) return true;
                else if(str.charAt(bag.length())=='.') return true;
            }
        }
        return false;
    }
    
    /**
     * Sql select sorgusu üretir
     */
    public String secmeSorgusuUret(Set baglar, Class nesneSinifi){
        //long t1 = System.currentTimeMillis();
        String nesneAdi = nesneSinifi.getSimpleName();
        String tabloAdi = Cevir.vt(nesneAdi);
        StringBuffer selectCumlesi = new StringBuffer("select ");
        StringBuffer fromCumlesi = new StringBuffer("from "+tabloAdi);
        tekrarlayanSorguUret(0,tabloAdi,nesneAdi,nesneSinifi,baglar,selectCumlesi,fromCumlesi);
        selectCumlesi.append(" 1=1");
        //System.out.println("Sorgu üretme zamaný="+(System.currentTimeMillis()-t1));        
        return selectCumlesi.toString()+" \n"+fromCumlesi.toString();
    }
    
    /**
     * Sql select sorgusu üretir, ancak alanlarý almaz count(distinct tabloadi.no) alýr
     */
    public String saymaSorgusuUret(Set baglar, Class nesneSinifi){
        //long t1 = System.currentTimeMillis();
        String nesneAdi = nesneSinifi.getSimpleName();
        String tabloAdi = Cevir.vt(nesneAdi);
        StringBuffer selectCumlesi = new StringBuffer();
        StringBuffer fromCumlesi = new StringBuffer("from "+tabloAdi);
        tekrarlayanSorguUret(0,tabloAdi,nesneAdi,nesneSinifi,baglar,selectCumlesi,fromCumlesi);
        //System.out.println("Sorgu üretme zamaný="+(System.currentTimeMillis()-t1));        
        return "select count(distinct "+tabloAdi+".no) \n"+fromCumlesi.toString();
    }
    
    
    /**
     * insert-update sorgusu üretir
     */
    public String guncellemeSorgusuUret(Object nesne){
        BeanWrapper wr = new BeanWrapperImpl(nesne);
        String nesneAdi = nesne.getClass().getSimpleName();
        String tabloAdi = Cevir.vt(nesneAdi);
        StringBuffer sorgu;
        PropertyDescriptor[] pdArr = wr.getPropertyDescriptors();
        
        boolean yeni = wr.getPropertyValue("no")==null;
        if (yeni){ //ekle
            sorgu = new StringBuffer("insert into "+tabloAdi+"(");
            StringBuffer degerler = new StringBuffer(" values(");
            for (int i = 0; i < pdArr.length; i++) {
                Class alanSinifi = pdArr[i].getPropertyType(); //alan sýnýfý
                String sutunAdi = Cevir.vt(pdArr[i].getName()); //sütun adý
                String alanAdi = pdArr[i].getName(); //alan adý
                if (alanAdi.equals("no")) continue; //YAP: Belki squenci bozar, ama bozmuyosa kaldýr
                if (vtSiniflari.contains(alanSinifi)){ //veritabaný direk alan (Integer,String,Date, vs)
                    sorgu.append(sutunAdi);
                    sorgu.append(",");
                    degerler.append(":"+alanAdi);
                    degerler.append(",");
                }
                if (alanSinifi.getPackage()!=null && nesnePaketleri.contains(alanSinifi.getPackage().getName())){ //nesne
                    sorgu.append(Cevir.vt(alanAdi)+"_no");
                    sorgu.append(",");
                    degerler.append(":"+alanAdi+".no");
                    degerler.append(",");
                }
            }
            sorgu.deleteCharAt(sorgu.length()-1);
            sorgu.append(")");
            degerler.deleteCharAt(degerler.length()-1);
            degerler.append(");");                    
            sorgu.append(degerler);            
            
        }else{ //güncelle
            sorgu = new StringBuffer("update "+tabloAdi+" set ");
            for (int i = 0; i < pdArr.length; i++) {
                Class alanSinifi = pdArr[i].getPropertyType(); //alan sýnýfý
                String sutunAdi = Cevir.vt(pdArr[i].getName()); //sütun adý
                String alanAdi = pdArr[i].getName(); //alan adý
                if (vtSiniflari.contains(alanSinifi)){ //veritabaný direk alan (Integer,String,Date, vs)
                    sorgu.append(sutunAdi+"=:"+alanAdi);
                    sorgu.append(",");
                }
                if (alanSinifi.getPackage()!=null && nesnePaketleri.contains(alanSinifi.getPackage().getName())){ //nesne
                    sorgu.append(sutunAdi+"_no=:"+alanAdi+".no");
                    sorgu.append(",");
                }
            }
            sorgu.deleteCharAt(sorgu.length()-1);
            sorgu.append(" where no = :no ;"); //YAP: sonuncu virgülü sil
        }
        return sorgu.toString();
    }
    
    public Set getNesnePaketleri() {
        return nesnePaketleri;
    }
    
    public void setNesnePaketleri(Set nesnePaketleri) {
        this.nesnePaketleri = nesnePaketleri;
    }
}
