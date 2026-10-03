package com.ratiotech.underwriting.api.controllers.responses

import org.springframework.data.domain.Page

/**
 * Generic paginated response wrapper.
 *
 * @param T the type of the items in the page
 * @property content the items in the current page
 * @property page the pagination metadata for the current page
 */
data class PagedResponse<T>(
  val content: List<T>,
  val page: PageMetadata,
) {
  companion object {
    /**
     * Builds a [PagedResponse] from a Spring Data [Page].
     *
     * @param page the Spring Data page to convert
     * @return the paginated response containing the page content and metadata
     */
    fun <T> from(page: Page<T>): PagedResponse<T> =
      PagedResponse(
        content = page.content,
        page =
          PageMetadata(
            size = page.size,
            number = page.number,
            totalElements = page.totalElements,
            totalPages = page.totalPages,
          ),
      )
  }
}

/**
 * Pagination metadata.
 *
 * @property size the requested page size
 * @property number the zero-indexed page number
 * @property totalElements the total number of elements across all pages
 * @property totalPages the total number of pages
 */
data class PageMetadata(
  val size: Int,
  val number: Int,
  val totalElements: Long,
  val totalPages: Int,
)