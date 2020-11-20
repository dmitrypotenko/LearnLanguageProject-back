package com.dpotenko.kirillweb.service.sitemap

import com.dpotenko.kirillweb.dto.CourseType
import com.dpotenko.kirillweb.service.CourseService
import com.dpotenko.kirillweb.service.LessonService
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement
import org.springframework.stereotype.Service
import java.time.LocalDate

const val defaultPriority = 0.50
const val sitemapItemTemplate = "https://lessonsbox.com/courses/"

@Service
class SitemapService(private val courseService: CourseService,
                     private val lessonService: LessonService) {

    private val defaultRoutes = listOf(
            UrlDetails("https://lessonsbox.com/", LocalDate.now(), 1.0),
            UrlDetails("https://lessonsbox.com/courses", LocalDate.now(), 0.10),
            UrlDetails("https://lessonsbox.com/courses/create", LocalDate.now(), 0.10),
            UrlDetails("https://lessonsbox.com/welcome", LocalDate.now(), 0.10),
            UrlDetails("https://lessonsbox.com/policy", LocalDate.now(), 0.10),
            UrlDetails("https://lessonsbox.com/teachers", LocalDate.now(), 0.10),
            UrlDetails("https://lessonsbox.com/groups", LocalDate.now(), 0.10)
    )

    fun generate(): UrlsSet {
        val allCourses = courseService.getAllCourses(null).filter { it.type == CourseType.PUBLIC }
        allCourses.forEach { courseDto ->
            courseDto.lessons = lessonService.getLessonsMetaByCourseId(courseDto.id!!)
        }

        val urlDetailsByCourses = allCourses.flatMap { course ->
            var i = 0
            val urlDetails = mutableListOf<UrlDetails>()
            while (i < course.lessons.size) {
                val lesson = course.lessons[i]
                urlDetails.add(UrlDetails(sitemapItemTemplate + course.id + "/" + formatNameToUrlFragment(course.name) + "/steps/" + lesson.order + "/" + formatNameToUrlFragment(lesson.name!!), LocalDate.now(), defaultPriority))
                i++
            }
            return@flatMap urlDetails
        }

        return UrlsSet(defaultRoutes.plus(urlDetailsByCourses))
    }

    fun formatNameToUrlFragment(name: String): String {
        return name.replace(Regex("[.,\\/#!\$%\\^&\\*;:{}=\\-_`~()+]"), "").toLowerCase().replace(Regex(" +"), "-");
    }
}


@JacksonXmlRootElement(localName = "urlset")
data class UrlsSet(@JacksonXmlProperty(localName = "url")
                   @JacksonXmlElementWrapper(useWrapping = false) val urlset: List<UrlDetails>) {
    @JacksonXmlProperty(isAttribute = true, localName = "xsi:schemaLocation")
    private val schemaLocation = "http://www.sitemaps.org/schemas/sitemap/0.9\n" +
            "http://www.sitemaps.org/schemas/sitemap/0.9/sitemap.xsd"

    @JacksonXmlProperty(isAttribute = true, localName = "xmlns:xsi")
    private val xsi = "http://www.w3.org/2001/XMLSchema-instance"

    @JacksonXmlProperty(isAttribute = true, localName = "xmlns")
    private val xmlns = "http://www.sitemaps.org/schemas/sitemap/0.9"
}


data class UrlDetails(
        val loc: String,
        val lastmod: LocalDate,
        val priority: Double
)
