package synk.meeteam.domain.recruitment.recruitment_post.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import synk.meeteam.domain.common.course.entity.Course;
import synk.meeteam.domain.common.course.entity.Professor;
import synk.meeteam.domain.common.course.service.CourseService;
import synk.meeteam.domain.common.course.service.ProfessorService;
import synk.meeteam.domain.common.field.entity.Field;
import synk.meeteam.domain.common.field.service.FieldService;
import synk.meeteam.domain.common.role.entity.Role;
import synk.meeteam.domain.common.role.service.RoleService;
import synk.meeteam.domain.common.skill.entity.Skill;
import synk.meeteam.domain.common.skill.service.SkillService;
import synk.meeteam.domain.common.tag.dto.TagDto;
import synk.meeteam.domain.common.tag.entity.TagType;
import synk.meeteam.domain.common.university.entity.University;
import synk.meeteam.domain.recruitment.bookmark.service.BookmarkService;
import synk.meeteam.domain.recruitment.recruitment_applicant.entity.RecruitStatus;
import synk.meeteam.domain.recruitment.recruitment_applicant.entity.RecruitmentApplicant;
import synk.meeteam.domain.recruitment.recruitment_applicant.service.RecruitmentApplicantService;
import synk.meeteam.domain.recruitment.recruitment_comment.entity.RecruitmentComment;
import synk.meeteam.domain.recruitment.recruitment_comment.service.RecruitmentCommentService;
import synk.meeteam.domain.recruitment.recruitment_post.dto.RecruitmentPostMapper;
import synk.meeteam.domain.recruitment.recruitment_post.dto.SearchCondition;
import synk.meeteam.domain.recruitment.recruitment_post.dto.request.ApplyRecruitmentRequestDto;
import synk.meeteam.domain.recruitment.recruitment_post.dto.request.CreateCommentRequestDto;
import synk.meeteam.domain.recruitment.recruitment_post.dto.request.CreateRecruitmentPostRequestDto;
import synk.meeteam.domain.recruitment.recruitment_post.dto.request.ModifyCommentRequestDto;
import synk.meeteam.domain.recruitment.recruitment_post.dto.response.CreateRecruitmentPostResponseDto;
import synk.meeteam.domain.recruitment.recruitment_post.dto.response.GetApplyInfoResponseDto;
import synk.meeteam.domain.recruitment.recruitment_post.dto.response.GetCommentResponseDto;
import synk.meeteam.domain.recruitment.recruitment_post.dto.response.GetRecruitmentPostResponseDto;
import synk.meeteam.domain.recruitment.recruitment_post.dto.response.PaginationSearchPostResponseDto;
import synk.meeteam.domain.recruitment.recruitment_post.entity.RecruitmentPost;
import synk.meeteam.domain.recruitment.recruitment_post.facade.RecruitmentPostFacade;
import synk.meeteam.domain.recruitment.recruitment_post.service.RecruitmentPostService;
import synk.meeteam.domain.recruitment.recruitment_role.dto.AvailableRecruitmentRoleDto;
import synk.meeteam.domain.recruitment.recruitment_role.entity.RecruitmentRole;
import synk.meeteam.domain.recruitment.recruitment_role.service.RecruitmentRoleService;
import synk.meeteam.domain.recruitment.recruitment_role_skill.entity.RecruitmentRoleSkill;
import synk.meeteam.domain.recruitment.recruitment_tag.entity.RecruitmentTag;
import synk.meeteam.domain.recruitment.recruitment_tag.service.RecruitmentTagService;
import synk.meeteam.domain.user.user.entity.User;
import synk.meeteam.domain.user.user.service.UserService;
import synk.meeteam.global.entity.Category;
import synk.meeteam.global.entity.Scope;
import synk.meeteam.infra.aws.S3FilePath;
import synk.meeteam.infra.aws.service.CloudFrontService;
import synk.meeteam.security.AuthUser;


@RestController
@RequiredArgsConstructor
@RequestMapping("/recruitment/postings")
public class RecruitmentPostController implements RecruitmentPostApi {

    private final Long DEVELOP_ID = 1L;

    private final RecruitmentPostFacade recruitmentPostFacade;

