package org.example.facebookregression.specification;

import org.example.facebookregression.models.ModelesRegressionFacebook;
import org.example.facebookregression.models.dto.ModelesRegressionFacebookSearch;
import org.example.facebookregression.utils.WebUtils;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;

import java.util.ArrayList;
import java.util.List;

public class ModelesRegressionFacebookSpecification {

    public static Specification<ModelesRegressionFacebook> filter(ModelesRegressionFacebookSearch object) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            System.out.println("🔍 TEMPLATE DEBUG: name=id | columnType=[serial] | isText=false | isNumeric=true");

            
            if(object.getId()!=null){
                predicates.add(
                    criteriaBuilder.equal(
                        root.get("id"), object.getId()
                    )
                );
            }
            
            System.out.println("🔍 TEMPLATE DEBUG: name=nomModele | columnType=[text] | isText=true | isNumeric=false");

            
            WebUtils.addLikeIfPresent(criteriaBuilder, root.get("nomModele"), object.getNomModele(), predicates);
            
            System.out.println("🔍 TEMPLATE DEBUG: name=typeModele | columnType=[text] | isText=true | isNumeric=false");

            
            WebUtils.addLikeIfPresent(criteriaBuilder, root.get("typeModele"), object.getTypeModele(), predicates);
            
            System.out.println("🔍 TEMPLATE DEBUG: name=variableCible | columnType=[text] | isText=true | isNumeric=false");

            
            WebUtils.addLikeIfPresent(criteriaBuilder, root.get("variableCible"), object.getVariableCible(), predicates);
            
            System.out.println("🔍 TEMPLATE DEBUG: name=mse | columnType=[float8] | isText=false | isNumeric=true");

            
            if(object.getMse()!=null){
                predicates.add(
                    criteriaBuilder.equal(
                        root.get("mse"), object.getMse()
                    )
                );
            }
            WebUtils.addInterval(criteriaBuilder, root.get("mse"), object.getMseMin(), object.getMseMax(), predicates);
            
            System.out.println("🔍 TEMPLATE DEBUG: name=r2 | columnType=[float8] | isText=false | isNumeric=true");

            
            if(object.getR2()!=null){
                predicates.add(
                    criteriaBuilder.equal(
                        root.get("r2"), object.getR2()
                    )
                );
            }
            WebUtils.addInterval(criteriaBuilder, root.get("r2"), object.getR2Min(), object.getR2Max(), predicates);
            
            System.out.println("🔍 TEMPLATE DEBUG: name=dateEntrainement | columnType=[timestamp] | isText=false | isNumeric=false");

            
            if(object.getDateEntrainement()!=null){
                predicates.add(
                    criteriaBuilder.equal(
                        root.get("dateEntrainement"), object.getDateEntrainement()
                    )
                );
            }
            WebUtils.addInterval(criteriaBuilder, root.get("dateEntrainement"), object.getDateEntrainementMin(), object.getDateEntrainementMax(), predicates);
            
            System.out.println("🔍 TEMPLATE DEBUG: name=modeleBinaire | columnType=[bytea] | isText=false | isNumeric=false");

            
            if(object.getModeleBinaire()!=null){
                predicates.add(
                    criteriaBuilder.equal(
                        root.get("modeleBinaire"), object.getModeleBinaire()
                    )
                );
            }
            
            
            return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
        };
    }
}
