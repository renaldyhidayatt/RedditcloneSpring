package com.sanedge.reditclone.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sanedge.reditclone.dto.SubredditDto;
import com.sanedge.reditclone.dto.SubredditDtos;
import com.sanedge.reditclone.exception.SpringRedditException;
import com.sanedge.reditclone.models.Subreddit;
import com.sanedge.reditclone.repository.SubredditRepository;
import com.sanedge.reditclone.utils.SubredditToDto;

@Service
public class SubredditService {
    private final SubredditRepository subredditRepository;
    private final SubredditToDto subredditToDto;
    private final AuthService authService;

    @Autowired
    public SubredditService(SubredditRepository subredditRepository, SubredditToDto subredditToDto,
            AuthService authService) {
        this.subredditRepository = subredditRepository;
        this.subredditToDto = subredditToDto;
        this.authService = authService;
    }

    @Transactional
    public SubredditDto create(SubredditDto subredditDto) {
        Subreddit _subreddit = new Subreddit();

        _subreddit.setName(subredditDto.getName());
        _subreddit.setDescription(subredditDto.getDescription());
        _subreddit.setUser(authService.getCurrentUser());

        this.subredditRepository.save(_subreddit);

        return this.subredditToDto.mapSubredditToDto(_subreddit);
    }

    public List<SubredditDtos> getAll() {
        List<Subreddit> _subreddits = this.subredditRepository.findAll();

        return this.subredditToDto.mapSubredditToDtos(_subreddits);
    }

    public SubredditDto getSubReddit(long id) {
        Subreddit subreddit = subredditRepository.findById(id)
                .orElseThrow(() -> new SpringRedditException("No subreddit found with ID - " + id));
        return this.subredditToDto.mapSubredditToDto(subreddit);
    }

}
