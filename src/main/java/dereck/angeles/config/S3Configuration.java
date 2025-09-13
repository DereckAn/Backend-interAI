package dereck.angeles.config;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.net.URI;

@ApplicationScoped
public class S3Configuration {

    @ConfigProperty(name = "quarkus.s3.endpoint-override")
    String endpointOverride;

    @ConfigProperty(name = "quarkus.s3.aws.credentials.static-provider.access-key-id")
    String accessKey;

    @ConfigProperty(name = "quarkus.s3.aws.credentials.static-provider.secret-access-key")
    String secretKey;

    @ConfigProperty(name = "quarkus.s3.aws.region", defaultValue = "us-east-1")
    String region;

    @Produces
    @ApplicationScoped
    public S3Client s3Client() {
        return S3Client.builder()
                .endpointOverride(URI.create(endpointOverride))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey)))
                .region(Region.of(region))
                .forcePathStyle(true) // Important for MinIO
                .build();
    }
}