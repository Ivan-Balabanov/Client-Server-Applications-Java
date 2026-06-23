package practice5;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.sun.net.httpserver.Authenticator;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpPrincipal;

public class JwtAuthenticator extends Authenticator {

    @Override
    public Result authenticate(HttpExchange exchange) {
        String authHeader = exchange.getRequestHeaders()
                .getFirst("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return new Failure(401);
        }

        String token = authHeader.substring(7);

        try {
            String username = JwtUtil.validateTokenAndGetSubject(token);
            return new Success(new HttpPrincipal(username, "store"));
        } catch (JWTVerificationException e) {
            return new Failure(401);
        }
    }
}