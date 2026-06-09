package model;
import java.util.*;
import java.util.Collections.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;


public class Planning {

    private List<Soutenance> soutenances=new ArrayList<>();
    public List<Soutenance> getSoutenances() {
        return Collections.unmodifiableList(soutenances);
    }
    public void ajouterSoutenance(Soutenance s) {
        soutenances.add(s);
    }


}
