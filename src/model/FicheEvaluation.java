package model;

public class FicheEvaluation {
    private Etudiant etudiant;
    private Encadrant encadrant;
    private Creneau creneau;
    private Salle salle;
    private Professeur jury1;
    private Professeur jury2;


    public FicheEvaluation(Etudiant etudiant, Encadrant encadrant, Creneau creneau, Salle salle,
                           Professeur jury1, Professeur jury2 ){
        this.etudiant=etudiant;
        this.encadrant=encadrant;
        this.creneau=creneau;
        this.salle=salle;
        this.jury1=jury1;
        this.jury2=jury2;

    }
    public Etudiant getEtudiant(){return etudiant;}
    public Encadrant getEncadrant(){return encadrant;}
    public Creneau getCreneau(){return creneau;}
    public Salle getSalle(){return salle;}
    public Professeur getJury1(){return jury1; }
    public Professeur getJury2(){return jury2;}

    public String toString(){
        return etudiant.getNom()+"  "+etudiant.getPrenom()+ " "+etudiant.getSujet()+" "+
                        etudiant.getFiliere()+" "+
                        encadrant.getNom()+"  "+encadrant.getPrenom()+" "+
                        jury1.getNom()+"  "+jury1.getPrenom()+" "+
                        jury2.getNom()+"  "+jury2.getNom()+" "+
                        salle.getNom()+ " "+
                        creneau.getDate()+" "+
                        creneau.getHeureDebut()+" "+creneau.getHeureFin();

    }


}