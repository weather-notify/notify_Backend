package com.weather.notify.user

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.weather.notify.domain.entity.User
import com.weather.notify.domain.repository.UserRepository
import com.weather.notify.dto.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.context.TestConstructor
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@AutoConfigureMockMvc
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class UserTest(
    private val mock: MockMvc,
    private val objectMapper: ObjectMapper,
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder = BCryptPasswordEncoder()
) {

    private val user = User(
        email = "marbling1293@dsm.hs.kr",
        name = "hanif",
        password = passwordEncoder.encode("123456")
    )

    private val login = LoginRequest(
        email = "marbling1293@dsm.hs.kr",
        password = "123456"
    )

    @AfterEach
    fun clean() {
        userRepository.deleteAll()
    }

    @Test
    @DisplayName(value = "이메일 중복 확인 true")
    fun checkEmailTrue() {
        mock.perform(get("/user/email")
            .contentType(MediaType.APPLICATION_JSON)
            .param("email", "610hanif@gmail.com"))
            .andExpect(status().isOk)
    }

    @Test
    @DisplayName(value = "이메일 중복 확인 false")
    fun checkEmailFalse() {
        userRepository.save(user)

        mock.perform(get("/user/email")
            .contentType(MediaType.APPLICATION_JSON)
            .param("email", "marbling1293@dsm.hs.kr"))
            .andExpect(status().isOk)
    }

    @Test
    @DisplayName(value = "유저 추가")
    fun join() {
        val user = JoinRequest(
            email = "marbling1293@dsm.hs.kr",
            name = "hanif",
            password = "123456"
        )

        mock.perform(post("/user")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(user)))
            .andExpect(status().isOk)

        userRepository.findByEmail(user.email)?.let {
            assertNotNull(it.email)
            assertNotNull(it.name)
            assertNotNull(it.password)
        }
    }

    @Test
    @DisplayName(value = "프로필 보기")
    fun profile() {
        userRepository.save(user)

        val token = getToken(post("/auth"), login)

        val response = objectMapper.readValue<ProfileResponse>(
            mock.perform(
                get("/user")
                    .header("Authorization", "Bearer $token")
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk)
                .andReturn().response.contentAsString)

        assertNotNull(response.email)
        assertNotNull(response.name)
    }

    @Test
    @DisplayName(value = "프로필 이름 수정")
    fun updateName() {
        userRepository.save(user)

        val token = getToken(post("/auth"), login)
        val updateName = UpdateNameRequest(
            name = "배현호"
        )

        mock.perform(
            put("/user/name")
                .header("Authorization", "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateName)))
            .andExpect(status().isOk)
    }

    @Test
    @DisplayName(value = "프로필 비밀번호 수정")
    fun updatePassword() {
        userRepository.save(user)

        val token = getToken(post("/auth"), login)
        val updatePassword = UpdatePasswordRequest(
            password = "asd123"
        )

        mock.perform(
            put("/user/password")
                .header("Authorization", "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatePassword)))
            .andExpect(status().isOk)
    }

    @Test
    @DisplayName(value = "유저 삭제")
    fun deleteUser() {
        userRepository.save(user)

        val token = getToken(post("/auth"), login)

        mock.perform(
            delete("/user")
                .header("Authorization", "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk)
    }

    private fun getToken(request: MockHttpServletRequestBuilder, obj: Any? = null): String {
        return objectMapper.readValue<TokenResponse>(
            mock.perform(
                request
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(obj)))
                .andExpect(status().isOk)
                .andReturn().response.contentAsString).accessToken
    }
}