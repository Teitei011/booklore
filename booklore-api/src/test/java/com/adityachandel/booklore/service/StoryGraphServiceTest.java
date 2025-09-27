package com.adityachandel.booklore.service;

import com.adityachandel.booklore.model.dto.StoryGraphBookDto;
import com.adityachandel.booklore.model.entity.BookEntity;
import com.adityachandel.booklore.model.entity.BookLoreUserEntity;
import com.adityachandel.booklore.model.entity.BookMetadataEntity;
import com.adityachandel.booklore.model.entity.UserBookProgressEntity;
import com.adityachandel.booklore.model.enums.ReadStatus;
import com.adityachandel.booklore.repository.BookMetadataRepository;
import com.adityachandel.booklore.repository.BookRepository;
import com.adityachandel.booklore.repository.UserBookProgressRepository;
import com.adityachandel.booklore.service.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class StoryGraphServiceTest {

    @Mock
    private BookRepository bookRepository;
    @Mock
    private BookMetadataRepository bookMetadataRepository;
    @Mock
    private UserBookProgressRepository userBookProgressRepository;
    @Mock
    private UserService userService;

    @InjectMocks
    private StoryGraphService storyGraphService;

    private BookLoreUserEntity testUser;

    @BeforeEach
    void setUp() {
        testUser = new BookLoreUserEntity();
        testUser.setId(1L);
        when(userService.getAuthenticatedUserEntity()).thenReturn(testUser);
    }

    @Test
    void testImportFromCsv_NewBook() throws IOException {
        String csvContent = "Title,Authors,Rating,Review,Date Read,Read Status\n" +
                "New Book,New Author,4.5,Great book!,2023-01-01,read";
        MockMultipartFile file = new MockMultipartFile("file", "test.csv", "text/csv", csvContent.getBytes(StandardCharsets.UTF_8));

        when(bookRepository.findByMetadataTitleAndAuthorNames(any(), any())).thenReturn(Collections.emptyList());

        storyGraphService.importFromCsv(file);

        verify(bookRepository, times(1)).findByMetadataTitleAndAuthorNames("New Book", Collections.singletonList("New Author"));
        verify(bookMetadataRepository, never()).save(any());
        verify(userBookProgressRepository, never()).save(any());
    }

    @Test
    void testImportFromCsv_ExistingBook() throws IOException {
        String csvContent = "Title,Authors,Rating,Review,Date Read,Read Status\n" +
                "Existing Book,Existing Author,4.0,Good book,2023-02-01,read";
        MockMultipartFile file = new MockMultipartFile("file", "test.csv", "text/csv", csvContent.getBytes(StandardCharsets.UTF_8));

        BookEntity existingBook = new BookEntity();
        existingBook.setId(1L);
        BookMetadataEntity metadata = new BookMetadataEntity();
        metadata.setBookId(1L);
        existingBook.setMetadata(metadata);

        when(bookRepository.findByMetadataTitleAndAuthorNames("Existing Book", Collections.singletonList("Existing Author")))
                .thenReturn(Collections.singletonList(existingBook));
        when(userBookProgressRepository.findByUserIdAndBookId(testUser.getId(), existingBook.getId())).thenReturn(Optional.empty());

        storyGraphService.importFromCsv(file);

        verify(bookMetadataRepository, times(1)).save(any(BookMetadataEntity.class));
        verify(userBookProgressRepository, times(1)).save(any(UserBookProgressEntity.class));
    }
}