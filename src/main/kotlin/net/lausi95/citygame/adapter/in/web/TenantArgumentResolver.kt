package net.lausi95.citygame.adapter.`in`.web

import jakarta.servlet.http.HttpServletRequest
import net.lausi95.citygame.common.InvalidTenantOriginException
import net.lausi95.citygame.common.Tenant
import org.springframework.core.MethodParameter
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer

/**
 * Resolves a controller `tenant: Tenant` parameter from the request's frontend origin (see
 * [TenantOriginExtractor]). Runs inside the DispatcherServlet, so a missing/malformed origin
 * throws [InvalidTenantOriginException], which `HttpExceptionHandler` renders as a 400
 * `ProblemDetail`. Endpoints that take no `Tenant` parameter are never touched. See ADR 0011.
 */
class TenantArgumentResolver(
    private val tenantOriginExtractor: TenantOriginExtractor,
) : HandlerMethodArgumentResolver {

    override fun supportsParameter(parameter: MethodParameter): Boolean =
        parameter.parameterType == Tenant::class.java

    override fun resolveArgument(
        parameter: MethodParameter,
        mavContainer: ModelAndViewContainer?,
        webRequest: NativeWebRequest,
        binderFactory: WebDataBinderFactory?
    ): Tenant {
        val request = webRequest.getNativeRequest(HttpServletRequest::class.java)
            ?: throw InvalidTenantOriginException(null)
        return Tenant.fromOrigin(tenantOriginExtractor.extract(request))
    }
}
