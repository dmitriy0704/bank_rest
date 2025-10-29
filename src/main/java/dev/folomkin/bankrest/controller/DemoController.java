package dev.folomkin.bankrest.controller;

import dev.folomkin.bankrest.service.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/demo")
@RequiredArgsConstructor
@Tag(name = "Демонстрация", description = "Для текущего авторизованного пользователя")
public class DemoController {
    private final UserService service;

    @GetMapping("/set-admin")
    @Operation(summary = "Стать Админом")
    public String getAdmin() {
        service.getAdmin();
        return "Теперь вы можете отправлять запросы от имени администратора";
    }


    @GetMapping("/set-user")
    @Operation(summary = "Стать Пользователем")
    public String getUser() {
        service.getUser();
        return "Теперь вы можете отправлять запросы от имени пользователя";
    }

    @GetMapping("/admin")
    @Operation(summary = "Демонстрация доступа только авторизованным пользователям с ролью ADMIN")
    @PreAuthorize("hasRole('ADMIN')")
    public String exampleAdmin() {
        return "Hello, admin!";
    }
}