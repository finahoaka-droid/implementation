package org.example.facebookregression.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


@Service
public class FacebookGraphApiService {

    private static final String GRAPH_URL = "https://graph.facebook.com/v21.0";
    private static final String PAGE_ID   = "464664200069364";

    private final String accessToken;

    private final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(15))
            .build();

    public FacebookGraphApiService(@Value("${facebook.access-token}") String accessToken) {
        this.accessToken = accessToken;
    }

    static class Json {
        private final String s;
        private int i;
        private Json(String s) { this.s = s; }

        static Object parse(String s) {
            Json p = new Json(s);
            p.skipWs();
            return p.parseValue();
        }

        private void skipWs() {
            while (i < s.length() && Character.isWhitespace(s.charAt(i))) i++;
        }

        private Object parseValue() {
            skipWs();
            char c = s.charAt(i);
            if (c == '{') return parseObject();
            if (c == '[') return parseArray();
            if (c == '"') return parseString();
            if (c == 't') { i += 4; return Boolean.TRUE; }
            if (c == 'f') { i += 5; return Boolean.FALSE; }
            if (c == 'n') { i += 4; return null; }
            return parseNumber();
        }

        private Map<String, Object> parseObject() {
            Map<String, Object> map = new LinkedHashMap<>();
            i++; skipWs();
            if (s.charAt(i) == '}') { i++; return map; }
            while (true) {
                skipWs();
                String key = parseString();
                skipWs(); i++;
                map.put(key, parseValue());
                skipWs();
                char c = s.charAt(i);
                if (c == ',') { i++; continue; }
                i++; break;
            }
            return map;
        }

        private List<Object> parseArray() {
            List<Object> list = new ArrayList<>();
            i++; skipWs();
            if (s.charAt(i) == ']') { i++; return list; }
            while (true) {
                list.add(parseValue());
                skipWs();
                char c = s.charAt(i);
                if (c == ',') { i++; continue; }
                i++; break;
            }
            return list;
        }

        private String parseString() {
            StringBuilder sb = new StringBuilder();
            i++;
            while (s.charAt(i) != '"') {
                char c = s.charAt(i);
                if (c == '\\') {
                    i++;
                    char e = s.charAt(i);
                    switch (e) {
                        case 'n' -> sb.append('\n');
                        case 't' -> sb.append('\t');
                        case 'r' -> sb.append('\r');
                        case 'b' -> sb.append('\b');
                        case 'f' -> sb.append('\f');
                        case '"' -> sb.append('"');
                        case '\\' -> sb.append('\\');
                        case '/' -> sb.append('/');
                        case 'u' -> {
                            sb.append((char) Integer.parseInt(s.substring(i + 1, i + 5), 16));
                            i += 4;
                        }
                        default -> sb.append(e);
                    }
                } else {
                    sb.append(c);
                }
                i++;
            }
            i++;
            return sb.toString();
        }

        private Object parseNumber() {
            int start = i;
            while (i < s.length() && "-+.eE0123456789".indexOf(s.charAt(i)) >= 0) i++;
            return Double.parseDouble(s.substring(start, i));
        }
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> obj(Object o) {
        return o == null ? new LinkedHashMap<>() : (Map<String, Object>) o;
    }

    @SuppressWarnings("unchecked")
    public static List<Object> arr(Object o) {
        return o == null ? new ArrayList<>() : (List<Object>) o;
    }

    public static String text(Object o, String def) { return o == null ? def : o.toString(); }
    public static int intOf(Object o, int def)      { return o instanceof Double ? (int)(double)(Double)o : def; }
    public static Double doubleOf(Object o)          { return o instanceof Double ? (Double)o : null; }

    private Map<String, Object> fetchJson(String url) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(30))
                    .GET()
                    .build();
            HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
            return obj(Json.parse(resp.body()));
        } catch (Exception e) {
            Map<String, Object> err = new LinkedHashMap<>();
            Map<String, Object> msg = new LinkedHashMap<>();
            msg.put("message", e.getMessage());
            err.put("error", msg);
            return err;
        }
    }

    private String buildUrl(String base, Map<String, String> params) {
        StringBuilder query = new StringBuilder();
        for (Map.Entry<String, String> e : params.entrySet()) {
            if (!query.isEmpty()) query.append("&");
            query.append(URLEncoder.encode(e.getKey(), StandardCharsets.UTF_8))
                 .append("=")
                 .append(URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8));
        }
        return query.isEmpty() ? base : base + "?" + query;
    }

    private Map<String, String> withToken(String... keyValuePairs) {
        Map<String, String> map = new LinkedHashMap<>();
        for (int k = 0; k < keyValuePairs.length; k += 2) {
            map.put(keyValuePairs[k], keyValuePairs[k + 1]);
        }
        map.put("access_token", accessToken);
        return map;
    }

    public static class PostsResult {
        public final List<Map<String, Object>> posts;
        public final String erreur;

        private PostsResult(List<Map<String, Object>> posts, String erreur) {
            this.posts  = posts;
            this.erreur = erreur;
        }
        public static PostsResult ok(List<Map<String, Object>> p) { return new PostsResult(p, null); }
        public static PostsResult err(String msg)                  { return new PostsResult(List.of(), msg); }
        public boolean isOk() { return erreur == null; }
    }

    public PostsResult getPosts(String since, String until) {
        List<Map<String, Object>> posts = new ArrayList<>();

        Map<String, String> params = withToken(
                "fields",
                "id,created_time,message,message_tags,"
              + "attachments{media_type,url,title,media{image{height,width}},"
              + "target,subattachments{media_type,target}}",
                "since", since,
                "until", until,
                "limit", "50"
        );

        String nextUrl = buildUrl(GRAPH_URL + "/" + PAGE_ID + "/posts", params);

        while (nextUrl != null) {
            Map<String, Object> resp = fetchJson(nextUrl);
            if (resp.containsKey("error")) {
                String detail = obj(resp.get("error")).getOrDefault("message", resp.get("error")).toString();
                System.err.println("[FacebookGraphApiService] Erreur API posts : " + detail);
                return PostsResult.err(detail);
            }
            for (Object item : arr(resp.get("data"))) posts.add(obj(item));
            Map<String, Object> paging = obj(resp.get("paging"));
            nextUrl = paging.containsKey("next") ? text(paging.get("next"), null) : null;
        }
        return PostsResult.ok(posts);
    }

    public Map<String, Object> testConnexion() {
        Map<String, String> params = withToken("fields", "id,name");
        return fetchJson(buildUrl(GRAPH_URL + "/me", params));
    }

    public Map<String, Object> getVideoInfo(String videoId) {
        if (videoId == null) return new LinkedHashMap<>();
        Map<String, String> params = withToken("fields", "length,title,live_status");
        return fetchJson(buildUrl(GRAPH_URL + "/" + videoId, params));
    }

    public double[] getInsights(String postId) {
        Map<String, String> params = withToken("metric", "post_media_view,post_reactions_by_type_total");
        Map<String, Object> resp = fetchJson(buildUrl(GRAPH_URL + "/" + postId + "/insights", params));

        double vues = 0, reactions = 0;
        for (Object m : arr(resp.get("data"))) {
            Map<String, Object> metric = obj(m);
            String name = text(metric.get("name"), "");
            List<Object> values = arr(metric.get("values"));
            if (values.isEmpty()) continue;
            Object value = obj(values.get(0)).get("value");
            if ("post_media_view".equals(name) && value instanceof Double d) {
                vues = d;
            } else if ("post_reactions_by_type_total".equals(name) && value instanceof Map<?,?> map) {
                for (Object v : map.values()) {
                    if (v instanceof Double d) reactions += d;
                }
            }
        }
        return new double[]{vues, reactions};
    }

    public int getCommentsCount(String postId) {
        Map<String, String> params = withToken("summary", "true");
        Map<String, Object> resp = fetchJson(buildUrl(GRAPH_URL + "/" + postId + "/comments", params));
        return intOf(obj(resp.get("summary")).get("total_count"), 0);
    }
}
