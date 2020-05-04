package com.dpotenko.kirillweb.service

import com.dpotenko.kirillweb.dto.SearchResultDto
import org.jooq.DSLContext
import org.jooq.Record
import org.springframework.stereotype.Service

@Service
class SearchService(private val dslContext: DSLContext) {

    fun search(queryToSearch: String,
               limit: Int): List<SearchResultDto> {
        return dslContext.fetch("SELECT l.name as lessonName, ts_rank_cd(textsearchable_index_col, query) AS rank, ts_headline('simple', l.name || ' ' || l.lesson_text, query) as matchedExtract,\n" +
                "       l.order_number as orderNumber, l.course_id as courseId\n" +
                "FROM lesson l, websearch_to_tsquery('simple', ?) query\n" +
                "WHERE textsearchable_index_col @@ query\n" +
                "ORDER BY rank DESC\n" +
                "LIMIT ?", queryToSearch, limit)
                .map { record: Record ->

                    return@map SearchResultDto(
                            record.get("ordernumber", Long::class.java),
                            record.get("courseid", Long::class.java),
                            record.get("matchedextract", String::class.java),
                            record.get("lessonname", String::class.java)
                    )
                }
    }
}
