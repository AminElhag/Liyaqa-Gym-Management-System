package com.liyaqa.gym.domain.rules

/**
 * Base interface for business rules
 * A business rule evaluates a condition and returns whether it's satisfied
 */
interface BusinessRule<in T> {
    /**
     * Evaluate the rule
     * @param input The input to evaluate
     * @return RuleResult indicating whether the rule is satisfied
     */
    fun evaluate(input: T): RuleResult
}

/**
 * Result of evaluating a business rule
 */
sealed class RuleResult {
    /**
     * Rule is satisfied
     */
    data object Satisfied : RuleResult()

    /**
     * Rule is not satisfied with a reason
     */
    data class NotSatisfied(val reason: String) : RuleResult()

    /**
     * Check if rule is satisfied
     */
    fun isSatisfied(): Boolean = this is Satisfied

    /**
     * Check if rule is not satisfied
     */
    fun isNotSatisfied(): Boolean = this is NotSatisfied

    /**
     * Get the reason if not satisfied, null otherwise
     */
    fun getReasonOrNull(): String? = when (this) {
        is Satisfied -> null
        is NotSatisfied -> reason
    }

    /**
     * Map the reason if not satisfied
     */
    fun mapReason(transform: (String) -> String): RuleResult {
        return when (this) {
            is Satisfied -> this
            is NotSatisfied -> NotSatisfied(transform(reason))
        }
    }
}

/**
 * Combine multiple rule results
 * Returns Satisfied only if all results are Satisfied
 * Returns the first NotSatisfied result encountered
 */
fun combineRuleResults(vararg results: RuleResult): RuleResult {
    for (result in results) {
        if (result.isNotSatisfied()) {
            return result
        }
    }
    return RuleResult.Satisfied
}

/**
 * Execute multiple business rules
 */
fun <T> evaluateRules(input: T, vararg rules: BusinessRule<T>): RuleResult {
    return combineRuleResults(*rules.map { it.evaluate(input) }.toTypedArray())
}
