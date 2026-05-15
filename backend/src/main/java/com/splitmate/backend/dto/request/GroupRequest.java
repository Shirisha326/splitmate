package com.splitmate.backend.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

public class GroupRequest {

    @Data
    public static class Create {
        @NotBlank(message = "Group name is required")
        @Size(min = 2, max = 100)
        private String name;

        @Size(max = 500)
        private String description;

        private String category = "OTHER";

        private List<String> memberEmails;
    }

    @Data
    public static class Update {
        @Size(min = 2, max = 100)
        private String name;

        @Size(max = 500)
        private String description;

        private String category;
    }

    @Data
    public static class AddMember {
        @NotBlank
        @Email
        private String email;
    }
}
