package com.sanedge.reditclone.services;

import java.time.Instant;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sanedge.reditclone.dto.PostRequest;
import com.sanedge.reditclone.dto.PostResponse;
import com.sanedge.reditclone.exception.PostNotFoundException;
import com.sanedge.reditclone.exception.SubredditNotFoundException;
import com.sanedge.reditclone.models.Post;
import com.sanedge.reditclone.models.Subreddit;
import com.sanedge.reditclone.models.User;
import com.sanedge.reditclone.repository.PostRepository;
import com.sanedge.reditclone.repository.SubredditRepository;
import com.sanedge.reditclone.repository.UserRepository;
import com.sanedge.reditclone.utils.PostToDto;

@Service
public class PostService {
    private final PostToDto postToDto;
    private final PostRepository postRepository;
    private final SubredditRepository subredditRepository;
    private final AuthService authService;
    private final UserRepository userRepository;

    @Autowired
    public PostService(PostToDto postToDto, PostRepository postRepository, SubredditRepository subredditRepository,
            AuthService authService, UserRepository userRepository) {
        this.postToDto = postToDto;
        this.postRepository = postRepository;
        this.subredditRepository = subredditRepository;
        this.authService = authService;
        this.userRepository = userRepository;
    }

    public void create(PostRequest postRequest) {
        Subreddit subreddit = this.subredditRepository.findByName(postRequest.getSubredditName())
                .orElseThrow(() -> new SubredditNotFoundException(postRequest.getSubredditName()));
        Post post = new Post();
        post.setSubreddit(subreddit);
        post.setPostName(postRequest.getPostName());
        post.setUrl(postRequest.getUrl());
        post.setUser(authService.getCurrentUser());
        post.setCreatedDate(Instant.now());
        post.setDescription(postRequest.getDescription());

        this.postRepository.save(post);

    }

    @Transactional(readOnly = true)
    public PostResponse getPost(Long id) {
        Post post = this.postRepository.findById(id).orElseThrow(() -> new PostNotFoundException(id.toString()));
        return this.postToDto.mapPostToDto(post);
    }

    @Transactional(readOnly = true)
    public List<PostResponse> getAllPosts() {
        List<Post> _posts = this.postRepository.findAll();

        return this.postToDto.mapPostToDtos(_posts);
    }

    @Transactional(readOnly = true)
    public List<PostResponse> getPostsBySubreddit(Long subredditId) {
        Subreddit subreddit = subredditRepository.findById(subredditId)
                .orElseThrow(() -> new SubredditNotFoundException(subredditId.toString()));
        List<Post> posts = postRepository.findAllBySubreddit(subreddit);
        return this.postToDto.mapPostToDtos(posts);
    }

    @Transactional(readOnly = true)
    public List<PostResponse> getPostsByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(username));
        List<Post> findByUser = this.postRepository.findByUser(user);
        return this.postToDto.mapPostToDtos(findByUser);
    }

}
