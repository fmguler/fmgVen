/*
 * SinifBildirenLinkedList.java
 *
 * Created on December 16, 2006, 9:06 PM
 *
 * Fatih Mehmet G�ler
 */

package net.fmg.ven.arac;

import java.util.LinkedList;

/**
 * ��inde tuttu�u nesne t�r�n� bildiren linked list
 * @author Fatih
 */
public class SinifBildirenLinkedList extends LinkedList{
    private Class nesneSinifi;
    private String bagAlani = "";
    
    /**
     * Creates a new instance of SinifBildirenLinkedList
     */
    public SinifBildirenLinkedList() {                
        System.out.println("FmgList, normal LinkedList kipinde �al���yor...");
    }
    
    /**
     * i�indeki bile�en t�r� s�n�f� belirt
     */
    public SinifBildirenLinkedList(Class nesneSinifi) {
        this.nesneSinifi = nesneSinifi;
        System.out.println("**Uyar�-> bile�en alan� verilmeden ��zme hen�z yap�lmad�, sorgunuz �al��mayacak");
    }
    
    /**
     * i�indeki bile�en t�r� s�n�f� ve o s�n�ftaki hangi alana birle�ti�ini belirt
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

    //zorlamaya gerek yok, sadece t�r�n� belirlemek �abam�z
    //public boolean add(E o) {
    //    if (o.getClass()!=nesneSinifi) throw new RuntimeException("Desteklenmeyen bile�en t�r�!");        
    //    return super.add(o);
    //}

    public String getBagAlani() {
        return bagAlani;
    }

    public void setBagAlani(String bagAlani) {
        this.bagAlani = bagAlani;
    }
}
