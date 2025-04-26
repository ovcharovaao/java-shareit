package ru.practicum.shareit.item;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CommentDto {
    private Long id;

    @NotBlank(message = "Комментарий не может быть пустым")
    private String text;

    private String authorName;
}
