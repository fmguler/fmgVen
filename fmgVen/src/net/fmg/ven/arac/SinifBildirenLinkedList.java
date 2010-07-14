/*
 * SinifBildirenLinkedList.java
 *
 * Created on December 16, 2006, 9:06 PM
 *
 * Fatih Mehmet Güler
 */

package net.fmg.ven.arac;

import java.util.LinkedList;

/**
 * İçinde tuttuğu nesne türünü bildiren linked list
 * @author Fatih
 */
public class SinifBildirenLinkedList extends LinkedList{
    private Class nesneSinifi;
    private String bagAlani = "";
    
    /**
     * Creates a new instance of SinifBildirenLinkedList
     */
    public SinifBildirenLinkedList() {                
        System.out.println("FmgList, normal LinkedList kipinde çalışıyor...");
    }
    
    /**
     * içindeki bileşen türü sınıfı belirt
     */
    public SinifBildirenLinkedList(Class nesneSinifi) {
        this.nesneSinifi = nesneSinifi;
        System.out.println("**Uyarı-> bileşen alanı verilmeden çözme henüz yapılmadı, sorgunuz çalışmayacak");
    }
    
    /**
     * içindeki bileşen türü sınıfı ve o sınıftaki hangi alana birleştiğini belirt
     */
    public SinifBildirenLinkedList(Class nesneSinifi, String bagAlani) {
        this.nesneSinifi = nesneSinifi;
        this.bagAlani = bagAlani;
    }

    public Class getNesneSinifi() {
        return nesneSinifi;
    }

    public void setNesneSinifi(Class nesneSinifi) {
        this.nesneSinifi = nesneSinifi;
    }

    //zorlamaya gerek yok, sadece türünü belirlemek çabamız
    //public boolean add(E o) {
    //    if (o.getClass()!=nesneSinifi) throw new RuntimeException("Desteklenmeyen bileşen türü!");        
    //    return super.add(o);
    //}

    public String getBagAlani() {
        return bagAlani;
    }

    public void setBagAlani(String bagAlani) {
        this.bagAlani = bagAlani;
    }
}
