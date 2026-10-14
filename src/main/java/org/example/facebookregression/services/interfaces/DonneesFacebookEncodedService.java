package org.example.facebookregression.services.interfaces;

import org.example.facebookregression.models.DonneesFacebookEncoded;
import org.example.facebookregression.models.dto.DonneesFacebookEncodedSearch;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;    
import java.util.List;


public interface DonneesFacebookEncodedService {
    Page<DonneesFacebookEncoded> getAllDonneesFacebookEncoded(Pageable pageable);

    Page<DonneesFacebookEncoded> getAllDonneesFacebookEncoded(Pageable pageable, DonneesFacebookEncodedSearch object);

    DonneesFacebookEncoded getDonneesFacebookEncodedById(Integer id);

    public String exportDonneesFacebookEncodedToCSV(List<DonneesFacebookEncoded> donneesFacebookEncoded);

    

    DonneesFacebookEncoded createDonneesFacebookEncoded(DonneesFacebookEncoded donneesFacebookEncoded);

    DonneesFacebookEncoded updateDonneesFacebookEncoded(Integer id, DonneesFacebookEncoded donneesFacebookEncoded);

    void deleteDonneesFacebookEncoded(Integer id);
    

}
