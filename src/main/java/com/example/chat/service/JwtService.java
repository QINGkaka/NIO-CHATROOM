package com.example.chat.service;

public interface JwtService {
    
    /**
     * 生成JWT令牌
     * 
     * @param userId 用户ID
     * @return JWT令牌
     */
    String generateToken(String userId);
    
    /**
     * 从令牌中提取用户ID
     * 
     * @param token JWT令牌
     * @return 用户ID
     */
    String extractUserId(String token);
    
    /**
     * 验证令牌是否有效
     * 
     * @param token JWT令牌
     * @return 是否有效
     */
    boolean validateToken(String token);
}