package ru.practicum.shareit.item.mapper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class CommentMapperTest {
    @Autowired
    private CommentMapper commentMapper;

    @Test
    @DisplayName("Маппинг Comment в CommentDto должен работать корректно")
    void toCommentDto_shouldMapCorrectly() {
        User author = new User(1L, "AuthorName", "author@example.com");
        Item item = new Item();

        Comment comment = Comment.builder()
                .id(1L)
                .text("Текст комментария")
                .author(author)
                .item(item)
                .created(LocalDateTime.now())
                .build();

        CommentDto commentDto = commentMapper.toCommentDto(comment);

        assertThat(commentDto).isNotNull();
        assertThat(commentDto.getId()).isEqualTo(comment.getId());
        assertThat(commentDto.getText()).isEqualTo(comment.getText());
        assertThat(commentDto.getAuthorName()).isEqualTo(comment.getAuthor().getName());
    }
}
