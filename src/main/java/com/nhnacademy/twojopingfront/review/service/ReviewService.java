package com.nhnacademy.twojopingfront.review.service;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhnacademy.twojopingfront.common.error.exception.image.ImageUploadException;
import com.nhnacademy.twojopingfront.review.client.ReviewClient;
import com.nhnacademy.twojopingfront.review.dto.request.*;
import com.nhnacademy.twojopingfront.review.dto.response.ReviewCreateResponseDto;
import com.nhnacademy.twojopingfront.review.dto.response.ReviewModifyResponseDto;
import com.nhnacademy.twojopingfront.review.dto.response.ReviewResponseDto;
import lombok.RequiredArgsConstructor;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.client.RestTemplate;

import org.springframework.web.client.HttpMessageConverterExtractor;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;


@Component
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewClient reviewClient;
    private final RestTemplate restTemplate = new RestTemplate();

    public ReviewCreateResponseDto registerReview(ReviewDetailRequestDto reviewDetailRequestDto, ReviewImageUploadRequestDto reviewImageUploadRequestDto){
        String reviewImageUrl = saveImage(reviewImageUploadRequestDto.reviewImage());
        ReviewImageUrlRequestDto urlRequestDto = new ReviewImageUrlRequestDto(reviewImageUrl);
        return reviewClient.registerReview(new ReviewCreateRequestDto(reviewDetailRequestDto,urlRequestDto));
    }


    public ReviewModifyResponseDto modifyReview(Long reviewId,
                                                 ReviewModifyDetailRequestDto reviewModifyDetailRequestDto,
                                                ReviewImageUploadRequestDto reviewImageUploadRequestDto){
        String reviewImageUrl = saveImage(reviewImageUploadRequestDto.reviewImage());
        ReviewImageUrlRequestDto urlRequestDto = new ReviewImageUrlRequestDto(reviewImageUrl);
        return reviewClient.modifyReview(reviewId,new ReviewModifyRequestDto(reviewModifyDetailRequestDto,urlRequestDto));
    }

    public ReviewResponseDto getReview(Long reviewId) {
        return reviewClient.getReview(reviewId);
    }

    public Page<ReviewResponseDto> getReviewsByBookId(int page,int size,Long bookId) {
        return reviewClient.getReviewsByBookId(page,size,bookId);
    }


    public Page<ReviewResponseDto> getReviewsByCustomerId(int page,int size,Long customerId) {
        return reviewClient.getReviewsByCustomerId(page,size,customerId);
    }

    public String saveImage(MultipartFile image) {
        if (image == null || image.isEmpty()) {
            return null;
        }

        String uploadUrl = null;

        try {
            String folderPath = "review/";
//            if ("detail".equalsIgnoreCase(type)) {
//                folderPath = "detail/";
//            }

            InputStream inputStream = image.getInputStream();
            String fileName = UUID.randomUUID().toString() + ".jpg";
            uploadUrl = "https://api-image.nhncloudservice.com/image/v2.0/appkeys/rUN43QEwj1P6jThk/images?path=/ejoping/book/" + folderPath + fileName;

            RequestCallback requestCallback = request -> {
                request.getHeaders().add("Content-Type", "multipart/form-data");
                request.getHeaders().add("Authorization", "I1XLVufp");
                IOUtils.copy(inputStream, request.getBody());
            };

            String responseBody = restTemplate.execute(uploadUrl, HttpMethod.PUT, requestCallback, response -> {
                InputStream body = response.getBody();
                if (body != null) {
                    return new String(body.readAllBytes(), StandardCharsets.UTF_8);
                }
                return null;
            });

            if (responseBody != null) {
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode rootNode = objectMapper.readTree(responseBody);
                return rootNode.path("file").path("url").asText();
            }

        } catch (IOException e) {
            throw new ImageUploadException();
        }
        return null;
    }
}
