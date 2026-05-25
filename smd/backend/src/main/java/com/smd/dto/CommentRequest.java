package com.smd.dto;

import com.smd.entity.CommentErrorType;
import jakarta.validation.constraints.NotBlank;

public class CommentRequest {

    @NotBlank
    private String content;

    private CommentErrorType errorType;

    public CommentRequest() {}

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public CommentErrorType getErrorType() { return errorType; }
    public void setErrorType(CommentErrorType errorType) { this.errorType = errorType; }
}
