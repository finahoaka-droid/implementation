package org.example.facebookregression.services;

import org.example.facebookregression.models.DonneesFacebookBrute;
import org.example.facebookregression.models.DonneesFacebookEncoded;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.example.facebookregression.services.FacebookGraphApiService.*;


@Service
public class FacebookEncoderService {

    private static final Pattern HASHTAG_PATTERN = Pattern.compile("#\\w+");
    private static final Pattern EMOJI_PATTERN = Pattern.compile(
            "[\\x{1F300}-\\x{1FAFF}\\x{2600}-\\x{27BF}\\x{1F1E6}-\\x{1F1FF}]");
    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ");

    private final FacebookGraphApiService graphApi;

    public FacebookEncoderService(FacebookGraphApiService graphApi) {
        this.graphApi = graphApi;
    }

    public List<DonneesFacebookBrute> buildBruteEntities(List<Map<String, Object>> posts) {
        List<Map<String, Object>> sorted = new ArrayList<>(posts);
        sorted.sort(Comparator.comparing(p -> text(p.get("created_time"), "")));

        List<LocalDate> dates = new ArrayList<>();
        for (Map<String, Object> p : sorted) {
            dates.add(OffsetDateTime.parse(text(p.get("created_time"), ""), DT_FMT).toLocalDate());
        }

        List<DonneesFacebookBrute> result = new ArrayList<>();
        int n = sorted.size();
        for (int i = 0; i < n; i++) {
            Map<String, Object> post = sorted.get(i);
            System.out.printf("[%d/%d] Traitement brut du post %s%n", i + 1, n, text(post.get("id"), ""));

            LocalDate current = dates.get(i);
            Integer joursDepuisDernierPost = i == 0 ? null
                    : (int) ChronoUnit.DAYS.between(dates.get(i - 1), current);

            int nbPosts7j = 0;
            int nbPosts30j = 0;
            for (int j = 0; j < i; j++) {
                long diff = ChronoUnit.DAYS.between(dates.get(j), current);
                if (diff <= 7)  nbPosts7j++;
                if (diff <= 30) nbPosts30j++;
            }
            result.add(toBruteEntity(post, joursDepuisDernierPost, nbPosts7j, nbPosts30j));
        }
        return result;
    }

