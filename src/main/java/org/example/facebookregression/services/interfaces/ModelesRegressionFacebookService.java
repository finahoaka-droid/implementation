package org.example.facebookregression.services.interfaces;

import org.example.facebookregression.models.ModelesRegressionFacebook;
import org.example.facebookregression.models.dto.ModelesRegressionFacebookSearch;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;    
import java.util.List;


public interface ModelesRegressionFacebookService {
    Page<ModelesRegressionFacebook> getAllModelesRegressionFacebook(Pageable pageable);

    Page<ModelesRegressionFacebook> getAllModelesRegressionFacebook(Pageable pageable, ModelesRegressionFacebookSearch object);

    ModelesRegressionFacebook getModelesRegressionFacebookById(Integer id);

    public String exportModelesRegressionFacebookToCSV(List<ModelesRegressionFacebook> modelesRegressionFacebook);

    

    ModelesRegressionFacebook createModelesRegressionFacebook(ModelesRegressionFacebook modelesRegressionFacebook);

    ModelesRegressionFacebook updateModelesRegressionFacebook(Integer id, ModelesRegressionFacebook modelesRegressionFacebook);

    void deleteModelesRegressionFacebook(Integer id);
    

}
