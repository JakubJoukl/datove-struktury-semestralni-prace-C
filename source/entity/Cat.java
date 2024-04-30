package source.entity;

import java.io.Serializable;

public class Cat implements Serializable {
    private int id;
    private String nazev;
    private double vaha;

    public Cat(int id, String nazev, double vaha) {
        this.id = id;
        this.nazev = nazev;
        this.vaha = vaha;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNazev() {
        return nazev;
    }

    public void setNazev(String nazev) {
        this.nazev = nazev;
    }

    public double getVaha() {
        return vaha;
    }

    public void setVaha(double vaha) {
        this.vaha = vaha;
    }

    @Override
    public String toString() {
        return "Cat{" +
                "id=" + id +
                ", nazev='" + nazev + '\'' +
                ", vaha=" + vaha +
                '}';
    }
}