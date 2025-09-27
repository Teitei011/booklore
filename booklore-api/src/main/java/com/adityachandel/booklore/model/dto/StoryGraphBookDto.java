package com.adityachandel.booklore.model.dto;

import com.opencsv.bean.CsvBindByName;
import lombok.Data;

@Data
public class StoryGraphBookDto {

    @CsvBindByName(column = "Title")
    private String title;

    @CsvBindByName(column = "Authors")
    private String authors;

    @CsvBindByName(column = "Rating")
    private Double rating;

    @CsvBindByName(column = "Review")
    private String review;

    @CsvBindByName(column = "Date Read")
    private String dateRead;

    @CsvBindByName(column = "Read Status")
    private String readStatus;

}