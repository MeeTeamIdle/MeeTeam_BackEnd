package synk.meeteam.infra.aws.service;

import static synk.meeteam.infra.aws.S3FilePath.USER;
import static synk.meeteam.infra.aws.S3FilePath.getFileFullName;
import static synk.meeteam.infra.aws.S3FilePath.getPortfolioPath;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.cloudfront.CloudFrontUtilities;
import software.amazon.awssdk.services.cloudfront.model.CannedSignerRequest;
import software.amazon.awssdk.services.cloudfront.url.SignedUrl;
import synk.meeteam.domain.portfolio.portfolio.entity.Portfolio;
import synk.meeteam.global.entity.ServiceType;
import synk.meeteam.global.util.Encryption;
import synk.meeteam.infra.aws.exception.AwsException;
import synk.meeteam.infra.aws.exception.AwsExceptionType;
import synk.meeteam.infra.aws.service.vo.SignedUrlVO;
import synk.meeteam.infra.aws.strategy.NoPortfolioStrategy;
import synk.meeteam.infra.aws.strategy.PortfolioExistStrategy;
import synk.meeteam.infra.aws.strategy.PortfolioStrategy;

@Component
@RequiredArgsConstructor
@Slf4j
public class CloudFrontService {
    private static final Long PRE_SIGNED_URL_EXPIRE_SECONDS = 30L;
    @Value("${aws-property.distribution-domain}")
    private String distributionDomain;
    @Value("${aws-property.private-key-file-path}")
    private String privateKeyFilePath;
    @Value("${aws-property.key-pair-id}")
    private String keyPairId;

    public String getSignedUrl(String path, String fileName, long version) {
        if (fileName == null || fileName.isEmpty()) {
            return null;
        }
        try {
            String resourcePath = getEncodedResourcePath(path, fileName);
            String cloudFrontUrl = String.format("https://%s/%s?v=%d", distributionDomain, resourcePath, version);
            Instant expirationTime = Instant.now().plus(PRE_SIGNED_URL_EXPIRE_SECONDS, ChronoUnit.SECONDS);
            Path keyPath = Paths.get(privateKeyFilePath);
            CloudFrontUtilities cloudFrontUtilities = CloudFrontUtilities.create();
            CannedSignerRequest cannedSignerRequest = CannedSignerRequest.builder()
                    .resourceUrl(cloudFrontUrl)
                    .privateKey(keyPath)
                    .keyPairId(keyPairId)
                    .expirationDate(expirationTime)
                    .build();

            SignedUrl signedUrl = cloudFrontUtilities.getSignedUrlWithCannedPolicy(cannedSignerRequest);
            return signedUrl.url();
        } catch (AwsException e) {
            throw new AwsException(AwsExceptionType.FAIL_GET_URL);
        } catch (Exception e) {
            throw new AwsException(AwsExceptionType.CAN_NOT_FOUND_KEY);
        }
    }

    public SignedUrlVO getProfileSignedUrl(String extension, Long userId, long version) {
        String filename = getFileFullName(Encryption.encryptLong(userId), extension);
        String url = getSignedUrl(USER, filename, version);
        return SignedUrlVO.of(ServiceType.PROFILE, filename, url);
    }

    public List<SignedUrlVO> getPortfolioSignedUrl(String thumbnailExtension, Portfolio portfolio, Long userId) {
        PortfolioStrategy strategy =
                (portfolio != null) ? new PortfolioExistStrategy(portfolio) : new NoPortfolioStrategy();

        String portfolioPath = getPortfolioPath(Encryption.encryptLong(userId));
        String zipUrl = getSignedUrl(portfolioPath, strategy.getZipFileName(), strategy.getVersion());
        String thumbNailUrl = getSignedUrl(portfolioPath, strategy.getThumbnailFileName(thumbnailExtension),
                strategy.getVersion());

        return List.of(
                SignedUrlVO.of(ServiceType.PORTFOLIOS, strategy.getZipFileName(), zipUrl),
                SignedUrlVO.of(ServiceType.THUMBNAIL_PORTFOLIO, strategy.getThumbnailFileName(thumbnailExtension),
                        thumbNailUrl)
        );
    }

    private String getEncodedResourcePath(String path, String fileName) throws UnsupportedEncodingException {
        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8);
        return path + encodedFileName;
    }
}