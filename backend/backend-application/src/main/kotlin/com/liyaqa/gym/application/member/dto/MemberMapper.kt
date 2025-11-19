package com.liyaqa.gym.application.member.dto

import com.liyaqa.gym.domain.entities.Member
import org.springframework.stereotype.Component

/**
 * Mapper for converting between Member domain entities and DTOs.
 */
@Component
class MemberMapper {

    /**
     * Converts a Member domain entity to a complete MemberDTO.
     */
    fun toDTO(member: Member): MemberDTO {
        return MemberDTO(
            id = member.id,
            branchId = member.branchId,
            name = member.name,
            nameArabic = member.nameArabic,
            email = member.contactInfo.email,
            phone = member.contactInfo.phone,
            nationalId = member.nationalId,
            gender = member.gender,
            dateOfBirth = member.dateOfBirth,
            age = member.getAge(),
            status = member.status,
            profilePhotoUrl = member.profilePhotoUrl,
            emergencyContactName = member.emergencyContactName,
            emergencyContactPhone = member.emergencyContactPhone,
            notes = member.notes,
            createdAt = member.createdAt,
            updatedAt = member.updatedAt
        )
    }

    /**
     * Converts a Member domain entity to a summary MemberSummaryDTO.
     */
    fun toSummaryDTO(member: Member): MemberSummaryDTO {
        return MemberSummaryDTO(
            id = member.id,
            branchId = member.branchId,
            name = member.name,
            email = member.contactInfo.email,
            phone = member.contactInfo.phone,
            status = member.status,
            gender = member.gender,
            profilePhotoUrl = member.profilePhotoUrl,
            createdAt = member.createdAt
        )
    }

    /**
     * Converts a list of Member entities to MemberDTOs.
     */
    fun toDTOList(members: List<Member>): List<MemberDTO> {
        return members.map { toDTO(it) }
    }

    /**
     * Converts a list of Member entities to MemberSummaryDTOs.
     */
    fun toSummaryDTOList(members: List<Member>): List<MemberSummaryDTO> {
        return members.map { toSummaryDTO(it) }
    }
}
