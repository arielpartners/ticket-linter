package me.mprieto.covidio.linter.services.validators

import me.mprieto.covidio.linter.utils.whenever
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean

@SpringBootTest
class ValidatorServiceIntegrationTest {

    @Autowired
    private lateinit var validatorService: ValidatorService

    @MockBean
    private lateinit var userStoryValidator: UserStoryValidator

    @MockBean
    private lateinit var acceptanceCriteriaValidator: AcceptanceCriteriaValidator

    @Test
    fun `when context is loaded expect validators to be registered`() {
        assertEquals(2, validatorService.validators.size)
        assertTrue(validatorService.validators.any { it is UserStoryValidator })
        assertTrue(validatorService.validators.any { it is AcceptanceCriteriaValidator })
    }

    @Test
    fun `when all validators pass expect a isValid to be true`() {
        whenever(userStoryValidator.validate(anyString()))
                .thenReturn(ValidationResult(true, "User Story - OK"))

        whenever(acceptanceCriteriaValidator.validate(anyString()))
                .thenReturn(ValidationResult(true, "Acceptance Criteria - OK"))

        val result = validatorService.validate("mocked validators")
        assertTrue(result.isValid)
        assertEquals(2, result.messages.size)
        assertEquals("User Story - OK", result.messages[0])
        assertEquals("Acceptance Criteria - OK", result.messages[1])
    }

    @Test
    fun `when one validators fails expect a isValid to be false`() {
        whenever(userStoryValidator.validate(anyString()))
                .thenReturn(ValidationResult(true, "User Story - OK"))

        whenever(acceptanceCriteriaValidator.validate(anyString()))
                .thenReturn(ValidationResult(false, "No Acceptance Criteria"))

        val result = validatorService.validate("mocked validators")
        assertFalse(result.isValid)
        assertEquals(2, result.messages.size)
        assertEquals("User Story - OK", result.messages[0])
        assertEquals("No Acceptance Criteria", result.messages[1])
    }


}