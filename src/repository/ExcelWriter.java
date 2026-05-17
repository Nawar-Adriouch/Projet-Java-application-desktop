package repository;
import model.Planning;
import model.Soutenance;
import java.util.List;


public interface ExcelWriter {

    void exporterPlanning(Planning planning, String cheminSortie);


    void exporterFiches(List<Soutenance> soutenances, String cheminSortie);
}