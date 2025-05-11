package ru.practicum.shareit.item.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class CommentDtoTest {

    @Test
    @DisplayName("Создание CommentDto через конструктор")
    void createCommentDto_withConstructor() {
        LocalDateTime now = LocalDateTime.now();

        CommentDto commentDto = new CommentDto(1L, "Отличная вещь!", "Иван", now);

        assertThat(commentDto.getId()).isEqualTo(1L);
        assertThat(commentDto.getText()).isEqualTo("Отличная вещь!");
        assertThat(commentDto.getAuthorName()).isEqualTo("Иван");
        assertThat(commentDto.getCreated()).isEqualTo(now);
    }

    @Test
    @DisplayName("Создание CommentDto через билдер")
    void createCommentDto_withBuilder() {
        LocalDateTime now = LocalDateTime.now();

        CommentDto commentDto = CommentDto.builder()
                .id(2L)
                .text("Очень удобная дрель")
                .authorName("Мария")
                .created(now)
                .build();

        assertThat(commentDto.getId()).isEqualTo(2L);
        assertThat(commentDto.getText()).isEqualTo("Очень удобная дрель");
        assertThat(commentDto.getAuthorName()).isEqualTo("Мария");
        assertThat(commentDto.getCreated()).isEqualTo(now);
    }

    @Test
    @DisplayName("Геттеры и сеттеры работают корректно")
    void gettersAndSetters_shouldWorkCorrectly() {
        CommentDto commentDto = new CommentDto();
        LocalDateTime now = LocalDateTime.now();

        commentDto.setId(3L);
        commentDto.setText("Хорошее качество");
        commentDto.setAuthorName("Петр");
        commentDto.setCreated(now);

        assertThat(commentDto.getId()).isEqualTo(3L);
        assertThat(commentDto.getText()).isEqualTo("Хорошее качество");
        assertThat(commentDto.getAuthorName()).isEqualTo("Петр");
        assertThat(commentDto.getCreated()).isEqualTo(now);
    }
}
