package com.ead.authuser.dtos;

import com.ead.authuser.enums.CourseLevel;
import com.ead.authuser.enums.CourseStatus;
import lombok.Data;

import java.util.UUID;

@Data
public class CourseDTO {

    private final UUID courseId;
    private final String name;
    private final String description;
    private final String imageUrl;
    private final CourseStatus courseStatus;
    private final UUID userInstructor;
    private final CourseLevel courseLevel;

}
