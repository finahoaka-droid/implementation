package org.example.facebookregression.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;      
import org.springframework.data.jpa.domain.Specification;
import org.example.facebookregression.models.ModelesRegressionFacebook;
import org.example.facebookregression.models.dto.ModelesRegressionFacebookSearch;
import org.springframework.stereotype.Service;
import org.example.facebookregression.repositories.ModelesRegressionFacebookRepository;
import java.util.Optional;
import org.example.facebookregression.services.interfaces.ModelesRegressionFacebookService;
import org.example.facebookregression.specification.ModelesRegressionFacebookSpecification;
import org.example.facebookregression.exception.ResourceNotFoundException;
import org.example.facebookregression.exception.InternalServerErrorException;
import org.example.facebookregression.utils.ExportUtils;
import org.springframework.dao.DataIntegrityViolationException;
import java.util.List;


@Service
public class DefaultModelesRegressionFacebookService implements ModelesRegressionFacebookService {
	private final ModelesRegressionFacebookRepository modelesRegressionFacebookRepository;

	public DefaultModelesRegressionFacebookService(ModelesRegressionFacebookRepository modelesRegressionFacebookRepository) {
	   this.modelesRegressionFacebookRepository = modelesRegressionFacebookRepository;
	}

	@Override
	public String exportModelesRegressionFacebookToCSV(List<ModelesRegressionFacebook> modelesRegressionFacebook) {
	   return ExportUtils.generateCsv(modelesRegressionFacebook);
	}  

	@Override
	public Page<ModelesRegressionFacebook> getAllModelesRegressionFacebook(Pageable pageable) {
	    try {
	        return modelesRegressionFacebookRepository.findAll(pageable);
	    } catch (Exception ex) {
	        throw new InternalServerErrorException("Error while retrieving modeles regression facebook", ex);
	    }
	}

	@Override
	public Page<ModelesRegressionFacebook> getAllModelesRegressionFacebook(Pageable pageable, ModelesRegressionFacebookSearch object) {
	    try {
	        Specification<ModelesRegressionFacebook> spec=ModelesRegressionFacebookSpecification.filter(object);
	        return modelesRegressionFacebookRepository.findAll(spec, pageable);
	    } catch (Exception ex) {
	        throw new InternalServerErrorException("Error while retrieving modeles regression facebook", ex);
	    }
	}

	@Override
	public ModelesRegressionFacebook getModelesRegressionFacebookById(Integer id) {
	    Optional<ModelesRegressionFacebook> modelesRegressionFacebook = modelesRegressionFacebookRepository.findById(id);
	    if (modelesRegressionFacebook.isPresent()) {
	        return modelesRegressionFacebook.get();
	    } else {
	        throw new ResourceNotFoundException("ModelesRegressionFacebook not found with id : " + id);
	    }
	}

	@Override
	public ModelesRegressionFacebook createModelesRegressionFacebook(ModelesRegressionFacebook modelesRegressionFacebook) {
	    try {
	        return modelesRegressionFacebookRepository.save(modelesRegressionFacebook);
	    } catch (DataIntegrityViolationException ex) {
	        throw ex;
	    } catch (Exception ex) {
	        throw new InternalServerErrorException("Error while creating modeles regression facebook", ex);
	    }
	}

	@Override
	public ModelesRegressionFacebook updateModelesRegressionFacebook(Integer id, ModelesRegressionFacebook modelesRegressionFacebook) {
	    Optional<ModelesRegressionFacebook> existingModelesRegressionFacebook = modelesRegressionFacebookRepository.findById(id);
	    if (existingModelesRegressionFacebook.isPresent()) {
	        modelesRegressionFacebook.setId(id);
	        try {
	            return modelesRegressionFacebookRepository.save(modelesRegressionFacebook);
	    } catch (DataIntegrityViolationException ex) {
	            throw ex;    
	    } catch (Exception ex) {
	            throw new InternalServerErrorException("Error while updating modeles regression facebook", ex);
	        }
	    } else {
	        throw new ResourceNotFoundException("ModelesRegressionFacebook not found with id : " + id);
	    }
	}

	@Override
	public void deleteModelesRegressionFacebook(Integer id) {
	    try {
	        modelesRegressionFacebookRepository.deleteById(id);
	    } catch (Exception ex) {
	        throw new InternalServerErrorException("Error while deleting modeles regression facebook", ex);
	    }
	}



}
