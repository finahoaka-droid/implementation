package org.example.facebookregression.services.interfaces;

import org.example.facebookregression.models.DonneesFacebookBrute;
import org.example.facebookregression.models.dto.DonneesFacebookBruteSearch;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;    
import java.util.List;


public interface DonneesFacebookBruteService {
    Page<DonneesFacebookBrute> getAllDonneesFacebookBrute(Pageable pageable);

    Page<DonneesFacebookBrute> getAllDonneesFacebookBrute(Pageable pageable, DonneesFacebookBruteSearch object);

    DonneesFacebookBrute getDonneesFacebookBruteById(Integer id);

    public String exportDonneesFacebookBruteToCSV(List<DonneesFacebookBrute> donneesFacebookBrute);

    

    DonneesFacebookBrute createDonneesFacebookBrute(DonneesFacebookBrute donneesFacebookBrute);

    DonneesFacebookBrute updateDonneesFacebookBrute(Integer id, DonneesFacebookBrute donneesFacebookBrute);

    void deleteDonneesFacebookBrute(Integer id);
    

}
