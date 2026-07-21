package com.example.mozic.core.domain.model

/**
 * Thrown by a repository action that requires [AuthState.LoggedIn] when the
 * caller isn't (currently only [com.example.mozic.core.domain.repository.SocialRepository]'s
 * follow/unfollow). A dedicated type, not a bare `IllegalStateException` —
 * Ktor's `ResponseException` hierarchy (thrown for any non-2xx HTTP response,
 * see `NetworkModule`'s `expectSuccess = true`) also extends
 * `IllegalStateException`, so catching that broadly there mislabeled real API
 * failures (e.g. an RLS/grant rejection) as "please log in".
 */
class NotLoggedInException : Exception("Must be logged in to perform this action")
