package org.example.facebookregression.specification;

import org.example.facebookregression.models.DonneesFacebookEncoded;
import org.example.facebookregression.models.dto.DonneesFacebookEncodedSearch;
import org.example.facebookregression.utils.WebUtils;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;

import java.util.ArrayList;
import java.util.List;

public class DonneesFacebookEncodedSpecification {

    public static Specification<DonneesFacebookEncoded> filter(DonneesFacebookEncodedSearch object) {
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
            
            System.out.println("🔍 TEMPLATE DEBUG: name=longueurMessage | columnType=[float8] | isText=false | isNumeric=true");

            
            if(object.getLongueurMessage()!=null){
                predicates.add(
                    criteriaBuilder.equal(
                        root.get("longueurMessage"), object.getLongueurMessage()
                    )
                );
            }
            WebUtils.addInterval(criteriaBuilder, root.get("longueurMessage"), object.getLongueurMessageMin(), object.getLongueurMessageMax(), predicates);
            
            System.out.println("🔍 TEMPLATE DEBUG: name=nbHashtags | columnType=[float8] | isText=false | isNumeric=true");

            
            if(object.getNbHashtags()!=null){
                predicates.add(
                    criteriaBuilder.equal(
                        root.get("nbHashtags"), object.getNbHashtags()
                    )
                );
            }
            WebUtils.addInterval(criteriaBuilder, root.get("nbHashtags"), object.getNbHashtagsMin(), object.getNbHashtagsMax(), predicates);
            
            System.out.println("🔍 TEMPLATE DEBUG: name=nbTags | columnType=[float8] | isText=false | isNumeric=true");

            
            if(object.getNbTags()!=null){
                predicates.add(
                    criteriaBuilder.equal(
                        root.get("nbTags"), object.getNbTags()
                    )
                );
            }
            WebUtils.addInterval(criteriaBuilder, root.get("nbTags"), object.getNbTagsMin(), object.getNbTagsMax(), predicates);
            
            System.out.println("🔍 TEMPLATE DEBUG: name=contientLienExterne | columnType=[float8] | isText=false | isNumeric=true");

            
            if(object.getContientLienExterne()!=null){
                predicates.add(
                    criteriaBuilder.equal(
                        root.get("contientLienExterne"), object.getContientLienExterne()
                    )
                );
            }
            WebUtils.addInterval(criteriaBuilder, root.get("contientLienExterne"), object.getContientLienExterneMin(), object.getContientLienExterneMax(), predicates);
            
            System.out.println("🔍 TEMPLATE DEBUG: name=contientEmoji | columnType=[float8] | isText=false | isNumeric=true");

            
            if(object.getContientEmoji()!=null){
                predicates.add(
                    criteriaBuilder.equal(
                        root.get("contientEmoji"), object.getContientEmoji()
                    )
                );
            }
            WebUtils.addInterval(criteriaBuilder, root.get("contientEmoji"), object.getContientEmojiMin(), object.getContientEmojiMax(), predicates);
            
            System.out.println("🔍 TEMPLATE DEBUG: name=contientQuestion | columnType=[float8] | isText=false | isNumeric=true");

            
            if(object.getContientQuestion()!=null){
                predicates.add(
                    criteriaBuilder.equal(
                        root.get("contientQuestion"), object.getContientQuestion()
                    )
                );
            }
            WebUtils.addInterval(criteriaBuilder, root.get("contientQuestion"), object.getContientQuestionMin(), object.getContientQuestionMax(), predicates);
            
            System.out.println("🔍 TEMPLATE DEBUG: name=typePostPhoto | columnType=[float8] | isText=false | isNumeric=true");

            
            if(object.getTypePostPhoto()!=null){
                predicates.add(
                    criteriaBuilder.equal(
                        root.get("typePostPhoto"), object.getTypePostPhoto()
                    )
                );
            }
            WebUtils.addInterval(criteriaBuilder, root.get("typePostPhoto"), object.getTypePostPhotoMin(), object.getTypePostPhotoMax(), predicates);
            
            System.out.println("🔍 TEMPLATE DEBUG: name=typePostAlbum | columnType=[float8] | isText=false | isNumeric=true");

            
            if(object.getTypePostAlbum()!=null){
                predicates.add(
                    criteriaBuilder.equal(
                        root.get("typePostAlbum"), object.getTypePostAlbum()
                    )
                );
            }
            WebUtils.addInterval(criteriaBuilder, root.get("typePostAlbum"), object.getTypePostAlbumMin(), object.getTypePostAlbumMax(), predicates);
            
            System.out.println("🔍 TEMPLATE DEBUG: name=typePostVideo | columnType=[float8] | isText=false | isNumeric=true");

            
            if(object.getTypePostVideo()!=null){
                predicates.add(
                    criteriaBuilder.equal(
                        root.get("typePostVideo"), object.getTypePostVideo()
                    )
                );
            }
            WebUtils.addInterval(criteriaBuilder, root.get("typePostVideo"), object.getTypePostVideoMin(), object.getTypePostVideoMax(), predicates);
            
            System.out.println("🔍 TEMPLATE DEBUG: name=typePostLien | columnType=[float8] | isText=false | isNumeric=true");

            
            if(object.getTypePostLien()!=null){
                predicates.add(
                    criteriaBuilder.equal(
                        root.get("typePostLien"), object.getTypePostLien()
                    )
                );
            }
            WebUtils.addInterval(criteriaBuilder, root.get("typePostLien"), object.getTypePostLienMin(), object.getTypePostLienMax(), predicates);
            
            System.out.println("🔍 TEMPLATE DEBUG: name=typePostEvenement | columnType=[float8] | isText=false | isNumeric=true");

            
            if(object.getTypePostEvenement()!=null){
                predicates.add(
                    criteriaBuilder.equal(
                        root.get("typePostEvenement"), object.getTypePostEvenement()
                    )
                );
            }
            WebUtils.addInterval(criteriaBuilder, root.get("typePostEvenement"), object.getTypePostEvenementMin(), object.getTypePostEvenementMax(), predicates);
            
            System.out.println("🔍 TEMPLATE DEBUG: name=ratioImage | columnType=[float8] | isText=false | isNumeric=true");

            
            if(object.getRatioImage()!=null){
                predicates.add(
                    criteriaBuilder.equal(
                        root.get("ratioImage"), object.getRatioImage()
                    )
                );
            }
            WebUtils.addInterval(criteriaBuilder, root.get("ratioImage"), object.getRatioImageMin(), object.getRatioImageMax(), predicates);
            
            System.out.println("🔍 TEMPLATE DEBUG: name=nbPhotosAlbum | columnType=[float8] | isText=false | isNumeric=true");

            
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
            
            System.out.println("🔍 TEMPLATE DEBUG: name=longueurTitreVideo | columnType=[float8] | isText=false | isNumeric=true");

            
            if(object.getLongueurTitreVideo()!=null){
                predicates.add(
                    criteriaBuilder.equal(
                        root.get("longueurTitreVideo"), object.getLongueurTitreVideo()
                    )
                );
            }
            WebUtils.addInterval(criteriaBuilder, root.get("longueurTitreVideo"), object.getLongueurTitreVideoMin(), object.getLongueurTitreVideoMax(), predicates);
            
            System.out.println("🔍 TEMPLATE DEBUG: name=jourLundi | columnType=[float8] | isText=false | isNumeric=true");

            
            if(object.getJourLundi()!=null){
                predicates.add(
                    criteriaBuilder.equal(
                        root.get("jourLundi"), object.getJourLundi()
                    )
                );
            }
            WebUtils.addInterval(criteriaBuilder, root.get("jourLundi"), object.getJourLundiMin(), object.getJourLundiMax(), predicates);
            
            System.out.println("🔍 TEMPLATE DEBUG: name=jourMardi | columnType=[float8] | isText=false | isNumeric=true");

            
            if(object.getJourMardi()!=null){
                predicates.add(
                    criteriaBuilder.equal(
                        root.get("jourMardi"), object.getJourMardi()
                    )
                );
            }
            WebUtils.addInterval(criteriaBuilder, root.get("jourMardi"), object.getJourMardiMin(), object.getJourMardiMax(), predicates);
            
            System.out.println("🔍 TEMPLATE DEBUG: name=jourMercredi | columnType=[float8] | isText=false | isNumeric=true");

            
            if(object.getJourMercredi()!=null){
                predicates.add(
                    criteriaBuilder.equal(
                        root.get("jourMercredi"), object.getJourMercredi()
                    )
                );
            }
            WebUtils.addInterval(criteriaBuilder, root.get("jourMercredi"), object.getJourMercrediMin(), object.getJourMercrediMax(), predicates);
            
            System.out.println("🔍 TEMPLATE DEBUG: name=jourJeudi | columnType=[float8] | isText=false | isNumeric=true");

            
            if(object.getJourJeudi()!=null){
                predicates.add(
                    criteriaBuilder.equal(
                        root.get("jourJeudi"), object.getJourJeudi()
                    )
                );
            }
            WebUtils.addInterval(criteriaBuilder, root.get("jourJeudi"), object.getJourJeudiMin(), object.getJourJeudiMax(), predicates);
            
            System.out.println("🔍 TEMPLATE DEBUG: name=jourVendredi | columnType=[float8] | isText=false | isNumeric=true");

            
            if(object.getJourVendredi()!=null){
                predicates.add(
                    criteriaBuilder.equal(
                        root.get("jourVendredi"), object.getJourVendredi()
                    )
                );
            }
            WebUtils.addInterval(criteriaBuilder, root.get("jourVendredi"), object.getJourVendrediMin(), object.getJourVendrediMax(), predicates);
            
            System.out.println("🔍 TEMPLATE DEBUG: name=jourSamedi | columnType=[float8] | isText=false | isNumeric=true");

            
            if(object.getJourSamedi()!=null){
                predicates.add(
                    criteriaBuilder.equal(
                        root.get("jourSamedi"), object.getJourSamedi()
                    )
                );
            }
            WebUtils.addInterval(criteriaBuilder, root.get("jourSamedi"), object.getJourSamediMin(), object.getJourSamediMax(), predicates);
            
            System.out.println("🔍 TEMPLATE DEBUG: name=heurePublication | columnType=[float8] | isText=false | isNumeric=true");

            
            if(object.getHeurePublication()!=null){
                predicates.add(
                    criteriaBuilder.equal(
                        root.get("heurePublication"), object.getHeurePublication()
                    )
                );
            }
            WebUtils.addInterval(criteriaBuilder, root.get("heurePublication"), object.getHeurePublicationMin(), object.getHeurePublicationMax(), predicates);
            
            System.out.println("🔍 TEMPLATE DEBUG: name=joursDepuisDernierPost | columnType=[float8] | isText=false | isNumeric=true");

            
            if(object.getJoursDepuisDernierPost()!=null){
                predicates.add(
                    criteriaBuilder.equal(
                        root.get("joursDepuisDernierPost"), object.getJoursDepuisDernierPost()
                    )
                );
            }
            WebUtils.addInterval(criteriaBuilder, root.get("joursDepuisDernierPost"), object.getJoursDepuisDernierPostMin(), object.getJoursDepuisDernierPostMax(), predicates);
            
            System.out.println("🔍 TEMPLATE DEBUG: name=nbPosts7j | columnType=[float8] | isText=false | isNumeric=true");

            
            if(object.getNbPosts7j()!=null){
                predicates.add(
                    criteriaBuilder.equal(
                        root.get("nbPosts7j"), object.getNbPosts7j()
                    )
                );
            }
            WebUtils.addInterval(criteriaBuilder, root.get("nbPosts7j"), object.getNbPosts7jMin(), object.getNbPosts7jMax(), predicates);
            
            System.out.println("🔍 TEMPLATE DEBUG: name=nbPosts30j | columnType=[float8] | isText=false | isNumeric=true");

            
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
            
            System.out.println("🔍 TEMPLATE DEBUG: name=commentaires | columnType=[float8] | isText=false | isNumeric=true");

            
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
