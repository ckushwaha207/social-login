/*
 * Copyright 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.social.login.signup;

import javax.inject.Inject;
import javax.validation.Valid;

import org.springframework.social.connect.*;
import org.springframework.social.connect.web.ProviderSignInUtils;
import com.social.login.account.Account;
import com.social.login.account.AccountRepository;
import com.social.login.account.UsernameAlreadyInUseException;
import com.social.login.message.Message;
import com.social.login.message.MessageType;
import com.social.login.signin.SignInUtils;
import org.springframework.social.facebook.api.Facebook;
import org.springframework.social.facebook.api.User;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.context.request.WebRequest;

@Controller
public class SignupController {

    private final AccountRepository accountRepository;
    private final ProviderSignInUtils providerSignInUtils;

    @Inject
    public SignupController(AccountRepository accountRepository,
                            ConnectionFactoryLocator connectionFactoryLocator,
                            UsersConnectionRepository connectionRepository) {
        this.accountRepository = accountRepository;
        this.providerSignInUtils = new ProviderSignInUtils(connectionFactoryLocator, connectionRepository);
    }

    @RequestMapping(value = "/signup", method = RequestMethod.GET)
    public SignupForm signupForm(WebRequest request) {
        Connection<?> connection = providerSignInUtils.getConnectionFromSession(request);
        if (connection != null) {
            request.setAttribute("message", new Message(MessageType.INFO, "Your " + StringUtils.capitalize(connection.getKey().getProviderId()) + " account is not associated with a Spring Social Showcase account. If you're new, please sign up."), WebRequest.SCOPE_REQUEST);

            Object conObj = connection.getApi();
            UserProfile userProfile = null;
            if (conObj instanceof Facebook) {
                final Facebook facebook = (Facebook) conObj;
                final String[] fields = {"id", "about", "age_range", "birthday", "context", "cover", "currency", "devices", "education", "email",
                        "favorite_athletes", "favorite_teams", "first_name", "gender", "hometown", "inspirational_people", "installed", "install_type",
                        "is_verified", "languages", "last_name", "link", "locale", "location", "meeting_for", "middle_name", "name", "name_format",
                        "political", "quotes", "payment_pricepoints", "relationship_status", "religion", "security_settings", "significant_other",
                        "sports", "test_group", "timezone", "third_party_id", "updated_time", "verified", "video_upload_limits", "viewer_can_send_gift",
                        "website", "work"};

                final User profile = facebook.fetchObject("me", User.class, fields);
                userProfile = new UserProfileBuilder().setName(profile.getName()).setFirstName(profile.getFirstName()).setLastName(profile.getLastName()).
                        setEmail(profile.getEmail()).build();
            } else {
                userProfile = connection.fetchUserProfile();
            }
            return SignupForm.fromProviderUser(userProfile);
        } else {
            return new SignupForm();
        }
    }

    @RequestMapping(value = "/signup", method = RequestMethod.POST)
    public String signup(@Valid SignupForm form, BindingResult formBinding, WebRequest request) {
        if (formBinding.hasErrors()) {
            return null;
        }
        Account account = createAccount(form, formBinding);
        if (account != null) {
            SignInUtils.signin(account.getUsername());
            providerSignInUtils.doPostSignUp(account.getUsername(), request);
            return "redirect:/";
        }
        return null;
    }

    // internal helpers

    private Account createAccount(SignupForm form, BindingResult formBinding) {
        try {
            Account account = new Account(form.getUsername(), form.getPassword(), form.getFirstName(), form.getLastName());
            accountRepository.createAccount(account);
            return account;
        } catch (UsernameAlreadyInUseException e) {
            formBinding.rejectValue("username", "user.duplicateUsername", "already in use");
            return null;
        }
    }

}
