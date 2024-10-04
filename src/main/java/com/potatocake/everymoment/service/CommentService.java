package com.potatocake.everymoment.service;

import com.potatocake.everymoment.dto.request.CommentRequest;
import com.potatocake.everymoment.dto.response.CommentFriendResponse;
import com.potatocake.everymoment.dto.response.CommentResponse;
import com.potatocake.everymoment.dto.response.CommentsResponse;
import com.potatocake.everymoment.entity.Comment;
import com.potatocake.everymoment.entity.Diary;
import com.potatocake.everymoment.entity.Member;
import com.potatocake.everymoment.exception.ErrorCode;
import com.potatocake.everymoment.exception.GlobalException;
import com.potatocake.everymoment.repository.CommentRepository;
import com.potatocake.everymoment.repository.DiaryRepository;
import com.potatocake.everymoment.repository.MemberRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional
@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final DiaryRepository diaryRepository;
    private final MemberRepository memberRepository;

    // 댓글 목록 조회
    public CommentsResponse getComments(Long diaryId, int key, int size) {
        Pageable pageable = PageRequest.of(key, size);
        Page<Comment> commentPage = commentRepository.findAllByDiaryId(diaryId, pageable);

        List<CommentResponse> commentResponses = commentPage.getContent().stream()
                .map(this::convertToCommentResponseDTO)
                .collect(Collectors.toList());

        Integer nextKey = commentPage.hasNext() ? key + 1 : null;

        return CommentsResponse.builder()
                .comments(commentResponses)
                .next(nextKey)
                .build();
    }

    // 댓글 작성
    public void createComment(Long memberId, Long diaryId, CommentRequest commentRequest) {
        Member currentMember = memberRepository.findById(memberId)
                .orElseThrow(() -> new GlobalException(ErrorCode.MEMBER_NOT_FOUND));

        Diary diary = diaryRepository.findById(diaryId)
                .orElseThrow(() -> new GlobalException(ErrorCode.DIARY_NOT_FOUND));

        Comment comment = Comment.builder()
                .content(commentRequest.getContent())
                .memberId(currentMember)
                .diaryId(diary)
                .build();

        commentRepository.save(comment);
    }

    // 친구 프로필 DTO 변환
    private CommentFriendResponse convertToCommentFriendResponseDTO(Member member){
        return CommentFriendResponse.builder()
                .id(member.getId())
                .nickname(member.getNickname())
                .profileImageUrl(member.getProfileImageUrl())
                .build();
    }

    // 댓글 DTO 변환
    private CommentResponse convertToCommentResponseDTO(Comment comment){
        return CommentResponse.builder()
                .id(comment.getId())
                .commentFriendResponse(convertToCommentFriendResponseDTO(comment.getMemberId()))
                .content(comment.getContent())
                .createdAt(comment.getCreateAt())
                .build();
    }
}

