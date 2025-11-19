package com.liyaqa.gym.presentation.security

import io.jsonwebtoken.*
import io.jsonwebtoken.security.Keys
import io.jsonwebtoken.security.SignatureException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets
import java.util.*
import javax.crypto.SecretKey

/**
 * JWT Token Provider for generating and validating JWT tokens
 */
@Component
class JwtTokenProvider(
    @Value("\${app.jwt.secret}")
    private val jwtSecret: String,

    @Value("\${app.jwt.access-token-expiration-ms:86400000}") // 24 hours default
    private val accessTokenExpirationMs: Long,

    @Value("\${app.jwt.refresh-token-expiration-ms:2592000000}") // 30 days default
    private val refreshTokenExpirationMs: Long
) {
    private val logger = LoggerFactory.getLogger(JwtTokenProvider::class.java)
    private val key: SecretKey = Keys.hmacShaKeyFor(jwtSecret.toByteArray(StandardCharsets.UTF_8))

    /**
     * Generate access token for authenticated user
     */
    fun generateToken(userDetails: UserDetails): String {
        return generateToken(userDetails, accessTokenExpirationMs, TokenType.ACCESS)
    }

    /**
     * Generate refresh token for authenticated user
     */
    fun generateRefreshToken(userDetails: UserDetails): String {
        return generateToken(userDetails, refreshTokenExpirationMs, TokenType.REFRESH)
    }

    /**
     * Generate JWT token with custom claims
     */
    private fun generateToken(userDetails: UserDetails, expirationMs: Long, tokenType: TokenType): String {
        val customUserDetails = userDetails as GymUserDetails
        val now = Date()
        val expiryDate = Date(now.time + expirationMs)

        val claims = mutableMapOf<String, Any>(
            "userId" to customUserDetails.getUserId().toString(),
            "email" to customUserDetails.username,
            "role" to customUserDetails.getRole().name,
            "organizationId" to customUserDetails.getOrganizationId().toString(),
            "tokenType" to tokenType.name
        )

        // Add branchId if present
        customUserDetails.getBranchId()?.let {
            claims["branchId"] = it.toString()
        }

        // Add memberId if present
        customUserDetails.getMemberId()?.let {
            claims["memberId"] = it.toString()
        }

        // Add staffId if present
        customUserDetails.getStaffId()?.let {
            claims["staffId"] = it.toString()
        }

        return Jwts.builder()
            .subject(customUserDetails.getUserId().toString())
            .claims(claims)
            .issuedAt(now)
            .expiration(expiryDate)
            .signWith(key, Jwts.SIG.HS512)
            .compact()
    }

    /**
     * Get user ID from JWT token
     */
    fun getUserIdFromToken(token: String): UUID {
        val claims = parseClaims(token)
        val userIdStr = claims.subject ?: throw JwtException("Token subject (userId) is missing")
        return try {
            UUID.fromString(userIdStr)
        } catch (e: IllegalArgumentException) {
            throw JwtException("Invalid userId format in token", e)
        }
    }

    /**
     * Get organization ID from JWT token
     */
    fun getOrganizationIdFromToken(token: String): UUID {
        val claims = parseClaims(token)
        val orgIdStr = claims["organizationId"] as? String
            ?: throw JwtException("Token claim 'organizationId' is missing")
        return try {
            UUID.fromString(orgIdStr)
        } catch (e: IllegalArgumentException) {
            throw JwtException("Invalid organizationId format in token", e)
        }
    }

    /**
     * Get branch ID from JWT token (if present)
     */
    fun getBranchIdFromToken(token: String): UUID? {
        val claims = parseClaims(token)
        val branchIdStr = claims["branchId"] as? String ?: return null
        return try {
            UUID.fromString(branchIdStr)
        } catch (e: IllegalArgumentException) {
            logger.warn("Invalid branchId format in token", e)
            null
        }
    }

    /**
     * Get email from JWT token
     */
    fun getEmailFromToken(token: String): String {
        val claims = parseClaims(token)
        return claims["email"] as? String ?: throw JwtException("Token claim 'email' is missing")
    }

    /**
     * Validate JWT token
     */
    fun validateToken(token: String): Boolean {
        return try {
            val claims = parseClaims(token)

            // Check token type
            val tokenType = claims["tokenType"] as? String
            if (tokenType != TokenType.ACCESS.name) {
                logger.error("Invalid token type: $tokenType")
                return false
            }

            // Token is valid if parsing succeeds and not expired
            true
        } catch (e: SignatureException) {
            logger.error("Invalid JWT signature: ${e.message}")
            false
        } catch (e: MalformedJwtException) {
            logger.error("Invalid JWT token: ${e.message}")
            false
        } catch (e: ExpiredJwtException) {
            logger.error("Expired JWT token: ${e.message}")
            false
        } catch (e: UnsupportedJwtException) {
            logger.error("Unsupported JWT token: ${e.message}")
            false
        } catch (e: IllegalArgumentException) {
            logger.error("JWT claims string is empty: ${e.message}")
            false
        } catch (e: JwtException) {
            logger.error("JWT validation error: ${e.message}")
            false
        }
    }

    /**
     * Validate refresh token
     */
    fun validateRefreshToken(token: String): Boolean {
        return try {
            val claims = parseClaims(token)

            // Check token type
            val tokenType = claims["tokenType"] as? String
            if (tokenType != TokenType.REFRESH.name) {
                logger.error("Invalid token type for refresh: $tokenType")
                return false
            }

            true
        } catch (e: Exception) {
            logger.error("Invalid refresh token: ${e.message}")
            false
        }
    }

    /**
     * Parse JWT claims
     */
    private fun parseClaims(token: String): Claims {
        return Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .payload
    }

    /**
     * Check if token is expired
     */
    fun isTokenExpired(token: String): Boolean {
        return try {
            val claims = parseClaims(token)
            claims.expiration.before(Date())
        } catch (e: ExpiredJwtException) {
            true
        } catch (e: Exception) {
            logger.error("Error checking token expiration: ${e.message}")
            true
        }
    }

    /**
     * Get token expiration date
     */
    fun getExpirationDateFromToken(token: String): Date? {
        return try {
            val claims = parseClaims(token)
            claims.expiration
        } catch (e: Exception) {
            logger.error("Error getting expiration date: ${e.message}")
            null
        }
    }
}

/**
 * Token type enumeration
 */
enum class TokenType {
    ACCESS,
    REFRESH
}
