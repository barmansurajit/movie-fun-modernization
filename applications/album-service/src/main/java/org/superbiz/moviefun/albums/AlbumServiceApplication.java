package org.superbiz.moviefun.albums;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.StorageOptions;
import org.apache.tika.io.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.Bean;
import org.superbiz.cloudfoundry.ServiceCredentials;
import org.superbiz.moviefun.blobstore.BlobStore;
import org.superbiz.moviefun.blobstore.CloudStorageStore;

import java.io.IOException;

@EnableEurekaClient
@SpringBootApplication
public class AlbumServiceApplication {
    public static void main(String... args) {
        SpringApplication.run(AlbumServiceApplication.class, args);
    }

    @Bean
    ServiceCredentials serviceCredentials(@Value("${vcap.services}") String vcapServices) {
        return new ServiceCredentials(vcapServices);
    }

    @Bean
    public BlobStore blobStore(ServiceCredentials serviceCredentials) {
        try {
            GoogleCredentials credentials = GoogleCredentials.fromStream(IOUtils.toInputStream(serviceCredentials.getCredentials("photo-storage", "user-provided")));
            return new CloudStorageStore(StorageOptions.newBuilder().setCredentials(credentials).build().getService(), "moviefun-1");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;

    }
}
