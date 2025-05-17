package fooding.foodingback.Auth.controller;

import fooding.foodingback.Auth.service.KakaoAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class KakaoAuthController {

    private final KakaoAuthService kakaoAuthService;

    @GetMapping("/kakao/callback")
    public ResponseEntity<String> kakaoLogin(@RequestParam String code) {
        String jwtToken = kakaoAuthService.kakaoLogin(code);
        return ResponseEntity.ok(jwtToken);
    }
}
