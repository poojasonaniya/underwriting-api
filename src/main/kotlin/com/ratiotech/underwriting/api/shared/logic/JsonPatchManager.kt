package com.ratiotech.underwriting.api.shared.logic

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.github.fge.jsonpatch.JsonPatch
import com.ratiotech.underwriting.api.shared.exceptions.BadRequestException
import kotlin.getOrElse
import kotlin.jvm.java
import kotlin.runCatching
import org.springframework.stereotype.Component

@Component
class JsonPatchManager(private val objectMapper: ObjectMapper) {
  /**
   * A method to apply a JSON patch to an arbitrary entity class
   *
   * @param patch the json patch to apply
   * @param entity the entity to be patched
   * @param clazz the type of the entity (as we're in a generic implementation)
   * @return the patched entity
   */
  fun <T> applyPatch(patch: JsonPatch, entity: T, clazz: Class<T>): T =
    runCatching {
        val patched = patch.apply(objectMapper.convertValue(entity, JsonNode::class.java))
        objectMapper.treeToValue(patched, clazz)
      }
      .getOrElse {
        throw BadRequestException("Unable to process update patch for entity ${clazz.name}", it)
      }
}
