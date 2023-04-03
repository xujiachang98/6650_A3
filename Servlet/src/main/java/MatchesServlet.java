import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@WebServlet(name = "MatchesServlet", value = "/matches")
public class MatchesServlet extends HttpServlet {
    private final static String REDIS_HOST = "35.145.143.31";
    private final static int REDIS_PORT = 6379;

    private JedisPool jedisPool;

    @Override
    public void init() throws ServletException {
        super.init();
        this.jedisPool = new JedisPool(REDIS_HOST, REDIS_PORT);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        String urlPath = req.getPathInfo();
        String[] urlParts = urlPath.split("/");
        if (!Utils.isValidUserId(urlParts)) {
            res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            res.getWriter().write("Invalid user ID: " + urlPath);
            return;
        }

        try (Jedis jedis = jedisPool.getResource()) {
            String key = urlParts[1] + "-matches";
            List<String> matches = new ArrayList<>();
            for (int i = 0; i < 100; i++) {
                String match = jedis.lindex(key, i);
                if (match == null) {
                    break;
                }
                matches.add(match);
            }

            if (matches.isEmpty()) {
                res.setStatus(HttpServletResponse.SC_NOT_FOUND);
                res.getWriter().write("Matches not found: " + urlPath);
                return;
            }

            Gson gson = new Gson();
            JsonArray matchList = new JsonArray();
            for (String match : matches) {
                matchList.add(match);
            }

            JsonObject result = new JsonObject();
            result.add("matchList", matchList);

            res.setStatus(HttpServletResponse.SC_OK);
            res.setContentType("application/json");
            res.setCharacterEncoding("UTF-8");
            res.getWriter().write(gson.toJson(result));
        } catch (Exception e) {
            res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            res.getWriter().write("Error: " + e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        // Not implemented for this servlet
    }
}