package ar.buildrr.feedback.config;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Mismo MinIO que ya usa el stack SGO (documentos-service), bucket propio
 * ("buildrr-feedback"). Privado — los adjuntos se sirven con URL prefirmada,
 * nunca con lectura pública (ver AdjuntoServiceImpl).
 */
@Configuration
@Slf4j
public class MinioConfig {

  @Bean
  public MinioClient minioClient(
      @Value("${minio.endpoint}") String endpoint,
      @Value("${minio.access-key}") String accessKey,
      @Value("${minio.secret-key}") String secretKey) {
    return MinioClient.builder()
        .endpoint(endpoint)
        .credentials(accessKey, secretKey)
        .build();
  }

  @Bean
  public CommandLineRunner minioBucketInitializer(
      MinioClient minioClient,
      @Value("${minio.bucket}") String bucket) {
    return args -> {
      try {
        boolean existe = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
        if (!existe) {
          minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
          log.info("Bucket MinIO '{}' creado", bucket);
        }
      } catch (Exception ex) {
        log.warn("No se pudo inicializar el bucket MinIO '{}' (el servicio arranca igual): {}",
            bucket, ex.getMessage());
      }
    };
  }
}
