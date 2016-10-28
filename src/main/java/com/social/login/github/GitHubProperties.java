package com.social.login.github;

import org.springframework.boot.autoconfigure.social.SocialProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Created by chandan.kushwaha on 28-10-2016.
 */
@ConfigurationProperties("spring.social.github")
public class GitHubProperties extends SocialProperties {
    public GitHubProperties() {

    }
}
