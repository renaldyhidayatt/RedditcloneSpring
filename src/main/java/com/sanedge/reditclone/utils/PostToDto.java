package com.sanedge.reditclone.utils;

import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.github.marlonlom.utilities.timeago.TimeAgo;
import com.sanedge.reditclone.dto.PostResponse;
import com.sanedge.reditclone.models.Post;
import com.sanedge.reditclone.models.Vote;
import com.sanedge.reditclone.models.VoteType;
import com.sanedge.reditclone.repository.CommentRepository;
import com.sanedge.reditclone.repository.VoteRepository;
import com.sanedge.reditclone.services.AuthService;

import static com.sanedge.reditclone.models.VoteType.UPVOTE;
import static com.sanedge.reditclone.models.VoteType.DOWNVOTE;;

@Component
public class PostToDto {

    private final CommentRepository commentRepository;
    private final AuthService authService;
    private final VoteRepository voteRepository;

    @Autowired
    public PostToDto(CommentRepository commentRepository, AuthService authService, VoteRepository voteRepository) {
        this.commentRepository = commentRepository;
        this.authService = authService;
        this.voteRepository = voteRepository;
    }

    public List<PostResponse> mapPostToDtos(List<Post> _post) {
        return _post.stream()
                .map(e -> new PostResponse(e.getPostId(), e.getPostName(), e.getUrl(), e.getDescription(),
                        e.getUser().getUsername(), e.getSubreddit().getName(), e.getVoteCount(), getCountComment(e),
                        getDuration(e), isPostUpVoted(e), isPostDownVoted(e)))
                .collect(Collectors.toList());
    }

    public Integer getCountComment(Post post) {
        return this.commentRepository.findByPost(post).size();
    }

    String getDuration(Post post) {
        Instant createdDate = post.getCreatedDate();
        Instant now = Instant.now();
        Duration duration = Duration.between(createdDate, now);
        long days = duration.toDays();
        long hours = duration.toHours() % 24;
        long minutes = duration.toMinutes() % 60;
        long seconds = duration.getSeconds() % 60;

        return String.format("%d days, %d hours, %d minutes, %d seconds", days, hours, minutes, seconds);
    }

    boolean isPostUpVoted(Post post) {
        return checkVoteType(post, UPVOTE);
    }

    boolean isPostDownVoted(Post post) {
        return checkVoteType(post, DOWNVOTE);
    }

    private boolean checkVoteType(Post post, VoteType voteType) {
        if (authService.isLoggedIn()) {
            Optional<Vote> voteForPostByUser = voteRepository.findTopByPostAndUserOrderByVoteIdDesc(post,
                    authService.getCurrentUser());
            return voteForPostByUser.filter(vote -> vote.getVoteType().equals(voteType))
                    .isPresent();
        }
        return false;
    }

    public PostResponse mapPostToDto(Post _post) {
        return mapPostToDtos(Arrays.asList(_post)).get(0);
    }
}
