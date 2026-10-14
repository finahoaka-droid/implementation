package org.example.facebookregression.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.example.facebookregression.models.DonneesFacebookEncoded;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface DonneesFacebookEncodedRepository extends JpaRepository<DonneesFacebookEncoded, Integer>, JpaSpecificationExecutor<DonneesFacebookEncoded> {

}
