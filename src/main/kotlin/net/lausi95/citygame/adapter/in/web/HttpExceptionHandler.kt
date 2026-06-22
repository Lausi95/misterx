package net.lausi95.citygame.adapter.`in`.web

import io.github.oshai.kotlinlogging.KotlinLogging
import net.lausi95.citygame.application.domain.ConflictDomainException
import net.lausi95.citygame.application.domain.DomainException
import net.lausi95.citygame.application.domain.NotFoundDomainException
import net.lausi95.citygame.application.domain.UnprocessableDomainException
import net.lausi95.citygame.common.InvalidTenantOriginException
import org.springframework.http.ProblemDetail
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.MissingRequestHeaderException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

private val log = KotlinLogging.logger { }

@RestControllerAdvice
class HttpExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleMethodArgumentNotValidException(ex: MethodArgumentNotValidException): ProblemDetail {
        val details = ProblemDetail.forStatus(400)
        details.setProperty("details", "Validation Error")
        ex.bindingResult.fieldErrors.forEach {
            details.setProperty(it.field, it.defaultMessage ?: "Invalid Value")
        }
        return details
    }

    @ExceptionHandler(MissingRequestHeaderException::class)
    fun handleMissingRequestHeaderException(ex: MissingRequestHeaderException): ProblemDetail {
        val problemDetail = ProblemDetail.forStatus(400)
        problemDetail.setProperty("details", ex.message)
        return problemDetail
    }

    @ExceptionHandler(DomainException::class)
    fun handleIllegalArgumentException(ex: DomainException): ProblemDetail {
        val problemDetail = ProblemDetail.forStatus(400)
        problemDetail.setProperty("details", ex.message)
        return problemDetail
    }

    @ExceptionHandler(InvalidTenantOriginException::class)
    fun handleInvalidTenantOriginException(ex: InvalidTenantOriginException): ProblemDetail {
        val problemDetail = ProblemDetail.forStatus(400)
        problemDetail.setProperty("details", ex.message)
        return problemDetail
    }

    @ExceptionHandler(NotFoundDomainException::class)
    fun handleNotFoundDomainException(ex: NotFoundDomainException): ProblemDetail {
        val problemDetail = ProblemDetail.forStatus(404)
        problemDetail.setProperty("details", ex.message)
        return problemDetail
    }

    @ExceptionHandler(ConflictDomainException::class)
    fun handleConflictDomainException(ex: ConflictDomainException): ProblemDetail {
        val problemDetail = ProblemDetail.forStatus(409)
        problemDetail.setProperty("details", ex.message)
        return problemDetail
    }

    @ExceptionHandler(UnprocessableDomainException::class)
    fun handleUnprocessableDomainException(ex: UnprocessableDomainException): ProblemDetail {
        val problemDetail = ProblemDetail.forStatus(422)
        problemDetail.setProperty("details", ex.message)
        return problemDetail
    }

    @ExceptionHandler
    fun handleException(ex: Exception): ProblemDetail {
        log.error(ex) { "Unexpected Error (${ex.javaClass.simpleName}): ${ex.message}" }
        return ProblemDetail.forStatus(500)
    }
}
