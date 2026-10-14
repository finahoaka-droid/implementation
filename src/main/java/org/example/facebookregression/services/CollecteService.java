package org.example.facebookregression.services;

import org.example.facebookregression.dto.CollecteRequest;
import org.example.facebookregression.dto.CollecteResultat;
import org.example.facebookregression.models.DonneesFacebookBrute;
import org.example.facebookregression.models.DonneesFacebookEncoded;
import org.example.facebookregression.repositories.DonneesFacebookBruteRepository;
import org.example.facebookregression.repositories.DonneesFacebookEncodedRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@Service
public class CollecteService {

    private static final DateTimeFormatter FILE_TS = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    private final FacebookGraphApiService            graphApi;
    private final FacebookEncoderService             encoder;
    private final DonneesFacebookBruteRepository     bruteRepo;
    private final DonneesFacebookEncodedRepository   encodedRepo;

    @Value("${app.collecte.csv-output-dir:output}")
    private String csvOutputDir;

    public CollecteService(FacebookGraphApiService graphApi,
                           FacebookEncoderService encoder,
                           DonneesFacebookBruteRepository bruteRepo,
                           DonneesFacebookEncodedRepository encodedRepo) {
        this.graphApi    = graphApi;
        this.encoder     = encoder;
        this.bruteRepo   = bruteRepo;
        this.encodedRepo = encodedRepo;
    }

    @Transactional
    public CollecteResultat lancer(CollecteRequest req) {
        String since = req.getSince();
        String until = req.getUntil();

        System.out.printf("[CollecteService] Démarrage collecte : %s → %s%n", since, until);

        FacebookGraphApiService.PostsResult postsResult = graphApi.getPosts(since, until);

        if (!postsResult.isOk()) {
            CollecteResultat res = new CollecteResultat();
            res.setSince(since);
            res.setUntil(until);
            res.setErreurApi(postsResult.erreur);
            res.setMessage("Erreur lors de l'appel à l'API Facebook : " + postsResult.erreur);
            return res;
        }

        List<Map<String, Object>> posts = postsResult.posts;
        System.out.printf("[CollecteService] %d publications récupérées.%n", posts.size());

        if (posts.isEmpty()) {
            CollecteResultat res = new CollecteResultat();
            res.setSince(since);
            res.setUntil(until);
            res.setNbPublicationsCollectees(0);
            res.setMessage("Aucune publication trouvée sur la période donnée. Vérifiez le token ou la plage de dates.");
            return res;
        }

        List<DonneesFacebookBrute>   brutes  = encoder.buildBruteEntities(posts);
        List<DonneesFacebookEncoded> encoded = encoder.buildEncodedEntities(brutes);

        bruteRepo.deleteAll();
        encodedRepo.deleteAll();

        bruteRepo.flush();
        encodedRepo.flush();

        List<DonneesFacebookBrute>   savedBrutes  = bruteRepo.saveAll(brutes);
        List<DonneesFacebookEncoded> savedEncoded = encodedRepo.saveAll(encoded);

        System.out.printf("[CollecteService] %d lignes brutes + %d lignes encodées sauvegardées.%n",
                savedBrutes.size(), savedEncoded.size());

        String csvPath = writeCsvTrace(savedBrutes, savedEncoded, since, until);

        CollecteResultat res = new CollecteResultat();
        res.setSince(since);
        res.setUntil(until);
        res.setNbPublicationsCollectees(posts.size());
        res.setNbLignesBrutes(savedBrutes.size());
        res.setNbLignesEncoded(savedEncoded.size());
        res.setCheminCsvTrace(csvPath);
        res.setMessage("Collecte terminée avec succès.");
        return res;
    }

    private String writeCsvTrace(List<DonneesFacebookBrute> brutes,
                                 List<DonneesFacebookEncoded> encoded,
                                 String since, String until) {
        try {
            Path dir = Path.of(csvOutputDir);
            Files.createDirectories(dir);

            String timestamp = LocalDateTime.now().format(FILE_TS);
            String filename  = String.format("trace_collecte_%s_%s_%s.csv", since, until, timestamp);
            Path   csvFile   = dir.resolve(filename);

            try (FileWriter fw = new FileWriter(csvFile.toFile(), StandardCharsets.UTF_8)) {

                fw.write("# Trace de collecte Facebook\n");
                fw.write("# Période : " + since + " → " + until + "\n");
                fw.write("# Généré le : " + LocalDateTime.now() + "\n");
                fw.write("# Publications : " + brutes.size() + "\n\n");

                fw.write("## DONNÉES BRUTES\n");
                fw.write(buildBruteCsvHeader() + "\n");
                for (DonneesFacebookBrute b : brutes) {
                    fw.write(bruteToCsvLine(b) + "\n");
                }

                fw.write("\n## DONNÉES ENCODÉES\n");
                fw.write(buildEncodedCsvHeader() + "\n");
                for (DonneesFacebookEncoded e : encoded) {
                    fw.write(encodedToCsvLine(e) + "\n");
                }
            }

            System.out.println("[CollecteService] CSV de trace écrit : " + csvFile.toAbsolutePath());
            return csvFile.toAbsolutePath().toString();

        } catch (IOException e) {
            System.err.println("[CollecteService] Impossible d'écrire le CSV de trace : " + e.getMessage());
            return null;
        }
    }

