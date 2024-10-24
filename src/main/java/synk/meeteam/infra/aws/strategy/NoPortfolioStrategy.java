package synk.meeteam.infra.aws.strategy;

import static synk.meeteam.infra.aws.S3FilePath.getFileFullName;
import static synk.meeteam.infra.aws.S3FilePath.getZipFileFullName;

import java.util.UUID;

public class NoPortfolioStrategy implements PortfolioStrategy {

    private String fileName;
    private String zipFileName;

    public NoPortfolioStrategy() {
        this.fileName = UUID.randomUUID().toString();
        this.zipFileName = UUID.randomUUID().toString();
    }

    @Override
    public String getZipFileName() {
        return getZipFileFullName(fileName);
    }

    @Override
    public String getThumbnailFileName(String thumbnailExtension) {
        return getFileFullName(zipFileName, thumbnailExtension);
    }

    @Override
    public long getVersion() {
        return 1;
    }
}
