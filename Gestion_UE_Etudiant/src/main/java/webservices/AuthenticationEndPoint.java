package webservices;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import javax.crypto.spec.SecretKeySpec;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.security.Key;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Path("/authentification")
public class AuthenticationEndPoint {

    private static final long ACCESS_TOKEN_LIFETIME_MS = 15 * 60 * 1000L;
    private static final long REFRESH_TOKEN_LIFETIME_MS = 7 * 24 * 60 * 60 * 1000L;

    /*
     * Demo only: this set is lost on restart and isn't shared across servers.
     * For production, store refresh-token IDs (or token hashes) in persistent storage.
     */
    private static final Set<String> ACTIVE_REFRESH_IDS =
            ConcurrentHashMap.newKeySet();

    @Context
    private UriInfo uriInfo;

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response authenticateUser(
            @FormParam("username") String username,
            @FormParam("password") String password) {

        try {
            authenticate(username, password);

            return Response.ok(createTokenResponse(username)).build();
        } catch (Exception e) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }
    }

    @POST
    @Path("/refresh")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response refreshToken(
            @FormParam("refreshToken") String refreshToken) {

        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(getSigningKey())
                    .parseClaimsJws(refreshToken)
                    .getBody();

            if (!"refresh".equals(claims.get("token_use", String.class))) {
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }

            String refreshId = claims.getId();
            String username = claims.getSubject();

            // Remove the old ID so this refresh token cannot be used again.
            if (refreshId == null
                    || username == null
                    || !ACTIVE_REFRESH_IDS.remove(refreshId)) {
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }

            // Return a new access token and a rotated refresh token.
            return Response.ok(createTokenResponse(username)).build();

        } catch (JwtException | IllegalArgumentException e) {
            // Includes invalid, expired, or incorrectly signed JWTs.
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
    }

    private void authenticate(String username, String password) {
        /*
         * TODO: Check the username and password against your real user store.
         * Do not leave this as a no-op: as written, it authenticates everyone.
         */
        if (username == null || password == null) {
            throw new SecurityException("Invalid credentials");
        }
    }

    private Map<String, String> createTokenResponse(String username) {
        Map<String, String> tokens = new HashMap<>();
        tokens.put("accessToken", issueToken(username, "access", ACCESS_TOKEN_LIFETIME_MS, false));
        tokens.put("refreshToken", issueToken(username, "refresh", REFRESH_TOKEN_LIFETIME_MS, true));
        tokens.put("tokenType", "Bearer");
        return tokens;
    }

    private String issueToken(
            String username,
            String tokenUse,
            long lifetimeMs,
            boolean trackRefreshId) {

        long now = System.currentTimeMillis();
        String tokenId = UUID.randomUUID().toString();

        if (trackRefreshId) {
            ACTIVE_REFRESH_IDS.add(tokenId);
        }

        return Jwts.builder()
                .setSubject(username)
                .setIssuer(uriInfo.getAbsolutePath().toString())
                .setId(tokenId)
                .claim("token_use", tokenUse)
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + lifetimeMs))
                .signWith(SignatureAlgorithm.HS512, getSigningKey())
                .compact();
    }

    private Key getSigningKey() {
        // Set JWT_SECRET_BASE64 to a securely generated Base64-encoded secret.
        String secret = System.getenv("JWT_SECRET_BASE64");
        if (secret == null || secret.trim().isEmpty()) {
            throw new IllegalStateException("JWT_SECRET_BASE64 is not configured");
        }

        byte[] keyBytes = Base64.getDecoder().decode(secret);
        if (keyBytes.length < 64) {
            throw new IllegalStateException(
                    "JWT_SECRET_BASE64 must decode to at least 64 bytes for HS512");
        }

        return new SecretKeySpec(keyBytes, "HmacSHA512");
    }
}
