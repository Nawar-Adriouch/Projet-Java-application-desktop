package model;

public class Etudiant {
    private String cne;
    private String nom;
    private String prenom;
    private String filiere;
    private String sujet;
    private String langue ;

    public Etudiant( String cne,String nom,String prenom,String filiere,String sujet,String langue ){
        this.cne=cne;
        this.nom=nom;
        this.prenom=prenom;
        this.filiere=filiere;
        this.sujet=sujet;
        this.langue=(langue!=null)?langue.toLowerCase():"français";
    }

    public String getCne(){return cne ;}
    public String getNom(){return nom;}
    public String getPrenom(){return prenom;}
    public String getFiliere(){return filiere;}
    public String getSujet(){return sujet;}
    public String getLangue(){return langue;}

    public boolean isAnglophone(){return "anglais".equals(langue);}

    @Override
    public String toString(){
        return cne+" "+nom+" "+filiere+" "+langue+" ";
    }

}