    private final RecruitmentPostService recruitmentPostService;
    private final RecruitmentRoleService recruitmentRoleService;
    private final RecruitmentTagService recruitmentTagService;
    private final RecruitmentCommentService recruitmentCommentService;
    private final RecruitmentApplicantService recruitmentApplicantService;
    private final BookmarkService bookmarkService;
    private final RoleService roleService;
    private final FieldService fieldService;
    private final SkillService skillService;
    private final UserService userService;
    private final CourseService courseService;
    private final ProfessorService professorService;

    private final CloudFrontService cloudFrontService;

    private final RecruitmentPostMapper recruitmentPostMapper;

    @PostMapping
    @Override
    public ResponseEntity<CreateRecruitmentPostResponseDto> createRecruitmentPost(
            @Valid @RequestBody CreateRecruitmentPostRequestDto requestDto, @AuthUser User user) {

        Field field = fieldService.findById(DEVELOP_ID);

        Course course = null;
        Professor professor = null;
        if (requestDto.courseTag().isCourse()) {
            course = courseService.getOrCreateCourse(requestDto.courseTag().courseTagName(),
                    user.getUniversity());
            professor = professorService.getOrCreateProfessor(requestDto.courseTag().courseProfessor(),
                    user.getUniversity());
        }

        RecruitmentPost recruitmentPost = recruitmentPostMapper.toRecruitmentEntity(requestDto, field, course,
                professor, requestDto.courseTag().isCourse());

        List<RecruitmentRoleSkill> recruitmentRoleSkills = new ArrayList<>();
        List<RecruitmentRole> recruitmentRoles = getRecruitmentRoles(requestDto, recruitmentPost,
                recruitmentRoleSkills);

        List<RecruitmentTag> recruitmentTags = getRecruitmentTags(requestDto, recruitmentPost);

        return ResponseEntity.status(HttpStatus.CREATED).body(CreateRecruitmentPostResponseDto.from(
                recruitmentPostFacade.createRecruitmentPost(recruitmentPost, recruitmentRoles, recruitmentRoleSkills,
                        recruitmentTags, course, professor)));
    }

    @GetMapping("/{id}")
    @Override
    public ResponseEntity<GetRecruitmentPostResponseDto> getRecruitmentPost(
            @PathVariable("id") Long postId, @AuthUser User user) {

        RecruitmentPost recruitmentPost = recruitmentPostService.getRecruitmentPost(postId);
        boolean isBookmarked = false;
        if (user != null) {
            isBookmarked = bookmarkService.isBookmarked(recruitmentPost, user);
        }

        User writer = userService.findById(recruitmentPost.getCreatedBy());
        String writerImgUrl = cloudFrontService.getSignedUrl(S3FilePath.USER, writer.getProfileImgFileName(),
                writer.getImgVersion());

        List<RecruitmentRole> recruitmentRoles = recruitmentRoleService.findByRecruitmentPostId(postId);

        List<TagDto> recruitmentTags = recruitmentTagService.findByRecruitmentPostId(postId);

        List<GetCommentResponseDto> recruitmentCommentDtos = recruitmentCommentService.getRecruitmentComments(
                recruitmentPost);

        boolean isApplied = recruitmentApplicantService.isAppliedUser(recruitmentPost, user);

        return ResponseEntity.ok()
                .body(GetRecruitmentPostResponseDto.from(recruitmentPost, isApplied, isBookmarked, recruitmentRoles,
                        writer, user,
                        writerImgUrl,
                        recruitmentTags,
                        recruitmentCommentDtos, recruitmentPost.getCourse(), recruitmentPost.getProfessor()));
    }

    @GetMapping("/{id}/apply-info")
    @Override
    public ResponseEntity<GetApplyInfoResponseDto> getApplyInfo(@PathVariable("id") Long postId,
                                                                @AuthUser User user) {
        List<AvailableRecruitmentRoleDto> availableRecruitmentRoleDtos = recruitmentRoleService.findAvailableRecruitmentRole(
                postId);

        return ResponseEntity.ok()
                .body(new GetApplyInfoResponseDto(user.getName(), user.getGpa(),
                        user.getUniversity().getName(), user.getDepartment().getName(),
                        user.getAdmissionYear(),
                        user.getUniversityEmail(), availableRecruitmentRoleDtos));
    }