    private DonneesFacebookBrute toBruteEntity(Map<String, Object> post, Integer joursDepuisDernierPost,
                                               int nbPosts7j, int nbPosts30j) {
        String postId = text(post.get("id"), "");
        OffsetDateTime dt = OffsetDateTime.parse(text(post.get("created_time"), ""), DT_FMT);
        String message = text(post.get("message"), "");

        ContentInfo content = analyzeContent(post);
        double[] insights = graphApi.getInsights(postId);
        double vues = insights[0];
        double reactions = insights[1];
        int commentaires = graphApi.getCommentsCount(postId);

        Double vuesFinales = ("video".equals(content.typePost) || "album".equals(content.typePost)) ? vues : null;

        DonneesFacebookBrute e = new DonneesFacebookBrute();
        e.setLongueurMessage(message.length());
        e.setNbHashtags(countHashtags(message));
        e.setNbTags(countTags(post));
        e.setContientLienExterne(containsExternalLink(post));
        e.setContientEmoji(countEmojis(message) > 0);
        e.setContientQuestion(message.contains("?"));
        e.setTypePost(content.typePost);
        e.setRatioImage(content.ratioImage);
        e.setNbPhotosAlbum(content.nbPhotosAlbum);
        e.setDureeVideo(content.dureeVideo);
        e.setLongueurTitreVideo(content.longueurTitreVideo);
        e.setJourSemaine(dt.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.FRENCH));
        e.setHeurePublication(dt.getHour());
        e.setJoursDepuisDernierPost(joursDepuisDernierPost);
        e.setNbPosts7j(nbPosts7j);
        e.setNbPosts30j(nbPosts30j);
        e.setVues(vuesFinales);
        e.setReactions(reactions);
        e.setCommentaires(commentaires);
        e.setDateInsertion(LocalDateTime.now());
        return e;
    }

    public List<DonneesFacebookEncoded> buildEncodedEntities(List<DonneesFacebookBrute> brutes) {
        List<DonneesFacebookEncoded> result = new ArrayList<>();
        for (DonneesFacebookBrute b : brutes) {
            result.add(toEncodedEntity(b));
        }
        return result;
    }

    private DonneesFacebookEncoded toEncodedEntity(DonneesFacebookBrute b) {
        DonneesFacebookEncoded e = new DonneesFacebookEncoded();
        e.setLongueurMessage(toDouble(b.getLongueurMessage()));
        e.setNbHashtags(toDouble(b.getNbHashtags()));
        e.setNbTags(toDouble(b.getNbTags()));
        e.setContientLienExterne(encodeBoolean(b.getContientLienExterne()));
        e.setContientEmoji(encodeBoolean(b.getContientEmoji()));
        e.setContientQuestion(encodeBoolean(b.getContientQuestion()));

        String tp = b.getTypePost() != null ? b.getTypePost() : "";
        e.setTypePostPhoto(oneHot(tp, "photo"));
        e.setTypePostAlbum(oneHot(tp, "album"));
        e.setTypePostVideo(oneHot(tp, "video"));
        e.setTypePostLien(oneHot(tp, "lien"));
        e.setTypePostEvenement(oneHot(tp, "evenement"));

        e.setRatioImage(encodeRatioImage(b.getRatioImage()));
        e.setNbPhotosAlbum(toDouble(b.getNbPhotosAlbum()));
        e.setDureeVideo(b.getDureeVideo());
        e.setLongueurTitreVideo(toDouble(b.getLongueurTitreVideo()));

        String jour = b.getJourSemaine() != null ? b.getJourSemaine().toLowerCase() : "";
        e.setJourLundi(oneHot(jour, "lundi"));
        e.setJourMardi(oneHot(jour, "mardi"));
        e.setJourMercredi(oneHot(jour, "mercredi"));
        e.setJourJeudi(oneHot(jour, "jeudi"));
        e.setJourVendredi(oneHot(jour, "vendredi"));
        e.setJourSamedi(oneHot(jour, "samedi"));

        e.setHeurePublication(toDouble(b.getHeurePublication()));
        e.setJoursDepuisDernierPost(toDouble(b.getJoursDepuisDernierPost()));
        e.setNbPosts7j(toDouble(b.getNbPosts7j()));
        e.setNbPosts30j(toDouble(b.getNbPosts30j()));
        e.setVues(b.getVues());
        e.setReactions(b.getReactions());
        e.setCommentaires(toDouble(b.getCommentaires()));
        e.setDateInsertion(LocalDateTime.now());
        return e;
    }

    private ContentInfo analyzeContent(Map<String, Object> post) {
        ContentInfo info = new ContentInfo();
        List<Object> data = arr(obj(post.get("attachments")).get("data"));
        if (data.isEmpty()) return info;

        Map<String, Object> first = obj(data.get(0));
        String mediaType = text(first.get("media_type"), "");
        switch (mediaType) {
            case "album" -> fillAlbumInfo(first, info);
            case "video" -> { info.typePost = "video"; fillVideoInfo(first, info); }
            case "photo" -> { info.typePost = "photo"; fillPhotoRatio(first, info); }
            case "link"  -> info.typePost = "lien";
            case "event" -> info.typePost = "evenement";
            default      -> info.typePost = mediaType.isEmpty() ? "texte" : mediaType;
        }
        return info;
    }

    private void fillAlbumInfo(Map<String, Object> first, ContentInfo info) {
        info.typePost = "album";
        int nb = 0;
        for (Object it : arr(obj(first.get("subattachments")).get("data"))) {
            if ("photo".equals(text(obj(it).get("media_type"), ""))) nb++;
        }
        info.nbPhotosAlbum = nb;
    }

    private void fillVideoInfo(Map<String, Object> attachment, ContentInfo info) {
        String videoId = text(obj(attachment.get("target")).get("id"), null);
        Map<String, Object> videoData = graphApi.getVideoInfo(videoId);
        info.dureeVideo = doubleOf(videoData.get("length"));
        info.longueurTitreVideo = text(videoData.get("title"), "").length();
    }

    private void fillPhotoRatio(Map<String, Object> attachment, ContentInfo info) {
        Map<String, Object> image = obj(obj(attachment.get("media")).get("image"));
        Double w = doubleOf(image.get("width"));
        Double h = doubleOf(image.get("height"));
        if (w != null && h != null && h != 0.0) {
            info.ratioImage = simplifierRatio(w.intValue(), h.intValue());
        }
    }

    private static String simplifierRatio(int l, int h) {
        int d = pgcd(l, h);
        return (l / d) + ":" + (h / d);
    }

    private static int pgcd(int a, int b) { return b == 0 ? a : pgcd(b, a % b); }

    private static boolean containsExternalLink(Map<String, Object> post) {
        for (Object it : arr(obj(post.get("attachments")).get("data"))) {
            Map<String, Object> att = obj(it);
            if ("link".equals(text(att.get("media_type"), "")) && att.get("url") != null) return true;
        }
        return false;
    }

    private static int countHashtags(String msg) {
        if (msg == null) return 0;
        Matcher m = HASHTAG_PATTERN.matcher(msg);
        int c = 0;
        while (m.find()) c++;
        return c;
    }

    private static int countEmojis(String msg) {
        if (msg == null) return 0;
        Matcher m = EMOJI_PATTERN.matcher(msg);
        int c = 0;
        while (m.find()) c++;
        return c;
    }

    private static int countTags(Map<String, Object> post) {
        Object rawTags = post.get("message_tags");
        int total = 0;
        if (rawTags instanceof Map<?, ?> map) {
            for (Object v : map.values()) {
                if (v instanceof List<?> l) total += l.size();
            }
        } else if (rawTags instanceof List<?> l) {
            total = l.size();
        }
        return total;
    }

    private static Double toDouble(Integer v) { return v == null ? null : v.doubleValue(); }

    private static double encodeBoolean(Boolean b) { return Boolean.TRUE.equals(b) ? 1.0 : 0.0; }

    private static double oneHot(String val, String cat) { return cat.equals(val) ? 1.0 : 0.0; }

    private static Double encodeRatioImage(String val) {
        if (val == null || val.isEmpty() || !val.contains(":")) return 0.0;
        try {
            String[] parts = val.split(":");
            double l = Double.parseDouble(parts[0]);
            double h = Double.parseDouble(parts[1]);
            if (h == 0 || l == 0) return 0.0;
            return Math.log(l / h);
        } catch (Exception e) {
            return 0.0;
        }
    }

    static class ContentInfo {
        String  typePost          = "texte";
        String  ratioImage        = null;
        Integer nbPhotosAlbum     = null;
        Double  dureeVideo        = null;
        Integer longueurTitreVideo = null;
    }
}
