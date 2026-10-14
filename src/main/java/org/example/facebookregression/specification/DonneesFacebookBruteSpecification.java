package org.example.facebookregression.specification;

import org.example.facebookregression.models.DonneesFacebookBrute;
import org.example.facebookregression.models.dto.DonneesFacebookBruteSearch;
import org.example.facebookregression.utils.WebUtils;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;

import java.util.ArrayList;
import java.util.List;

public class DonneesFacebookBruteSpecification {

    public static Specification<DonneesFacebookBrute> filter(DonneesFacebookBruteSearch object) {
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
            
            System.out.println("🔍 TEMPLATE DEBUG: name=longueurMessage | columnType=[int4] | isText=false | isNumeric=true");

            
            if(object.getLongueurMessage()!=null){
                predicates.add(
                    criteriaBuilder.equal(
                        root.get("longueurMessage"), object.getLongueurMessage()
                    )
                );
            }
            WebUtils.addInterval(criteriaBuilder, root.get("longueurMessage"), object.getLongueurMessageMin(), object.getLongueurMessageMax(), predicates);
            
            System.out.println("🔍 TEMPLATE DEBUG: name=nbHashtags | columnType=[int4] | isText=false | isNumeric=true");

            
            if(object.getNbHashtags()!=null){
                predicates.add(
                    criteriaBuilder.equal(
                        root.get("nbHashtags"), object.getNbHashtags()
                    )
                );
            }
            WebUtils.addInterval(criteriaBuilder, root.get("nbHashtags"), object.getNbHashtagsMin(), object.getNbHashtagsMax(), predicates);
            
            System.out.println("🔍 TEMPLATE DEBUG: name=nbTags | columnType=[int4] | isText=false | isNumeric=true");

            
            if(object.getNbTags()!=null){
                predicates.add(
                    criteriaBuilder.equal(
                        root.get("nbTags"), object.getNbTags()
                    )
                );
            }
            WebUtils.addInterval(criteriaBuilder, root.get("nbTags"), object.getNbTagsMin(), object.getNbTagsMax(), predicates);
            
            System.out.println("🔍 TEMPLATE DEBUG: name=contientLienExterne | columnType=[bool] | isText=false | isNumeric=false");

            
            if(object.getContientLienExterne()!=null){
                predicates.add(
                    criteriaBuilder.equal(
                        root.get("contientLienExterne"), object.getContientLienExterne()
                    )
                );
            }
            
            System.out.println("🔍 TEMPLATE DEBUG: name=contientEmoji | columnType=[bool] | isText=false | isNumeric=false");

            
            if(object.getContientEmoji()!=null){
                predicates.add(
                    criteriaBuilder.equal(
                        root.get("contientEmoji"), object.getContientEmoji()
                    )
                );
            }
            
            System.out.println("🔍 TEMPLATE DEBUG: name=contientQuestion | columnType=[bool] | isText=false | isNumeric=false");

            
            if(object.getContientQuestion()!=null){
                predicates.add(
                    criteriaBuilder.equal(
                        root.get("contientQuestion"), object.getContientQuestion()
                    )
                );
            }
            
            System.out.println("🔍 TEMPLATE DEBUG: name=typePost | columnType=[text] | isText=true | isNumeric=false");

            
            WebUtils.addLikeIfPresent(criteriaBuilder, root.get("typePost"), object.getTypePost(), predicates);
            
            System.out.println("🔍 TEMPLATE DEBUG: name=ratioImage | columnType=[text] | isText=true | isNumeric=false");

            
            WebUtils.addLikeIfPresent(criteriaBuilder, root.get("ratioImage"), object.getRatioImage(), predicates);
            
            System.out.println("🔍 TEMPLATE DEBUG: name=nbPhotosAlbum | columnType=[int4] | isText=false | isNumeric=true");

            
            if(object.getNbPhotosAlbum()!=null){
                predicates.add(
                    criteriaBuilder.equal(
                        root.get("nbPhotosAlbum"), object.getNbPhotosAlbum()
                    )
                );
            }
            WebUtils.addInterval(criteriaBuilder, root.get("nbPhotosAlbum"), object.getNbPhotosAlbumMin(), object.getNbPhotosAlbumMax(), predicates);
            
            System.out.println("🔍 TEMPLATE DEBUG: name=dureeVideo | columnType=[float8] | isText=false | isNumeric=true");

            
            if(object.getDureeVideo()!=null){
                predicates.add(
                    criteriaBuilder.equal(
                        root.get("dureeVideo"), object.getDureeVideo()
                    )
                );
            }
            WebUtils.addInterval(criteriaBuilder, root.get("dureeVideo"), object.getDureeVideoMin(), object.getDureeVideoMax(), predicates);
            
            System.out.println("🔍 TEMPLATE DEBUG: name=longueurTitreVideo | columnType=[int4] | isText=false | isNumeric=true");

            
            if(object.getLongueurTitreVideo()!=null){
                predicates.add(
                    criteriaBuilder.equal(
                        root.get("longueurTitreVideo"), object.getLongueurTitreVideo()
                    )
                );
            }
            WebUtils.addInterval(criteriaBuilder, root.get("longueurTitreVideo"), object.getLongueurTitreVideoMin(), object.getLongueurTitreVideoMax(), predicates);
            
            System.out.println("🔍 TEMPLATE DEBUG: name=jourSemaine | columnType=[text] | isText=true | isNumeric=false");

            
            WebUtils.addLikeIfPresent(criteriaBuilder, root.get("jourSemaine"), object.getJourSemaine(), predicates);
            
            System.out.println("🔍 TEMPLATE DEBUG: name=heurePublication | columnType=[int4] | isText=false | isNumeric=true");

            
            if(object.getHeurePublication()!=null){
                predicates.add(
                    criteriaBuilder.equal(
                        root.get("heurePublication"), object.getHeurePublication()
                    )
                );
            }
            WebUtils.addInterval(criteriaBuilder, root.get("heurePublication"), object.getHeurePublicationMin(), object.getHeurePublicationMax(), predicates);
            
            System.out.println("🔍 TEMPLATE DEBUG: name=joursDepuisDernierPost | columnType=[int4] | isText=false | isNumeric=true");

            
            if(object.getJoursDepuisDernierPost()!=null){
                predicates.add(
                    criteriaBuilder.equal(
                        root.get("joursDepuisDernierPost"), object.getJoursDepuisDernierPost()
                    )
                );
            }
            WebUtils.addInterval(criteriaBuilder, root.get("joursDepuisDernierPost"), object.getJoursDepuisDernierPostMin(), object.getJoursDepuisDernierPostMax(), predicates);
            
            System.out.println("🔍 TEMPLATE DEBUG: name=nbPosts7j | columnType=[int4] | isText=false | isNumeric=true");

            
            if(object.getNbPosts7j()!=null){
                predicates.add(
                    criteriaBuilder.equal(
                        root.get("nbPosts7j"), object.getNbPosts7j()
                    )
                );
            }
            WebUtils.addInterval(criteriaBuilder, root.get("nbPosts7j"), object.getNbPosts7jMin(), object.getNbPosts7jMax(), predicates);
            
            System.out.println("🔍 TEMPLATE DEBUG: name=nbPosts30j | columnType=[int4] | isText=false | isNumeric=true");

            
            if(object.getNbPosts30j()!=null){
                predicates.add(
                    criteriaBuilder.equal(
                        root.get("nbPosts30j"), object.getNbPosts30j()
                    )
                );
            }
            WebUtils.addInterval(criteriaBuilder, root.get("nbPosts30j"), object.getNbPosts30jMin(), object.getNbPosts30jMax(), predicates);
            
            System.out.println("🔍 TEMPLATE DEBUG: name=vues | columnType=[float8] | isText=false | isNumeric=true");

            
            if(object.getVues()!=null){
                predicates.add(
                    criteriaBuilder.equal(
                        root.get("vues"), object.getVues()
                    )
                );
            }
            WebUtils.addInterval(criteriaBuilder, root.get("vues"), object.getVuesMin(), object.getVuesMax(), predicates);
            
            System.out.println("🔍 TEMPLATE DEBUG: name=reactions | columnType=[float8] | isText=false | isNumeric=true");

            
            if(object.getReactions()!=null){
                predicates.add(
                    criteriaBuilder.equal(
                        root.get("reactions"), object.getReactions()
                    )
                );
            }
            WebUtils.addInterval(criteriaBuilder, root.get("reactions"), object.getReactionsMin(), object.getReactionsMax(), predicates);
            
            System.out.println("🔍 TEMPLATE DEBUG: name=commentaires | columnType=[int4] | isText=false | isNumeric=true");

            
            if(object.getCommentaires()!=null){
                predicates.add(
                    criteriaBuilder.equal(
                        root.get("commentaires"), object.getCommentaires()
                    )
                );
            }
            WebUtils.addInterval(criteriaBuilder, root.get("commentaires"), object.getCommentairesMin(), object.getCommentairesMax(), predicates);
            
            System.out.println("🔍 TEMPLATE DEBUG: name=dateInsertion | columnType=[timestamp] | isText=false | isNumeric=false");

            
            if(object.getDateInsertion()!=null){
                predicates.add(
                    criteriaBuilder.equal(
                        root.get("dateInsertion"), object.getDateInsertion()
                    )
                );
            }
            WebUtils.addInterval(criteriaBuilder, root.get("dateInsertion"), object.getDateInsertionMin(), object.getDateInsertionMax(), predicates);
            
            
            return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
        };
    }
}
