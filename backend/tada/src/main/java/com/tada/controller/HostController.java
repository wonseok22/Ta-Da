package com.tada.controller;


import com.tada.domain.dto.HostRequest;
import com.tada.domain.dto.HostResponse;
import com.tada.service.HostService;
import com.tada.util.JwtTokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@CrossOrigin(origins = {"*"}, maxAge = 6000)
@RestController
@RequestMapping("/hosts")
//@Api(tags = {"회원 관리 api"})
public class HostController {
    @Autowired
    private HostService hostService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;


    public static final Logger logger = LoggerFactory.getLogger(HostController.class);

//    @ApiOperation(value = "회원가입", notes = "회원가입 요청 API", response = Map.class)
    @PostMapping("")
    public ResponseEntity<?> joinHost(HttpServletRequest request, @RequestBody HostRequest hostRequest) {
        HostResponse hostResponse = new HostResponse();
        HttpStatus status = HttpStatus.OK;

        try {

            String hostId = hostRequest.getHostId();
            String type = hostRequest.getType();
            hostId = type + "_" + hostId;
            boolean isNewHost = hostService.joinUser(hostId); // kakao id값을 통해 로그인 or 회원가입
            if (isNewHost) { // 새로 회원가입한 유저라면
                status = HttpStatus.CREATED;
            }

            String accessToken = jwtTokenProvider.createAccessToken("hostId", hostId); // 사용자 액세스 토큰 발급
            String refreshToken = jwtTokenProvider.createRefreshToken("hostId", hostId); // 사용자 리프레쉬 토큰 발급
            hostService.saveRefreshToken(hostId, refreshToken);
            logger.debug("로그인 accessToken 정보 : {}", accessToken);
            logger.debug("로그인 refreshToken 정보 : {}", refreshToken);

            hostResponse.setAccessToken(accessToken);
            hostResponse.setRefreshToken(refreshToken);
        } catch (Exception e) {
            logger.error("로그인 실패 : {}", e);
            status = HttpStatus.INTERNAL_SERVER_ERROR;
            return new ResponseEntity<>(status);
        }

        return new ResponseEntity<>(hostResponse, status);
    }


    @PostMapping("/logout")
    public ResponseEntity<?> logoutHost(HttpServletRequest request) {
        HttpStatus status = HttpStatus.OK;
        String header = request.getHeader("Authorization");
        String accessToken = jwtTokenProvider.getTokenByHeader(header);

        if(accessToken == null){ // 토큰이 제대로 담겨오지 않은 경우
            logger.error("토큰 에러");
            status = HttpStatus.FORBIDDEN;
            return new ResponseEntity<>(status);
        }


        if (jwtTokenProvider.validateToken(accessToken)) {
            String hostId = jwtTokenProvider.getHostID(accessToken);
            try {
                logger.info("로그아웃 시도");
                hostService.logoutHost(hostId);
                status = HttpStatus.OK;
            } catch (Exception e) {
                logger.error("로그아웃 실패 : {}", e);
                status = HttpStatus.INTERNAL_SERVER_ERROR;
            }
        }else{  // 토큰이 만료된 경우

            logger.info("로그아웃 실패 액세스 토큰 만료");
            status = HttpStatus.UNAUTHORIZED;
        }
        return new ResponseEntity<>(status);
    }

    @PostMapping("/token/refresh")
    public ResponseEntity<?> refreshToken(HttpServletRequest request) {
        HttpStatus status = HttpStatus.OK;
        HostResponse hostResponse = new HostResponse();
        String header = request.getHeader("Authorization");
        String refreshToken = jwtTokenProvider.getTokenByHeader(header);

        if(refreshToken == null){ // 토큰이 제대로 담겨오지 않은 경우
            logger.error("리프레쉬 토큰 에러");
            status = HttpStatus.FORBIDDEN;
            return new ResponseEntity<>(status);
        }


        if (jwtTokenProvider.validateToken(refreshToken)) {
            try {
                String hostId = jwtTokenProvider.getHostID(refreshToken);
                String hostRefreshToken = hostService.getRefreshtoken(hostId);
                if (hostRefreshToken == null ) {
                    logger.debug("로그인되지 않은 host");
                    status = HttpStatus.UNAUTHORIZED;
                } else if (hostRefreshToken.equals(refreshToken)){ // 로그인 되어있고 리프레시 토큰도 일치할 경우
                    String accessToken = jwtTokenProvider.createAccessToken("hostId", hostId);
                    logger.debug("access-token : {}", accessToken);
                    logger.debug("access-token 재발급 완료");
                    status = HttpStatus.OK;
                    hostResponse.setAccessToken(accessToken);
                    return new ResponseEntity<>(hostResponse, status);
                } else {
                    status = HttpStatus.UNAUTHORIZED;
                }
            } catch (Exception e) {
                logger.error("액세스 토큰 재발급 실패 : {}", e);
                status = HttpStatus.INTERNAL_SERVER_ERROR;
            }
        }else{  // 리프레쉬 토큰이 만료된 경우
            logger.info("액세스 토큰 재발급 실패 리프레쉬 토큰 만료");
            status = HttpStatus.UNAUTHORIZED;
        }
        return new ResponseEntity<>(status);
    }

    @DeleteMapping("")
    public ResponseEntity<?> deleteHost(HttpServletRequest request) {
        HttpStatus status = HttpStatus.OK;
        String header = request.getHeader("Authorization");
        String accessToken = jwtTokenProvider.getTokenByHeader(header);

        if(accessToken == null){ // 토큰이 제대로 담겨오지 않은 경우
            logger.error("토큰 에러");
            status = HttpStatus.FORBIDDEN;
            return new ResponseEntity<>(status);
        }


        if (jwtTokenProvider.validateToken(accessToken)) {
            String hostId = jwtTokenProvider.getHostID(accessToken);
            try {
                logger.info("회원탈퇴 시도");
                hostService.deleteHost(hostId);
                status = HttpStatus.OK;
            } catch (Exception e) {
                logger.error("회원탈퇴 실패 : {}", e);
                status = HttpStatus.INTERNAL_SERVER_ERROR;
            }
        }else{  // 토큰이 만료된 경우

            logger.info("회원탈퇴 실패 액세스 토큰 만료");
            status = HttpStatus.UNAUTHORIZED;
        }
        return new ResponseEntity<>(status);
    }
}
