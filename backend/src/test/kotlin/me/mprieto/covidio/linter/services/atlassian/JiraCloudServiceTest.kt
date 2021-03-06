package me.mprieto.covidio.linter.services.atlassian

import com.atlassian.connect.spring.AtlassianHostRestClients
import com.fasterxml.jackson.module.kotlin.readValue
import me.mprieto.covidio.linter.exceptions.RestClientException
import me.mprieto.covidio.linter.services.atlassian.Jira.IssueSearch
import me.mprieto.covidio.linter.services.atlassian.Jira.Project
import me.mprieto.covidio.linter.utils.TestUtils.Companion.MAPPER
import me.mprieto.covidio.linter.utils.getResourceAsString
import me.mprieto.covidio.linter.utils.mock
import me.mprieto.covidio.linter.utils.typeRef
import me.mprieto.covidio.linter.utils.whenever
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.eq
import org.slf4j.Logger
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.RequestEntity
import org.springframework.http.ResponseEntity
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate
import java.net.URI


class JiraCloudServiceTest {

    @Test
    fun `when invoking projects() if restClient successfully retrieves a list of projects expect it to be returned`() {
        val log: Logger = mock()
        val restClient: AtlassianHostRestClients = mock()
        val restTemplate: RestTemplate = mock()
        whenever(restClient.authenticatedAsAddon()).thenReturn(restTemplate)

        val service = JiraCloudService(log, restClient)
        val requestEntity = RequestEntity<Any>(HttpMethod.GET, URI("/rest/api/3/project"))
        val projects: List<Project> = MAPPER.readValue(getResourceAsString("/samples/projects/project-list.json"))

        whenever(restTemplate.exchange(requestEntity, typeRef<List<Project>>()))
                .thenReturn(ResponseEntity.ok(projects))

        val foundProjects = service.projects()
        assertFalse(foundProjects.isEmpty())
        assertEquals(projects, foundProjects)
    }

    @Test
    fun `when invoking projects() if restClient response is successful but not a 200 expect an Exception`() {
        val log: Logger = mock()
        val restClient: AtlassianHostRestClients = mock()
        val restTemplate: RestTemplate = mock()
        whenever(restClient.authenticatedAsAddon()).thenReturn(restTemplate)

        val service = JiraCloudService(log, restClient)
        val requestEntity = RequestEntity<Any>(HttpMethod.GET, URI("/rest/api/3/project"))


        whenever(restTemplate.exchange(requestEntity, typeRef<List<Project>>()))
                .thenReturn(ResponseEntity.status(HttpStatus.CONTINUE).build())

        val exception: RestClientException = assertThrows { service.projects() }
        assertEquals("Error while getting projects", exception.message)
    }

    @Test
    fun `when invoking projects() if restClient fails to retrieve a list projects expect an Exception`() {
        val log: Logger = mock()
        val restClient: AtlassianHostRestClients = mock()
        val restTemplate: RestTemplate = mock()
        whenever(restClient.authenticatedAsAddon()).thenReturn(restTemplate)

        val service = JiraCloudService(log, restClient)
        val requestEntity = RequestEntity<Any>(HttpMethod.GET, URI("/rest/api/3/project"))

        whenever(restTemplate.exchange(requestEntity, typeRef<List<Project>>()))
                .thenThrow(HttpClientErrorException(HttpStatus.UNAUTHORIZED))

        val exception: RestClientException = assertThrows { service.projects() }
        assertEquals("Error while making a request to Jira. Response status code: '401'", exception.message)
    }

    @Test
    fun `when invoking issues() if restClient successfully retrieves a list of issues expect it to be returned`() {
        val log: Logger = mock()
        val restClient: AtlassianHostRestClients = mock()
        val restTemplate: RestTemplate = mock()
        whenever(restClient.authenticatedAsAddon()).thenReturn(restTemplate)

        val service = JiraCloudService(log, restClient)

        val searchResult: IssueSearch = MAPPER.readValue(getResourceAsString("/samples/issues/search-result.json"))
        whenever(restTemplate.exchange(any(), eq(typeRef<IssueSearch>())))
                .thenReturn(ResponseEntity.ok(searchResult))

        val page = service.issues("COV")
        assertEquals(202, page.total)
        assertEquals(2, page.data.size)
        //TODO more asserts over the returned data
    }

    @Test
    fun `when invoking issues() if restClient response is successful but not a 200 expect an Exception`() {
        val log: Logger = mock()
        val restClient: AtlassianHostRestClients = mock()
        val restTemplate: RestTemplate = mock()
        whenever(restClient.authenticatedAsAddon()).thenReturn(restTemplate)

        val service = JiraCloudService(log, restClient)

        whenever(restTemplate.exchange(any(), eq(typeRef<IssueSearch>())))
                .thenThrow(HttpClientErrorException(HttpStatus.UNAUTHORIZED))

        val exception: RestClientException = assertThrows { service.issues("COV") }
        assertEquals("Error while making a request to Jira. Response status code: '401'", exception.message)
    }

    @Test
    fun `when invoking issues() if rest fails to retrieves a list of issues expect an Exception`() {
        val log: Logger = mock()
        val restClient: AtlassianHostRestClients = mock()
        val restTemplate: RestTemplate = mock()
        whenever(restClient.authenticatedAsAddon()).thenReturn(restTemplate)

        val service = JiraCloudService(log, restClient)

        // makes no sense, right?... just in case. Defensive programming
        whenever(restTemplate.exchange(any(), eq(typeRef<IssueSearch>())))
                .thenReturn(ResponseEntity.status(HttpStatus.NO_CONTENT).build())

        val exception: RestClientException = assertThrows { service.issues("COV") }
        assertEquals("Error while searching issues", exception.message)
    }
}