package com.gigshift.allocation.shift.search.model;

import java.math.BigDecimal;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Getter
@Setter
@NoArgsConstructor
@Document(indexName = "shifts")
public class ShiftDocument {

    @Id
    private String id; // same as Shift.shiftId

    @Field(type = FieldType.Text)
    private String title;

    @Field(type = FieldType.Keyword)
    private String requiredSkills; // "BARISTA,CLEANER"

    @Field(type = FieldType.Integer)
    private Integer durationHours;

    @Field(type = FieldType.Double)
    private BigDecimal payRate;

    @Field(type = FieldType.Date)
    private Instant createdAt;

    @Field(type = FieldType.Keyword)
    private String status; // OPEN, ASSIGNED, ...

    @Field(type = FieldType.Keyword)
    private String employerId;

    @Field(type = FieldType.Text)
    private String address;

    @Field(type = FieldType.Double)
    private Double positionAvailable;

    @Field(type = FieldType.Date)
    private Instant startTime;

    @Field(type = FieldType.Text)
    private String description;


    // getters and setters...
}
