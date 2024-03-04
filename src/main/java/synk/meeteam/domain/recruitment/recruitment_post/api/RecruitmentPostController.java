package synk.meeteam.domain.recruitment.recruitment_post.api;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import synk.meeteam.domain.recruitment.recruitment_post.dto.request.CreateRecruitmentPostRequestDto;
import synk.meeteam.domain.recruitment.recruitment_post.dto.request.applyRecruitmentRequestDto;
import synk.meeteam.domain.recruitment.recruitment_post.dto.response.CreateRecruitmentPostResponseDto;
import synk.meeteam.domain.recruitment.recruitment_post.dto.response.GetRecruitmentPostResponseDto;
import synk.meeteam.domain.user.user.entity.User;
import synk.meeteam.security.AuthUser;

@RestController
@RequiredArgsConstructor
@RequestMapping("/recruitment/post")
public class RecruitmentPostController implements RecruitmentPostApi {

    @PostMapping
    @Override
    public ResponseEntity<CreateRecruitmentPostResponseDto> createRecruitmentPost(
            @Valid CreateRecruitmentPostRequestDto requestDto) {
        return null;
    }

    @GetMapping
    @Override
    public ResponseEntity<GetRecruitmentPostResponseDto> getRecruitmentPost(@Valid @RequestParam Long id,
                                                                            @AuthUser User user) {
        return null;
    }

    @PostMapping("/apply")
    @Override
    public ResponseEntity<Void> applyRecruitment(applyRecruitmentRequestDto requestDto, User user) {

        return ResponseEntity.ok().build();
    }
}
