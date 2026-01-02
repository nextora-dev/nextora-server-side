package lk.iit.nextora.config.swagger;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "application.swagger")
public class SwaggerProperties {

    private String title;
    private String description;
    private String version;
    private Contact contact;
    private License license;
    private List<Server> servers;

    @Getter
    @Setter
    public static class Contact {
        private String name;
        private String url;
    }

    @Getter
    @Setter
    public static class License {
        private String name;
        private String url;
    }

    @Getter
    @Setter
    public static class Server {
        private String url;
        private String description;
    }
}
