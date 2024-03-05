package synk.meeteam.domain.recruitment.recruitment_post;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static synk.meeteam.domain.recruitment.recruitment_post.RecruitmentPostFixture.TITLE_EXCEED_40;

import jakarta.validation.ConstraintViolationException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import synk.meeteam.domain.recruitment.recruitment_post.entity.RecruitmentPost;
import synk.meeteam.domain.recruitment.recruitment_post.repository.RecruitmentPostRepository;
import synk.meeteam.domain.recruitment.recruitment_post.service.RecruitmentPostService;

@ExtendWith(MockitoExtension.class)
public class RecruitmentPostServiceTest {
    @InjectMocks
    private RecruitmentPostService recruitmentPostService;

    @Mock
    private RecruitmentPostRepository recruitmentPostRepository;


    @Test
    public void 구인글생성_구인글생성성공_정상입력경우() {
        // given
        RecruitmentPost recruitmentPost = RecruitmentPostFixture.createRecruitmentPost("정상입력");
        doReturn(RecruitmentPostFixture.createRecruitmentPost("정상입력")).when(recruitmentPostRepository)
                .save(recruitmentPost);

        // when
        RecruitmentPost savedRecruitmentPost = recruitmentPostService.createRecruitmentPost(recruitmentPost);

        // then
        Assertions.assertThat(savedRecruitmentPost)
                .extracting("title", "content", "scope", "category", "field", "proceedType", "proceedingStart",
                        "proceedingEnd", "deadline")
                .containsExactly(recruitmentPost.getTitle(), recruitmentPost.getContent(), recruitmentPost.getScope(),
                        recruitmentPost.getCategory(), recruitmentPost.getField(), recruitmentPost.getProceedType(),
                        recruitmentPost.getProceedingStart(), recruitmentPost.getProceedingEnd(),
                        recruitmentPost.getDeadline());
    }

    @Test
    public void 구인글생성_예외발생_제목이40자넘는경우() {
        RecruitmentPost recruitmentPost = RecruitmentPostFixture.createRecruitmentPost(TITLE_EXCEED_40);
        doThrow(ConstraintViolationException.class).when(recruitmentPostRepository).save(recruitmentPost);

        // when, then
        Assertions.assertThatThrownBy(() -> {
            recruitmentPostService.createRecruitmentPost(recruitmentPost);
        }).isInstanceOf(ConstraintViolationException.class);
    }
}