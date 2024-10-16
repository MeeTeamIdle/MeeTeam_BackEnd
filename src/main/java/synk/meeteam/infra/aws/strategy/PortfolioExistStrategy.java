package synk.meeteam.infra.aws.strategy;

import static synk.meeteam.infra.aws.S3FilePath.getFileFullName;

import org.springframework.util.StringUtils;
import synk.meeteam.domain.portfolio.portfolio.entity.Portfolio;

public class PortfolioExistStrategy implements PortfolioStrategy {
    private final Portfolio portfolio;

    public PortfolioExistStrategy(Portfolio portfolio) {
        this.portfolio = portfolio;
    }

    @Override
    public String getZipFileName() {
        return portfolio.getZipFileName();
    }

    @Override
    public String getThumbnailFileName(String thumbnailExtension) {
        return getFileFullName(StringUtils.stripFilenameExtension(portfolio.getMainImageFileName()),
                thumbnailExtension);
    }

    @Override
    public long getVersion() {
        return portfolio.getVersion();
    }
}
