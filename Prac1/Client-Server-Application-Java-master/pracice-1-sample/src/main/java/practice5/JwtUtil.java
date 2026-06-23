package practice5;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;

import java.util.Date;

public class JwtUtil {

    private static final String SECRET = "not_a_password";
    private static final long EXPIRE_MS = 1000 * 60 * 60;
    private static final Algorithm ALGORITHM = Algorithm.HMAC256(SECRET);

    public static String generateToken(String username) {
        return JWT.create().withSubject(username).withIssuedAt(new Date()).withExpiresAt(new Date(System.currentTimeMillis() + EXPIRE_MS)).sign(ALGORITHM);
    }

    public static String validateTokenAndGetSubject(String token)
            throws JWTVerificationException {
        DecodedJWT jwt = JWT.require(ALGORITHM).build().verify(token);
        return jwt.getSubject();
    }
}