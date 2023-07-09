package rabbit.umc.com.demo.article.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import rabbit.umc.com.demo.article.domain.Article;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Data
@AllArgsConstructor
public class ArticleRes {
    private Long articleId;
    private Long authorId;
    private String authorProfileImage;
    private String authorName;
    private LocalDateTime uploadTime;
    private String articleTitle;
    private String articleContent;
    private List<ArticleImageDto> articleImage;
    private List<CommentListDto> commentList;

    public static ArticleRes toArticleRes(Article article, List<ArticleImageDto> articleImage, List<CommentListDto> commentList){
        return new ArticleRes(
                article.getId(),
                article.getUser().getId(),
                article.getUser().getUserProfileImage(),
                article.getUser().getUserName(),
                article.getCreatedAt(),
                article.getTitle(),
                article.getContent(),
                articleImage,
                commentList
        );
    }

}
