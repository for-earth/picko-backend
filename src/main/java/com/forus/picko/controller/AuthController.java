package com.forus.picko.controller;

import com.forus.picko.domain.GrantType;
import com.forus.picko.domain.Token;
import com.forus.picko.dto.KakaoLoginRequestDto;
import com.forus.picko.service.KakaoOAuth2Service;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;

@Tag(name = "auth", description = "Auth API")
@RestController
@RequestMapping("/api/oauth")
public class AuthController {

    private static KakaoOAuth2Service kakaoOAuth2Service;

    @Autowired
    public AuthController(KakaoOAuth2Service kakaoOAuth2Service) {
        this.kakaoOAuth2Service = kakaoOAuth2Service;
    }

    @Operation(summary = "token", description = "token 발급 & 재발급")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = Token.class))),
    })
    @Parameters({
            @Parameter(name = "grantType", description = "토큰발급 타입", schema = @Schema(implementation = GrantType.class), required = true),
            @Parameter(name = "code", description = "토큰을 처음 발급 받을 때만 넣어주세요. grantType이 authorization_code 여야 합니다."),
            @Parameter(name = "refreshToken", description = "토큰 재발급 시에만 넣어주세요. grantType이 refresh_token 여야 합니다.")
    })
    @PostMapping("/kakao/token")
    @ResponseBody
    public ResponseEntity<Token> kakaoToken(@RequestHeader String origin, @RequestBody KakaoLoginRequestDto request) throws Exception {
        GrantType grantType = request.getGrantType();
        String code = request.getCode();
        String refreshToken = request.getRefreshToken();

        if (grantType == GrantType.authorization_code) {
            try {
                return ResponseEntity.ok(kakaoOAuth2Service.getToken(code, origin));
            } catch (HttpClientErrorException e) {
                return new ResponseEntity(e.getMessage(), e.getStatusCode());
            }
        }

        if (grantType == GrantType.refresh_token) {
            try {
                return ResponseEntity.ok(kakaoOAuth2Service.reissueToken(refreshToken));
            } catch (HttpClientErrorException e) {
                return new ResponseEntity(e.getMessage(), e.getStatusCode());
            }
        }

        throw new Exception("올바르지 않은 grant_type 입니다.");
    }
}

