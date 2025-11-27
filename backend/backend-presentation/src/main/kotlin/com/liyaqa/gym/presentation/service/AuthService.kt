package com.liyaqa.gym.presentation.service

import com.liyaqa.gym.domain.entities.Gender
import com.liyaqa.gym.domain.entities.Member
import com.liyaqa.gym.domain.entities.User
import com.liyaqa.gym.domain.repositories.BranchRepository
import com.liyaqa.gym.domain.repositories.MemberRepository
import com.liyaqa.gym.domain.repositories.UserRepository
import com.liyaqa.gym.domain.valueobjects.ContactInfo
import com.liyaqa.gym.presentation.dto.auth.*
import com.liyaqa.gym.presentation.security.GymUserDetails
import com.liyaqa.gym.presentation.security.JwtTokenProvider
import org.slf4j.LoggerFactory
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.util.UUID

/**
 * Authentication service handling login, registration, and token management
 */
@Service
class AuthService(
    private val authenticationManager: AuthenticationManager,
    private val userRepository: UserRepository,
    private val memberRepository: MemberRepository,
    private val branchRepository: BranchRepository,
    private val passwordEncoder: PasswordEncoder,
    private val tokenProvider: JwtTokenProvider
) {
    private val logger = LoggerFactory.getLogger(AuthService::class.java)

    /**
     * Authenticate user and generate tokens
     */
    fun login(request: LoginRequest): AuthResponse {
        logger.info("Login attempt for email: ${request.email}")

        // Authenticate user
        val authentication = authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken(request.email, request.password)
        )

        SecurityContextHolder.getContext().authentication = authentication
        val userDetails = authentication.principal as GymUserDetails

        // Verify organization match if provided
        if (request.organizationId != null && userDetails.getOrganizationId() != request.organizationId) {
            throw IllegalArgumentException("User does not belong to the specified organization")
        }

        // Update last login timestamp
        val updatedUser = userDetails.getUser().recordLogin()
        userRepository.save(updatedUser)

        // Generate tokens
        val accessToken = tokenProvider.generateToken(userDetails)
        val refreshToken = tokenProvider.generateRefreshToken(userDetails)

        logger.info("User logged in successfully: ${userDetails.username}")

        return AuthResponse(
            accessToken = accessToken,
            refreshToken = refreshToken,
            expiresIn = 86400, // 24 hours in seconds
            user = UserInfo(
                id = userDetails.getUserId(),
                email = userDetails.username,
                role = userDetails.getRole().name,
                organizationId = userDetails.getOrganizationId(),
                branchId = userDetails.getBranchId(),
                memberId = userDetails.getMemberId(),
                staffId = userDetails.getStaffId(),
                isEmailVerified = userDetails.getUser().isEmailVerified,
                mustChangePassword = updatedUser.mustChangePassword
            )
        )
    }

    /**
     * Register new member with user account
     */
    fun register(request: RegisterRequest): RegisterResponse {
        logger.info("Registration attempt for email: ${request.email}")

        val branchId = try {
            UUID.fromString(request.branchId)
        } catch (e: IllegalArgumentException) {
            throw IllegalArgumentException("Invalid branch ID format")
        }

        // Validate branch exists
        val branchOptional = branchRepository.findById(branchId).getOrThrow()
        if (!branchOptional.isPresent) {
            throw IllegalArgumentException("Branch not found")
        }

        val branch = branchOptional.get()
        if (!branch.isActive) {
            throw IllegalArgumentException("Branch is not active")
        }

        // Check if email already exists
        if (userRepository.existsByEmail(request.email)) {
            throw IllegalArgumentException("Email already registered")
        }

        // Parse gender
        val gender = try {
            Gender.valueOf(request.gender.uppercase())
        } catch (e: IllegalArgumentException) {
            throw IllegalArgumentException("Invalid gender value. Must be MALE or FEMALE")
        }

        // Verify branch accepts this gender
        if (!branch.canAcceptGender(gender)) {
            throw IllegalArgumentException("This branch does not accept ${gender.name} members")
        }

        // Parse date of birth if provided
        val dateOfBirth = request.dateOfBirth?.let {
            try {
                LocalDate.parse(it)
            } catch (e: Exception) {
                throw IllegalArgumentException("Invalid date of birth format. Use yyyy-MM-dd")
            }
        }

        // Create Member entity
        val member = Member.create(
            tenantId = branch.organizationId,
            organizationId = branch.organizationId,
            branchId = branchId,
            name = request.name,
            nameArabic = request.nameArabic,
            contactInfo = ContactInfo(
                email = request.email,
                phone = request.phone
            ),
            nationalId = request.nationalId,
            gender = gender,
            dateOfBirth = dateOfBirth
        )

        val savedMember = memberRepository.save(member)
            .getOrElse { error ->
                logger.error("Failed to save member: ${error.message}", error)
                throw error
            }

        // Create User entity
        val user = User.createMemberUser(
            tenantId = branch.organizationId,
            email = request.email,
            passwordHash = passwordEncoder.encode(request.password),
            organizationId = branch.organizationId,
            branchId = branchId,
            memberId = savedMember.id
        )

        val savedUser = userRepository.save(user)

        logger.info("Member registered successfully: ${request.email}, memberId: ${savedMember.id}")

        return RegisterResponse(
            message = "Registration successful. Please verify your email.",
            userId = savedUser.id,
            memberId = savedMember.id,
            requiresEmailVerification = true
        )
    }

    /**
     * Refresh access token using refresh token
     */
    fun refreshToken(request: RefreshTokenRequest): RefreshTokenResponse {
        logger.debug("Token refresh attempt")

        if (!tokenProvider.validateRefreshToken(request.refreshToken)) {
            throw IllegalArgumentException("Invalid or expired refresh token")
        }

        val userId = tokenProvider.getUserIdFromToken(request.refreshToken)
        val user = userRepository.findById(userId)
            ?: throw IllegalArgumentException("User not found")

        if (!user.isActive) {
            throw IllegalArgumentException("User account is not active")
        }

        val userDetails = GymUserDetails.create(user)
        val newAccessToken = tokenProvider.generateToken(userDetails)

        logger.debug("Access token refreshed for user: ${user.email}")

        return RefreshTokenResponse(
            accessToken = newAccessToken,
            expiresIn = 86400 // 24 hours in seconds
        )
    }

    /**
     * Logout user (client should discard tokens)
     */
    fun logout(): MessageResponse {
        val authentication = SecurityContextHolder.getContext().authentication
        if (authentication != null && authentication.principal is GymUserDetails) {
            val userDetails = authentication.principal as GymUserDetails
            logger.info("User logged out: ${userDetails.username}")
        }

        SecurityContextHolder.clearContext()

        return MessageResponse(message = "Logged out successfully")
    }

    /**
     * Initiate forgot password flow
     * TODO: Implement email sending with reset token
     */
    fun forgotPassword(request: ForgotPasswordRequest): MessageResponse {
        logger.info("Forgot password request for email: ${request.email}")

        val user = if (request.organizationId != null) {
            userRepository.findByEmailAndOrganizationId(request.email, request.organizationId)
        } else {
            userRepository.findByEmail(request.email)
        }

        // Don't reveal if user exists or not (security best practice)
        if (user == null) {
            logger.warn("Forgot password attempted for non-existent email: ${request.email}")
            return MessageResponse(
                message = "If the email exists, a password reset link has been sent"
            )
        }

        // TODO: Generate reset token, store it, and send email
        // For now, just log it
        logger.info("Password reset requested for user: ${user.email}")

        return MessageResponse(
            message = "If the email exists, a password reset link has been sent"
        )
    }

    /**
     * Reset password using reset token
     * TODO: Implement token validation
     */
    fun resetPassword(request: ResetPasswordRequest): MessageResponse {
        logger.info("Password reset attempt with token")

        // TODO: Validate reset token and get user
        // For now, this is a placeholder
        throw NotImplementedError("Password reset functionality is not yet implemented")
    }

    /**
     * Change password for authenticated user
     */
    fun changePassword(request: ChangePasswordRequest): MessageResponse {
        val authentication = SecurityContextHolder.getContext().authentication
            ?: throw IllegalStateException("User not authenticated")

        if (authentication.principal !is GymUserDetails) {
            throw IllegalStateException("Invalid authentication principal")
        }

        val userDetails = authentication.principal as GymUserDetails
        logger.info("Password change attempt for user: ${userDetails.username}")

        // Verify current password
        if (!passwordEncoder.matches(request.currentPassword, userDetails.password)) {
            throw IllegalArgumentException("Current password is incorrect")
        }

        // Validate new password is different
        if (request.currentPassword == request.newPassword) {
            throw IllegalArgumentException("New password must be different from current password")
        }

        // Get current user and update password
        val user = userRepository.findById(userDetails.getUserId())
            ?: throw IllegalArgumentException("User not found")

        val newPasswordHash = passwordEncoder.encode(request.newPassword)
        val updatedUser = user.updatePassword(newPasswordHash)
        userRepository.save(updatedUser)

        logger.info("Password changed successfully for user: ${userDetails.username}")

        return MessageResponse(message = "Password changed successfully")
    }
}
