package com.community.file.config;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.io.File;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "upload")
public class StorageConfig {

    private String path = "./uploads/";

    @PostConstruct
    public void init() {
        File uploadDir = new File(path);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }
    }
}
