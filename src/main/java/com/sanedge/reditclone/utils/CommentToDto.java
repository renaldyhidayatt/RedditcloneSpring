package com.sanedge.reditclone.utils;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.sanedge.reditclone.dto.CommentsDto;
import com.sanedge.reditclone.models.Comment;

@Service
public class CommentToDto {
    public List<CommentsDto> mapCommentToDtos(List<Comment> _comment) {
        return _comment.stream().map(e -> new CommentsDto(e.getId(), e.getPost().getPostId(), e.getCreatedDate(),
                e.getText(), e.getUser().getUsername())).collect(Collectors.toList());
    }

}
