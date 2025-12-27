package dev.folomkin.bankrest.domain.dto.user;

import dev.folomkin.bankrest.domain.model.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Созданный пользователь")
public class UserResponse {

    @Schema(description = "ID пользователя", example = "")
    private Long id;

    @Schema(description = "Имя пользователя", example = "")
    private String username;

    @Schema(description = "Email пользователя", example = "")
    private String email;

    @Schema(description = "Роль пользователя", example = "")
    private Collection<Role> roles;

    @Schema(description = "Дата создания", example = "")
    private LocalDateTime createdA;
}