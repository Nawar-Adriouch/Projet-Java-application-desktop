package model;

public class Professeur {
   private  String id ;
   private String nom;
   private String prenom;
   private String departement;
   private String specialite;


    public Professeur(String id, String nom,String prenom,String departement,String specialite){
        this.id=id;
        this.nom=nom;
        this.prenom=prenom;
        this.departement=departement;
        this.specialite=specialite;
    }

    public String getId(){return id;}
    public String getNom(){return nom;}
    public String getPrenom(){return prenom;}
    public String getDepartement(){return departement;}
    public String getSpecialite(){return specialite;}

    @Override
    public String toString(){
        return id+" "+nom+" "+departement+" "+specialite+" " ;
    }

    public boolean estAnglais(){

        return "anglais".equalsIgnoreCase(this.specialite);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Professeur)) return false;
        return this.id.equals(((Professeur) o).id);
    }

}
