package org.example.facebookregression.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;      
import org.springframework.data.jpa.domain.Specification;
import org.example.facebookregression.models.DonneesFacebookBrute;
import org.example.facebookregression.models.dto.DonneesFacebookBruteSearch;
import org.springframework.stereotype.Service;
import org.example.facebookregression.repositories.DonneesFacebookBruteRepository;
import java.util.Optional;
import org.example.facebookregression.services.interfaces.DonneesFacebookBruteService;
import org.example.facebookregression.specification.DonneesFacebookBruteSpecification;
import org.example.facebookregression.exception.ResourceNotFoundException;
import org.example.facebookregression.exception.InternalServerErrorException;
import org.example.facebookregression.utils.ExportUtils;
import org.springframework.dao.DataIntegrityViolationException;
import java.util.List;


@Service
public class DefaultDonneesFacebookBruteService implements DonneesFacebookBruteService {
	private final DonneesFacebookBruteRepository donneesFacebookBruteRepository;

	public DefaultDonneesFacebookBruteService(DonneesFacebookBruteRepository donneesFacebookBruteRepository) {
	   this.donneesFacebookBruteRepository = donneesFacebookBruteRepository;
	}

	@Override
	public String exportDonneesFacebookBruteToCSV(List<DonneesFacebookBrute> donneesFacebookBrute) {
	   return ExportUtils.generateCsv(donneesFacebookBrute);
	}  

	@Override
	public Page<DonneesFacebookBrute> getAllDonneesFacebookBrute(Pageable pageable) {
	    try {
	        return donneesFacebookBruteRepository.findAll(pageable);
	    } catch (Exception ex) {
	        throw new InternalServerErrorException("Error while retrieving donnees facebook brute", ex);
	    }
	}

	@Override
	public Page<DonneesFacebookBrute> getAllDonneesFacebookBrute(Pageable pageable, DonneesFacebookBruteSearch object) {
	    try {
	        Specification<DonneesFacebookBrute> spec=DonneesFacebookBruteSpecification.filter(object);
	        return donneesFacebookBruteRepository.findAll(spec, pageable);
	    } catch (Exception ex) {
	        throw new InternalServerErrorException("Error while retrieving donnees facebook brute", ex);
	    }
	}

	@Override
	public DonneesFacebookBrute getDonneesFacebookBruteById(Integer id) {
	    Optional<DonneesFacebookBrute> donneesFacebookBrute = donneesFacebookBruteRepository.findById(id);
	    if (donneesFacebookBrute.isPresent()) {
	        return donneesFacebookBrute.get();
	    } else {
	        throw new ResourceNotFoundException("DonneesFacebookBrute not found with id : " + id);
	    }
	}

	@Override
	public DonneesFacebookBrute createDonneesFacebookBrute(DonneesFacebookBrute donneesFacebookBrute) {
	    try {
	        return donneesFacebookBruteRepository.save(donneesFacebookBrute);
	    } catch (DataIntegrityViolationException ex) {
	        throw ex;
	    } catch (Exception ex) {
	        throw new InternalServerErrorException("Error while creating donnees facebook brute", ex);
	    }
	}

	@Override
	public DonneesFacebookBrute updateDonneesFacebookBrute(Integer id, DonneesFacebookBrute donneesFacebookBrute) {
	    Optional<DonneesFacebookBrute> existingDonneesFacebookBrute = donneesFacebookBruteRepository.findById(id);
	    if (existingDonneesFacebookBrute.isPresent()) {
	        donneesFacebookBrute.setId(id);
	        try {
	            return donneesFacebookBruteRepository.save(donneesFacebookBrute);
	    } catch (DataIntegrityViolationException ex) {
	            throw ex;    
	    } catch (Exception ex) {
	            throw new InternalServerErrorException("Error while updating donnees facebook brute", ex);
	        }
	    } else {
	        throw new ResourceNotFoundException("DonneesFacebookBrute not found with id : " + id);
	    }
	}

	@Override
	public void deleteDonneesFacebookBrute(Integer id) {
	    try {
	        donneesFacebookBruteRepository.deleteById(id);
	    } catch (Exception ex) {
	        throw new InternalServerErrorException("Error while deleting donnees facebook brute", ex);
	    }
	}



}
