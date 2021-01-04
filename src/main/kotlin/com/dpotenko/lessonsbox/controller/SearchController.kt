package com.dpotenko.lessonsbox.controller

import com.dpotenko.lessonsbox.dto.SearchResultDto
import com.dpotenko.lessonsbox.service.SearchService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import javax.validation.Valid
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Positive

@RestController
@RequestMapping("/search")
class SearchController(private val searchService: SearchService) {


    @GetMapping
    fun search(@Valid @NotBlank @RequestParam(name = "searchString") searchString: String,
               @Valid @Positive @RequestParam(name = "limit", defaultValue = "10", required = false) limit: Int): List<SearchResultDto> {
        return searchService.search(searchString, limit)
    }
}
