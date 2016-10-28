package com.social.login.github;

import org.springframework.social.connect.Connection;
import org.springframework.social.connect.ConnectionRepository;
import org.springframework.social.facebook.api.Facebook;
import org.springframework.social.github.api.GitHub;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.inject.Inject;

/**
 * Created by chandan.kushwaha on 28-10-2016.
 */
@Controller
public class GitHubProfileController {
    @Inject
    private ConnectionRepository connectionRepository;

    @RequestMapping(value = "/github", method = RequestMethod.GET)
    public String home(Model model) {
        Connection<GitHub> connection = connectionRepository.findPrimaryConnection(GitHub.class);
        if (connection == null) {
            return "redirect:/connect/github";
        }
        model.addAttribute("profile", connection.getApi().userOperations().getUserProfile());
        return "github/profile";
    }
}
