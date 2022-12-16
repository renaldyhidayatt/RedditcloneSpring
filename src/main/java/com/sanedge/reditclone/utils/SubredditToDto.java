package com.sanedge.reditclone.utils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.sanedge.reditclone.dto.SubredditDto;
import com.sanedge.reditclone.dto.SubredditDtos;
import com.sanedge.reditclone.models.Post;
import com.sanedge.reditclone.models.Subreddit;

@Component
public class SubredditToDto {
    public List<SubredditDtos> mapSubredditToDtos(List<Subreddit> _subreddit) {
        return _subreddit.stream()
                .map(e -> new SubredditDtos(e.getId(), e.getName(), e.getDescription(), getSizePost(e.getPosts())))
                .collect(Collectors.toList());
    }

    public Integer getSizePost(List<Post> numberPosts) {
        return numberPosts.size();
    }

    public List<SubredditDto> mapModelToDto(List<Subreddit> _subreddit) {
        return _subreddit.stream().map(s -> new SubredditDto(s.getId(), s.getName(), s.getDescription()))
                .collect(Collectors.toList());
    }

    public SubredditDto mapSubredditToDto(Subreddit _subreddit) {
        return mapModelToDto(Arrays.asList(_subreddit)).get(0);
    }
}
