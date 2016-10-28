package com.social.login.github;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.social.SocialAutoConfigurerAdapter;
import org.springframework.boot.autoconfigure.social.SocialWebAutoConfiguration;
import org.springframework.boot.autoconfigure.web.WebMvcAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.social.config.annotation.EnableSocial;
import org.springframework.social.config.annotation.SocialConfigurerAdapter;
import org.springframework.social.connect.Connection;
import org.springframework.social.connect.ConnectionFactory;
import org.springframework.social.connect.ConnectionRepository;
import org.springframework.social.connect.web.GenericConnectionStatusView;
import org.springframework.social.github.api.GitHub;
import org.springframework.social.github.connect.GitHubConnectionFactory;

/**
 * Created by chandan.kushwaha on 28-10-2016.
 */
@Configuration
@ConditionalOnClass({SocialConfigurerAdapter.class, GitHubConnectionFactory.class})
@ConditionalOnProperty(
        prefix = "spring.social.github",
        name = {"app-id"}
)
@AutoConfigureBefore(SocialWebAutoConfiguration.class)
@AutoConfigureAfter(WebMvcAutoConfiguration.class)
public class GitHubAutoConfiguration {
    public GitHubAutoConfiguration() {

    }

    @Configuration
    @EnableSocial
    @EnableConfigurationProperties({GitHubProperties.class})
    @ConditionalOnWebApplication
    protected static class GithubConfigurerAdapter extends SocialAutoConfigurerAdapter {
        private final GitHubProperties properties;

        protected GithubConfigurerAdapter(GitHubProperties properties) {
            this.properties = properties;
        }

        @Bean
        @ConditionalOnMissingBean({GitHub.class})
        @Scope(
                scopeName = "request",
                proxyMode = ScopedProxyMode.INTERFACES
        )
        public GitHub gitHub(ConnectionRepository repository) {
            Connection connection = repository.findPrimaryConnection(GitHub.class);
            return connection != null ? (GitHub) connection.getApi() : null;
        }

        @Bean(
                name = {"connect/githubConnect", "connect/githubConnected"}
        )
        @ConditionalOnProperty(
                prefix = "spring.social",
                name = {"auto-connection-views"}
        )
        public GenericConnectionStatusView gitHubConnectView() {
            return new GenericConnectionStatusView("github", "GitHub");
        }

        @Override
        protected ConnectionFactory<?> createConnectionFactory() {
            return new GitHubConnectionFactory(this.properties.getAppId(), this.properties.getAppSecret());
        }
    }
}
