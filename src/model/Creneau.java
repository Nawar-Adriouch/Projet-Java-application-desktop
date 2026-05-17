package model;
import java.time.LocalDate;
import java.time.LocalTime;

public class Creneau {
    private  LocalDate date;
    private  LocalTime heureDebut ;
    private  LocalTime heureFin;

    public Creneau(LocalDate date,LocalTime heureDebut, LocalTime heureFin){
        this.date=date;
        this.heureDebut=heureDebut;
        this.heureFin=heureFin;

    }
    public boolean estSuccessif(Creneau C){
        if(((this.heureFin.equals(C.heureDebut)) && (C.date.equals(this.date)))
                ||((this.heureDebut.equals(C.heureFin)) &&(this.date.equals(C.date)))){
            return true  ;
        }
        return false ;
    }

    public LocalTime getHeureDebut() {
        return heureDebut;
    }
    public LocalDate getDate() {
        return date;
    }
    public LocalTime getHeureFin(){
        return heureFin;
    }
    public String toString() {
        return date +" | "+ heureDebut+" | "+heureFin;
    }
}
