package rabbit.umc.com.demo.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import rabbit.umc.com.config.BaseException;
import rabbit.umc.com.config.BaseResponseStatus;
import rabbit.umc.com.demo.article.ArticleRepository;
import rabbit.umc.com.demo.article.domain.Article;
import rabbit.umc.com.demo.user.Domain.User;
import rabbit.umc.com.demo.user.Dto.UserArticleListResDto;
import rabbit.umc.com.demo.user.Dto.UserEmailNicknameDto;
import rabbit.umc.com.demo.user.Dto.UserGetProfileResDto;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static rabbit.umc.com.config.BaseResponseStatus.POST_USERS_INVALID_EMAIL;

@Service
public class UserService {
    @Autowired
    UserRepository userRepository;

    /**
     * 수정하기!! status가 inactive일 때도 오류나게
     * @param id
     * @return
     * @throws BaseException
     */
    public User findUser(Long id) throws BaseException {
        Optional<User> optionalUser = userRepository.findById(id);
        User user = optionalUser.orElseThrow(() -> new BaseException(BaseResponseStatus.RESPONSE_ERROR));
        return user;
    }

    public void getEmailandNickname(UserEmailNicknameDto userEmailNicknameReqDto) throws BaseException {
        User user = findUser(userEmailNicknameReqDto.getId());
        user.setUserName(userEmailNicknameReqDto.getUserName());
        user.setUserEmail(userEmailNicknameReqDto.getUserEmail());

        userRepository.save(user);
    }

    //이메일 형식 검증
    public void isEmailVerified(UserEmailNicknameDto userEmailNicknameReqDto) throws BaseException {
        String email = userEmailNicknameReqDto.getUserEmail();

        // 이메일 형식을 검증하기 위한 정규 표현식
        String emailPattern = "[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}";

        // 정규 표현식과 입력된 이메일을 비교하여 형식을 검증
        Pattern pattern = Pattern.compile(emailPattern);
        Matcher matcher = pattern.matcher(email);

        if (!matcher.matches()) {
            throw new BaseException(POST_USERS_INVALID_EMAIL);
        }
    }

    //프로필 이미지 수정
    @Transactional
    public void updateProfileImage(Long id, String newProfileImage) throws BaseException {
        userRepository.updateUserUserProfileImageById(id, newProfileImage);
    }

    //닉네임 수정
    @Transactional
    public void updateNickname(Long id, String newNickname){
        userRepository.updateUserUserNameById(id, newNickname);
    }

    //유저 프로필 조회
    public UserGetProfileResDto getProfile(Long id) throws BaseException {
        User user = findUser(id);
        UserGetProfileResDto userGetProfileResDto = new UserGetProfileResDto(user.getUserEmail(), user.getUserName(), user.getUserProfileImage());
        return userGetProfileResDto;
    }

    public List<UserArticleListResDto> getArticles(int page, Long userId) {

        int pageSize = 20;

        PageRequest pageRequest = PageRequest.of(page, pageSize, Sort.by("createdAt").descending());

        List<Article> articlePage = userRepository.findArticlesByUserIdOrderByCreatedAtDesc(userId, pageRequest);

        List<UserArticleListResDto> userArticleListResDtos = articlePage.stream()
                .map(UserArticleListResDto::toArticleListRes)
                .collect(Collectors.toList());

        return userArticleListResDtos;
    }

    public List<UserArticleListResDto> getCommentedArticles(int page, Long userId) {
        int pageSize = 20;

        PageRequest pageRequest = PageRequest.of(page, pageSize, Sort.by("createdAt").descending());

        List<Object[]> articlePageAndCreatedAt = userRepository.findCommentedArticlesByUserId(userId, pageRequest);

        List<Article> articlePage = new ArrayList<>();
        for (Object[] objArr : articlePageAndCreatedAt) {
            Article article = (Article) objArr[0];
            articlePage.add(article);
        }

        List<UserArticleListResDto> userArticleListResDtos = articlePage.stream()
                .map(UserArticleListResDto::toArticleListRes)
                .collect(Collectors.toList());

        return userArticleListResDtos;
    }

    //카테고리별 랭킹
    public Long getRank(Long userId, Long categoryId) throws BaseException {
        //먼저 해당 카테고리의 메인 미션 유저인지 확인
        //mainMissionUser 값들 중에 해당 userId랑 일치한 값이 있는지
        Boolean isMainMissionUser = userRepository.existsMainMissionUserByUserIdAndCategoryId(userId, categoryId);
        Long rank;
        if(isMainMissionUser){
            //순위 확인
            rank = userRepository.getRankByScoreForMainMissionByUserIdAndCategoryId(userId, categoryId);
        }
        else{
            //해당 게시판의 메인 미션에 참여하지 않음
            throw new BaseException(BaseResponseStatus.RESPONSE_ERROR);
        }
        return rank;
    }
}
