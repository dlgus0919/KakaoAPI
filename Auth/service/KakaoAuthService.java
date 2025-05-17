package fooding.foodingback.Auth.service;


import fooding.foodingback.global.util.JwtUtil;
import fooding.foodingback.User.entity.User;
import fooding.foodingback.User.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class KakaoAuthService {

    private final UserRepository userRepository;
    private final RestTemplate restTemplate = new RestTemplate();
    private final JwtUtil jwtUtil;

    @Value("${spring.security.oauth2.client.registration.kakao.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.provider.kakao.token-uri}")
    private String tokenUri;

    @Value("${spring.security.oauth2.client.provider.kakao.user-info-uri}")
    private String userInfoUri;

    public String kakaoLogin(String code) {
        String accessToken = getKakaoAccessToken(code);
        Map<String, Object> userInfo = getKakaoUserInfo(accessToken);
        String kakaoId = String.valueOf(userInfo.get("id"));
        User user = userRepository.findByKakaoId(kakaoId)
                .orElseGet(() -> userRepository.save(User.builder().kakaoId(kakaoId).build()));

        return jwtUtil.generateToken(user.getKakaoId());
    }

    // 카카오 토큰 요청
    private String getKakaoAccessToken(String code) {
        String requestUri = UriComponentsBuilder.fromHttpUrl(tokenUri)
                .queryParam("grant_type", "authorization_code")
                .queryParam("client_id", clientId)
                .queryParam("redirect_uri", "http://localhost:8080/auth/kakao/callback")
                .queryParam("code", code)
                .toUriString();

        ResponseEntity<Map> response = restTemplate.postForEntity(requestUri, null, Map.class);
        return (String) response.getBody().get("access_token");
    }

    // 카카오 사용자 정보 요청
    private Map<String, Object> getKakaoUserInfo(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<Map> response = restTemplate.exchange(userInfoUri, HttpMethod.GET, entity, Map.class);
        return response.getBody();
    }

}
