package com.huazai.prd.ingestion.auth;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 在 Controller 方法参数上使用，注入当前已认证用户的 ID。
 *
 * <pre>{@code
 * @GetMapping("/me")
 * public ResponseEntity<?> getCurrentUser(@CurrentUser String userId) {
 *     return ResponseEntity.ok(authService.getUserById(userId));
 * }
 * }</pre>
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface CurrentUser {
}