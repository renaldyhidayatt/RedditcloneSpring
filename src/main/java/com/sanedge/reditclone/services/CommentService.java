package com.sanedge.reditclone.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.sanedge.reditclone.dto.CommentsDto;
import com.sanedge.reditclone.exception.PostNotFoundException;
import com.sanedge.reditclone.models.Comment;
import com.sanedge.reditclone.models.NotificationEmail;
import com.sanedge.reditclone.models.Post;
import com.sanedge.reditclone.models.User;
import com.sanedge.reditclone.repository.CommentRepository;
import com.sanedge.reditclone.repository.PostRepository;
import com.sanedge.reditclone.repository.UserRepository;
import com.sanedge.reditclone.utils.CommentToDto;

@Service
public class CommentService {
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final AuthService authService;
    private final CommentToDto commentToDto;
    private final CommentRepository commentRepository;
    private final MailContentBuilder mailContentBuilder;
    private final MailService mailService;

    @Autowired
    public CommentService(PostRepository postRepository, UserRepository userRepository, AuthService authService,
            CommentToDto commentToDto, CommentRepository commentRepository, MailContentBuilder mailContentBuilder,
            MailService mailService) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.authService = authService;
        this.commentToDto = commentToDto;
        this.commentRepository = commentRepository;
        this.mailContentBuilder = mailContentBuilder;
        this.mailService = mailService;
    }

    public void save(CommentsDto commentsDto) {
        Post post = postRepository.findById(commentsDto.getPostId())
                .orElseThrow(() -> new PostNotFoundException(commentsDto.getPostId().toString()));
        Comment comment = new Comment();
        comment.setPost(post);
        comment.setCreatedDate(commentsDto.getCreatedDate());
        comment.setText(commentsDto.getText());
        comment.setUser(authService.getCurrentUser());

        this.commentRepository.save(comment);

        String message = mailContentBuilder
                .build(post.getUser().getUsername() + " posted a comment on your post.");
        this.sendCommentNotification(message, post.getUser());
    }

    private void sendCommentNotification(String message, User user) {
        this.mailService.sendMail(
                new NotificationEmail(user.getUsername() + " Commented on your post", user.getEmail(), message));
    }

    public List<CommentsDto> getAllCommentsForPost(Long postId) {
        Post post = this.postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException(postId.toString()));
        List<Comment> comment = this.commentRepository.findByPost(post);

        return this.commentToDto.mapCommentToDtos(comment);
    }

    public List<CommentsDto> getAllCommentsForUser(String userName) {
        User user = userRepository.findByUsername(userName)
                .orElseThrow(() -> new UsernameNotFoundException(userName));
        List<Comment> comments = this.commentRepository.findAllByUser(user);

        return this.commentToDto.mapCommentToDtos(comments);
    }
}
