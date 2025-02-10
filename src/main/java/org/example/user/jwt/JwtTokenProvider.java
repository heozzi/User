package org.example.user.jwt;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT토큰 생성, 검증, 필요한 정보 추출등 관리
 */
@Component
public class JwtTokenProvider {
    // 맴버 변수 형태 구성
    // 토큰 생성시 필요한 비밀키
    @Value("${jwt.token.raw_secret_key}")
    private String rawSecretKey;

    // 엑세스 토큰 만료시간
    @Value("${jwt.access-token-expiration}")
    private long accessTokenExpiration;

    // 리플레시 토큰 만료시간
    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    // JWT 시크릿키 처리
    private Key getSecretKey() {
        return Keys.hmacShaKeyFor(rawSecretKey.getBytes());
    }

    // 엑세스토큰 생성(발급)
    public String createAccessToken(String email, String role) {
        return createToken(email, role, accessTokenExpiration); // 1시간 만료시간
    }

    // 리플레시토큰 생성(발급) : 재료 x
    public String createRefreshToken(){
        return createToken(null, null, refreshTokenExpiration); // 7일 만료시간
    }

    // 토큰 생성 통합 : 이메일(or null), 롤(or null), 만료시간
    public String createToken(String email, String role, long expiration) {
        Map<String, Object> claims = new HashMap<>();
        if(email != null) {
            claims.put("email", email);
        }
        if(role != null) {
            claims.put("role", role);
        }

        Date now = new Date(); // 현재시간
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .setClaims( claims )
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSecretKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // 토큰 검증
    public boolean validateToken(String token){
        try{
            Jwts.parserBuilder().setSigningKey(getSecretKey()).build().parseClaimsJws(token);
            return true;
        }catch (Exception e){
            return false;
        }
    }
}
