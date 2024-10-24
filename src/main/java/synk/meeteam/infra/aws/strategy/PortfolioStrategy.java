package synk.meeteam.infra.aws.strategy;

public interface PortfolioStrategy {
    String getZipFileName();

    String getThumbnailFileName(String thumbnailExtension);

    long getVersion();
}
