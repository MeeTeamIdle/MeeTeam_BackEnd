package synk.meeteam.global.api;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import synk.meeteam.domain.portfolio.portfolio.entity.Portfolio;
import synk.meeteam.domain.portfolio.portfolio.service.PortfolioService;
import synk.meeteam.domain.user.user.entity.User;
import synk.meeteam.infra.aws.service.CloudFrontService;
import synk.meeteam.infra.aws.service.vo.SignedUrlVO;
import synk.meeteam.security.AuthUser;

@RestController
@RequiredArgsConstructor
public class ServerProfileController {

    private final PortfolioService portfolioService;
    private final CloudFrontService cloudFrontService;

    @GetMapping("/profile/signed-url")
    public ResponseEntity<SignedUrlVO> getSignedUrl(@AuthUser User user,
                                                    @RequestParam(name = "file-name") String fileName) {
        String extension = StringUtils.getFilenameExtension(fileName);

        return ResponseEntity.ok()
                .body(cloudFrontService.getProfileSignedUrl(extension, user.getId(), user.getImgVersion()));
    }

    @GetMapping("/portfolio/signed-url")
    public ResponseEntity<List<SignedUrlVO>> getSignedUrl(@AuthUser User user,
                                                          @RequestParam(name = "file-name") String fileName,
                                                          @RequestParam(name = "portfolio", required = false) Long portfolioId
    ) {
        String thumbnailExtension = StringUtils.getFilenameExtension(fileName);
        Portfolio portfolio = portfolioId != null ? portfolioService.getPortfolio(portfolioId) : null;

        return ResponseEntity.ok().body(
                cloudFrontService.getPortfolioSignedUrl(thumbnailExtension, portfolio, user.getId())
        );

    }


}
