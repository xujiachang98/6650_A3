import com.google.gson.Gson;
import com.google.gson.JsonObject;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.annotation.WebServlet;
import java.io.IOException;
import java.util.Map;

@WebServlet(name = "StatServlet", value = "/StatServlet")
public class StatServlet extends HttpServlet {
    private final static String REDIS_HOST = "35.160.148.12";
    private JedisPool jedisPool;

    @Override
    public void init() throws ServletException {
        super.init();
        this.jedisPool = new JedisPool(REDIS_HOST, 6379);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        try {
            res.setContentType("application/json");
            String urlPath = req.getPathInfo();
            String[] urlParts = urlPath.split("/");
            if (!Utils.isValidUserId(urlParts)) {
                res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                res.getWriter().write("Invalid input: " + urlPath);
                return;
            }

            try (Jedis jedis = jedisPool.getResource()) {
                Map<String, String> userInfo = jedis.hgetAll(urlParts[1]);
                if (userInfo.isEmpty()) {
                    res.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    res.getWriter().write("User not found: " + urlPath);
                } else {
                    String numLikes = userInfo.getOrDefault("left", "0");
                    String numDislikes = userInfo.getOrDefault("right", "0");
                    JsonObject jsonObject = new JsonObject();
                    jsonObject.addProperty("numLikes", numLikes);
                    jsonObject.addProperty("numDislikes", numDislikes);
                    String jsonString = new Gson().toJson(jsonObject);
                    res.setStatus(HttpServletResponse.SC_OK);
                    res.getWriter().write(jsonString);
                }
            }
        } catch (Exception e) {
            res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            res.getWriter().write("Error occurred: " + e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        // Not implemented for this servlet
    }


}
