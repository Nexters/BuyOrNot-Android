package com.sseotdabwa.buyornot.core.network.interceptor

import okhttp3.Interceptor
import okhttp3.Response
import java.util.concurrent.TimeUnit

/**
 * [HEADER] 헤더가 붙은 요청만 그 초만큼 응답을 기다린다. 헤더는 서버로 보내지 않는다.
 * 클라이언트 기본 타임아웃보다 서버 처리가 오래 걸리는 API에 Retrofit `@Headers`로 붙인다.
 */
class ReadTimeoutInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val seconds = request.header(HEADER)?.toIntOrNull() ?: return chain.proceed(request)
        return chain
            .withReadTimeout(seconds, TimeUnit.SECONDS)
            .proceed(request.newBuilder().removeHeader(HEADER).build())
    }

    companion object {
        const val HEADER = "X-Read-Timeout-Seconds"
    }
}
