package com.adityachandel.booklore.controller;

import com.adityachandel.booklore.model.dto.StoryGraphBookDto;
import com.adityachandel.booklore.service.StoryGraphService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/storygraph")
@AllArgsConstructor
public class StoryGraphController {

    private final StoryGraphService storyGraphService;

    @PostMapping("/import")
    public List<StoryGraphBookDto> importFromCsv(@RequestParam("file") MultipartFile file) {
        return storyGraphService.importFromCsv(file);
    }
}