    private static String buildBruteCsvHeader() {
        return String.join(",",
                "id","longueur_message","nb_hashtags","nb_tags",
                "contient_lien_externe","contient_emoji","contient_question",
                "type_post","ratio_image","nb_photos_album","duree_video","longueur_titre_video",
                "jour_semaine","heure_publication","jours_depuis_dernier_post",
                "nb_posts_7j","nb_posts_30j","vues","reactions","commentaires","date_insertion");
    }

    private static String bruteToCsvLine(DonneesFacebookBrute b) {
        List<String> vals = new ArrayList<>();
        vals.add(csv(b.getId()));
        vals.add(csv(b.getLongueurMessage()));
        vals.add(csv(b.getNbHashtags()));
        vals.add(csv(b.getNbTags()));
        vals.add(csv(b.getContientLienExterne()));
        vals.add(csv(b.getContientEmoji()));
        vals.add(csv(b.getContientQuestion()));
        vals.add(csvStr(b.getTypePost()));
        vals.add(csvStr(b.getRatioImage()));
        vals.add(csv(b.getNbPhotosAlbum()));
        vals.add(csv(b.getDureeVideo()));
        vals.add(csv(b.getLongueurTitreVideo()));
        vals.add(csvStr(b.getJourSemaine()));
        vals.add(csv(b.getHeurePublication()));
        vals.add(csv(b.getJoursDepuisDernierPost()));
        vals.add(csv(b.getNbPosts7j()));
        vals.add(csv(b.getNbPosts30j()));
        vals.add(csv(b.getVues()));
        vals.add(csv(b.getReactions()));
        vals.add(csv(b.getCommentaires()));
        vals.add(csvStr(b.getDateInsertion() != null ? b.getDateInsertion().toString() : null));
        return String.join(",", vals);
    }

    private static String buildEncodedCsvHeader() {
        return String.join(",",
                "id","longueur_message","nb_hashtags","nb_tags",
                "contient_lien_externe","contient_emoji","contient_question",
                "type_post_photo","type_post_album","type_post_video","type_post_lien","type_post_evenement",
                "ratio_image","nb_photos_album","duree_video","longueur_titre_video",
                "jour_lundi","jour_mardi","jour_mercredi","jour_jeudi","jour_vendredi","jour_samedi",
                "heure_publication","jours_depuis_dernier_post","nb_posts_7j","nb_posts_30j",
                "vues","reactions","commentaires","date_insertion");
    }

    private static String encodedToCsvLine(DonneesFacebookEncoded e) {
        List<String> vals = new ArrayList<>();
        vals.add(csv(e.getId()));
        vals.add(csv(e.getLongueurMessage()));
        vals.add(csv(e.getNbHashtags()));
        vals.add(csv(e.getNbTags()));
        vals.add(csv(e.getContientLienExterne()));
        vals.add(csv(e.getContientEmoji()));
        vals.add(csv(e.getContientQuestion()));
        vals.add(csv(e.getTypePostPhoto()));
        vals.add(csv(e.getTypePostAlbum()));
        vals.add(csv(e.getTypePostVideo()));
        vals.add(csv(e.getTypePostLien()));
        vals.add(csv(e.getTypePostEvenement()));
        vals.add(csv(e.getRatioImage()));
        vals.add(csv(e.getNbPhotosAlbum()));
        vals.add(csv(e.getDureeVideo()));
        vals.add(csv(e.getLongueurTitreVideo()));
        vals.add(csv(e.getJourLundi()));
        vals.add(csv(e.getJourMardi()));
        vals.add(csv(e.getJourMercredi()));
        vals.add(csv(e.getJourJeudi()));
        vals.add(csv(e.getJourVendredi()));
        vals.add(csv(e.getJourSamedi()));
        vals.add(csv(e.getHeurePublication()));
        vals.add(csv(e.getJoursDepuisDernierPost()));
        vals.add(csv(e.getNbPosts7j()));
        vals.add(csv(e.getNbPosts30j()));
        vals.add(csv(e.getVues()));
        vals.add(csv(e.getReactions()));
        vals.add(csv(e.getCommentaires()));
        vals.add(csvStr(e.getDateInsertion() != null ? e.getDateInsertion().toString() : null));
        return String.join(",", vals);
    }

    private static String csv(Object v)     { return v == null ? "" : v.toString(); }
    private static String csvStr(String v)  {
        if (v == null) return "\"\"";
        return "\"" + v.replace("\"", "\"\"") + "\"";
    }
}
