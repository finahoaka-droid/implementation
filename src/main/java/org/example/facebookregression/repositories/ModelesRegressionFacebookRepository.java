package org.example.facebookregression.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.example.facebookregression.models.ModelesRegressionFacebook;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ModelesRegressionFacebookRepository extends JpaRepository<ModelesRegressionFacebook, Integer>, JpaSpecificationExecutor<ModelesRegressionFacebook> {

}
