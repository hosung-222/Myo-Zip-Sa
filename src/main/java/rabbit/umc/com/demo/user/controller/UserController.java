package rabbit.umc.com.demo.user.controller;

import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import rabbit.umc.com.config.apiPayload.BaseException;
import rabbit.umc.com.config.apiPayload.BaseResponse;
import rabbit.umc.com.demo.user.Domain.User;
import rabbit.umc.com.demo.user.Dto.*;
import rabbit.umc.com.demo.user.service.KakaoService;
import rabbit.umc.com.demo.user.service.UserService;
import rabbit.umc.com.utils.JwtService;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import static rabbit.umc.com.config.apiPayload.BaseResponseStatus.*;


@RestController
@Slf4j
@RequiredArgsConstructor
@Tag(name = "user", description = "사용자 API")
@RequestMapping("/app/users")
public class UserController {
    private final UserService userService;
    private final KakaoService kakaoService;
    private final JwtService jwtService;


    /**
     * 카카오 로그인 api
     * // * @param accessToken
     *
     * @return
     * @throws IOException
     * @throws BaseException
     */

    @GetMapping("/kakao-login")
    @Operation(summary = "카카오 로그인 API")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "COMMON200", description = "OK, 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "JWT4001", description = "JWT 토큰을 주세요!", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "JWT4002", description = "JWT 토큰 만료", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "USER4010", description = "DB에 존재하지 않는 회원", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
    })
    @Parameters({
            @Parameter(name = "Authorization", description = "카카오에서 받아오는 엑세스 토큰을 넣어주세요.", in = ParameterIn.HEADER)
    })
    public BaseResponse<UserLoginResDto> kakaoLogin(@RequestHeader("Authorization") String accessToken) throws IOException, BaseException {
        try {
            if (accessToken == null) {
                throw new BaseException(EMPTY_KAKAO_ACCESS);
            }

            UserLoginResDto userLoginResDto = kakaoService.kakaoLogin(accessToken);

            return new BaseResponse<>(userLoginResDto);
        } catch (BaseException exception) {
            return new BaseResponse<>(exception.getStatus());
        }
    }

    @GetMapping("/kakao-login-web")
    @ApiOperation(value = "카카오 로그인 웹 API", hidden = true)
    public BaseResponse<UserLoginResDto> kakaoLoginWeb(@RequestParam String code, HttpServletResponse response) throws IOException, BaseException {
        try {
            String accessToken = kakaoService.getAccessToken(code);
            System.out.println("------------------------- kakao access token: " + accessToken + " -------------------------");
            UserLoginResDto userLoginResDto = kakaoService.kakaoLogin(accessToken);

            return new BaseResponse<>(userLoginResDto);
        } catch (BaseException exception) {
            return new BaseResponse<>(exception.getStatus());
        }
    }

    /**
     * 카카오 로그아웃
     *
     * @return 로그아웃 유저 id
     * @throws BaseException
     * @throws IOException
     */
    @GetMapping("/kakao-logout")
    @Operation(summary = "회원 로그아웃 API")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "COMMON200", description = "OK, 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "JWT4001", description = "JWT 토큰을 주세요!", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "JWT4002", description = "JWT 토큰 만료", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
    })
    public BaseResponse<Long> kakaoLogout(HttpServletResponse response) throws BaseException, IOException {
        try {
            int userId = jwtService.getUserIdx();
            Long logout_kakaoId = kakaoService.logout(Long.valueOf(userId));

            log.info("로그아웃이 완료되었습니다.");
            return new BaseResponse<>(logout_kakaoId);
        } catch (BaseException exception) {
            return new BaseResponse<>(exception.getStatus());
        }
    }

    /**
     * 회원 탈퇴(카카오 연결 끊기)
     *
     * @param response
     * @return 탈퇴 유저 id
     * @throws BaseException
     * @throws IOException
     */
    @GetMapping("/kakao-unlink")
    @Operation(summary = "회원 탈퇴 API")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "COMMON200", description = "OK, 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "JWT4001", description = "JWT 토큰을 주세요!", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "JWT4002", description = "JWT 토큰 만료", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
    })
    public BaseResponse<Long> kakaoUnlink(HttpServletResponse response) throws BaseException, IOException {
        try {
            //jwt 토큰으로 로그아웃할 유저 아이디 받아오기
            int userId = jwtService.getUserIdx();

            //유저 아이디로 카카오 아이디 받아오기
            Long logout_kakaoId = kakaoService.unlink((long) userId);
            log.info("회원 탈퇴가 완료되었습니다.");
            return new BaseResponse<>(logout_kakaoId);
        } catch (BaseException exception) {
            return new BaseResponse<>(exception.getStatus());
        }
    }

    /**
     * 회원 탈퇴(앱이 아닌 경로로 연결 끊기)
     *
     * @param authorizationHeader
     * @param kakaoId             연결 끊는 유저의 kakao id
     * @param referrerType
     * @throws BaseException
     * @throws IOException
     */
    @GetMapping("/kakao-disconnect")
    @Operation(summary = "회원 탈퇴 API - 사용자가 앱이 아닌 카카오 계정 관리 페이지나 고객센터에서 연결 끊기를 진행하는 경우")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "COMMON200", description = "OK, 성공"),
    })
    public BaseResponse<Long> kakaoDisconnect(
            @RequestParam("user_id") String kakaoId,
            @RequestParam("referrer_type") String referrerType,
            @RequestHeader("Authorization") String authorizationHeader,
            HttpServletResponse response) throws BaseException, IOException {

        try {
//            System.out.println("카카오 헤더: " + authorizationHeader);
//            System.out.println("카카오 유저 아이디: " + kakaoId);
//            System.out.println("referrer type: " + referrerType);
            log.info("탈퇴하는 user id: {}", kakaoId);

            Long logout_kakaoId = kakaoService.disconnect(Long.parseLong(kakaoId));
            log.info("회원 탈퇴가 완료되었습니다.");
            return new BaseResponse<>(logout_kakaoId);
        } catch (BaseException exception) {
            return new BaseResponse<>(exception.getStatus());
        }
    }

    /**
     * 회원가입 API
     *
     * @return
     * @throws BaseException
     */
    @PostMapping("/sign-up")
    @Operation(summary = "회원 가입 API")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "COMMON200", description = "OK, 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "JWT4001", description = "JWT 토큰을 주세요!", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "JWT4002", description = "JWT 토큰 만료", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "USER4011", description = "이미 존재하는 회원", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
    })
    @Parameters({
            @Parameter(name = "Authorization", description = "카카오에서 받아오는 엑세스 토큰을 넣어주세요.", in = ParameterIn.HEADER)
    })
    public BaseResponse<UserLoginResDto> signUpUser(@RequestHeader("Authorization") String accessToken,
                                                    @RequestBody UserNicknameReqDto userNicknameReqDto) throws IOException, BaseException {
        try {
            if (accessToken == null) {
                throw new BaseException(EMPTY_KAKAO_ACCESS);
            }
            UserLoginResDto userLoginResDto = kakaoService.signUpUser(userNicknameReqDto.getUserName(), accessToken);

            return new BaseResponse<>(userLoginResDto);
        } catch (BaseException exception) {
            return new BaseResponse<>(exception.getStatus());
        }

    }

    /**
     * 닉네임, 프로필 이미지 수정
     *
     * @param userProfileImage
     * @param userName
     * @return
     */
    @PatchMapping("/profile")
    @Operation(summary = "닉네임 및 프로필 이미지 수정 API")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "COMMON200", description = "OK, 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "JWT4001", description = "JWT 토큰을 주세요!", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "JWT4002", description = "JWT 토큰 만료", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
    })
    @Parameters({
            @Parameter(name = "userProfileImage", description = "MultipartFile 유저 프로필 이미지"),
            @Parameter(name = "userName", description = "유저 닉네임입니다.", in = ParameterIn.QUERY)
    })
    public BaseResponse<Long> updateProfile(@RequestPart MultipartFile userProfileImage, @RequestParam String userName) throws IOException {
        try {
            Long userId = (long) jwtService.getUserIdx();
//            User user = userService.findUser(userId);
            userService.updateProfile(userId, userName, userProfileImage);
            return new BaseResponse<>(userId);
        } catch (BaseException exception) {
            return new BaseResponse<>(exception.getStatus());
        }
    }

    /**
     * 닉네임 중복확인
     *
     * @param userName
     * @return
     * @throws BaseException
     */
    @GetMapping("/checkDuplication")
    @Operation(summary = "닉네임 중복 확인 API")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "COMMON200", description = "OK, 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "JWT4001", description = "JWT 토큰을 주세요!", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "JWT4002", description = "JWT 토큰 만료", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
    })
    @Parameters({
            @Parameter(name = "userName", description = "중복인지 확인할 닉네임입니다", in = ParameterIn.QUERY),
            @Parameter(name = "hasAccount", description = "기존 계정이 있는지 여부 (회원가입 하는 경우: false, 닉네임 수정하는 경우: true)")
    })
    public BaseResponse<Boolean> updateNickname(@RequestParam String userName, @RequestParam boolean hasAccount) throws BaseException {
        try {
            if (hasAccount) {
                Long jwtUserId = (long) jwtService.getUserIdx();
                return new BaseResponse<>(userService.isExistSameNickname(userName, jwtUserId));
            } else {
                return new BaseResponse<>(userService.isExistSameNicknameWithoutUserId(userName));
            }

        } catch (BaseException exception) {
            return new BaseResponse<>(exception.getStatus());
        }
    }

    /**
     * 유저 프로필 조회
     *
     * @return
     * @throws BaseException
     */
    @GetMapping("/profile")
    @Operation(summary = "유저 프로필 조회 API")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "COMMON200", description = "OK, 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "JWT4001", description = "JWT 토큰을 주세요!", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "JWT4002", description = "JWT 토큰 만료", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
    })
    public BaseResponse<UserGetProfileResDto> getProfile() throws BaseException {
        try {
            Long jwtUserId = (long) jwtService.getUserIdx();
            System.out.println("프로필을 조회합니다. 회원번호: " + jwtUserId);
            UserGetProfileResDto userGetProfileResDto = userService.getProfile(jwtUserId);
            return new BaseResponse(userGetProfileResDto);
        } catch (BaseException exception) {
            return new BaseResponse<>(exception.getStatus());
        }
    }

    /**
     * 유저가 작성한 글 전체 조회
     *
     * @param page
     * @return
     * @throws BaseException
     */
    @GetMapping("/articleList")
    @Operation(summary = "유저가 작성한 글 전체 조회 API")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "COMMON200", description = "OK, 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "JWT4001", description = "JWT 토큰을 주세요!", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "JWT4002", description = "JWT 토큰 만료", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
    })
    @Parameters({
            @Parameter(name = "page", description = "유저가 작성한 글 목록 중 몇번째 페이지를 조회할지 선택할 수 있습니다", in = ParameterIn.QUERY, required = false)
    })
    public BaseResponse<List<UserArticleListResDto>> getArticles(@RequestParam(defaultValue = "0", name = "page") int page) throws BaseException {
        try {
            Long jwtUserId = (long) jwtService.getUserIdx();
            System.out.println("유저가 작성한 글을 조회합니다. 회원번호: " + jwtUserId);
            List<UserArticleListResDto> userArticleListResDtos = userService.getArticles(page, jwtUserId);
            return new BaseResponse<>(userArticleListResDtos);
        } catch (BaseException exception) {
            return new BaseResponse<>(exception.getStatus());
        }
    }

    /**
     * 유저가 댓글단 글 전체 조회
     *
     * @param page
     * @return
     * @throws BaseException
     */
    @GetMapping("/commented-articles")
    @Operation(summary = "유저가 댓글단 글 전체 조회 API")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "COMMON200", description = "OK, 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "JWT4001", description = "JWT 토큰을 주세요!", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "JWT4002", description = "JWT 토큰 만료", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
    })
    @Parameters({
            @Parameter(name = "page", description = "유저가 댓글단 글 목록 중 몇번째 페이지를 조회할지 선택할 수 있습니다", in = ParameterIn.QUERY, required = false)
    })
    public BaseResponse<List<UserArticleListResDto>> getCommentedArticles(@RequestParam(defaultValue = "0", name = "page") int page) throws BaseException {
        try {
            Long jwtUserId = (long) jwtService.getUserIdx();
            System.out.println("유저가 댓글단 글을 조회합니다. 회원번호: " + jwtUserId);
            List<UserArticleListResDto> userArticleListResDtos = userService.getCommentedArticles(page, jwtUserId);
            return new BaseResponse<>(userArticleListResDtos);
        } catch (BaseException exception) {
            return new BaseResponse<>(exception.getStatus());
        }
    }

    /**
     * access token 재발급
     *
     * @param accessToken
     * @param refreshToken
     * @return
     * @throws BaseException
     */
    @GetMapping("/reissue")

    @Operation(summary = "access token 재발급 API")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "COMMON200", description = "OK, 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "JWT4001", description = "JWT 토큰을 주세요!", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "JWT4002", description = "JWT 토큰 만료", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
    })
    @Parameters({
            @Parameter(name = "X-ACCESS-TOKEN", description = "JWT에서 받아오는 엑세스 토큰입니다.", in = ParameterIn.HEADER),
            @Parameter(name = "X-REFRESH-TOKEN", description = "JWT에서 받아오는 리프레시 토큰입니다.", in = ParameterIn.HEADER)
    })
    public BaseResponse<ReissueTokenDto> reissueToken(@RequestHeader("X-ACCESS-TOKEN") String accessToken, @RequestHeader("X-REFRESH-TOKEN") String refreshToken) throws BaseException {
        try {
            ReissueTokenDto reissueTokenDto = null;
            if (jwtService.getExpirationDate(accessToken).before(new Date())) {
                //만료된 accessToken 이용해서 user id 알아내기
                Long userId = jwtService.getUserIdFromToken(accessToken);
                boolean canReissue = userService.isReissueAllowed(userId, refreshToken);

                if (canReissue) {
                    String jwtToken = jwtService.createJwt(Math.toIntExact(userId));
                    reissueTokenDto = new ReissueTokenDto(userId, jwtToken, refreshToken);
                    System.out.println(jwtToken);
                } else {
                    userService.cannotReissue(Long.valueOf(userId));
                    throw new BaseException(INVALID_JWT_REFRESH);
                }
            } else {
                log.info("access token의 유효기간이 남아있어 재발급이 불가합니다.");
                throw new BaseException(UNEXPIRED_JWT_ACCESS);
            }
            return new BaseResponse<>(reissueTokenDto);
        } catch (BaseException exception) {
            return new BaseResponse<>(exception.getStatus());
        }
    }

    /**
     * 성공 히스토리
     */
    @GetMapping("/success")
    @Operation(summary = "성공 히스토리 API")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "COMMON200", description = "OK, 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "JWT4001", description = "JWT 토큰을 주세요!", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "JWT4002", description = "JWT 토큰 만료", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
    })
    public BaseResponse<UserMissionHistoryDto> getSuccessMissions() {
        try {
            Long userId = (long) jwtService.getUserIdx();
            UserMissionHistoryDto result = userService.getSuccessMissions(userId);
            return new BaseResponse<>(result);
        } catch (BaseException e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * 실패 히스토리
     */
    @GetMapping("/failure")
    @Operation(summary = "실패 히스토리 API")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "COMMON200", description = "OK, 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "JWT4001", description = "JWT 토큰을 주세요!", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "JWT4002", description = "JWT 토큰 만료", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
    })
    public BaseResponse<UserMissionHistoryDto> getFailureMissions() {
        try {
            Long userId = (long) jwtService.getUserIdx();
            UserMissionHistoryDto result = userService.getFailureMissions(userId);
            return new BaseResponse<>(result);
        } catch (BaseException e) {
            return new BaseResponse(e.getMessage());
        }
    }

    @GetMapping("/isValid")
    @Operation(summary = "access token과 user가 유효한지 확인")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "COMMON200", description = "OK, 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "JWT4001", description = "JWT 토큰을 주세요!", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "JWT4002", description = "JWT 토큰 만료", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
    })
    @Parameters({
            @Parameter(name = "X-ACCESS-TOKEN", description = "JWT에서 받아오는 엑세스 토큰입니다.", in = ParameterIn.HEADER)
    })
    public BaseResponse<Boolean> isValid(@RequestHeader("X-ACCESS-TOKEN") String accessToken){
        try{
            Long userId = (long) jwtService.getUserIdx();
            return new BaseResponse<>(userService.isValidException(userId));
        } catch (BaseException e){
            return new BaseResponse(e.getMessage());
        }
    }

}