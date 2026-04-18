package com.hirehub.frontend.web;

import com.hirehub.frontend.password.PasswordResetService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    public PasswordResetController(PasswordResetService passwordResetService) {
        this.passwordResetService = passwordResetService;
    }

    @GetMapping("/forgot-password")
    public String forgotPasswordPage() {
        return "pages/public/forgot-password";
    }

    @PostMapping("/forgot-password")
    public String forgotPasswordSubmit(@RequestParam("email") String email, RedirectAttributes ra) {
        passwordResetService.requestReset(email);
        ra.addFlashAttribute("forgotPasswordSent", true);
        if (StringUtils.hasText(email)) {
            ra.addFlashAttribute("prefillResetEmail", email.trim());
        }
        return "redirect:/reset-password";
    }

    @GetMapping("/reset-password")
    public String resetPasswordPage(
            @RequestParam(value = "invalid", required = false) String invalid,
            Model model
    ) {
        if ("1".equals(invalid)) {
            model.addAttribute("resetError", "invalid");
        }
        return "pages/public/reset-password";
    }

    @PostMapping("/reset-password")
    public String resetPasswordSubmit(
            @RequestParam("email") String email,
            @RequestParam("code") String code,
            @RequestParam("password") String password,
            @RequestParam("passwordConfirm") String passwordConfirm,
            RedirectAttributes ra
    ) {
        if (!StringUtils.hasText(email)) {
            ra.addFlashAttribute("resetError", "invalid");
            return "redirect:/reset-password?invalid=1";
        }
        if (!StringUtils.hasText(password) || !password.equals(passwordConfirm)) {
            ra.addFlashAttribute("resetError", "mismatch");
            ra.addFlashAttribute("prefillResetEmail", email != null ? email.trim() : "");
            return "redirect:/reset-password";
        }
        try {
            passwordResetService.completeResetWithEmailAndCode(email, code, password);
            ra.addFlashAttribute("passwordResetOk", true);
            return "redirect:/login";
        } catch (IllegalArgumentException ex) {
            ra.addFlashAttribute("resetError", "invalid");
            ra.addFlashAttribute("prefillResetEmail", email != null ? email.trim() : "");
            return "redirect:/reset-password";
        }
    }
}