    @PostMapping("/{id}/apply")
    @Override
    public ResponseEntity<Void> applyRecruitment(@PathVariable("id") Long postId,
                                                 @Valid @RequestBody ApplyRecruitmentRequestDto requestDto,
                                                 @AuthUser User user) {
        RecruitmentRole recruitmentRole = recruitmentRoleService.findAppliableRecruitmentRole(requestDto.applyRoleId());

        RecruitmentApplicant recruitmentApplicant = recruitmentPostMapper.toRecruitmentApplicantEntity(
                recruitmentRole.getRecruitmentPost(), recruitmentRole.getRole(), user, requestDto.message(),
                RecruitStatus.NONE);
        RecruitmentPost recruitmentPost = recruitmentPostService.getRecruitmentPost(postId);
        User writer = userService.findById(recruitmentPost.getCreatedBy());
        recruitmentPostFacade.applyRecruitment(recruitmentRole, recruitmentApplicant, writer);

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}/apply")
    @Override
    public ResponseEntity<Void> cancelApplyRecruitment(@PathVariable("id") Long postId, @AuthUser User user) {
        recruitmentPostFacade.cancelApplyRecruitment(postId, user);

        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/close")
    @Override
    public ResponseEntity<Void> closeRecruitment(@PathVariable("id") Long postId, @AuthUser User user) {
        recruitmentPostService.closeRecruitment(postId, user.getId());
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}")
    @Override
    public ResponseEntity<Void> modifyRecruitmentPost(@Valid @RequestBody CreateRecruitmentPostRequestDto requestDto,
                                                      @PathVariable("id") Long postId, @AuthUser User user) {

        Field field = fieldService.findById(DEVELOP_ID);

        Course course = null;
        Professor professor = null;
        if (requestDto.courseTag().isCourse()) {
            course = courseService.getOrCreateCourse(requestDto.courseTag().courseTagName(),
                    user.getUniversity());
            professor = professorService.getOrCreateProfessor(requestDto.courseTag().courseProfessor(),
                    user.getUniversity());
        }

        RecruitmentPost srcRecruitmentPost = recruitmentPostMapper.toRecruitmentEntity(requestDto, field, course,
                professor, requestDto.courseTag()
                        .isCourse());
        RecruitmentPost dstRecruitmentPost = recruitmentPostService.getRecruitmentPost(postId);

        List<RecruitmentRoleSkill> recruitmentRoleSkills = new ArrayList<>();
        List<RecruitmentRole> recruitmentRoles = getRecruitmentRoles(requestDto, dstRecruitmentPost,
                recruitmentRoleSkills);

        List<RecruitmentTag> recruitmentTags = getRecruitmentTags(requestDto, dstRecruitmentPost);

        recruitmentPostFacade.modifyRecruitmentPost(dstRecruitmentPost, srcRecruitmentPost, recruitmentRoles,
                recruitmentRoleSkills, recruitmentTags, user.getId());

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    @Override
    public ResponseEntity<Void> deleteRecruitmentPost(@PathVariable("id") Long postId, @AuthUser User user) {
        recruitmentPostService.deleteRecruitmentPost(postId, user.getId());

        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/bookmark")
    @Override
    public ResponseEntity<Void> bookmarkRecruitmentPost(@PathVariable("id") Long postId,
                                                        @AuthUser User user) {
        recruitmentPostFacade.bookmarkRecruitmentPost(postId, user);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}/bookmark")
    @Override
    public ResponseEntity<Void> cancelBookmarkRecruitmentPost(@PathVariable("id") Long postId,
                                                              @AuthUser User user) {
        recruitmentPostFacade.cancelBookmarkRecruitmentPost(postId, user);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/search")
    @Override
    public ResponseEntity<PaginationSearchPostResponseDto> searchRecruitmentPost(
            @RequestParam(value = "size", required = false, defaultValue = "24") @Valid @Min(1) int size,
            @RequestParam(value = "page", required = false, defaultValue = "1") @Valid @Min(1) int page,
            @RequestParam(value = "field", required = false) Long fieldId,
            @RequestParam(value = "scope", required = false) Integer scopeOrdinal,
            @RequestParam(value = "category", required = false) Integer categoryOrdinal,
            @RequestParam(value = "skill", required = false) List<Long> skillIds,
            @RequestParam(value = "role", required = false) List<Long> roleIds,
            @RequestParam(value = "tag", required = false) List<Long> tagIds,
            @RequestParam(value = "course", required = false) Long courseId,
            @RequestParam(value = "professor", required = false) Long professorId,
            @RequestParam(value = "keyword", required = false) String keyword,
            @AuthUser User user) {
        SearchCondition condition = filterCondition(fieldId, scopeOrdinal, categoryOrdinal, skillIds, roleIds, tagIds,
                courseId, professorId);
        PaginationSearchPostResponseDto result = recruitmentPostService.searchWithPageRecruitmentPost(
                size, page, condition, keyword, user);

        return ResponseEntity.ok().body(result);
    }

    @PostMapping("/{id}/comment")
    @Override
    public ResponseEntity<Void> registerComment(@PathVariable("id") Long postId,
                                                @Valid @RequestBody CreateCommentRequestDto requestDto,
                                                @AuthUser User user) {
        RecruitmentPost recruitmentPost = recruitmentPostService.getRecruitmentPost(postId);
        RecruitmentComment recruitmentComment = recruitmentPostMapper.toRecruitmentCommentEntity(recruitmentPost,
                requestDto);

        recruitmentCommentService.registerRecruitmentComment(recruitmentComment);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/{id}/comment/{comment-id}")
    @Override
    public ResponseEntity<Void> deleteComment(@PathVariable("id") Long postId,
                                              @PathVariable("comment-id") Long commentId,
                                              @AuthUser User user) {

        RecruitmentPost recruitmentPost = recruitmentPostService.getRecruitmentPost(postId);
        recruitmentCommentService.deleteComment(commentId, user.getId(), recruitmentPost);

        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/comment")
    @Override
    public ResponseEntity<Void> modifyComment(@PathVariable("id") Long postId,
                                              @Valid @RequestBody ModifyCommentRequestDto requestDto,
                                              @AuthUser User user) {

        recruitmentCommentService.modifyRecruitmentComment(user.getId(), requestDto.commentId(), requestDto.content());

        return ResponseEntity.ok().build();
    }


    ////////////////  변환 로직들  ////////////////
    private List<RecruitmentRole> getRecruitmentRoles(CreateRecruitmentPostRequestDto requestDto,
                                                      RecruitmentPost recruitmentPost,
                                                      List<RecruitmentRoleSkill> recruitmentRoleSkills) {
        return requestDto.recruitmentRoles().stream().map(recruitmentRole -> {
            Role role = roleService.findById(recruitmentRole.roleId());
            RecruitmentRole recruitmentRoleEntity = recruitmentPostMapper.toRecruitmentRoleEntity(recruitmentPost, role,
                    recruitmentRole.count());

            recruitmentRole.skillIds().stream().forEach(skillId -> {
                Skill skill = skillService.findBySkillId(skillId);
                recruitmentRoleSkills.add(recruitmentPostMapper.toRecruitmentSkillEntity(recruitmentRoleEntity, skill));
            });
            return recruitmentRoleEntity;
        }).toList();
    }

    private List<RecruitmentTag> getRecruitmentTags(CreateRecruitmentPostRequestDto requestDto,
                                                    RecruitmentPost recruitmentPost) {
        List<RecruitmentTag> recruitmentTags = new ArrayList<>();
        recruitmentTags.addAll(requestDto.tags().stream()
                .map(tagName -> recruitmentPostMapper.toTagEntity(tagName, TagType.MEETEAM))
                .map(tag -> recruitmentPostMapper.toRecruitmentTagEntity(recruitmentPost, tag))
                .toList());

        return recruitmentTags;
    }

    private Course getCourse(String courseName, University university) {
        return Course.builder()
                .name(courseName)
                .university(university)
                .build();
    }

    private Professor getProfessor(String professorName, University university) {
        return Professor.builder()
                .name(professorName)
                .university(university)
                .build();
    }

    private SearchCondition filterCondition(Long fieldId, Integer scopeOrdinal, Integer categoryOrdinal,
                                            List<Long> skillIds, List<Long> roleIds, List<Long> tagIds, Long courseId,
                                            Long professorId) {
        Scope scope = null;
        Category category = null;
        if (scopeOrdinal != null && scopeOrdinal >= 1 && scopeOrdinal <= Scope.values().length) {
            scope = Scope.values()[scopeOrdinal - 1];
        }
        if (categoryOrdinal != null && categoryOrdinal >= 1 && categoryOrdinal <= Category.values().length) {
            category = Category.values()[categoryOrdinal - 1];
        }
        return new SearchCondition(fieldId, scope, category, skillIds, tagIds, roleIds, courseId, professorId);
    }
}
