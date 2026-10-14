package org.example.facebookregression.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;      
import org.springframework.data.jpa.domain.Specification;
import org.example.facebookregression.models.DonneesFacebookEncoded;
import org.example.facebookregression.models.dto.DonneesFacebookEncodedSearch;
import org.springframework.stereotype.Service;
import org.example.facebookregression.repositories.DonneesFacebookEncodedRepository;
import java.util.Optional;
import org.example.facebookregression.services.interfaces.DonneesFacebookEncodedService;
import org.example.facebookregression.specification.DonneesFacebookEncodedSpecification;
import org.example.facebookregression.exception.ResourceNotFoundException;
import org.example.facebookregression.exception.InternalServerErrorException;
import org.example.facebookregression.utils.ExportUtils;
import org.springframework.dao.DataIntegrityViolationException;
import java.util.List;


@Service
public class DefaultDonneesFacebookEncodedService implements DonneesFacebookEncodedService {
	private final DonneesFacebookEncodedRepository donneesFacebookEncodedRepository;

	public DefaultDonneesFacebookEncodedService(DonneesFacebookEncodedRepository donneesFacebookEncodedRepository) {
	   this.donneesFacebookEncodedRepository = donneesFacebookEncodedRepository;
	}

	@Override
	public String exportDonneesFacebookEncodedToCSV(List<DonneesFacebookEncoded> donneesFacebookEncoded) {
	   return ExportUtils.generateCsv(donneesFacebookEncoded);
	}  

	@Override
	public Page<DonneesFacebookEncoded> getAllDonneesFacebookEncoded(Pageable pageable) {
	    try {
	        return donneesFacebookEncodedRepository.findAll(pageable);
	    } catch (Exception ex) {
	        throw new InternalServerErrorException("Error while retrieving donnees facebook encoded", ex);
	    }
	}

	@Override
	public Page<DonneesFacebookEncoded> getAllDonneesFacebookEncoded(Pageable pageable, DonneesFacebookEncodedSearch object) {
	    try {
	        Specification<DonneesFacebookEncoded> spec=DonneesFacebookEncodedSpecification.filter(object);
	        return donneesFacebookEncodedRepository.findAll(spec, pageable);
	    } catch (Exception ex) {
	        throw new InternalServerErrorException("Error while retrieving donnees facebook encoded", ex);
	    }
	}

	@Override
	public DonneesFacebookEncoded getDonneesFacebookEncodedById(Integer id) {
	    Optional<DonneesFacebookEncoded> donneesFacebookEncoded = donneesFacebookEncodedRepository.findById(id);
	    if (donneesFacebookEncoded.isPresent()) {
	        return donneesFacebookEncoded.get();
	    } else {
	        throw new ResourceNotFoundException("DonneesFacebookEncoded not found with id : " + id);
	    }
	}

	@Override
	public DonneesFacebookEncoded createDonneesFacebookEncoded(DonneesFacebookEncoded donneesFacebookEncoded) {
	    try {
	        return donneesFacebookEncodedRepository.save(donneesFacebookEncoded);
	    } catch (DataIntegrityViolationException ex) {
	        throw ex;
	    } catch (Exception ex) {
	        throw new InternalServerErrorException("Error while creating donnees facebook encoded", ex);
	    }
	}

	@Override
	public DonneesFacebookEncoded updateDonneesFacebookEncoded(Integer id, DonneesFacebookEncoded donneesFacebookEncoded) {
	    Optional<DonneesFacebookEncoded> existingDonneesFacebookEncoded = donneesFacebookEncodedRepository.findById(id);
	    if (existingDonneesFacebookEncoded.isPresent()) {
	        donneesFacebookEncoded.setId(id);
	        try {
	            return donneesFacebookEncodedRepository.save(donneesFacebookEncoded);
	    } catch (DataIntegrityViolationException ex) {
	            throw ex;    
	    } catch (Exception ex) {
	            throw new InternalServerErrorException("Error while updating donnees facebook encoded", ex);
	        }
	    } else {
	        throw new ResourceNotFoundException("DonneesFacebookEncoded not found with id : " + id);
	    }
	}

	@Override
	public void deleteDonneesFacebookEncoded(Integer id) {
	    try {
	        donneesFacebookEncodedRepository.deleteById(id);
	    } catch (Exception ex) {
	        throw new InternalServerErrorException("Error while deleting donnees facebook encoded", ex);
	    }
	}



}
