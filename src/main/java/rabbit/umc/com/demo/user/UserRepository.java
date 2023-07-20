package rabbit.umc.com.demo.user;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Repository;
import rabbit.umc.com.demo.Status;
import rabbit.umc.com.demo.article.domain.Article;
import rabbit.umc.com.demo.mainmission.domain.MainMission;
import rabbit.umc.com.demo.user.Domain.User;
import rabbit.umc.com.demo.user.Dto.UserCommentedArticleListResDto;
import rabbit.umc.com.demo.user.Dto.UserGetProfileResDto;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    User findByKakaoId(Long kakaoId);
    boolean existsByKakaoId(Long kakaoId);
    boolean existsByUserName(String userName);

//    @Modifying
//    @Query("UPDATE User u SET u.userProfileImage = :newProfileImage WHERE u.id = :userId")
//    void updateUserUserProfileImageById(@Param("userId") Long userId, @Param("newProfileImage") String newProfileImage);


//    @Modifying
//    @Query("UPDATE User u SET u.userProfileImage = :newProfileImage WHERE u.id = :userId")
//    void updateUserUserProfileImageById(@Param("userId") Long userId, @Param("newProfileImage") String newProfileImage);

    //유저가 쓴 글 조회
    @Query("SELECT a FROM Article a " +
            "WHERE a.user.id = :userId " +
            "AND a.status = 'ACTIVE' " +
            "ORDER BY a.createdAt DESC")
    List<Article> findArticlesByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId, PageRequest pageRequest);

    //유저가 댓글을 남긴 글 조회
        @Query("SELECT a, max(c.createdAt) as maxCreatedAt " +
            "FROM Article a " +
            "JOIN a.comments c " +
            "WHERE c.user.id = :userId " +
            "AND a.status = 'ACTIVE' " +
            "GROUP BY a " +
            "ORDER BY maxCreatedAt DESC")
    List<UserCommentedArticleListResDto> findCommentedArticlesByUserId(@Param("userId") Long userId, PageRequest pageRequest);

//    //해당 유저가 해당 메인 미션 유저인지 확인
//    @Query("SELECT CASE WHEN COUNT(mmu) > 0 THEN true ELSE false END " +
//            "FROM MainMissionUsers mmu " +
//            "WHERE mmu.user.id = :userId " +
//            "AND mmu.mainMission.category.id = :categoryId")
//    boolean existsMainMissionUserByUserIdAndCategoryId(@Param("userId") Long userId, @Param("categoryId") Long categoryId);

    //유저 랭킹 조회
//    @Query("SELECT COUNT(*) + 1 " +
//            "FROM MainMissionUsers mmu " +
//            "WHERE mmu.mainMission.category.id = :categoryId " +
//            "AND mmu.score > (SELECT mmu2.score FROM MainMissionUsers mmu2 WHERE mmu2.user.id = :userId)")
//    Long getRankByScoreForMainMissionByUserIdAndCategoryId(@Param("userId") Long userId, @Param("categoryId") Long categoryId);

    //각 메인 미션 유저별로 미션 프루프 게시물 올린 개수 체크 + 그리고 개수가 더 해당 유저보다 더 많은 사람 체크+1
    //랭킹이 미션 게시물 개수였나???
}