package synk.meeteam.domain.user.user.api;

import static synk.meeteam.infra.aws.S3FilePath.USER;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import synk.meeteam.domain.portfolio.portfolio.dto.response.GetUserPortfolioResponseDto;
import synk.meeteam.domain.portfolio.portfolio.service.PortfolioService;
import synk.meeteam.domain.user.user.dto.request.UpdateProfileRequestDto;
import synk.meeteam.domain.user.user.dto.response.CheckDuplicateNicknameResponseDto;
import synk.meeteam.domain.user.user.dto.response.GetProfileImageResponseDto;
import synk.meeteam.domain.user.user.dto.response.GetProfileResponseDto;
import synk.meeteam.domain.user.user.entity.User;
import synk.meeteam.domain.user.user.service.ProfileFacade;
import synk.meeteam.domain.user.user.service.UserManagementService;
import synk.meeteam.domain.user.user.service.UserService;
import synk.meeteam.global.util.Encryption;
import synk.meeteam.infra.aws.service.CloudFrontService;
import synk.meeteam.security.AuthUser;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController implements UserApi {

    private final UserService userService;
    private final PortfolioService portfolioService;
    private final CloudFrontService cloudFrontService;

    private final ProfileFacade profileFacade;
    private final UserManagementService userManagementService;

    @Override
    @PutMapping("/profile")
    public ResponseEntity<String> editProfile(@AuthUser User user,
                                              @RequestBody @Valid UpdateProfileRequestDto requestDto) {

        profileFacade.editProfile(user, requestDto);

        return ResponseEntity.ok().body(user.getEncryptUserId());
    }

    @Override
    @GetMapping("/profile/{userId}")
    public ResponseEntity<GetProfileResponseDto> getProfile(@AuthUser User user,
                                                            @PathVariable("userId") String userId) {
        return ResponseEntity.ok(profileFacade.readProfile(user, userId));
    }

    @GetMapping("/encrypt/{userId}")
    public ResponseEntity<String> getEncryptedId(@PathVariable("userId") Long userId) {
        return ResponseEntity.ok(Encryption.encryptLong(userId));
    }


    @Override
    @GetMapping("/search/check-duplicate")
    public ResponseEntity<CheckDuplicateNicknameResponseDto> checkDuplicateNickname(
            @RequestParam("nickname") String nickname) {
        boolean available = userService.checkAvailableNickname(nickname);

        return ResponseEntity.ok(CheckDuplicateNicknameResponseDto.of(available));
    }

    @Override
    @GetMapping("/portfolios")
    public ResponseEntity<GetUserPortfolioResponseDto> getUserPortfolio(
            @AuthUser User user, @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "size", defaultValue = "12") int size) {
        return ResponseEntity.ok(portfolioService.getSliceMyAllPortfolio(page, size, user));
    }

    @Override
    @GetMapping("/profile/image")
    public ResponseEntity<GetProfileImageResponseDto> getProfileImage(@AuthUser User user) {
        String profileImgUrl = cloudFrontService.getSignedUrl(USER, user.getProfileImgFileName(), user.getImgVersion());
        return ResponseEntity.ok(GetProfileImageResponseDto.of(profileImgUrl));
    }

    @Override
    @DeleteMapping
    public ResponseEntity<Void> deleteUser(@AuthUser User user) {
        userManagementService.deleteUser(user);

        return ResponseEntity.ok(null);
    }
}