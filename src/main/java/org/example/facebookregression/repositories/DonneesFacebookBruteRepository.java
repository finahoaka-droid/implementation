package org.example.facebookregression.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.example.facebookregression.models.DonneesFacebookBrute;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface DonneesFacebookBruteRepository extends JpaRepository<DonneesFacebookBrute, Integer>, JpaSpecificationExecutor<DonneesFacebookBrute> {

}
