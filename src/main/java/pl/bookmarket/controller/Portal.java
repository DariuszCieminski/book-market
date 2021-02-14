package pl.bookmarket.controller;

import java.util.Collections;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import pl.bookmarket.dao.RoleDao;
import pl.bookmarket.dao.UserDao;
import pl.bookmarket.model.User;
import pl.bookmarket.service.MailService;
import pl.bookmarket.util.ChangeEmailModel;
import pl.bookmarket.util.ChangePasswordModel;
import pl.bookmarket.util.MailType;
import pl.bookmarket.util.PasswordGenerator;
import pl.bookmarket.util.ResetPasswordModel;
import pl.bookmarket.validation.ValidationGroups;

@Controller
public class Portal {

    private final UserDao userDao;
    private final RoleDao roleDao;
    private final MailService mail;
    private final MessageSource messageSource;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public Portal(UserDao userDao, RoleDao roleDao, MailService mail, MessageSource messageSource,
                  PasswordEncoder passwordEncoder) {
        this.userDao = userDao;
        this.roleDao = roleDao;
        this.mail = mail;
        this.messageSource = messageSource;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/register")
    public String register(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/register")
    public String register(
        @Validated(ValidationGroups.CreateUser.class) @ModelAttribute(name = "user") User user,
        BindingResult result, Model model, HttpServletResponse response) {

        if (result.hasErrors()) {
            response.setStatus(422);
            return "register";
        }

        String password = PasswordGenerator.generate();
        user.setPassword(passwordEncoder.encode(password));
        user.setRoles(Collections.singleton(roleDao.findRoleByName("USER")));
        userDao.save(user);

        mail.sendMessage(user.getEmail(), MailType.ACCOUNT_CREATED, Collections.singletonMap("userPassword", password));

        model.addAttribute("user", user);
        response.setStatus(HttpServletResponse.SC_CREATED);

        return "register-success";
    }

    @GetMapping("/login")
    public String login(@ModelAttribute(name = "loginError") String loginError, Model model) {
        model.addAttribute("user", new User());

        if (!loginError.equals("")) {
            String errorMessageCode = "";

            switch (loginError) {
                case "Bad credentials":
                    errorMessageCode = "invalid.login.password";
                    break;
                case "User is disabled":
                    errorMessageCode = "user.disabled";
                    break;
            }

            model.addAttribute("error", messageSource.getMessage(errorMessageCode, null, LocaleContextHolder.getLocale()));
        }

        return "login";
    }

    @GetMapping("/admin")
    public String admin() {
        return "admin";
    }

    @GetMapping("/books")
    public String myBooks() {
        return "books";
    }

    @GetMapping("/offers")
    public String myOffers() {
        return "offers";
    }

    @GetMapping("/market")
    public String market() {
        return "market";
    }

    @GetMapping("/messages")
    public String messages() {
        return "messages";
    }

    @GetMapping("/switchuser")
    public String switchUser() {
        return "switchuser";
    }

    @GetMapping("/changepassword")
    public String changePassword(Model model) {
        model.addAttribute("pass", new ChangePasswordModel());

        return "changepassword";
    }

    @PostMapping("/changepassword")
    public String changePassword(@Valid @ModelAttribute("pass") ChangePasswordModel password, BindingResult result,
                                 Model model, HttpServletResponse response) {
        if (!result.hasErrors()) {
            User user = userDao.findUserByLogin(SecurityContextHolder.getContext().getAuthentication().getName());
            user.setPassword(passwordEncoder.encode(password.getNewPassword()));
            userDao.save(user);
            model.addAttribute("success", true);
        } else {
            response.setStatus(422);
        }

        return "changepassword";
    }

    @GetMapping("/changeemail")
    public String changeEmail(Model model) {
        model.addAttribute("mail", new ChangeEmailModel());

        return "changeemail";
    }

    @PostMapping("/changeemail")
    public String changeEmail(@Valid @ModelAttribute("mail") ChangeEmailModel email, BindingResult result, Model model,
                              HttpServletResponse response) {
        if (!result.hasErrors()) {
            User user = userDao.findUserByLogin(SecurityContextHolder.getContext().getAuthentication().getName());
            user.setEmail(email.getNewEmail());
            userDao.save(user);
            model.addAttribute("success", true);
        } else {
            response.setStatus(422);
        }

        return "changeemail";
    }

    @GetMapping("/resetpassword")
    public String resetPassword(Model model) {
        model.addAttribute("resetPassword", new ResetPasswordModel());

        return "resetpassword";
    }

    @PostMapping("/resetpassword")
    public String resetPassword(@Valid @ModelAttribute("resetPassword") ResetPasswordModel resetPassword,
                                BindingResult result, Model model, HttpServletResponse response) {
        if (!result.hasErrors()) {
            User user = userDao.findUserByLogin(resetPassword.getLogin());
            String password = PasswordGenerator.generate();
            user.setPassword(passwordEncoder.encode(password));
            userDao.save(user);

            mail.sendMessage(user.getEmail(), MailType.PASSWORD_RESET, Collections.singletonMap("userPassword", password));
            model.addAttribute("success", true);
        } else {
            response.setStatus(422);
        }

        return "resetpassword";
    }

    @PostMapping("/setlanguage")
    @ResponseBody
    public void changeLanguage(@RequestParam("lang") String language, HttpServletResponse response) {
        if (language == null || language.length() < 2) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        } else {
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
        }
    }
}