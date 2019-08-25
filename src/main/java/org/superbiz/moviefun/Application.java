package org.superbiz.moviefun;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.StorageOptions;
import org.apache.tika.io.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.superbiz.moviefun.blobstore.BlobStore;
import org.superbiz.moviefun.blobstore.CloudStorageStore;

import java.io.IOException;

@SpringBootApplication
public class Application {

    public static void main(String... args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public ServletRegistrationBean actionServletRegistration(ActionServlet actionServlet) {
        return new ServletRegistrationBean(actionServlet, "/moviefun/*");
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
