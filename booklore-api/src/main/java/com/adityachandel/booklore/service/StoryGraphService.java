package com.adityachandel.booklore.service;

import com.adityachandel.booklore.model.dto.StoryGraphBookDto;
import com.adityachandel.booklore.model.entity.*;
import com.adityachandel.booklore.model.enums.ReadStatus;
import com.adityachandel.booklore.repository.AuthorRepository;
import com.adityachandel.booklore.repository.BookMetadataRepository;
import com.adityachandel.booklore.repository.BookRepository;
import com.adityachandel.booklore.repository.UserBookProgressRepository;
import com.adityachandel.booklore.service.user.UserService;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class StoryGraphService {

    private final BookRepository bookRepository;
    private final BookMetadataRepository bookMetadataRepository;
    private final AuthorRepository authorRepository;
    private final UserBookProgressRepository userBookProgressRepository;
    private final UserService userService;

    @Transactional
    public List<StoryGraphBookDto> importFromCsv(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        List<StoryGraphBookDto> storyGraphBooks = parseCsv(file);
        BookLoreUserEntity currentUser = userService.getAuthenticatedUserEntity();

        for (StoryGraphBookDto storyGraphBook : storyGraphBooks) {
            List<String> authorNames = Arrays.stream(storyGraphBook.getAuthors().split(","))
                    .map(String::trim)
                    .collect(Collectors.toList());

            List<BookEntity> foundBooks = bookRepository.findByMetadataTitleAndAuthorNames(storyGraphBook.getTitle(), authorNames);

            if (foundBooks.isEmpty()) {
                log.warn("Book not found in library: '{}' by {}", storyGraphBook.getTitle(), storyGraphBook.getAuthors());
                continue;
            }

            for (BookEntity book : foundBooks) {
                updateBookWithStoryGraphData(book, storyGraphBook, currentUser);
            }
        }
        return storyGraphBooks;
    }

    private void updateBookWithStoryGraphData(BookEntity book, StoryGraphBookDto storyGraphBook, BookLoreUserEntity user) {
        BookMetadataEntity metadata = book.getMetadata();
        if (metadata == null) {
            log.warn("Book with id {} has no metadata.", book.getId());
            return;
        }

        boolean metadataUpdated = false;

        // Update rating
        if (storyGraphBook.getRating() != null && storyGraphBook.getRating() > 0) {
            metadata.setPersonalRating(storyGraphBook.getRating());
            metadataUpdated = true;
        }

        // Update review
        if (storyGraphBook.getReview() != null && !storyGraphBook.getReview().isEmpty()) {
            BookReviewEntity review = new BookReviewEntity();
            review.setBookMetadata(metadata);
            review.setBody(storyGraphBook.getReview());
            review.setRating(storyGraphBook.getRating() != null ? storyGraphBook.getRating().floatValue() : null);
            metadata.getReviews().add(review);
            metadataUpdated = true;
        }

        if (metadataUpdated) {
            bookMetadataRepository.save(metadata);
        }

        // Update read status and date read
        if (storyGraphBook.getReadStatus() != null && !storyGraphBook.getReadStatus().isEmpty()) {
            Optional<UserBookProgressEntity> progressOptional = userBookProgressRepository.findByUserIdAndBookId(user.getId(), book.getId());
            UserBookProgressEntity progress = progressOptional.orElseGet(() -> {
                UserBookProgressEntity newProgress = new UserBookProgressEntity();
                newProgress.setBook(book);
                newProgress.setUser(user);
                return newProgress;
            });

            progress.setReadStatus(mapStoryGraphStatusToReadStatus(storyGraphBook.getReadStatus()));
            if (storyGraphBook.getDateRead() != null && !storyGraphBook.getDateRead().isEmpty()) {
                try {
                    LocalDate date = LocalDate.parse(storyGraphBook.getDateRead(), DateTimeFormatter.ISO_LOCAL_DATE);
                    progress.setDateFinished(date.atStartOfDay().toInstant(ZoneOffset.UTC));
                } catch (Exception e) {
                    log.warn("Could not parse date: {}", storyGraphBook.getDateRead());
                }
            }
            userBookProgressRepository.save(progress);
        }
    }

    private ReadStatus mapStoryGraphStatusToReadStatus(String status) {
        if (status == null) {
            return null;
        }
        switch (status.toLowerCase()) {
            case "read":
                return ReadStatus.READ;
            case "currently-reading":
                return ReadStatus.READING;
            case "to-read":
                return ReadStatus.UNREAD;
            case "did-not-finish":
                return ReadStatus.ABANDONED;
            default:
                return null;
        }
    }

    private List<StoryGraphBookDto> parseCsv(MultipartFile file) {
        try (Reader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            CsvToBean<StoryGraphBookDto> csvToBean = new CsvToBeanBuilder<StoryGraphBookDto>(reader)
                    .withType(StoryGraphBookDto.class)
                    .withIgnoreLeadingWhiteSpace(true)
                    .build();
            return csvToBean.parse();
        } catch (IOException e) {
            log.error("Error while parsing StoryGraph CSV file", e);
            throw new RuntimeException("Error while parsing StoryGraph CSV file", e);
        }
    }
}