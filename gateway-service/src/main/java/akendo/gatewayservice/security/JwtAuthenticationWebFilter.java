package akendo.gatewayservice.security;

import akendo.gatewayservice.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationWebFilter implements WebFilter {

    private  final JwtService jwtService;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {

        String path = exchange.getRequest().getURI().getPath();

        List<String> publicPaths = List.of(
                "/auth/login",
                "/auth/register",
                "/auth/refresh",
                "/auth/logout"
        );

        if (path.startsWith("/actuator") || publicPaths.contains(path)) {
            return chain.filter(exchange);
        }

        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if(authHeader == null || !authHeader.startsWith("Bearer ")) {
            return unauthorized(exchange);
        }

        String token = authHeader.substring(7);

        return Mono.fromCallable(() ->
            jwtService.getUserIdFromToken(token))
                .flatMap(userId -> {
                    ServerWebExchange mutatedExchange = exchange.mutate()
                            .request(builder -> builder
                                    .headers(headers -> {
                                        headers.remove("X-User-Id");
                                        headers.add("X-User-Id", userId);
                                    })
                            )
                            .build();

                    Authentication auth = new UsernamePasswordAuthenticationToken(
                            userId,
                            token,
                            List.of()
                    );

                    return chain.filter(mutatedExchange)
                            .contextWrite(ReactiveSecurityContextHolder.withAuthentication(auth)
                            );
                })
                .onErrorResume(e -> unauthorized(exchange));
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }
}
