package com.dpotenko.lessonsbox.service

import com.dpotenko.lessonsbox.dto.SearchResultDto
import org.jooq.DSLContext
import org.jooq.Record
import org.springframework.stereotype.Service

@Service
class SearchService(private val dslContext: DSLContext) {

    fun search(queryToSearch: String,
               limit: Int): List<SearchResultDto> {
        return dslContext.fetch("SELECT l.name                                                       as lessonName,\n" +
                "c.name                                                       as courseName,\n" +
                "       ts_rank_cd(l.textsearchable_index_col, query)                  AS rank,\n" +
                "       ts_headline('simple', l.name || ' ' || l.lesson_text, query) as matchedExtract,\n" +
                "       l.order_number                                               as orderNumber,\n" +
                "       l.course_id                                                  as courseId\n" +
                "FROM lesson l\n" +
                "         join course c on l.course_id = c.id,\n" +
                "     websearch_to_tsquery('simple', ?) query\n" +
                "WHERE l.deleted = FALSE\n" +
                "  and c.deleted = FALSE\n" +
                "  and l.textsearchable_index_col @@ query\n" +
                "ORDER BY rank DESC\n" +
                "LIMIT ?", queryToSearch, limit)
                .map { record: Record ->

                    return@map SearchResultDto(
                            record.get("ordernumber", Long::class.java),
                            record.get("courseid", Long::class.java),
                            record.get("matchedextract", String::class.java),
                            record.get("lessonname", String::class.java),
                            record.get("coursename", String::class.java)
                    )
                }
    }
}
