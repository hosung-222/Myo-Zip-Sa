package rabbit.umc.com.demo.community.domain.mapping;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import rabbit.umc.com.demo.base.BaseTimeEntity;
import rabbit.umc.com.demo.base.Status;
import rabbit.umc.com.demo.community.domain.Article;
import rabbit.umc.com.demo.user.Domain.User;
import javax.persistence.*;
import static javax.persistence.GenerationType.IDENTITY;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "like_article")
public class LikeArticle extends BaseTimeEntity {
    @Id@GeneratedValue(strategy = IDENTITY)
    @Column(name = "like_article_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id",nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "article_id",nullable = false)
    private Article article;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "VARCHAR(15) DEFAULT 'ACTIVE'")
    private Status status;


    //Setter
    public void setLikeArticle(User user, Article article){
        this.user = user;
        this.article = article;
    }
}
