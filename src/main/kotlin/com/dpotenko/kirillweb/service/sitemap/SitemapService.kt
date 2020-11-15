package com.dpotenko.kirillweb.service.sitemap

import com.dpotenko.kirillweb.dto.CourseType
import com.dpotenko.kirillweb.service.CourseService
import com.dpotenko.kirillweb.service.LessonService
import com.dpotenko.kirillweb.service.TestService
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement
import org.springframework.stereotype.Service
import java.time.LocalDate

const val defaultPriority = 0.51
const val sitemapItemTemplate = "https://lessonsbox.com/courses/"

@Service
class SitemapService(private val courseService: CourseService,
                     private val lessonService: LessonService,
                     private val testService: TestService) {

    private val defaultRoutes = listOf(
            UrlDetails("https://lessonsbox.com/", LocalDate.now(), 1.0),
            UrlDetails("https://lessonsbox.com/courses", LocalDate.now(), 0.80),
            UrlDetails("https://lessonsbox.com/courses/create", LocalDate.now(), 0.80),
            UrlDetails("https://lessonsbox.com/welcome", LocalDate.now(), 0.80),
            UrlDetails("https://lessonsbox.com/policy", LocalDate.now(), 0.80),
            UrlDetails("https://lessonsbox.com/teachers", LocalDate.now(), 0.80),
            UrlDetails("https://lessonsbox.com/groups", LocalDate.now(), 0.80)
    )

    fun generate(): UrlsSet {
        val allCourses = courseService.getAllCourses(null).filter { it.type == CourseType.PUBLIC }
        allCourses.forEach { courseDto ->
            courseDto.lessons = lessonService.getLessonsByCourseId(courseDto.id!!)
            courseDto.tests = testService.getTestsByCourseId(courseDto.id!!)
        }

        val coursesSitemapInfo = allCourses.map { courseDto -> CourseSitemapInfo(courseDto.id!!, courseDto.lessons.size + courseDto.tests.size) }

        val urlDetailsByCourses = coursesSitemapInfo.flatMap { coursesSitemapInfo ->
            var i = 0;
            val urlDetails = mutableListOf<UrlDetails>()
            while (i < coursesSitemapInfo.steps) {
                urlDetails.add(UrlDetails(sitemapItemTemplate + coursesSitemapInfo.id + "/steps/" + i, LocalDate.now(), defaultPriority))
                i++;
            }
            return@flatMap urlDetails
        }

        return UrlsSet(defaultRoutes.plus(urlDetailsByCourses))
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

private data class CourseSitemapInfo(val id: Long,
                                     val steps: Int)
